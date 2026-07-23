package com.whut.assessment.service;

import com.whut.assessment.dto.ExamAttemptGradeRequest;
import com.whut.assessment.dto.ExamAttemptSubmitRequest;
import com.whut.assessment.entity.Exam;
import com.whut.assessment.entity.ExamAttempt;
import com.whut.assessment.entity.Question;
import com.whut.assessment.mapper.ExamAttemptMapper;
import com.whut.assessment.mapper.QuestionMapper;
import com.whut.assessment.vo.ExamAttemptResponse;
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
class ExamAttemptServiceTest {

    @Mock private ExamAttemptMapper examAttemptMapper;
    @Mock private ExamService examService;
    @Mock private QuestionMapper questionMapper;

    private ExamAttemptService examAttemptService;
    private AuthUser studentUser;
    private AuthUser teacherUser;
    private Exam exam;
    private ExamAttempt attempt;

    @BeforeEach
    void setUp() {
        examAttemptService = new ExamAttemptService(examAttemptMapper, examService, questionMapper);

        studentUser = new AuthUser(2L, "student1", UserRole.STUDENT.getCode());
        teacherUser = new AuthUser(1L, "teacher1", UserRole.TEACHER.getCode());

        exam = new Exam();
        exam.setId(50L);
        exam.setCourseId(10L);
        exam.setTeacherId(1L);
        exam.setTitle("期中考试");
        exam.setType("midterm");
        exam.setDuration(60);
        exam.setFullScore(100);
        exam.setStatus(1);
        exam.setStartTime(LocalDateTime.now().minusHours(1));
        exam.setEndTime(LocalDateTime.now().plusHours(1));

        attempt = new ExamAttempt();
        attempt.setId(200L);
        attempt.setExamId(50L);
        attempt.setStudentId(2L);
        attempt.setStatus(0); // in progress
    }

    // ── start ───────────────────────────────────────────────────

    @Test
    void start_notStudent_shouldThrow() {
        try (MockedStatic<AuthContext> ctx = mockStatic(AuthContext.class)) {
            ctx.when(AuthContext::get).thenReturn(teacherUser);

            assertThatThrownBy(() -> examAttemptService.start(50L))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("只有学生可以参加考试");
        }
    }

    @Test
    void start_examNotOpen_shouldThrow() {
        exam.setEndTime(LocalDateTime.now().minusMinutes(10));
        try (MockedStatic<AuthContext> ctx = mockStatic(AuthContext.class)) {
            ctx.when(AuthContext::get).thenReturn(studentUser);
            when(examService.requireExam(50L)).thenReturn(exam);
            doNothing().when(examService).assertApprovedStudent(10L, studentUser);

            assertThatThrownBy(() -> examAttemptService.start(50L))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("尚未开始或已经结束");
        }
    }

    @Test
    void start_existingInProgress_shouldReturnExisting() {
        ExamAttempt existingAttempt = new ExamAttempt();
        existingAttempt.setId(201L);
        existingAttempt.setExamId(50L);
        existingAttempt.setStudentId(2L);
        existingAttempt.setStatus(0);

        try (MockedStatic<AuthContext> ctx = mockStatic(AuthContext.class)) {
            ctx.when(AuthContext::get).thenReturn(studentUser);
            when(examService.requireExam(50L)).thenReturn(exam);
            doNothing().when(examService).assertApprovedStudent(10L, studentUser);
            when(examAttemptMapper.findByExamAndStudent(50L, 2L)).thenReturn(existingAttempt);
            when(examAttemptMapper.findResponseById(201L)).thenReturn(mockRespRow(201L));

            ExamAttemptResponse result = examAttemptService.start(50L);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(201L);
            verify(examAttemptMapper, never()).insert(any(ExamAttempt.class));
        }
    }

    @Test
    void start_newAttempt_shouldCreateAndReturn() {
        try (MockedStatic<AuthContext> ctx = mockStatic(AuthContext.class)) {
            ctx.when(AuthContext::get).thenReturn(studentUser);
            when(examService.requireExam(50L)).thenReturn(exam);
            doNothing().when(examService).assertApprovedStudent(10L, studentUser);
            when(examAttemptMapper.findByExamAndStudent(50L, 2L)).thenReturn(null);
            when(examAttemptMapper.insert(any(ExamAttempt.class))).thenAnswer(inv -> {
                ExamAttempt a = inv.getArgument(0);
                a.setId(300L);
                return 1;
            });
            when(examAttemptMapper.findResponseById(300L)).thenReturn(mockRespRow(300L));

            ExamAttemptResponse result = examAttemptService.start(50L);

            assertThat(result).isNotNull();
            verify(examAttemptMapper).insert(any(ExamAttempt.class));
        }
    }

