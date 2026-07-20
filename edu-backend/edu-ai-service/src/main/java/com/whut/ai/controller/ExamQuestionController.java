package com.whut.ai.controller;

import com.whut.common.result.Result;
import com.whut.ai.dto.ExamGenerateRequest;
import com.whut.ai.service.ExamQuestionService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ai/exam")
public class ExamQuestionController {

    private final ExamQuestionService examQuestionService;

    public ExamQuestionController(ExamQuestionService examQuestionService) {
        this.examQuestionService = examQuestionService;
    }

    @PostMapping("/generate-questions")
    public Result<List<Map<String, Object>>> generateQuestions(@RequestBody ExamGenerateRequest request) {
        return Result.success(examQuestionService.generate(request));
    }
}
