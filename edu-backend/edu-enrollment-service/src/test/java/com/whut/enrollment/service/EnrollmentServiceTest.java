package com.whut.enrollment.service;

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
import com.whut.enrollment.mapper.GradeComponentMapper;
import com.whut.enrollment.mapper.StudentGradeMapper;
import com.whut.enrollment.service.WarningService;
import com.whut.enrollment.vo.EnrollmentResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.quality.Strictness;
import org.mockito.junit.jupiter.MockitoSettings;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class EnrollmentServiceTest {

    @Mock private EnrollmentMapper enrollmentMapper;
    @Mock private GradeComponentMapper gradeComponentMapper;
    @Mock private StudentGradeMapper studentGradeMapper;
    @Mock private WarningService warningService;

    private EnrollmentService enrollmentService;
    private AuthUser studentUser;
    private AuthUser teacherUser;

    @BeforeEach
    void setUp() {
        enrollmentService = new EnrollmentService(enrollmentMapper, gradeComponentMapper,
                studentGradeMapper, warningService);
        studentUser = new AuthUser(2L, "student1", UserRole.STUDENT.getCode());
        teacherUser = new AuthUser(1L, "teacher1", UserRole.TEACHER.getCode());
    }

    @Test
    void apply_asStudent_shouldSucceed() {
        EnrollmentCreateRequest request = new EnrollmentCreateRequest();
        request.setCourseId(10L);
        request.setClassId(20L);

        CourseSnapshot course = new CourseSnapshot();
        course.setId(10L);
        course.setStatus(1);
        course.setDeleted(0);

        EnrollmentMapper.ClassSnapshot classSection = mock(EnrollmentMapper.ClassSnapshot.class);
        when(classSection.getCourseId()).thenReturn(10L);
        when(classSection.getEnrolledCount()).thenReturn(10);
        when(classSection.getMaxStudents()).thenReturn(100);

        try (MockedStatic<AuthContext> ctx = mockStatic(AuthContext.class)) {
            ctx.when(AuthContext::get).thenReturn(studentUser);
            when(enrollmentMapper.findCourseById(10L)).thenReturn(course);
            when(enrollmentMapper.findClassById(20L)).thenReturn(classSection);
            when(enrollmentMapper.selectOne(any())).thenReturn(null);
            when(enrollmentMapper.findScheduleSlotsByClassId(20L)).thenReturn(java.util.List.of());
            when(enrollmentMapper.findEnrolledClassIds(anyLong())).thenReturn(java.util.List.of());

            // Simulate auto-generated ID after insert
            Enrollment saved = new Enrollment();
            saved.setId(100L);
            saved.setCourseId(10L);
            saved.setClassId(20L);
            saved.setStudentId(2L);
            saved.setStatus(0);
            doAnswer(inv -> { inv.<Enrollment>getArgument(0).setId(100L); return 1; })
                .when(enrollmentMapper).insert(any(Enrollment.class));

            // Mock getOwnEnrollment inner chain
            when(enrollmentMapper.selectById(100L)).thenReturn(saved);
            EnrollmentMapper.EnrollmentResponseRow rrow = new EnrollmentMapper.EnrollmentResponseRow();
            rrow.setId(100L);
            when(enrollmentMapper.findByCourseId(10L, 20L, null)).thenReturn(java.util.List.of(rrow));

            EnrollmentResponse response = enrollmentService.apply(request);

            assertThat(response).isNotNull();
            verify(enrollmentMapper).insert(any(Enrollment.class));
        }
    }

    @Test
    void apply_nonStudent_shouldThrow() {
        EnrollmentCreateRequest request = new EnrollmentCreateRequest();
        try (MockedStatic<AuthContext> ctx = mockStatic(AuthContext.class)) {
            ctx.when(AuthContext::get).thenReturn(teacherUser);
            assertThatThrownBy(() -> enrollmentService.apply(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("\u53ea\u6709\u5b66\u751f");
        }
    }

    @Test
    void apply_classFull_shouldThrow() {
        EnrollmentCreateRequest request = new EnrollmentCreateRequest();
        request.setCourseId(10L);
        request.setClassId(20L);

        CourseSnapshot course = new CourseSnapshot();
        course.setId(10L);
        course.setStatus(1);
        course.setDeleted(0);

        EnrollmentMapper.ClassSnapshot classSection = mock(EnrollmentMapper.ClassSnapshot.class);
        when(classSection.getCourseId()).thenReturn(10L);
        when(classSection.getEnrolledCount()).thenReturn(100);
        when(classSection.getMaxStudents()).thenReturn(100);

        try (MockedStatic<AuthContext> ctx = mockStatic(AuthContext.class)) {
            ctx.when(AuthContext::get).thenReturn(studentUser);
            when(enrollmentMapper.findCourseById(10L)).thenReturn(course);
            when(enrollmentMapper.findClassById(20L)).thenReturn(classSection);

            assertThatThrownBy(() -> enrollmentService.apply(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("\u4eba\u6570\u5df2\u6ee1");
        }
    }

    @Test
    void approve_shouldUpdateStatus() {
        Enrollment enrollment = new Enrollment();
        enrollment.setId(100L);
        enrollment.setCourseId(10L);
        enrollment.setClassId(20L);
        enrollment.setStudentId(2L);
        enrollment.setStatus(EnrollmentStatus.PENDING.getCode());

        CourseSnapshot course = new CourseSnapshot();
        course.setId(10L);
        course.setTeacherId(1L);
        course.setDeleted(0);

        EnrollmentMapper.ClassSnapshot cls = mock(EnrollmentMapper.ClassSnapshot.class);
        when(cls.getTeacherId()).thenReturn(1L);

        try (MockedStatic<AuthContext> ctx = mockStatic(AuthContext.class)) {
            ctx.when(AuthContext::get).thenReturn(teacherUser);
            when(enrollmentMapper.selectById(100L)).thenReturn(enrollment);
            when(enrollmentMapper.findCourseById(10L)).thenReturn(course);
            when(enrollmentMapper.findClassById(20L)).thenReturn(cls);
            when(enrollmentMapper.increaseClassEnrollment(20L)).thenReturn(1);

            EnrollmentMapper.EnrollmentResponseRow approveRow = new EnrollmentMapper.EnrollmentResponseRow();
            approveRow.setId(100L);
            when(enrollmentMapper.findByCourseId(10L, 20L, null)).thenReturn(java.util.List.of(approveRow));

            enrollmentService.approve(100L, new EnrollmentReviewRequest());

            verify(enrollmentMapper).updateReviewStatus(100L, EnrollmentStatus.APPROVED.getCode(), null);
        }
    }

    @Test
    void reject_shouldUpdateStatus() {
        Enrollment enrollment = new Enrollment();
        enrollment.setId(100L);
        enrollment.setCourseId(10L);
        enrollment.setClassId(20L);
        enrollment.setStudentId(2L);
        enrollment.setStatus(EnrollmentStatus.PENDING.getCode());

        CourseSnapshot course = new CourseSnapshot();
        course.setId(10L);
        course.setTeacherId(1L);
        course.setDeleted(0);

        EnrollmentMapper.ClassSnapshot cls = mock(EnrollmentMapper.ClassSnapshot.class);
        when(cls.getTeacherId()).thenReturn(1L);

        try (MockedStatic<AuthContext> ctx = mockStatic(AuthContext.class)) {
            ctx.when(AuthContext::get).thenReturn(teacherUser);
            when(enrollmentMapper.selectById(100L)).thenReturn(enrollment);
            when(enrollmentMapper.findCourseById(10L)).thenReturn(course);
            when(enrollmentMapper.findClassById(20L)).thenReturn(cls);

            EnrollmentMapper.EnrollmentResponseRow rejectRow = new EnrollmentMapper.EnrollmentResponseRow();
            rejectRow.setId(100L);
            when(enrollmentMapper.findByCourseId(10L, 20L, null)).thenReturn(java.util.List.of(rejectRow));

            enrollmentService.reject(100L, new EnrollmentReviewRequest());

            verify(enrollmentMapper).updateReviewStatus(100L, EnrollmentStatus.REJECTED.getCode(), null);
        }
    }

    @Test
    void drop_asStudent_shouldSucceed() {
        Enrollment enrollment = new Enrollment();
        enrollment.setId(100L);
        enrollment.setCourseId(10L);
        enrollment.setClassId(20L);
        enrollment.setStudentId(2L);
        enrollment.setStatus(EnrollmentStatus.APPROVED.getCode());

        try (MockedStatic<AuthContext> ctx = mockStatic(AuthContext.class)) {
            ctx.when(AuthContext::get).thenReturn(studentUser);
            when(enrollmentMapper.selectById(100L)).thenReturn(enrollment);

            // Mock getOwnEnrollment inner chain (called by drop after the drop)
            EnrollmentMapper.EnrollmentResponseRow rrow = new EnrollmentMapper.EnrollmentResponseRow();
            rrow.setId(100L);
            when(enrollmentMapper.findByCourseId(10L, 20L, null)).thenReturn(java.util.List.of(rrow));

            enrollmentService.drop(100L);

            verify(enrollmentMapper).drop(100L);
            verify(enrollmentMapper).decreaseClassEnrollment(20L);
            verify(enrollmentMapper).decreaseCourseEnrollment(10L);
        }
    }
}
