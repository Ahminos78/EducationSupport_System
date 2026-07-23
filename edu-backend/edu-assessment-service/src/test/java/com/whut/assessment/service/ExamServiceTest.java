package com.whut.assessment.service;

import com.whut.assessment.dto.ExamCreateRequest;
import com.whut.assessment.dto.ExamStatusUpdateRequest;
import com.whut.assessment.dto.ExamWithQuestionsRequest;
import com.whut.assessment.entity.CourseSnapshot;
import com.whut.assessment.entity.Exam;
import com.whut.assessment.mapper.ExamAttemptMapper;
import com.whut.assessment.mapper.ExamMapper;
import com.whut.assessment.mapper.QuestionMapper;
import com.whut.assessment.vo.ExamResponse;
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
class ExamServiceTest {

    @Mock private ExamMapper examMapper;
    @Mock private QuestionMapper questionMapper;
    @Mock private ExamAttemptMapper examAttemptMapper;

    private ExamService examService;
    private AuthUser teacherUser;
    private CourseSnapshot course;
    private Exam exam;

    @BeforeEach
    void setUp() {
        examService = new ExamService(examMapper, questionMapper, examAttemptMapper);
        teacherUser = new AuthUser(1L, "teacher1", UserRole.TEACHER.getCode());
        course = new CourseSnapshot();
        course.setId(10L);
        course.setTeacherId(1L);
        course.setStatus(1);
        course.setDeleted(0);
        exam = new Exam();
        exam.setId(100L);
        exam.setCourseId(10L);
        exam.setTeacherId(1L);
        exam.setTitle("\u671f\u4e2d\u8003\u8bd5");
        exam.setStartTime(LocalDateTime.now().minusDays(1));
        exam.setEndTime(LocalDateTime.now().plusDays(7));
        exam.setDuration(60);
        exam.setType("quiz");
        exam.setStatus(1);
    }

    @Test
    void create_asTeacher_shouldSucceed() {
        ExamCreateRequest req = new ExamCreateRequest();
        req.setCourseId(10L);
        req.setTitle("\u6d4b\u9a8c\u8003\u8bd5");
        req.setStartTime(LocalDateTime.now().plusDays(1));
        req.setEndTime(LocalDateTime.now().plusDays(2));
        req.setDuration(90);
        req.setType("quiz");

        try (MockedStatic<AuthContext> ctx = mockStatic(AuthContext.class)) {
            ctx.when(AuthContext::get).thenReturn(teacherUser);
            when(examMapper.findCourseById(10L)).thenReturn(course);
            when(examMapper.selectById(any())).thenReturn(exam);
            when(examMapper.findResponseById(any())).thenReturn(mockExamRow());

            ExamResponse r = examService.create(req);
            assertThat(r).isNotNull();

        }
    }

    @Test
    void create_endTimeBeforeStart_shouldThrow() {
        ExamCreateRequest req = new ExamCreateRequest();
        req.setCourseId(10L);
        req.setTitle("\u8003\u8bd5");
        req.setStartTime(LocalDateTime.now().plusDays(5));
        req.setEndTime(LocalDateTime.now().plusDays(3));

        try (MockedStatic<AuthContext> ctx = mockStatic(AuthContext.class)) {
            ctx.when(AuthContext::get).thenReturn(teacherUser);
            when(examMapper.findCourseById(10L)).thenReturn(course);

            assertThatThrownBy(() -> examService.create(req))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("\u7ed3\u675f\u65f6\u95f4\u5fc5\u987b\u665a\u4e8e\u5f00\u59cb\u65f6\u95f4");
        }
    }

    @Test
    void createWithQuestions_shouldSucceed() {
        ExamWithQuestionsRequest req = new ExamWithQuestionsRequest();
        req.setCourseId(10L);
        req.setTitle("\u5e26\u9898\u76ee\u7684\u8003\u8bd5");
        req.setStartTime(LocalDateTime.now().plusDays(1));
        req.setEndTime(LocalDateTime.now().plusDays(2));
        req.setType("quiz");
        req.setDuration(60);
        ExamWithQuestionsRequest.QuestionItem qi = new ExamWithQuestionsRequest.QuestionItem();
        qi.setType(1);
        qi.setTitle("1+1=?");
        qi.setAnswer("2");
        qi.setSortOrder(1);
        req.setQuestions(List.of(qi));

        try (MockedStatic<AuthContext> ctx = mockStatic(AuthContext.class)) {
            ctx.when(AuthContext::get).thenReturn(teacherUser);
            when(examMapper.findCourseById(10L)).thenReturn(course);
            when(examMapper.selectById(any())).thenReturn(exam);
            when(examMapper.findResponseById(any())).thenReturn(mockExamRow());

            ExamResponse r = examService.createWithQuestions(req);
            assertThat(r).isNotNull();
        }
    }

    @Test
    void delete_shouldSucceed() {
        try (MockedStatic<AuthContext> ctx = mockStatic(AuthContext.class)) {
            ctx.when(AuthContext::get).thenReturn(teacherUser);
            when(examMapper.selectById(any())).thenReturn(exam);
            when(examMapper.findCourseById(10L)).thenReturn(course);

            examService.delete(100L);
            verify(examMapper).deleteById(100L);
        }
    }

    private ExamMapper.ExamResponseRow mockExamRow() {
        ExamMapper.ExamResponseRow r = new ExamMapper.ExamResponseRow();
        r.setId(100L);
        r.setCourseId(10L);
        r.setTeacherId(1L);
        r.setTitle("\u6d4b\u9a8c\u8003\u8bd5");
        r.setCourseName("\u6d4b\u8bd5\u8bfe\u7a0b");
        r.setDuration(60);
        r.setType("quiz");
        r.setStatus(1);
        return r;
    }
}
