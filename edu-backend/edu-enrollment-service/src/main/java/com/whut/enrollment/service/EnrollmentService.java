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
import com.whut.enrollment.mapper.GradeComponentMapper;
import com.whut.enrollment.mapper.StudentGradeMapper;
import com.whut.enrollment.entity.GradeComponent;
import com.whut.enrollment.entity.StudentGrade;
import com.whut.enrollment.vo.CourseStudyScoreResponse;
import com.whut.enrollment.vo.CourseStudyScoreResponse.ComponentScoreItem;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class EnrollmentService {

    private static final int COURSE_ONLINE = 1;

    private final EnrollmentMapper enrollmentMapper;
    private final GradeComponentMapper gradeComponentMapper;
    private final StudentGradeMapper studentGradeMapper;

    public EnrollmentService(EnrollmentMapper enrollmentMapper,
                             GradeComponentMapper gradeComponentMapper,
                             StudentGradeMapper studentGradeMapper) {
        this.enrollmentMapper = enrollmentMapper;
        this.gradeComponentMapper = gradeComponentMapper;
        this.studentGradeMapper = studentGradeMapper;
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
    public EnrollmentResponse removeByTeacher(Long id) {
        Enrollment enrollment = requireEnrollment(id);
        CourseSnapshot course = requireCourse(enrollment.getCourseId());
        AuthUser currentUser = currentUser();
        if (!canManageCourse(currentUser, course)) {
            throw BusinessException.forbidden("无权移出该课程学生");
        }
        if (enrollment.getStatus() != EnrollmentStatus.APPROVED.getCode()) {
            throw BusinessException.badRequest("只能移出已通过审核的学生");
        }
        enrollmentMapper.drop(id);
        enrollmentMapper.decreaseCourseEnrollment(enrollment.getCourseId());
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



    public CourseStudyScoreResponse getStudyScore(Long courseId) {
        AuthUser currentUser = currentUser();
        if (currentUser.getRole() != 1) {
            throw BusinessException.forbidden("只有学生可以查看学情成绩");
        }
        // 1. Find enrollment for this student + course
        Enrollment enrollment = findByCourseAndStudent(courseId, currentUser.getId());
        if (enrollment == null || enrollment.getStatus() != 1) {
            return null;
        }
        // 2. Get grade components
        List<GradeComponent> components = gradeComponentMapper.findByCourseId(courseId);
        if (components == null || components.isEmpty()) {
            return null;
        }
        // 3. Count total assignments/exams and completed ones
        int totalAssignments = enrollmentMapper.countAssignments(courseId);
        int totalExams = enrollmentMapper.countExams(courseId);
        int totalTasks = totalAssignments + totalExams;
        List<Integer> submissionScores = enrollmentMapper.findSubmissionScores(courseId, currentUser.getId());
        List<Integer> examScores = enrollmentMapper.findExamScores(courseId, currentUser.getId());
        int completedAssignments = submissionScores.size();
        int completedExams = examScores.size();
        int completedTasks = completedAssignments + completedExams;
        // 4. Calculate each component score
        List<ComponentScoreItem> componentItems = new ArrayList<>();
        BigDecimal totalWeightedScore = BigDecimal.ZERO;
        BigDecimal totalWeight = BigDecimal.ZERO;
        for (GradeComponent comp : components) {
            BigDecimal componentScore = null;
            String name = comp.getName();
            if (name != null && (name.contains("作业") || name.contains("Assignment") || name.contains("assignment"))) {
                // Average of submission scores
                if (!submissionScores.isEmpty()) {
                    double avg = submissionScores.stream().mapToInt(Integer::intValue).average().orElse(0);
                    componentScore = BigDecimal.valueOf(avg);
                }
            } else if (name != null && (name.contains("考试") || name.contains("Exam") || name.contains("exam"))) {
                // Use latest exam attempt score
                if (!examScores.isEmpty()) {
                    componentScore = BigDecimal.valueOf(examScores.get(examScores.size() - 1));
                }
            } else {
                // Other components: check tb_student_grade
                if (enrollment != null && enrollment.getId() != null) {
                    StudentGrade sg = studentGradeMapper.selectOne(
                            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<StudentGrade>()
                                    .eq(StudentGrade::getEnrollmentId, enrollment.getId())
                                    .eq(StudentGrade::getComponentId, comp.getId()));
                    if (sg != null && sg.getScore() != null) {
                        componentScore = sg.getScore();
                    }
                }
            }
            if (componentScore != null) {
                componentItems.add(new ComponentScoreItem(comp.getName(), comp.getWeight(), componentScore, comp.getMaxScore()));
                BigDecimal weight = comp.getWeight() != null ? comp.getWeight() : BigDecimal.ONE;
                totalWeightedScore = totalWeightedScore.add(componentScore.multiply(weight));
                totalWeight = totalWeight.add(weight);
            } else {
                componentItems.add(new ComponentScoreItem(comp.getName(), comp.getWeight(), null, comp.getMaxScore()));
            }
        }
        // 5. Calculate final weighted score
        BigDecimal finalScore = null;
        String gradeLetter = null;
        Integer passed = null;
        if (totalWeight.compareTo(BigDecimal.ZERO) > 0) {
            finalScore = totalWeightedScore.divide(totalWeight, 1, RoundingMode.HALF_UP);
            double scoreVal = finalScore.doubleValue();
            if (scoreVal >= 90) gradeLetter = "A";
            else if (scoreVal >= 80) gradeLetter = "B";
            else if (scoreVal >= 70) gradeLetter = "C";
            else if (scoreVal >= 60) gradeLetter = "D";
            else gradeLetter = "F";
            passed = scoreVal >= 60 ? 1 : 0;
        }
        // 6. Build response
        CourseStudyScoreResponse response = new CourseStudyScoreResponse();
        response.setFinalScore(finalScore);
        response.setGradeLetter(gradeLetter);
        response.setPassed(passed);
        response.setCompletedTasks(completedTasks);
        response.setTotalTasks(totalTasks);
        response.setCompletedAssignments(completedAssignments);
        response.setTotalAssignments(totalAssignments);
        response.setCompletedExams(completedExams);
        response.setTotalExams(totalExams);
        response.setCompletionPercent(totalTasks > 0 ? completedTasks * 100 / totalTasks : 0);
        response.setComponentScores(componentItems);
        return response;
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
        response.setStudentName(row.getStudentName());
        response.setStatus(row.getStatus());
        response.setApplyReason(row.getApplyReason());
        response.setReviewComment(row.getReviewComment());
        response.setAppliedAt(row.getAppliedAt());
        response.setReviewedAt(row.getReviewedAt());
        return response;
    }
}
