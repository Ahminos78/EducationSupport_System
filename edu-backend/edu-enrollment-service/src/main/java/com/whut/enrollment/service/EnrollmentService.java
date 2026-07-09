package com.whut.enrollment.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.whut.common.auth.AuthContext;
import com.whut.common.auth.AuthUser;
import com.whut.common.enums.EnrollmentStatus;
import com.whut.common.enums.UserRole;
import com.whut.common.exception.BusinessException;
import com.whut.enrollment.dto.EnrollmentCreateRequest;
import com.whut.enrollment.dto.EnrollmentReviewRequest;
import com.whut.enrollment.entity.CourseSnapshot;
import com.whut.enrollment.entity.Enrollment;
import com.whut.enrollment.mapper.EnrollmentMapper;
import com.whut.enrollment.vo.EnrollmentResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class EnrollmentService {

    private static final int COURSE_ONLINE = 1;

    private final EnrollmentMapper enrollmentMapper;

    public EnrollmentService(EnrollmentMapper enrollmentMapper) {
        this.enrollmentMapper = enrollmentMapper;
    }

    @Transactional
    public EnrollmentResponse apply(EnrollmentCreateRequest request) {
        AuthUser currentUser = currentUser();
        if (currentUser.getRole() != UserRole.STUDENT.getCode()) {
            throw BusinessException.forbidden("只有学生可以申请选课");
        }
        if (request.getCourseId() == null) {
            throw BusinessException.badRequest("课程ID不能为空");
        }
        CourseSnapshot course = requireAvailableCourse(request.getCourseId());
        Enrollment existing = findByCourseAndStudent(request.getCourseId(), currentUser.getId());
        if (existing != null && existing.getStatus() != EnrollmentStatus.REJECTED.getCode()) {
            throw BusinessException.badRequest("已存在该课程的选课记录");
        }
        if (course.getEnrolledCount() >= course.getMaxStudents()) {
            throw BusinessException.badRequest("课程人数已满");
        }
        Enrollment enrollment = new Enrollment();
        enrollment.setCourseId(request.getCourseId());
        enrollment.setStudentId(currentUser.getId());
        enrollment.setStatus(EnrollmentStatus.PENDING.getCode());
        enrollment.setApplyReason(request.getApplyReason());
        enrollmentMapper.insert(enrollment);
        return getOwnEnrollment(enrollment.getId());
    }

    public List<EnrollmentResponse> myEnrollments() {
        AuthUser currentUser = currentUser();
        if (currentUser.getRole() != UserRole.STUDENT.getCode()) {
            throw BusinessException.forbidden("只有学生可以查看自己的选课记录");
        }
        return enrollmentMapper.findByStudentId(currentUser.getId()).stream()
                .map(this::toResponse)
                .toList();
    }

    public List<EnrollmentResponse> courseEnrollments(Long courseId, Integer status) {
        AuthUser currentUser = currentUser();
        CourseSnapshot course = requireCourse(courseId);
        if (!canManageCourse(currentUser, course)) {
            throw BusinessException.forbidden("无权查看该课程选课记录");
        }
        assertValidStatusWhenPresent(status);
        return enrollmentMapper.findByCourseId(courseId, status).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public EnrollmentResponse approve(Long id, EnrollmentReviewRequest request) {
        Enrollment enrollment = requireEnrollment(id);
        CourseSnapshot course = requireAvailableCourse(enrollment.getCourseId());
        AuthUser currentUser = currentUser();
        if (!canManageCourse(currentUser, course)) {
            throw BusinessException.forbidden("无权审核该课程选课申请");
        }
        if (enrollment.getStatus() != EnrollmentStatus.PENDING.getCode()) {
            throw BusinessException.badRequest("只能通过待审核的选课申请");
        }
        if (enrollmentMapper.increaseCourseEnrollment(enrollment.getCourseId()) == 0) {
            throw BusinessException.badRequest("课程人数已满或课程不可选");
        }
        enrollmentMapper.updateReviewStatus(id, EnrollmentStatus.APPROVED.getCode(), reviewComment(request));
        return getEnrollmentForManager(id);
    }

    @Transactional
    public EnrollmentResponse reject(Long id, EnrollmentReviewRequest request) {
        Enrollment enrollment = requireEnrollment(id);
        CourseSnapshot course = requireCourse(enrollment.getCourseId());
        AuthUser currentUser = currentUser();
        if (!canManageCourse(currentUser, course)) {
            throw BusinessException.forbidden("无权审核该课程选课申请");
        }
        if (enrollment.getStatus() != EnrollmentStatus.PENDING.getCode()) {
            throw BusinessException.badRequest("只能拒绝待审核的选课申请");
        }
        enrollmentMapper.updateReviewStatus(id, EnrollmentStatus.REJECTED.getCode(), reviewComment(request));
        return getEnrollmentForManager(id);
    }

    @Transactional
    public EnrollmentResponse drop(Long id) {
        AuthUser currentUser = currentUser();
        Enrollment enrollment = requireEnrollment(id);
        if (currentUser.getRole() != UserRole.STUDENT.getCode() || !enrollment.getStudentId().equals(currentUser.getId())) {
            throw BusinessException.forbidden("只能退选自己的课程");
        }
        if (enrollment.getStatus() != EnrollmentStatus.APPROVED.getCode()
                && enrollment.getStatus() != EnrollmentStatus.PENDING.getCode()) {
            throw BusinessException.badRequest("当前状态不能退选");
        }
        boolean shouldDecrease = enrollment.getStatus() == EnrollmentStatus.APPROVED.getCode();
        enrollmentMapper.drop(id);
        if (shouldDecrease) {
            enrollmentMapper.decreaseCourseEnrollment(enrollment.getCourseId());
        }
        return getOwnEnrollment(id);
    }

    private EnrollmentResponse getOwnEnrollment(Long id) {
        Enrollment enrollment = requireEnrollment(id);
        AuthUser currentUser = currentUser();
        if (!enrollment.getStudentId().equals(currentUser.getId())) {
            throw BusinessException.forbidden("无权查看该选课记录");
        }
        return findEnrollmentResponse(enrollment);
    }

    private EnrollmentResponse getEnrollmentForManager(Long id) {
        Enrollment enrollment = requireEnrollment(id);
        return findEnrollmentResponse(enrollment);
    }

    private EnrollmentResponse findEnrollmentResponse(Enrollment enrollment) {
        return enrollmentMapper.findByCourseId(enrollment.getCourseId(), null).stream()
                .filter(item -> item.getId().equals(enrollment.getId()))
                .findFirst()
                .map(this::toResponse)
                .orElseThrow(() -> BusinessException.notFound("选课记录不存在"));
    }

    private Enrollment requireEnrollment(Long id) {
        Enrollment enrollment = enrollmentMapper.selectById(id);
        if (enrollment == null) {
            throw BusinessException.notFound("选课记录不存在");
        }
        return enrollment;
    }

    private Enrollment findByCourseAndStudent(Long courseId, Long studentId) {
        return enrollmentMapper.selectOne(new LambdaQueryWrapper<Enrollment>()
                .eq(Enrollment::getCourseId, courseId)
                .eq(Enrollment::getStudentId, studentId));
    }

    private CourseSnapshot requireAvailableCourse(Long courseId) {
        CourseSnapshot course = requireCourse(courseId);
        if (course.getDeleted() != null && course.getDeleted() == 1) {
            throw BusinessException.notFound("课程不存在");
        }
        if (course.getStatus() != COURSE_ONLINE) {
            throw BusinessException.badRequest("课程未开放选课");
        }
        return course;
    }

    private CourseSnapshot requireCourse(Long courseId) {
        if (courseId == null) {
            throw BusinessException.badRequest("课程ID不能为空");
        }
        CourseSnapshot course = enrollmentMapper.findCourseById(courseId);
        if (course == null || (course.getDeleted() != null && course.getDeleted() == 1)) {
            throw BusinessException.notFound("课程不存在");
        }
        return course;
    }

    private boolean canManageCourse(AuthUser currentUser, CourseSnapshot course) {
        return currentUser.getRole() == UserRole.ADMIN.getCode()
                || (currentUser.getRole() == UserRole.TEACHER.getCode()
                && course.getTeacherId().equals(currentUser.getId()));
    }

    private void assertValidStatusWhenPresent(Integer status) {
        if (status == null) {
            return;
        }
        for (EnrollmentStatus enrollmentStatus : EnrollmentStatus.values()) {
            if (enrollmentStatus.getCode() == status) {
                return;
            }
        }
        throw BusinessException.badRequest("选课状态不合法");
    }

    private String reviewComment(EnrollmentReviewRequest request) {
        return request == null ? null : request.getReviewComment();
    }

    private AuthUser currentUser() {
        AuthUser currentUser = AuthContext.get();
        if (currentUser == null) {
            throw BusinessException.unauthorized("请先登录");
        }
        return currentUser;
    }

    private EnrollmentResponse toResponse(EnrollmentMapper.EnrollmentResponseRow row) {
        EnrollmentResponse response = new EnrollmentResponse();
        response.setId(row.getId());
        response.setCourseId(row.getCourseId());
        response.setCourseName(row.getCourseName());
        response.setStudentId(row.getStudentId());
        response.setStatus(row.getStatus());
        response.setApplyReason(row.getApplyReason());
        response.setReviewComment(row.getReviewComment());
        response.setAppliedAt(row.getAppliedAt());
        response.setReviewedAt(row.getReviewedAt());
        return response;
    }
}
