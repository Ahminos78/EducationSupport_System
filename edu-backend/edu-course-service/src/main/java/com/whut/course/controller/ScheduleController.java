package com.whut.course.controller;

import com.whut.common.enums.UserRole;
import com.whut.common.annotation.RequireRole;
import com.whut.common.result.Result;
import com.whut.course.service.ScheduleService;
import com.whut.course.vo.ScheduleResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/courses/schedule")
@RequireRole({UserRole.STUDENT})
public class ScheduleController {

    private final ScheduleService scheduleService;

    public ScheduleController(ScheduleService scheduleService) {
        this.scheduleService = scheduleService;
    }

    @GetMapping("/my")
    public Result<List<ScheduleResponse>> mySchedule(@RequestParam(defaultValue = "1") int week) {
        return Result.success(scheduleService.getMySchedule(week));
    }

    @GetMapping("/my/all")
    public Result<List<ScheduleResponse>> myScheduleAllWeeks() {
        return Result.success(scheduleService.getMyScheduleAllWeeks());
    }

    @GetMapping("/max-week")
    public Result<Map<String, Integer>> maxWeek() {
        return Result.success(Map.of("maxWeek", scheduleService.getMaxWeek()));
    }
}
