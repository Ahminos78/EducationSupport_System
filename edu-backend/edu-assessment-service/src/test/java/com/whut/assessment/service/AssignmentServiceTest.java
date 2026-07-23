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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AssignmentServiceTest {

    @Mock private AssignmentMapper assignmentMapper;

    private AssignmentService assignmentService;
    private AuthUser teacherUser;
    private AuthUser adminUser;
    private AuthUser studentUser;
    private CourseSnapshot course;
    private Assignment assignment;

    @BeforeEach
    void setUp() {
        assignmentService = new AssignmentService(assignmentMapper);

        teacherUser = new AuthUser(1L, "teacher1", UserRole.TEACHER.getCode());
        adminUser = new AuthUser(3L, "admin1", UserRole.ADMIN.getCode());
        studentUser = new AuthUser(2L, "student1", UserRole.STUDENT.getCode());

        course = new CourseSnapshot();
        course.setId(10L);
        course.setTeacherId(1L);
        course.setStatus(1);
        course.setDeleted(0);

        assignment = new Assignment();
        assignment.setId(100L);
        assignment.setCourseId(10L);
        assignment.setTeacherId(1L);
        assignment.setTitle("\u4f5c\u4e1a1");
        assignment.setFullScore(100);
        assignment.setStartTime(LocalDateTime.now().minusDays(1));
        assignment.setDeadline(LocalDateTime.now().plusDays(7));
        assignment.setStatus(1);
        assignment.setAllowLateSubmission(0);
    }

    @Test
    void create_asTeacher_shouldSucceed() {
        AssignmentCreateRequest req = new AssignmentCreateRequest();
        req.setCourseId(10L);
        req.setTitle("\u65b0\u4f5c\u4e1a");
        req.setDeadline(LocalDateTime.now().plusDays(5));
        req.setFullScore(100);

        try (MockedStatic<AuthContext> ctx = mockStatic(AuthContext.class)) {
            ctx.when(AuthContext::get).thenReturn(teacherUser);
            when(assignmentMapper.findCourseById(10L)).thenReturn(course);
            when(assignmentMapper.selectById(anyLong())).thenReturn(assignment);
            when(assignmentMapper.findResponseById(any())).thenReturn(mockRow(100L, "\u65b0\u4f5c\u4e1a"));

            AssignmentResponse r = assignmentService.create(req);
            assertThat(r).isNotNull();

        }
    }

    @Test
    void create_deadlineBeforeStart_shouldThrow() {
        AssignmentCreateRequest req = new AssignmentCreateRequest();
        req.setCourseId(10L);
        req.setTitle("\u4f5c\u4e1a");
        req.setStartTime(LocalDateTime.now().plusDays(3));
        req.setDeadline(LocalDateTime.now().plusDays(1));

        try (MockedStatic<AuthContext> ctx = mockStatic(AuthContext.class)) {
            ctx.when(AuthContext::get).thenReturn(teacherUser);
            when(assignmentMapper.findCourseById(10L)).thenReturn(course);

            assertThatThrownBy(() -> assignmentService.create(req))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("\u622a\u6b62\u65f6\u95f4\u5fc5\u987b\u665a\u4e8e\u5f00\u59cb\u65f6\u95f4");
        }
    }

    @Test
    void create_student_shouldThrow() {
        AssignmentCreateRequest req = new AssignmentCreateRequest();
        req.setCourseId(10L);
        req.setTitle("\u4f5c\u4e1a");

        try (MockedStatic<AuthContext> ctx = mockStatic(AuthContext.class)) {
            ctx.when(AuthContext::get).thenReturn(studentUser);
            assertThatThrownBy(() -> assignmentService.create(req))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("\u53ea\u6709\u6559\u5e08\u6216\u7ba1\u7406\u5458");
        }
    }

    @Test
    void detail_draft_visibleToTeacher() {
        assignment.setStatus(0);
        try (MockedStatic<AuthContext> ctx = mockStatic(AuthContext.class)) {
            ctx.when(AuthContext::get).thenReturn(teacherUser);
            when(assignmentMapper.selectById(100L)).thenReturn(assignment);
            when(assignmentMapper.findCourseById(10L)).thenReturn(course);
            when(assignmentMapper.findResponseById(any())).thenReturn(mockRow(100L, "\u4f5c\u4e1a1"));

            AssignmentResponse r = assignmentService.detail(100L);
            assertThat(r).isNotNull();
        }
    }

    @Test
    void updateStatus_shouldSucceed() {
        AssignmentStatusUpdateRequest req = new AssignmentStatusUpdateRequest();
        req.setStatus(2);
        try (MockedStatic<AuthContext> ctx = mockStatic(AuthContext.class)) {
            ctx.when(AuthContext::get).thenReturn(teacherUser);
            when(assignmentMapper.selectById(100L)).thenReturn(assignment);
            when(assignmentMapper.findCourseById(10L)).thenReturn(course);
            when(assignmentMapper.findResponseById(any())).thenReturn(mockRow(100L, "\u4f5c\u4e1a1"));

            AssignmentResponse r = assignmentService.updateStatus(100L, req);
            assertThat(r).isNotNull();
            verify(assignmentMapper).updateStatus(100L, 2);
        }
    }

    @Test
    void delete_shouldSucceed() {
        try (MockedStatic<AuthContext> ctx = mockStatic(AuthContext.class)) {
            ctx.when(AuthContext::get).thenReturn(teacherUser);
            when(assignmentMapper.selectById(100L)).thenReturn(assignment);
            when(assignmentMapper.findCourseById(10L)).thenReturn(course);

            assignmentService.delete(100L);
            verify(assignmentMapper).deleteById(100L);
        }
    }

    private AssignmentMapper.AssignmentResponseRow mockRow(Long id, String title) {
        AssignmentMapper.AssignmentResponseRow r = new AssignmentMapper.AssignmentResponseRow();
        r.setId(id);
        r.setTitle(title);
        r.setCourseId(10L);
        r.setTeacherId(1L);
        r.setCourseName("\u6d4b\u8bd5\u8bfe\u7a0b");
        r.setTeacherName("\u5f20\u8001\u5e08");
        r.setFullScore(100);
        r.setStatus(1);
        return r;
    }
}
