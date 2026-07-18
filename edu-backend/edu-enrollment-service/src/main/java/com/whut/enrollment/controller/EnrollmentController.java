package com.whut.enrollment.controller;

import com.whut.common.annotation.RequireRole;
import com.whut.common.enums.UserRole;
import com.whut.common.result.Result;
import com.whut.enrollment.dto.EnrollmentCreateRequest;
import com.whut.enrollment.dto.EnrollmentReviewRequest;
import com.whut.enrollment.service.EnrollmentService;
import com.whut.enrollment.vo.CourseStudyScoreResponse;
import com.whut.enrollment.vo.EnrollmentResponse;
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
@RequestMapping("/api/enrollments")
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    public EnrollmentController(EnrollmentService enrollmentService) {
        this.enrollmentService = enrollmentService;
    }

    @RequireRole(UserRole.STUDENT)
    @PostMapping
    public Result<EnrollmentResponse> apply(@RequestBody EnrollmentCreateRequest request) {
        return Result.success(enrollmentService.apply(request));
    }

    @RequireRole(UserRole.STUDENT)
    @GetMapping("/my")
    public Result<List<EnrollmentResponse>> myEnrollments() {
        return Result.success(enrollmentService.myEnrollments());
    }

    @RequireRole({UserRole.TEACHER, UserRole.ADMIN})
    @GetMapping("/course/{courseId}")
    public Result<List<EnrollmentResponse>> courseEnrollments(@PathVariable Long courseId,
                                                              @RequestParam(required = false) Integer status) {
        return Result.success(enrollmentService.courseEnrollments(courseId, status));
    }

    @RequireRole({UserRole.TEACHER, UserRole.ADMIN})
    @PutMapping("/{id}/approve")
    public Result<EnrollmentResponse> approve(@PathVariable Long id,
                                              @RequestBody(required = false) EnrollmentReviewRequest request) {
        return Result.success(enrollmentService.approve(id, request));
    }

    @RequireRole({UserRole.TEACHER, UserRole.ADMIN})
    @PutMapping("/{id}/reject")
    public Result<EnrollmentResponse> reject(@PathVariable Long id,
                                             @RequestBody(required = false) EnrollmentReviewRequest request) {
        return Result.success(enrollmentService.reject(id, request));
    }

    @RequireRole({UserRole.TEACHER, UserRole.ADMIN})
    @PutMapping("/{id}/remove")
    public Result<EnrollmentResponse> remove(@PathVariable Long id) {
        return Result.success(enrollmentService.removeByTeacher(id));
    }

    @RequireRole(UserRole.STUDENT)
    @GetMapping("/courses/{courseId}/study-score")
    public Result<CourseStudyScoreResponse> studyScore(@PathVariable Long courseId) {
        return Result.success(enrollmentService.getStudyScore(courseId));
    }

    @RequireRole(UserRole.STUDENT)
    @PutMapping("/{id}/drop")
    public Result<EnrollmentResponse> drop(@PathVariable Long id) {
        return Result.success(enrollmentService.drop(id));
    }
}
