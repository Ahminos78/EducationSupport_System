package com.whut.assessment.service;

import com.whut.assessment.entity.CourseSnapshot;
import com.whut.assessment.entity.Exam;
import com.whut.assessment.mapper.ExamMapper;
import com.whut.assessment.vo.ExamResponse;
import com.whut.common.auth.AuthContext;
import com.whut.common.auth.AuthUser;
import com.whut.common.enums.UserRole;
import com.whut.common.exception.BusinessException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ExamService {

    private static final int STATUS_DRAFT = 0;

    private final ExamMapper examMapper;

    public ExamService(ExamMapper examMapper) {
        this.examMapper = examMapper;
    }

    public List<ExamResponse> listByCourse(Long courseId) {
        CourseSnapshot course = requireCourse(courseId);
        AuthUser currentUser = currentUser();
        assertCourseAccess(currentUser, course);
        boolean includeDraft = canManageCourse(currentUser, course);
        return examMapper.findByCourseId(courseId, includeDraft).stream()
                .map(this::toResponse)
                .toList();
    }

    public ExamResponse detail(Long id) {
        Exam exam = requireExam(id);
        AuthUser currentUser = currentUser();
        CourseSnapshot course = requireCourse(exam.getCourseId());
        assertCourseAccess(currentUser, course);
        if (exam.getStatus() == STATUS_DRAFT && !canManageExam(currentUser, exam)) {
            throw BusinessException.forbidden("无权查看该考试");
        }
        return toResponse(requireResponse(id));
    }

    Exam requireExam(Long id) {
        Exam exam = examMapper.selectById(id);
        if (exam == null) {
            throw BusinessException.notFound("考试不存在");
        }
        return exam;
    }

    boolean canManageExam(AuthUser currentUser, Exam exam) {
        return currentUser.getRole() == UserRole.ADMIN.getCode()
                || (currentUser.getRole() == UserRole.TEACHER.getCode()
                && exam.getTeacherId().equals(currentUser.getId()));
    }

    void assertApprovedStudent(Long courseId, AuthUser currentUser) {
        if (currentUser.getRole() != UserRole.STUDENT.getCode()
                || !examMapper.isApprovedStudent(courseId, currentUser.getId())) {
            throw BusinessException.forbidden("只有已通过选课审核的学生可以访问该课程考试");
        }
    }

    private void assertCourseAccess(AuthUser currentUser, CourseSnapshot course) {
        if (canManageCourse(currentUser, course)) {
            return;
        }
        assertApprovedStudent(course.getId(), currentUser);
    }

    private boolean canManageCourse(AuthUser currentUser, CourseSnapshot course) {
        return currentUser.getRole() == UserRole.ADMIN.getCode()
                || (currentUser.getRole() == UserRole.TEACHER.getCode()
                && course.getTeacherId().equals(currentUser.getId()));
    }

    private CourseSnapshot requireCourse(Long courseId) {
        CourseSnapshot course = examMapper.findCourseById(courseId);
        if (course == null || (course.getDeleted() != null && course.getDeleted() == 1)) {
            throw BusinessException.notFound("课程不存在");
        }
        return course;
    }

    private ExamMapper.ExamResponseRow requireResponse(Long id) {
        ExamMapper.ExamResponseRow row = examMapper.findResponseById(id);
        if (row == null) {
            throw BusinessException.notFound("考试不存在");
        }
        return row;
    }

    private AuthUser currentUser() {
        AuthUser currentUser = AuthContext.get();
        if (currentUser == null) {
            throw BusinessException.unauthorized("请先登录");
        }
        return currentUser;
    }

    private ExamResponse toResponse(ExamMapper.ExamResponseRow row) {
        ExamResponse response = new ExamResponse();
        response.setId(row.getId());
        response.setCourseId(row.getCourseId());
        response.setCourseName(row.getCourseName());
        response.setTeacherId(row.getTeacherId());
        response.setTitle(row.getTitle());
        response.setDescription(row.getDescription());
        response.setStartTime(row.getStartTime());
        response.setEndTime(row.getEndTime());
        response.setFullScore(row.getFullScore());
        response.setStatus(row.getStatus());
        response.setCreatedAt(row.getCreatedAt());
        return response;
    }
}
