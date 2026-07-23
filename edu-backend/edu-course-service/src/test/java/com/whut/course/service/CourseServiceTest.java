package com.whut.course.service;

import com.whut.common.auth.AuthContext;
import com.whut.common.auth.AuthUser;
import com.whut.common.enums.UserRole;
import com.whut.common.exception.BusinessException;
import com.whut.course.dto.CourseCreateRequest;
import com.whut.course.dto.CourseStatusUpdateRequest;
import com.whut.course.dto.CourseUpdateRequest;
import com.whut.course.entity.Course;
import com.whut.course.mapper.CourseMapper;
import com.whut.course.vo.CourseResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CourseServiceTest {

    @Mock private CourseMapper courseMapper;
    @Mock private CourseClassService courseClassService;

    private CourseService courseService;
    private AuthUser teacherUser;
    private AuthUser studentUser;
    private Course testCourse;

    @BeforeEach
    void setUp() {
        courseService = new CourseService(courseMapper, courseClassService);
        teacherUser = new AuthUser(1L, "teacher1", UserRole.TEACHER.getCode());
        studentUser = new AuthUser(2L, "student1", UserRole.STUDENT.getCode());
        testCourse = new Course();
        testCourse.setId(10L);
        testCourse.setName("\u6d4b\u8bd5\u8bfe\u7a0b");
        testCourse.setTeacherId(1L);
        testCourse.setStatus(1);
        testCourse.setMaxStudents(100);
        testCourse.setEnrolledCount(0);
    }

    @Test
    void create_asStudent_shouldThrow() {
        CourseCreateRequest request = new CourseCreateRequest();
        request.setName("\u65b0\u8bfe\u7a0b");
        try (MockedStatic<AuthContext> ctx = mockStatic(AuthContext.class)) {
            ctx.when(AuthContext::get).thenReturn(studentUser);
            assertThatThrownBy(() -> courseService.create(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("\u53ea\u6709\u6559\u5e08\u6216\u7ba1\u7406\u5458");
        }
    }

    @Test
    void create_withNameExists_shouldCreateNewClassSection() {
        CourseCreateRequest request = new CourseCreateRequest();
        request.setName("\u6570\u5b66");
        request.setMaxStudents(50);
        Course existing = new Course();
        existing.setId(10L);
        existing.setName("\u6570\u5b66");
        existing.setMaxStudents(100);
        existing.setStatus(1);
        try (MockedStatic<AuthContext> ctx = mockStatic(AuthContext.class)) {
            ctx.when(AuthContext::get).thenReturn(teacherUser);
            when(courseMapper.findByName("\u6570\u5b66")).thenReturn(existing);
            when(courseMapper.selectById(10L)).thenReturn(existing);
            when(courseMapper.findResponseById(10L)).thenReturn(mockResponseRow(10L, "\u6570\u5b66"));

            CourseResponse response = courseService.create(request);

            assertThat(response).isNotNull();
            assertThat(response.getHint()).contains("\u65b0\u589e\u6559\u5b66\u73ed");
            verify(courseClassService).createClassSection(eq(10L), eq(1L), eq(100), any());
        }
    }

    @Test
    void detail_asStudent_onlineCourse_shouldSucceed() {
        try (MockedStatic<AuthContext> ctx = mockStatic(AuthContext.class)) {
            ctx.when(AuthContext::get).thenReturn(studentUser);
            when(courseMapper.selectById(10L)).thenReturn(testCourse);
            when(courseMapper.findResponseById(10L)).thenReturn(mockResponseRow(10L, "\u6d4b\u8bd5\u8bfe\u7a0b"));

            CourseResponse response = courseService.detail(10L);

            assertThat(response).isNotNull();
            assertThat(response.getId()).isEqualTo(10L);
        }
    }

    @Test
    void update_maxStudentsBelowEnrolled_shouldThrow() {
        testCourse.setEnrolledCount(30);
        CourseUpdateRequest request = new CourseUpdateRequest();
        request.setMaxStudents(20);
        try (MockedStatic<AuthContext> ctx = mockStatic(AuthContext.class)) {
            ctx.when(AuthContext::get).thenReturn(teacherUser);
            when(courseMapper.selectById(10L)).thenReturn(testCourse);

            assertThatThrownBy(() -> courseService.update(10L, request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("\u4e0d\u80fd\u5c0f\u4e8e\u5f53\u524d\u5df2\u9009\u4eba\u6570");
        }
    }

    @Test
    void updateStatus_shouldSucceed() {
        CourseStatusUpdateRequest request = new CourseStatusUpdateRequest();
        request.setStatus(0);
        try (MockedStatic<AuthContext> ctx = mockStatic(AuthContext.class)) {
            ctx.when(AuthContext::get).thenReturn(teacherUser);
            when(courseMapper.selectById(10L)).thenReturn(testCourse);
            when(courseMapper.findResponseById(10L)).thenReturn(mockResponseRow(10L, "\u6d4b\u8bd5\u8bfe\u7a0b"));

            CourseResponse response = courseService.updateStatus(10L, request);

            assertThat(response).isNotNull();
            verify(courseMapper).updateStatus(10L, 0);
        }
    }

    @Test
    void delete_asTeacher_shouldSucceed() {
        try (MockedStatic<AuthContext> ctx = mockStatic(AuthContext.class)) {
            ctx.when(AuthContext::get).thenReturn(teacherUser);
            when(courseMapper.selectById(10L)).thenReturn(testCourse);

            courseService.delete(10L);

            verify(courseMapper).deleteById(10L);
        }
    }

    private CourseMapper.CourseResponseRow mockResponseRow(Long id, String name) {
        CourseMapper.CourseResponseRow row = new CourseMapper.CourseResponseRow();
        row.setId(id);
        row.setName(name);
        row.setStatus(1);
        row.setTeacherId(1L);
        row.setMaxStudents(100);
        row.setEnrolledCount(0);
        row.setTeacherName("\u5f20\u8001\u5e08");
        return row;
    }
}
