package com.whut.assessment.controller;

import com.whut.assessment.dto.ExamCreateRequest;
import com.whut.assessment.dto.ExamStatusUpdateRequest;
import com.whut.assessment.dto.ExamWithQuestionsRequest;
import com.whut.assessment.service.ExamService;
import com.whut.assessment.vo.ExamResponse;
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
@RequestMapping("/api/assessments/exams")
public class ExamController {

    private final ExamService examService;

    public ExamController(ExamService examService) {
        this.examService = examService;
    }

    @GetMapping("/course/{courseId}")
    public Result<List<ExamResponse>> listByCourse(@PathVariable Long courseId) {
        return Result.success(examService.listByCourse(courseId));
    }

    @GetMapping("/{id}")
    public Result<ExamResponse> detail(@PathVariable Long id) {
        return Result.success(examService.detail(id));
    }

    @RequireRole({UserRole.TEACHER, UserRole.ADMIN})
    @PostMapping
    public Result<ExamResponse> create(@RequestBody ExamCreateRequest request) {
        return Result.success(examService.create(request));
    }

    @RequireRole({UserRole.TEACHER, UserRole.ADMIN})
    @PostMapping("/with-questions")
    public Result<ExamResponse> createWithQuestions(@RequestBody ExamWithQuestionsRequest request) {
        return Result.success(examService.createWithQuestions(request));
    }

    @RequireRole({UserRole.TEACHER, UserRole.ADMIN})
    @PutMapping("/{id}/with-questions")
    public Result<ExamResponse> updateWithQuestions(@PathVariable Long id,
                                                     @RequestBody ExamWithQuestionsRequest request) {
        return Result.success(examService.updateWithQuestions(id, request));
    }

    @RequireRole({UserRole.TEACHER, UserRole.ADMIN})
    @PutMapping("/{id}/status")
    public Result<ExamResponse> updateStatus(@PathVariable Long id,
                                              @RequestBody ExamStatusUpdateRequest request) {
        return Result.success(examService.updateStatus(id, request));
    }

    @RequireRole({UserRole.TEACHER, UserRole.ADMIN})
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        examService.delete(id);
        return Result.success();
    }
}
