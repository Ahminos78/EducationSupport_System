package com.whut.enrollment.service;

import com.whut.enrollment.entity.AcademicWarning;
import com.whut.enrollment.mapper.AcademicWarningMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WarningServiceTest {

    @Mock private AcademicWarningMapper warningMapper;

    private WarningService warningService;

    @BeforeEach
    void setUp() {
        warningService = new WarningService(warningMapper);
    }

    @Test
    void evaluateAndCreateWarning_scoreBelow60_createsNewWarning() {
        when(warningMapper.countActiveByStudentAndCourse(2L, 10L, 2)).thenReturn(0);

        warningService.evaluateAndCreateWarning(2L, 10L, 100L, BigDecimal.valueOf(45));

        ArgumentCaptor<AcademicWarning> captor = ArgumentCaptor.forClass(AcademicWarning.class);
        verify(warningMapper).insert(captor.capture());
        AcademicWarning warning = captor.getValue();
        assertThat(warning.getStudentId()).isEqualTo(2L);
        assertThat(warning.getCourseId()).isEqualTo(10L);
        assertThat(warning.getWarningType()).isEqualTo(2);
        assertThat(warning.getCurrentScore()).isEqualByComparingTo(BigDecimal.valueOf(45));
        assertThat(warning.getStatus()).isZero();
    }

    @Test
    void evaluateAndCreateWarning_scoreBelow30_shouldBeSeverity3() {
        when(warningMapper.countActiveByStudentAndCourse(2L, 10L, 2)).thenReturn(0);

        warningService.evaluateAndCreateWarning(2L, 10L, 100L, BigDecimal.valueOf(25));

        ArgumentCaptor<AcademicWarning> captor = ArgumentCaptor.forClass(AcademicWarning.class);
        verify(warningMapper).insert(captor.capture());
        assertThat(captor.getValue().getSeverity()).isEqualTo(3);
    }

    @Test
    void evaluateAndCreateWarning_scoreAbove60_shouldCloseActiveWarnings() {
        AcademicWarning activeWarning = new AcademicWarning();
        activeWarning.setId(999L);
        activeWarning.setStatus(0);

        when(warningMapper.selectList(any())).thenReturn(List.of(activeWarning));

        warningService.evaluateAndCreateWarning(2L, 10L, 100L, BigDecimal.valueOf(75));

        verify(warningMapper, never()).insert(any(AcademicWarning.class));
        verify(warningMapper, times(1)).updateById(any(AcademicWarning.class));
    }

    @Test
    void evaluateAndCreateWarning_scoreNull_shouldDoNothing() {
        warningService.evaluateAndCreateWarning(2L, 10L, 100L, null);

        verify(warningMapper, never()).insert(any(AcademicWarning.class));
        verify(warningMapper, never()).updateById(any(AcademicWarning.class));
    }
}