    @Test
    void start_existingCompleted_shouldCreateNewAttempt() {
        ExamAttempt completedAttempt = new ExamAttempt();
        completedAttempt.setId(202L);
        completedAttempt.setExamId(50L);
        completedAttempt.setStudentId(2L);
        completedAttempt.setStatus(2); // already submitted

        try (MockedStatic<AuthContext> ctx = mockStatic(AuthContext.class)) {
            ctx.when(AuthContext::get).thenReturn(studentUser);
            when(examService.requireExam(50L)).thenReturn(exam);
            doNothing().when(examService).assertApprovedStudent(10L, studentUser);
            when(examAttemptMapper.findByExamAndStudent(50L, 2L)).thenReturn(completedAttempt);
            when(examAttemptMapper.insert(any(ExamAttempt.class))).thenAnswer(inv -> {
                ExamAttempt a = inv.getArgument(0);
                a.setId(400L);
                return 1;
            });
            when(examAttemptMapper.findResponseById(400L)).thenReturn(mockRespRow(400L));

            ExamAttemptResponse result = examAttemptService.start(50L);

            assertThat(result).isNotNull();
            verify(examAttemptMapper).insert(any(ExamAttempt.class));
        }
    }

    // ── submit ──────────────────────────────────────────────────

    @Test
    void submit_emptyContent_shouldThrow() {
        ExamAttemptSubmitRequest req = new ExamAttemptSubmitRequest();
        req.setAnswerContent("");

        try (MockedStatic<AuthContext> ctx = mockStatic(AuthContext.class)) {
            ctx.when(AuthContext::get).thenReturn(studentUser);
            when(examService.requireExam(50L)).thenReturn(exam);
            doNothing().when(examService).assertApprovedStudent(10L, studentUser);

            assertThatThrownBy(() -> examAttemptService.submit(50L, req))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("作答内容不能为空");
        }
    }

    @Test
    void submit_noAttempt_shouldThrow() {
        ExamAttemptSubmitRequest req = new ExamAttemptSubmitRequest();
        req.setAnswerContent("[{\"questionId\":1,\"answer\":\"2\"}]");

        try (MockedStatic<AuthContext> ctx = mockStatic(AuthContext.class)) {
            ctx.when(AuthContext::get).thenReturn(studentUser);
            when(examService.requireExam(50L)).thenReturn(exam);
            doNothing().when(examService).assertApprovedStudent(10L, studentUser);
            when(examAttemptMapper.findByExamAndStudent(50L, 2L)).thenReturn(null);

            assertThatThrownBy(() -> examAttemptService.submit(50L, req))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("请先开始考试");
        }
    }

    @Test
    void submit_alreadySubmitted_shouldThrow() {
        attempt.setStatus(2);
        ExamAttemptSubmitRequest req = new ExamAttemptSubmitRequest();
        req.setAnswerContent("[{\"questionId\":1,\"answer\":\"2\"}]");

        try (MockedStatic<AuthContext> ctx = mockStatic(AuthContext.class)) {
            ctx.when(AuthContext::get).thenReturn(studentUser);
            when(examService.requireExam(50L)).thenReturn(exam);
            doNothing().when(examService).assertApprovedStudent(10L, studentUser);
            when(examAttemptMapper.findByExamAndStudent(50L, 2L)).thenReturn(attempt);

            assertThatThrownBy(() -> examAttemptService.submit(50L, req))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("已交卷");
        }
    }

    @Test
    void submit_shouldSucceed() {
        ExamAttemptSubmitRequest req = new ExamAttemptSubmitRequest();
        req.setAnswerContent("[{\"questionId\":1,\"answer\":\"2\"}]");

        try (MockedStatic<AuthContext> ctx = mockStatic(AuthContext.class)) {
            ctx.when(AuthContext::get).thenReturn(studentUser);
            when(examService.requireExam(50L)).thenReturn(exam);
            doNothing().when(examService).assertApprovedStudent(10L, studentUser);
            when(examAttemptMapper.findByExamAndStudent(50L, 2L)).thenReturn(attempt);
            when(examAttemptMapper.submit(200L, req.getAnswerContent())).thenReturn(1);
            when(examAttemptMapper.selectById(200L)).thenReturn(attempt);
            when(questionMapper.findByExamId(50L)).thenReturn(List.of());
            when(examAttemptMapper.findResponseById(200L)).thenReturn(mockRespRow(200L));

            ExamAttemptResponse result = examAttemptService.submit(50L, req);

            assertThat(result).isNotNull();
            verify(examAttemptMapper).submit(200L, req.getAnswerContent());
        }
    }

