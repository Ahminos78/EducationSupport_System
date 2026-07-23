package com.whut.assessment.service;

import com.whut.assessment.entity.Assignment;
import com.whut.assessment.entity.AssignmentAttachment;
import com.whut.assessment.entity.Submission;
import com.whut.assessment.entity.SubmissionAttachment;
import com.whut.assessment.mapper.AssignmentAttachmentMapper;
import com.whut.assessment.mapper.SubmissionAttachmentMapper;
import com.whut.assessment.vo.AttachmentResponse;
import com.whut.common.auth.AuthContext;
import com.whut.common.auth.AuthUser;
import com.whut.common.enums.UserRole;
import com.whut.common.exception.BusinessException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AttachmentServiceTest {

    @Mock private AssignmentAttachmentMapper assignmentAttachmentMapper;
    @Mock private SubmissionAttachmentMapper submissionAttachmentMapper;
    @Mock private AssignmentService assignmentService;
    @Mock private SubmissionService submissionService;

    private AttachmentService attachmentService;
    private AuthUser teacherUser;
    private AuthUser studentUser;
    private Assignment assignment;
    private Submission submission;
    private Path tempDir;

    @BeforeEach
    void setUp() throws IOException {
        tempDir = Files.createTempDirectory("attachment-test-");
        attachmentService = new AttachmentService(assignmentAttachmentMapper, submissionAttachmentMapper,
                assignmentService, submissionService, tempDir.toString());

        teacherUser = new AuthUser(1L, "teacher1", UserRole.TEACHER.getCode());
        studentUser = new AuthUser(2L, "student1", UserRole.STUDENT.getCode());

        assignment = new Assignment();
        assignment.setId(100L);
        assignment.setCourseId(10L);
        assignment.setTeacherId(1L);

        submission = new Submission();
        submission.setId(999L);
        submission.setAssignmentId(100L);
        submission.setStudentId(2L);
    }

    @AfterEach
    void tearDown() throws IOException {
        try (var files = Files.walk(tempDir)) {
            files.sorted(Comparator.reverseOrder()).forEach(p -> {
                try { Files.deleteIfExists(p); } catch (IOException ignored) {}
            });
        }
    }

    // ── listAssignmentAttachments ───────────────────────────────

    @Test
    void listAssignmentAttachments_shouldReturnList() {
        try (MockedStatic<AuthContext> ctx = mockStatic(AuthContext.class)) {
            ctx.when(AuthContext::get).thenReturn(teacherUser);
            when(assignmentService.requireAssignment(100L)).thenReturn(assignment);
            doNothing().when(assignmentService).assertAssignmentAccess(assignment, teacherUser);

            AssignmentAttachment att = new AssignmentAttachment();
            att.setId(1L);
            att.setAssignmentId(100L);
            att.setOriginalName("test.pdf");
            when(assignmentAttachmentMapper.selectList(any())).thenReturn(List.of(att));

            List<AttachmentResponse> result = attachmentService.listAssignmentAttachments(100L);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getOriginalName()).isEqualTo("test.pdf");
        }
    }

    @Test
    void listSubmissionAttachments_shouldReturnList() {
        try (MockedStatic<AuthContext> ctx = mockStatic(AuthContext.class)) {
            ctx.when(AuthContext::get).thenReturn(teacherUser);
            doNothing().when(submissionService).assertSubmissionAccess(999L, teacherUser);

            SubmissionAttachment att = new SubmissionAttachment();
            att.setId(1L);
            att.setSubmissionId(999L);
            att.setOriginalName("submission.pdf");
            when(submissionAttachmentMapper.selectList(any())).thenReturn(List.of(att));

            List<AttachmentResponse> result = attachmentService.listSubmissionAttachments(999L);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getOriginalName()).isEqualTo("submission.pdf");
        }
    }

    // ── uploadAssignmentAttachment ───────────────────────────────

    @Test
    void uploadAssignmentAttachment_shouldSucceed() {
        MultipartFile file = new MockMultipartFile("file", "homework.pdf", "application/pdf",
                "test content".getBytes());

        try (MockedStatic<AuthContext> ctx = mockStatic(AuthContext.class)) {
            ctx.when(AuthContext::get).thenReturn(teacherUser);
            when(assignmentService.requireAssignment(100L)).thenReturn(assignment);
            when(assignmentService.canManageAssignment(teacherUser, assignment)).thenReturn(true);

            AttachmentResponse result = attachmentService.uploadAssignmentAttachment(100L, file);

            assertThat(result).isNotNull();
            assertThat(result.getOriginalName()).isEqualTo("homework.pdf");
            assertThat(result.getContentType()).isEqualTo("application/pdf");
            assertThat(result.getDownloadUrl()).contains("assignment-attachments");

            verify(assignmentAttachmentMapper).insert(any(AssignmentAttachment.class));
        }
    }

    @Test
    void uploadAssignmentAttachment_noPermission_shouldThrow() {
        MultipartFile file = new MockMultipartFile("file", "test.pdf", "application/pdf",
                "test".getBytes());
        AuthUser otherTeacher = new AuthUser(5L, "other", UserRole.TEACHER.getCode());

        try (MockedStatic<AuthContext> ctx = mockStatic(AuthContext.class)) {
            ctx.when(AuthContext::get).thenReturn(otherTeacher);
            when(assignmentService.requireAssignment(100L)).thenReturn(assignment);
            when(assignmentService.canManageAssignment(otherTeacher, assignment)).thenReturn(false);

            assertThatThrownBy(() -> attachmentService.uploadAssignmentAttachment(100L, file))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("无权上传");
        }
    }

    @Test
    void uploadAssignmentAttachment_emptyFile_shouldThrow() {
        MultipartFile file = new MockMultipartFile("file", "empty.pdf", "application/pdf", new byte[0]);

        try (MockedStatic<AuthContext> ctx = mockStatic(AuthContext.class)) {
            ctx.when(AuthContext::get).thenReturn(teacherUser);
            when(assignmentService.requireAssignment(100L)).thenReturn(assignment);
            when(assignmentService.canManageAssignment(teacherUser, assignment)).thenReturn(true);

            assertThatThrownBy(() -> attachmentService.uploadAssignmentAttachment(100L, file))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("文件");
        }
    }

    @Test
    void uploadAssignmentAttachment_fileTooLarge_shouldThrow() {
        byte[] largeContent = new byte[21 * 1024 * 1024]; // 21 MB
        MultipartFile file = new MockMultipartFile("file", "big.pdf", "application/pdf", largeContent);

        try (MockedStatic<AuthContext> ctx = mockStatic(AuthContext.class)) {
            ctx.when(AuthContext::get).thenReturn(teacherUser);
            when(assignmentService.requireAssignment(100L)).thenReturn(assignment);
            when(assignmentService.canManageAssignment(teacherUser, assignment)).thenReturn(true);

            assertThatThrownBy(() -> attachmentService.uploadAssignmentAttachment(100L, file))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("20MB");
        }
    }

    // ── uploadSubmissionAttachment ──────────────────────────────

    @Test
    void uploadSubmissionAttachment_shouldSucceed() {
        MultipartFile file = new MockMultipartFile("file", "answer.pdf", "application/pdf",
                "answer content".getBytes());

        try (MockedStatic<AuthContext> ctx = mockStatic(AuthContext.class)) {
            ctx.when(AuthContext::get).thenReturn(studentUser);
            when(submissionService.requireOwnedSubmission(999L, studentUser)).thenReturn(submission);

            AttachmentResponse result = attachmentService.uploadSubmissionAttachment(999L, file);

            assertThat(result).isNotNull();
            assertThat(result.getOriginalName()).isEqualTo("answer.pdf");
            verify(submissionAttachmentMapper).insert(any(SubmissionAttachment.class));
        }
    }

    // ── deleteAssignmentAttachment ──────────────────────────────

    @Test
    void deleteAssignmentAttachment_notFound_shouldThrow() {
        when(assignmentAttachmentMapper.selectById(999L)).thenReturn(null);

        assertThatThrownBy(() -> attachmentService.deleteAssignmentAttachment(999L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("作业附件不存在");
    }

    @Test
    void deleteAssignmentAttachment_noPermission_shouldThrow() {
        AssignmentAttachment att = new AssignmentAttachment();
        att.setId(1L);
        att.setAssignmentId(100L);
        when(assignmentAttachmentMapper.selectById(1L)).thenReturn(att);
        when(assignmentService.requireAssignment(100L)).thenReturn(assignment);
        when(assignmentService.canManageAssignment(any(), eq(assignment))).thenReturn(false);

        try (MockedStatic<AuthContext> ctx = mockStatic(AuthContext.class)) {
            ctx.when(AuthContext::get).thenReturn(teacherUser);

            assertThatThrownBy(() -> attachmentService.deleteAssignmentAttachment(1L))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("无权删除");
        }
    }

    @Test
    void deleteAssignmentAttachment_shouldSucceed() {
        AssignmentAttachment att = new AssignmentAttachment();
        att.setId(1L);
        att.setAssignmentId(100L);
        att.setStoredName("test-stored-name.pdf");
        when(assignmentAttachmentMapper.selectById(1L)).thenReturn(att);
        when(assignmentService.requireAssignment(100L)).thenReturn(assignment);
        when(assignmentService.canManageAssignment(any(), eq(assignment))).thenReturn(true);

        try (MockedStatic<AuthContext> ctx = mockStatic(AuthContext.class)) {
            ctx.when(AuthContext::get).thenReturn(teacherUser);

            attachmentService.deleteAssignmentAttachment(1L);

            verify(assignmentAttachmentMapper).deleteById(1L);
        }
    }

    // ── download ────────────────────────────────────────────────

    @Test
    void assignmentDownload_shouldReturnFile() throws IOException {
        // Pre-create a file on disk
        Path assignmentsDir = tempDir.resolve("assignments");
        Files.createDirectories(assignmentsDir);
        Files.writeString(assignmentsDir.resolve("test-file.pdf"), "file content");

        AssignmentAttachment att = new AssignmentAttachment();
        att.setId(1L);
        att.setAssignmentId(100L);
        att.setOriginalName("homework.pdf");
        att.setStoredName("test-file.pdf");
        att.setContentType("application/pdf");
        when(assignmentAttachmentMapper.selectById(1L)).thenReturn(att);
        when(assignmentService.requireAssignment(100L)).thenReturn(assignment);
        doNothing().when(assignmentService).assertAssignmentAccess(assignment, teacherUser);

        try (MockedStatic<AuthContext> ctx = mockStatic(AuthContext.class)) {
            ctx.when(AuthContext::get).thenReturn(teacherUser);

            AttachmentService.DownloadFile result = attachmentService.assignmentDownload(1L);

            assertThat(result).isNotNull();
            assertThat(result.originalName()).isEqualTo("homework.pdf");
            assertThat(result.contentType()).isEqualTo("application/pdf");
            assertThat(result.resource().exists()).isTrue();
        }
    }

    @Test
    void assignmentDownload_notFound_shouldThrow() {
        when(assignmentAttachmentMapper.selectById(999L)).thenReturn(null);

        assertThatThrownBy(() -> attachmentService.assignmentDownload(999L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("作业附件不存在");
    }
}
