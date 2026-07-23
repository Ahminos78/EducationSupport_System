package com.whut.assessment.service;

import com.whut.assessment.entity.CourseSnapshot;
import com.whut.assessment.entity.Exam;
import com.whut.assessment.entity.Question;
import com.whut.assessment.mapper.ExamAttemptMapper;
import com.whut.assessment.mapper.ExamMapper;
import com.whut.assessment.mapper.QuestionMapper;
import com.whut.assessment.vo.QuestionResponse;
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

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class QuestionServiceTest {

    @Mock private QuestionMapper questionMapper;
    @Mock private ExamMapper examMapper;
    @Mock private ExamAttemptMapper examAttemptMapper;

    private ExamService examService;
    private QuestionService questionService;
    private AuthUser teacherUser;
    private Exam exam;
    private CourseSnapshot course;

    @BeforeEach
    void setUp() {
        examService = new ExamService(examMapper, questionMapper, examAttemptMapper);
        questionService = new QuestionService(questionMapper, examService);
        teacherUser = new AuthUser(1L, "teacher1", UserRole.TEACHER.getCode());
        course = new CourseSnapshot();
        course.setId(10L);
        course.setTeacherId(1L);
        course.setStatus(1);
        exam = new Exam();
        exam.setId(50L);
        exam.setCourseId(10L);
        exam.setTeacherId(1L);
        exam.setStatus(1);
    }

    @Test
    void listByExam_withoutAnswers_shouldReturnPublic() {
        Question q = new Question();
        q.setId(1L);
        q.setExamId(50L);
        q.setTitle("1+1=?");
        q.setAnswer("2");

        try (MockedStatic<AuthContext> ctx = mockStatic(AuthContext.class)) {
            ctx.when(AuthContext::get).thenReturn(teacherUser);
            when(examMapper.selectById(50L)).thenReturn(exam);
            when(examMapper.findCourseById(10L)).thenReturn(course);
            when(questionMapper.findByExamId(50L)).thenReturn(List.of(q));

            List<QuestionResponse> list = questionService.listByExam(50L, false);
            assertThat(list).hasSize(1);
            assertThat(list.get(0).getAnswer()).isNull();
        }
    }

    @Test
    void create_shouldSucceed() {
        try (MockedStatic<AuthContext> ctx = mockStatic(AuthContext.class)) {
            ctx.when(AuthContext::get).thenReturn(teacherUser);
            when(examMapper.selectById(50L)).thenReturn(exam);
            when(examMapper.findCourseById(10L)).thenReturn(course);
            when(examAttemptMapper.countByExamId(50L)).thenReturn(0);

            QuestionResponse r = questionService.create(50L, 1, "1+1=?", null, "2", 10, 1);
            assertThat(r).isNotNull();
            assertThat(r.getAnswer()).isEqualTo("2");
        }
    }

    @Test
    void delete_examMismatch_shouldThrow() {
        Question q = new Question();
        q.setId(1L);
        q.setExamId(99L);

        try (MockedStatic<AuthContext> ctx = mockStatic(AuthContext.class)) {
            ctx.when(AuthContext::get).thenReturn(teacherUser);
            when(examMapper.selectById(50L)).thenReturn(exam);
            when(examMapper.findCourseById(10L)).thenReturn(course);
            when(examAttemptMapper.countByExamId(50L)).thenReturn(0);
            when(questionMapper.selectById(1L)).thenReturn(q);

            assertThatThrownBy(() -> questionService.delete(50L, 1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("\u9898\u76ee\u4e0d\u5b58\u5728");
        }
    }
}
