package com.whut.assessment.controller;

import com.whut.assessment.dto.ExamAttemptGradeRequest;
import com.whut.assessment.dto.ExamAttemptSubmitRequest;
import com.whut.assessment.service.ExamAttemptService;
import com.whut.assessment.vo.ExamAttemptResponse;
import com.whut.common.annotation.RequireRole;
import com.whut.common.enums.UserRole;
import com.whut.common.result.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/assessments")
public class ExamAttemptController {

    private final ExamAttemptService examAttemptService;

    public ExamAttemptController(ExamAttemptService examAttemptService) {
        this.examAttemptService = examAttemptService;
    }

    @RequireRole(UserRole.STUDENT)
    @PostMapping("/exams/{examId}/attempts/start")
    public Result<ExamAttemptResponse> start(@PathVariable Long examId) {
        return Result.success(examAttemptService.start(examId));
    }

    @RequireRole(UserRole.STUDENT)
    @PutMapping("/exams/{examId}/attempts/submit")
    public Result<ExamAttemptResponse> submit(@PathVariable Long examId,
                                              @RequestBody ExamAttemptSubmitRequest request) {
        return Result.success(examAttemptService.submit(examId, request));
    }

    @RequireRole(UserRole.STUDENT)
    @GetMapping("/exam-attempts/my")
    public Result<List<ExamAttemptResponse>> myAttempts() {
        return Result.success(examAttemptService.myAttempts());
    }

    @RequireRole({UserRole.TEACHER, UserRole.ADMIN})
    @GetMapping("/exams/{examId}/attempts")
    public Result<List<ExamAttemptResponse>> examAttempts(@PathVariable Long examId) {
        return Result.success(examAttemptService.examAttempts(examId));
    }

    @GetMapping("/exam-attempts/{id}")
    public Result<ExamAttemptResponse> detail(@PathVariable Long id) {
        return Result.success(examAttemptService.detail(id));
    }

    @RequireRole({UserRole.TEACHER, UserRole.ADMIN})
    @PutMapping("/exam-attempts/{id}/grade")
    public Result<ExamAttemptResponse> grade(@PathVariable Long id,
                                             @RequestBody ExamAttemptGradeRequest request) {
        return Result.success(examAttemptService.grade(id, request));
    }
}
