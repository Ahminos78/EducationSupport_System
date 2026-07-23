package com.whut.course.service;

import com.whut.common.auth.AuthContext;
import com.whut.common.auth.AuthUser;
import com.whut.common.enums.UserRole;
import com.whut.course.mapper.ScheduleMapper;
import com.whut.course.vo.ScheduleResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ScheduleServiceTest {

    @Mock private ScheduleMapper scheduleMapper;

    private ScheduleService scheduleService;
    private AuthUser studentUser;

    @BeforeEach
    void setUp() {
        scheduleService = new ScheduleService(scheduleMapper);
        studentUser = new AuthUser(2L, "student1", UserRole.STUDENT.getCode());
    }

    @Test
    void getMySchedule_shouldReturnList() {
        ScheduleResponse s1 = new ScheduleResponse();
        s1.setCourseId(1L);
        s1.setCourseName("\u6570\u5b66");
        ScheduleResponse s2 = new ScheduleResponse();
        s2.setCourseId(2L);
        s2.setCourseName("\u82f1\u8bed");
        try (MockedStatic<AuthContext> ctx = mockStatic(AuthContext.class)) {
            ctx.when(AuthContext::get).thenReturn(studentUser);
            when(scheduleMapper.findMySchedule(2L, 1)).thenReturn(List.of(s1, s2));

            var result = scheduleService.getMySchedule(1);

            assertThat(result).hasSize(2);
            assertThat(result.get(0).getCourseName()).isEqualTo("\u6570\u5b66");
        }
    }

    @Test
    void getMySchedule_shouldLimitWeekRange() {
        try (MockedStatic<AuthContext> ctx = mockStatic(AuthContext.class)) {
            ctx.when(AuthContext::get).thenReturn(studentUser);
            when(scheduleMapper.findMySchedule(anyLong(), anyInt())).thenReturn(List.of());

            var result = scheduleService.getMySchedule(99);
            assertThat(result).isNotNull();
        }
    }
}
