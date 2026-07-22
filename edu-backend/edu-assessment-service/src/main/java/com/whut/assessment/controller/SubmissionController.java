package com.whut.assessment.controller;

import com.whut.assessment.dto.SubmissionCreateRequest;
import com.whut.assessment.dto.SubmissionGradeRequest;
import com.whut.assessment.dto.AiAutoCommentRequest;
import com.whut.assessment.service.SubmissionService;
import com.whut.assessment.vo.SubmissionResponse;
import com.whut.assessment.client.AiExamClient;
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
public class SubmissionController {

    private final SubmissionService submissionService;
    private final AiExamClient aiExamClient;

    public SubmissionController(SubmissionService submissionService, AiExamClient aiExamClient) {
        this.submissionService = submissionService;
        this.aiExamClient = aiExamClient;
    }

    @RequireRole(UserRole.STUDENT)
    @PostMapping("/assignments/{assignmentId}/submissions")
    public Result<SubmissionResponse> submit(@PathVariable Long assignmentId,
                                             @RequestBody SubmissionCreateRequest request) {
        return Result.success(submissionService.submit(assignmentId, request));
    }

    @RequireRole(UserRole.STUDENT)
    @GetMapping("/submissions/my")
    public Result<List<SubmissionResponse>> mySubmissions() {
        return Result.success(submissionService.mySubmissions());
    }

    @GetMapping("/assignments/{assignmentId}/submissions")
    public Result<List<SubmissionResponse>> assignmentSubmissions(@PathVariable Long assignmentId) {
        return Result.success(submissionService.assignmentSubmissions(assignmentId));
    }

    @GetMapping("/submissions/{id}")
    public Result<SubmissionResponse> detail(@PathVariable Long id) {
        return Result.success(submissionService.detail(id));
    }

    @PutMapping("/submissions/{id}/grade")
    public Result<SubmissionResponse> grade(@PathVariable Long id,
                                            @RequestBody SubmissionGradeRequest request) {
        return Result.success(submissionService.grade(id, request));
    }

    @PostMapping("/submissions/{id}/ai-comment")
    public Result<String> generateAiComment(@PathVariable Long id) {
        return Result.success(submissionService.generateAiComment(id));
    }
}