    // ── myAttempts ──────────────────────────────────────────────

    @Test
    void myAttempts_shouldReturnList() {
        try (MockedStatic<AuthContext> ctx = mockStatic(AuthContext.class)) {
            ctx.when(AuthContext::get).thenReturn(studentUser);
            when(examAttemptMapper.findByStudentId(2L)).thenReturn(List.of(mockRespRow(200L)));

            List<ExamAttemptResponse> result = examAttemptService.myAttempts();

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getId()).isEqualTo(200L);
        }
    }

    // ── examAttempts ────────────────────────────────────────────

    @Test
    void examAttempts_notManager_shouldThrow() {
        try (MockedStatic<AuthContext> ctx = mockStatic(AuthContext.class)) {
            ctx.when(AuthContext::get).thenReturn(teacherUser);
            when(examService.requireExam(50L)).thenReturn(exam);
            when(examService.canManageExam(teacherUser, exam)).thenReturn(false);

            assertThatThrownBy(() -> examAttemptService.examAttempts(50L))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("无权查看");
        }
    }

    @Test
    void examAttempts_shouldReturnList() {
        try (MockedStatic<AuthContext> ctx = mockStatic(AuthContext.class)) {
            ctx.when(AuthContext::get).thenReturn(teacherUser);
            when(examService.requireExam(50L)).thenReturn(exam);
            when(examService.canManageExam(teacherUser, exam)).thenReturn(true);
            when(examAttemptMapper.findByExamId(50L)).thenReturn(List.of(mockRespRow(200L)));

            List<ExamAttemptResponse> result = examAttemptService.examAttempts(50L);

            assertThat(result).hasSize(1);
        }
    }

    // ── detail ──────────────────────────────────────────────────

    @Test
    void detail_asStudentViewOwn_shouldSucceed() {
        try (MockedStatic<AuthContext> ctx = mockStatic(AuthContext.class)) {
            ctx.when(AuthContext::get).thenReturn(studentUser);
            when(examAttemptMapper.selectById(200L)).thenReturn(attempt);
            when(examService.requireExam(50L)).thenReturn(exam);
            when(examAttemptMapper.findResponseById(200L)).thenReturn(mockRespRow(200L));

            ExamAttemptResponse result = examAttemptService.detail(200L);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(200L);
        }
    }

    @Test
    void detail_asStudentViewOthers_shouldThrow() {
        attempt.setStudentId(99L); // another student's attempt
        try (MockedStatic<AuthContext> ctx = mockStatic(AuthContext.class)) {
            ctx.when(AuthContext::get).thenReturn(studentUser);
            when(examAttemptMapper.selectById(200L)).thenReturn(attempt);
            when(examService.requireExam(50L)).thenReturn(exam);

            assertThatThrownBy(() -> examAttemptService.detail(200L))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("无权查看");
        }
    }

    // ── grade ───────────────────────────────────────────────────

    @Test
    void grade_notManager_shouldThrow() {
        ExamAttemptGradeRequest req = new ExamAttemptGradeRequest();
        req.setScore(85);

        try (MockedStatic<AuthContext> ctx = mockStatic(AuthContext.class)) {
            ctx.when(AuthContext::get).thenReturn(teacherUser);
            when(examAttemptMapper.selectById(200L)).thenReturn(attempt);
            when(examService.requireExam(50L)).thenReturn(exam);
            when(examService.canManageExam(teacherUser, exam)).thenReturn(false);

            assertThatThrownBy(() -> examAttemptService.grade(200L, req))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("无权批改");
        }
    }

    @Test
    void grade_notSubmitted_shouldThrow() {
        attempt.setStatus(0);
        ExamAttemptGradeRequest req = new ExamAttemptGradeRequest();
        req.setScore(85);

        try (MockedStatic<AuthContext> ctx = mockStatic(AuthContext.class)) {
            ctx.when(AuthContext::get).thenReturn(teacherUser);
            when(examAttemptMapper.selectById(200L)).thenReturn(attempt);
            when(examService.requireExam(50L)).thenReturn(exam);
            when(examService.canManageExam(teacherUser, exam)).thenReturn(true);

            assertThatThrownBy(() -> examAttemptService.grade(200L, req))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("尚未交卷");
        }
    }

    @Test
    void grade_scoreOutOfRange_shouldThrow() {
        attempt.setStatus(1);
        ExamAttemptGradeRequest req = new ExamAttemptGradeRequest();
        req.setScore(999);

        try (MockedStatic<AuthContext> ctx = mockStatic(AuthContext.class)) {
            ctx.when(AuthContext::get).thenReturn(teacherUser);
            when(examAttemptMapper.selectById(200L)).thenReturn(attempt);
            when(examService.requireExam(50L)).thenReturn(exam);
            when(examService.canManageExam(teacherUser, exam)).thenReturn(true);

            assertThatThrownBy(() -> examAttemptService.grade(200L, req))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("分数必须在");
        }
    }

    @Test
    void grade_shouldSucceed() {
        attempt.setStatus(1);
        ExamAttemptGradeRequest req = new ExamAttemptGradeRequest();
        req.setScore(88);
        req.setTeacherComment("不错");

        try (MockedStatic<AuthContext> ctx = mockStatic(AuthContext.class)) {
            ctx.when(AuthContext::get).thenReturn(teacherUser);
            when(examAttemptMapper.selectById(200L)).thenReturn(attempt);
            when(examService.requireExam(50L)).thenReturn(exam);
            when(examService.canManageExam(teacherUser, exam)).thenReturn(true);
            when(examAttemptMapper.grade(200L, 88, "不错")).thenReturn(1);
            when(examAttemptMapper.findResponseById(200L)).thenReturn(mockRespRow(200L));

            ExamAttemptResponse result = examAttemptService.grade(200L, req);

            assertThat(result).isNotNull();
            verify(examAttemptMapper).grade(200L, 88, "不错");
        }
    }

    // ── auto-grade ──────────────────────────────────────────────

    @Test
    void submit_withAutoGrade_shouldCalculateScore() {
        Question q1 = new Question();
        q1.setId(1L);
        q1.setExamId(50L);
        q1.setAnswer("2");
        q1.setScore(50);
        Question q2 = new Question();
        q2.setId(2L);
        q2.setExamId(50L);
        q2.setAnswer("4");
        q2.setScore(50);

        ExamAttemptSubmitRequest req = new ExamAttemptSubmitRequest();
        req.setAnswerContent("[{\"questionId\":1,\"type\":1,\"answer\":\"2\"},{\"questionId\":2,\"type\":1,\"answer\":\"wrong\"}]");

        // Make the attempt hold the answer content for autoGrade to read
        attempt.setAnswerContent(req.getAnswerContent());

        try (MockedStatic<AuthContext> ctx = mockStatic(AuthContext.class)) {
            ctx.when(AuthContext::get).thenReturn(studentUser);
            when(examService.requireExam(50L)).thenReturn(exam);
            doNothing().when(examService).assertApprovedStudent(10L, studentUser);
            when(examAttemptMapper.findByExamAndStudent(50L, 2L)).thenReturn(attempt);
            when(examAttemptMapper.submit(200L, req.getAnswerContent())).thenReturn(1);
            when(examAttemptMapper.selectById(200L)).thenReturn(attempt);
            when(questionMapper.findByExamId(50L)).thenReturn(List.of(q1, q2));
            when(examAttemptMapper.autoGrade(200L, 50)).thenReturn(1);
            when(examAttemptMapper.findResponseById(200L)).thenReturn(mockRespRow(200L));

            ExamAttemptResponse result = examAttemptService.submit(50L, req);

            assertThat(result).isNotNull();
            verify(examAttemptMapper).autoGrade(200L, 50);
        }
    }

    // ── helper ──────────────────────────────────────────────────

    private ExamAttemptMapper.ExamAttemptResponseRow mockRespRow(Long id) {
        ExamAttemptMapper.ExamAttemptResponseRow row = new ExamAttemptMapper.ExamAttemptResponseRow();
        row.setId(id);
        row.setExamId(50L);
        row.setExamTitle("期中考试");
        row.setCourseId(10L);
        row.setCourseName("数学");
        row.setStudentId(2L);
        row.setStudentName("学生A");
        row.setAnswerContent(null);
        row.setStatus(0);
        row.setScore(null);
        row.setTeacherComment(null);
        return row;
    }
}
