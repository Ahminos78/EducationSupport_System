package com.whut.assessment.service;

import com.whut.assessment.dto.SubmissionCreateRequest;
import com.whut.assessment.dto.SubmissionGradeRequest;
import com.whut.assessment.dto.AiAutoCommentRequest;
import com.whut.assessment.entity.Assignment;
import com.whut.assessment.entity.Submission;
import com.whut.assessment.mapper.SubmissionMapper;
import com.whut.assessment.vo.SubmissionResponse;
import com.whut.assessment.client.AiExamClient;
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
class SubmissionServiceTest {

    @Mock private SubmissionMapper submissionMapper;
    @Mock private AssignmentService assignmentService;
    @Mock private AiExamClient aiExamClient;

    private SubmissionService submissionService;
    private AuthUser studentUser;
    private AuthUser teacherUser;
    private Assignment assignment;
    private Submission submission;

    @BeforeEach
    void setUp() {
        submissionService = new SubmissionService(submissionMapper, assignmentService, aiExamClient);
        studentUser = new AuthUser(2L, "student1", UserRole.STUDENT.getCode());
        teacherUser = new AuthUser(1L, "teacher1", UserRole.TEACHER.getCode());
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
        submission = new Submission();
        submission.setId(999L);
        submission.setAssignmentId(100L);
        submission.setStudentId(2L);
        submission.setContent("\u6211\u7684\u4f5c\u4e1a");
        submission.setScore(null);
    }

    @Test
    void submit_asStudent_shouldSucceed() {
        SubmissionCreateRequest req = new SubmissionCreateRequest();
        req.setContent("\u65b0\u63d0\u4ea4\u7684\u4f5c\u4e1a");

        try (MockedStatic<AuthContext> ctx = mockStatic(AuthContext.class)) {
            ctx.when(AuthContext::get).thenReturn(studentUser);
            when(assignmentService.requireAssignment(100L)).thenReturn(assignment);
            when(assignmentService.canSubmit(assignment)).thenReturn(true);
            when(submissionMapper.selectOne(any())).thenReturn(null);
            when(submissionMapper.selectById(any())).thenReturn(submission);
            when(submissionMapper.findResponseById(any())).thenReturn(mockSubRow());

            SubmissionResponse r = submissionService.submit(100L, req);
            assertThat(r).isNotNull();

        }
    }

    @Test
    void submit_nonStudent_shouldThrow() {
        SubmissionCreateRequest req = new SubmissionCreateRequest();
        try (MockedStatic<AuthContext> ctx = mockStatic(AuthContext.class)) {
            ctx.when(AuthContext::get).thenReturn(teacherUser);
            assertThatThrownBy(() -> submissionService.submit(100L, req))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("\u53ea\u6709\u5b66\u751f");
        }
    }

    @Test
    void assignmentSubmissions_teacher_shouldReturn() {
        try (MockedStatic<AuthContext> ctx = mockStatic(AuthContext.class)) {
            ctx.when(AuthContext::get).thenReturn(teacherUser);
            when(assignmentService.requireAssignment(100L)).thenReturn(assignment);
            when(assignmentService.canManageAssignment(teacherUser, assignment)).thenReturn(true);
            when(submissionMapper.findByAssignmentId(100L)).thenReturn(List.of(mockSubRow()));

            List<SubmissionResponse> list = submissionService.assignmentSubmissions(100L);
            assertThat(list).hasSize(1);
        }
    }

    @Test
    void grade_shouldSucceed() {
        SubmissionGradeRequest req = new SubmissionGradeRequest();
        req.setScore(85);
        req.setTeacherComment("\u4e0d\u9519");

        try (MockedStatic<AuthContext> ctx = mockStatic(AuthContext.class)) {
            ctx.when(AuthContext::get).thenReturn(teacherUser);
            when(submissionMapper.selectById(999L)).thenReturn(submission);
            when(assignmentService.requireAssignment(100L)).thenReturn(assignment);
            when(assignmentService.canManageAssignment(teacherUser, assignment)).thenReturn(true);
            when(submissionMapper.findResponseById(any())).thenReturn(mockSubRow());
            when(aiExamClient.autoComment(any())).thenReturn("\u7b2c\u4e09\u65b9AI\u8bc4\u8bed");

            SubmissionResponse r = submissionService.grade(999L, req);
            assertThat(r).isNotNull();
            verify(submissionMapper).grade(999L, 85, "\u4e0d\u9519");
        }
    }

    @Test
    void grade_scoreOutOfRange_shouldThrow() {
        SubmissionGradeRequest req = new SubmissionGradeRequest();
        req.setScore(999);
        try (MockedStatic<AuthContext> ctx = mockStatic(AuthContext.class)) {
            ctx.when(AuthContext::get).thenReturn(teacherUser);
            when(submissionMapper.selectById(999L)).thenReturn(submission);
            when(assignmentService.requireAssignment(100L)).thenReturn(assignment);
            when(assignmentService.canManageAssignment(teacherUser, assignment)).thenReturn(true);

            assertThatThrownBy(() -> submissionService.grade(999L, req))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("\u5206\u6570\u5fc5\u987b\u5728");
        }
    }

    private SubmissionMapper.SubmissionResponseRow mockSubRow() {
        SubmissionMapper.SubmissionResponseRow r = new SubmissionMapper.SubmissionResponseRow();
        r.setId(999L);
        r.setAssignmentId(100L);
        r.setAssignmentTitle("\u4f5c\u4e1a1");
        r.setCourseId(10L);
        r.setCourseName("\u6d4b\u8bd5\u8bfe\u7a0b");
        r.setStudentId(2L);
        r.setStudentName("\u5b66\u751fA");
        r.setContent("\u6211\u7684\u4f5c\u4e1a");
        return r;
    }
}
