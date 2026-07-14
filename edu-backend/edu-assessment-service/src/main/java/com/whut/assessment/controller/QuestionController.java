package com.whut.assessment.controller;

import com.whut.assessment.dto.QuestionCreateRequest;
import com.whut.assessment.dto.QuestionUpdateRequest;
import com.whut.assessment.service.QuestionService;
import com.whut.assessment.vo.QuestionResponse;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/assessments/exams/{examId}/questions")
public class QuestionController {

    private final QuestionService questionService;

    public QuestionController(QuestionService questionService) {
        this.questionService = questionService;
    }

    @GetMapping
    public Result<List<QuestionResponse>> list(@PathVariable Long examId,
                                                @RequestParam(defaultValue = "false") boolean withAnswers) {
        return Result.success(questionService.listByExam(examId, withAnswers));
    }

    @RequireRole({UserRole.TEACHER, UserRole.ADMIN})
    @PostMapping
    public Result<QuestionResponse> create(@PathVariable Long examId,
                                            @RequestBody QuestionCreateRequest request) {
        return Result.success(questionService.create(examId, request.getType(),
                request.getTitle(), request.getOptions(), request.getAnswer(),
                request.getScore(), request.getSortOrder()));
    }

    @RequireRole({UserRole.TEACHER, UserRole.ADMIN})
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long examId, @PathVariable Long id,
                                @RequestBody QuestionUpdateRequest request) {
        questionService.update(id, request.getType(), request.getTitle(),
                request.getOptions(), request.getAnswer(),
                request.getScore(), request.getSortOrder());
        return Result.success();
    }

    @RequireRole({UserRole.TEACHER, UserRole.ADMIN})
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long examId, @PathVariable Long id) {
        questionService.delete(id);
        return Result.success();
    }
}
