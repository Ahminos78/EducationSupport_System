package com.whut.assessment.controller;

import com.whut.assessment.dto.AssignmentCreateRequest;
import com.whut.assessment.dto.AssignmentStatusUpdateRequest;
import com.whut.assessment.dto.AssignmentUpdateRequest;
import com.whut.assessment.service.AssignmentService;
import com.whut.assessment.vo.AssignmentResponse;
import com.whut.common.annotation.RequireRole;
import com.whut.common.enums.UserRole;
import com.whut.common.result.Result;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/assessments/assignments")
public class AssignmentController {

    private final AssignmentService assignmentService;

    public AssignmentController(AssignmentService assignmentService) {
        this.assignmentService = assignmentService;
    }

    @GetMapping("/course/{courseId}")
    public Result<List<AssignmentResponse>> listByCourse(@PathVariable Long courseId) {
        return Result.success(assignmentService.listByCourse(courseId));
    }

    @GetMapping("/{id}")
    public Result<AssignmentResponse> detail(@PathVariable Long id) {
        return Result.success(assignmentService.detail(id));
    }

    @RequireRole({UserRole.TEACHER, UserRole.ADMIN})
    @PostMapping
    public Result<AssignmentResponse> create(@RequestBody AssignmentCreateRequest request) {
        return Result.success(assignmentService.create(request));
    }

    @RequireRole({UserRole.TEACHER, UserRole.ADMIN})
    @PutMapping("/{id}")
    public Result<AssignmentResponse> update(@PathVariable Long id,
                                             @RequestBody AssignmentUpdateRequest request) {
        return Result.success(assignmentService.update(id, request));
    }

    @RequireRole({UserRole.TEACHER, UserRole.ADMIN})
    @PutMapping("/{id}/status")
    public Result<AssignmentResponse> updateStatus(@PathVariable Long id,
                                                   @RequestBody AssignmentStatusUpdateRequest request) {
        return Result.success(assignmentService.updateStatus(id, request));
    }

    @RequireRole({UserRole.TEACHER, UserRole.ADMIN})
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        assignmentService.delete(id);
        return Result.success();
    }
}
