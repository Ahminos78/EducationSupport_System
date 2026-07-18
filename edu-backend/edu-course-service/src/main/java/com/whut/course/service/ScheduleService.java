package com.whut.course.service;

import com.whut.common.auth.AuthContext;
import com.whut.common.auth.AuthUser;
import com.whut.common.exception.BusinessException;
import com.whut.course.mapper.ScheduleMapper;
import com.whut.course.vo.ScheduleResponse;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ScheduleService {

    private final ScheduleMapper scheduleMapper;

    public ScheduleService(ScheduleMapper scheduleMapper) {
        this.scheduleMapper = scheduleMapper;
    }

    public List<ScheduleResponse> getMySchedule(int week) {
        AuthUser user = currentUser();
        int safeWeek = Math.max(1, Math.min(week, 30));
        return scheduleMapper.findMySchedule(user.getId(), safeWeek);
    }

    public List<ScheduleResponse> getMyScheduleAllWeeks() {
        AuthUser user = currentUser();
        return scheduleMapper.findMyScheduleAllWeeks(user.getId());
    }

    public int getMaxWeek() {
        AuthUser user = currentUser();
        Integer max = scheduleMapper.findMaxWeek(user.getId());
        return max != null ? max : 16;
    }

    private AuthUser currentUser() {
        AuthUser user = AuthContext.get();
        if (user == null) {
            throw BusinessException.unauthorized("请先登录");
        }
        return user;
    }
}
