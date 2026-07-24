package com.whut.course.service;

import com.whut.common.auth.AuthContext;
import com.whut.common.auth.AuthUser;
import com.whut.common.exception.BusinessException;
import com.whut.course.mapper.ScheduleMapper;
import com.whut.course.vo.ScheduleResponse;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

    public List<ScheduleResponse> getTodaySchedule() {
        AuthUser user = currentUser();
        // Get max week for this student's enrolled courses
        Integer maxWeek = scheduleMapper.findMaxWeek(user.getId());
        if (maxWeek == null || maxWeek < 1) {
            return List.of();
        }
        // Find schedule entries for the last/max week
        // This ensures we get entries within a valid week range
        List<ScheduleResponse> weekSchedule = scheduleMapper.findMySchedule(user.getId(), maxWeek);
        if (weekSchedule == null || weekSchedule.isEmpty()) {
            return List.of();
        }
        // Get today's day of week: Java DayOfWeek returns MONDAY=1..SUNDAY=7
        int today = LocalDate.now().getDayOfWeek().getValue();
        // Deduplicate by (classId + location + startPeriod + endPeriod)
        Set<String> seen = new HashSet<>();
        List<ScheduleResponse> result = new ArrayList<>();
        for (ScheduleResponse s : weekSchedule) {
            if (s.getDayOfWeek() == null || s.getDayOfWeek() != today) {
                continue;
            }
            String key = s.getClassId() + "|" +
                         (s.getLocation() != null ? s.getLocation() : "") + "|" +
                         s.getStartPeriod() + "|" + s.getEndPeriod();
            if (seen.add(key)) {
                result.add(s);
            }
        }
        return result;
    }

    private AuthUser currentUser() {
        AuthUser user = AuthContext.get();
        if (user == null) {
            throw BusinessException.unauthorized("请先登录");
        }
        return user;
    }
}
