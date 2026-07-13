package com.whut.assessment.controller;

import com.whut.assessment.service.ExamService;
import com.whut.assessment.vo.ExamResponse;
import com.whut.common.result.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
}
