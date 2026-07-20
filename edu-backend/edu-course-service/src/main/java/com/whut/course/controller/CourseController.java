package com.whut.course.controller;

import com.whut.common.annotation.RequireRole;
import com.whut.common.enums.UserRole;
import com.whut.common.result.Result;
import com.whut.course.dto.CourseCreateRequest;
import com.whut.course.dto.CourseStatusUpdateRequest;
import com.whut.course.dto.CourseUpdateRequest;
import com.whut.course.service.CourseClassService;
import com.whut.course.service.CourseService;
import com.whut.course.vo.CoursePageResponse;
import com.whut.course.vo.CourseResponse;
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
@RequestMapping("/api/courses")
public class CourseController {

    private final CourseService courseService;
    private final CourseClassService courseClassService;

    public CourseController(CourseService courseService, CourseClassService courseClassService) {
        this.courseService = courseService;
        this.courseClassService = courseClassService;
    }

    @GetMapping("/page")
    public Result<CoursePageResponse> page(@RequestParam(defaultValue = "1") int page,
                                           @RequestParam(defaultValue = "10") int size,
                                           @RequestParam(required = false) Integer status,
                                           @RequestParam(required = false) Long teacherId) {
        return Result.success(courseService.page(page, size, status, teacherId));
    }

    @GetMapping("/{id}")
    public Result<CourseResponse> detail(@PathVariable Long id) {
        return Result.success(courseService.detail(id));
    }

    @RequireRole({UserRole.TEACHER, UserRole.ADMIN})
    @PostMapping
    public Result<CourseResponse> create(@RequestBody CourseCreateRequest request) {
        return Result.success(courseService.create(request));
    }

    @RequireRole({UserRole.TEACHER, UserRole.ADMIN})
    @PutMapping("/{id}")
    public Result<CourseResponse> update(@PathVariable Long id, @RequestBody CourseUpdateRequest request) {
        return Result.success(courseService.update(id, request));
    }

    @RequireRole({UserRole.TEACHER, UserRole.ADMIN})
    @PutMapping("/{id}/status")
    public Result<CourseResponse> updateStatus(@PathVariable Long id,
                                               @RequestBody CourseStatusUpdateRequest request) {
        return Result.success(courseService.updateStatus(id, request));
    }

    @RequireRole({UserRole.TEACHER, UserRole.ADMIN})
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        courseService.delete(id);
        return Result.success();
    }

    @GetMapping("/count")
    public Result<Long> count() {
        return Result.success(courseService.countTotal());
    }

    @GetMapping("/{courseId}/classes")
    public Result<List<CourseClassService.CourseClassWithSchedule>> courseClasses(@PathVariable Long courseId) {
        return Result.success(courseClassService.getClassesByCourse(courseId));
    }

    @GetMapping("/my-taught")
    public Result<List<CourseResponse>> myTaughtCourses() {
        return Result.success(courseService.myTaughtCourses());
    }
}
