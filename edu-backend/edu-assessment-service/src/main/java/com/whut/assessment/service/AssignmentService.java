package com.whut.assessment.service;

import com.whut.assessment.dto.AssignmentCreateRequest;
import com.whut.assessment.dto.AssignmentStatusUpdateRequest;
import com.whut.assessment.dto.AssignmentUpdateRequest;
import com.whut.assessment.entity.Assignment;
import com.whut.assessment.entity.CourseSnapshot;
import com.whut.assessment.mapper.AssignmentMapper;
import com.whut.assessment.vo.AssignmentResponse;
import com.whut.common.auth.AuthContext;
import com.whut.common.auth.AuthUser;
import com.whut.common.enums.UserRole;
import com.whut.common.exception.BusinessException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AssignmentService {

    private static final int COURSE_ONLINE = 1;
    private static final int STATUS_DRAFT = 0;
    private static final int STATUS_PUBLISHED = 1;
    private static final int STATUS_CLOSED = 2;

    private final AssignmentMapper assignmentMapper;

    public AssignmentService(AssignmentMapper assignmentMapper) {
        this.assignmentMapper = assignmentMapper;
    }

    public List<AssignmentResponse> listByCourse(Long courseId) {
        CourseSnapshot course = requireCourse(courseId);
        AuthUser currentUser = currentUser();
        boolean includeDraft = canManageCourse(currentUser, course);
        return assignmentMapper.findByCourseId(courseId, includeDraft).stream()
                .map(this::toResponse)
                .toList();
    }

    public AssignmentResponse detail(Long id) {
        Assignment assignment = requireAssignment(id);
        AuthUser currentUser = currentUser();
        if (assignment.getStatus() == STATUS_DRAFT && !canManageAssignment(currentUser, assignment)) {
            throw BusinessException.forbidden("无权查看该作业");
        }
        return toResponse(requireResponse(id));
    }

    public AssignmentResponse create(AssignmentCreateRequest request) {
        AuthUser currentUser = currentUser();
        assertTeacherOrAdmin(currentUser);
        if (request.getCourseId() == null) {
            throw BusinessException.badRequest("课程ID不能为空");
        }
        CourseSnapshot course = requireAvailableCourse(request.getCourseId());
        if (!canManageCourse(currentUser, course)) {
            throw BusinessException.forbidden("无权在该课程发布作业");
        }
        requireText(request.getTitle(), "作业标题不能为空");
        int fullScore = request.getFullScore() == null ? 100 : request.getFullScore();
        assertValidFullScore(fullScore);
        if (request.getDeadline() == null) {
            throw BusinessException.badRequest("截止时间不能为空");
        }
        int status = request.getStatus() == null ? STATUS_PUBLISHED : request.getStatus();
        assertValidStatus(status);
        Assignment assignment = new Assignment();
        assignment.setCourseId(request.getCourseId());
        assignment.setTeacherId(currentUser.getId());
        assignment.setTitle(request.getTitle());
        assignment.setDescription(request.getDescription());
        assignment.setFullScore(fullScore);
        assignment.setDeadline(request.getDeadline());
        assignment.setStatus(status);
        assignmentMapper.insert(assignment);
        return toResponse(requireResponse(assignment.getId()));
    }

    public AssignmentResponse update(Long id, AssignmentUpdateRequest request) {
        Assignment assignment = requireAssignment(id);
        AuthUser currentUser = currentUser();
        if (!canManageAssignment(currentUser, assignment)) {
            throw BusinessException.forbidden("无权修改该作业");
        }
        if (StringUtils.hasText(request.getTitle())) {
            assignment.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            assignment.setDescription(request.getDescription());
        }
        if (request.getFullScore() != null) {
            assertValidFullScore(request.getFullScore());
            assignment.setFullScore(request.getFullScore());
        }
        if (request.getDeadline() != null) {
            assignment.setDeadline(request.getDeadline());
        }
        assignmentMapper.update(assignment);
        return toResponse(requireResponse(id));
    }

    public AssignmentResponse updateStatus(Long id, AssignmentStatusUpdateRequest request) {
        Assignment assignment = requireAssignment(id);
        AuthUser currentUser = currentUser();
        if (!canManageAssignment(currentUser, assignment)) {
            throw BusinessException.forbidden("无权修改该作业状态");
        }
        assertValidStatus(request.getStatus());
        assignmentMapper.updateStatus(id, request.getStatus());
        return toResponse(requireResponse(id));
    }

    public void delete(Long id) {
        Assignment assignment = requireAssignment(id);
        AuthUser currentUser = currentUser();
        if (!canManageAssignment(currentUser, assignment)) {
            throw BusinessException.forbidden("无权删除该作业");
        }
        assignmentMapper.logicalDelete(id);
    }

    Assignment requireAssignment(Long id) {
        Assignment assignment = assignmentMapper.findById(id);
        if (assignment == null) {
            throw BusinessException.notFound("作业不存在");
        }
        return assignment;
    }

    boolean canManageAssignment(AuthUser currentUser, Assignment assignment) {
        return currentUser.getRole() == UserRole.ADMIN.getCode()
                || (currentUser.getRole() == UserRole.TEACHER.getCode()
                && assignment.getTeacherId().equals(currentUser.getId()));
    }

    boolean canSubmit(Assignment assignment) {
        return assignment.getStatus() == STATUS_PUBLISHED
                && !LocalDateTime.now().isAfter(assignment.getDeadline());
    }

    private boolean canManageCourse(AuthUser currentUser, CourseSnapshot course) {
        return currentUser.getRole() == UserRole.ADMIN.getCode()
                || (currentUser.getRole() == UserRole.TEACHER.getCode()
                && course.getTeacherId().equals(currentUser.getId()));
    }

    private CourseSnapshot requireAvailableCourse(Long courseId) {
        CourseSnapshot course = requireCourse(courseId);
        if (course.getStatus() != COURSE_ONLINE) {
            throw BusinessException.badRequest("课程未开放");
        }
        return course;
    }

    private CourseSnapshot requireCourse(Long courseId) {
        CourseSnapshot course = assignmentMapper.findCourseById(courseId);
        if (course == null || (course.getDeleted() != null && course.getDeleted() == 1)) {
            throw BusinessException.notFound("课程不存在");
        }
        return course;
    }

    private AssignmentMapper.AssignmentResponseRow requireResponse(Long id) {
        AssignmentMapper.AssignmentResponseRow row = assignmentMapper.findResponseById(id);
        if (row == null) {
            throw BusinessException.notFound("作业不存在");
        }
        return row;
    }

    private void assertTeacherOrAdmin(AuthUser currentUser) {
        if (currentUser.getRole() != UserRole.TEACHER.getCode()
                && currentUser.getRole() != UserRole.ADMIN.getCode()) {
            throw BusinessException.forbidden("只有教师或管理员可以管理作业");
        }
    }

    private void assertValidStatus(Integer status) {
        if (status == null || (status != STATUS_DRAFT && status != STATUS_PUBLISHED && status != STATUS_CLOSED)) {
            throw BusinessException.badRequest("作业状态不合法");
        }
    }

    private void assertValidFullScore(Integer fullScore) {
        if (fullScore == null || fullScore <= 0) {
            throw BusinessException.badRequest("满分必须大于0");
        }
    }

    private AuthUser currentUser() {
        AuthUser currentUser = AuthContext.get();
        if (currentUser == null) {
            throw BusinessException.unauthorized("请先登录");
        }
        return currentUser;
    }

    private void requireText(String value, String message) {
        if (!StringUtils.hasText(value)) {
            throw BusinessException.badRequest(message);
        }
    }

    private AssignmentResponse toResponse(AssignmentMapper.AssignmentResponseRow row) {
        AssignmentResponse response = new AssignmentResponse();
        response.setId(row.getId());
        response.setCourseId(row.getCourseId());
        response.setCourseName(row.getCourseName());
        response.setTeacherId(row.getTeacherId());
        response.setTitle(row.getTitle());
        response.setDescription(row.getDescription());
        response.setFullScore(row.getFullScore());
        response.setDeadline(row.getDeadline());
        response.setStatus(row.getStatus());
        response.setCreatedAt(row.getCreatedAt());
        return response;
    }
}
