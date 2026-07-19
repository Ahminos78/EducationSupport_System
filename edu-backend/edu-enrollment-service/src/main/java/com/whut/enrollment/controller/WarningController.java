package com.whut.enrollment.controller;

import com.whut.common.annotation.RequireRole;
import com.whut.common.enums.UserRole;
import com.whut.common.result.Result;
import com.whut.enrollment.mapper.AcademicWarningMapper;
import com.whut.enrollment.service.WarningService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/warnings")
public class WarningController {

    private final WarningService warningService;

    public WarningController(WarningService warningService) {
        this.warningService = warningService;
    }

    @RequireRole(UserRole.STUDENT)
    @GetMapping("/my")
    public Result<List<AcademicWarningMapper.WarningRow>> myWarnings() {
        return Result.success(warningService.getMyWarnings());
    }

    @RequireRole(UserRole.STUDENT)
    @GetMapping("/my/active")
    public Result<List<AcademicWarningMapper.WarningRow>> myActiveWarnings() {
        return Result.success(warningService.getMyActiveWarnings());
    }

    @RequireRole(UserRole.STUDENT)
    @GetMapping("/my/count")
    public Result<Map<String, Integer>> myActiveWarningCount() {
        return Result.success(Map.of("count", warningService.getMyActiveWarningCount()));
    }
}
