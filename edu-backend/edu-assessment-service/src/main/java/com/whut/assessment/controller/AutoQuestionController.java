package com.whut.assessment.controller;

import com.whut.assessment.service.AutoQuestionService;
import com.whut.assessment.vo.QuestionResponse;
import com.whut.common.annotation.RequireRole;
import com.whut.common.enums.UserRole;
import com.whut.common.result.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/assessments/courses/{courseId}/auto-questions")
public class AutoQuestionController {

    private final AutoQuestionService autoQuestionService;

    public AutoQuestionController(AutoQuestionService autoQuestionService) {
        this.autoQuestionService = autoQuestionService;
    }

    @GetMapping
    @RequireRole({UserRole.TEACHER, UserRole.ADMIN})
    public Result<List<QuestionResponse>> autoGenerate(
            @PathVariable Long courseId,
            @RequestParam(defaultValue = "10") int count) {
        return Result.success(autoQuestionService.autoGenerate(courseId, count));
    }
}
