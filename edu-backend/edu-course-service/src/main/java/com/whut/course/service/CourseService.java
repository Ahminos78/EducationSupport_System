package com.whut.course.service;

import com.whut.common.auth.AuthContext;
import com.whut.common.auth.AuthUser;
import com.whut.common.enums.UserRole;
import com.whut.common.exception.BusinessException;
import com.whut.course.dto.CourseCreateRequest;
import com.whut.course.dto.CourseStatusUpdateRequest;
import com.whut.course.dto.CourseUpdateRequest;
import com.whut.course.entity.Course;
import com.whut.course.mapper.CourseMapper;
import com.whut.course.vo.CourseResponse;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class CourseService {

    private static final int STATUS_OFFLINE = 0;
    private static final int STATUS_ONLINE = 1;

    private final CourseMapper courseMapper;

    public CourseService(CourseMapper courseMapper) {
        this.courseMapper = courseMapper;
    }

    public List<CourseResponse> page(int page, int size, Integer status, Long teacherId) {
        AuthUser currentUser = currentUser();
        int safePage = Math.max(page, 1);
        int safeSize = Math.min(Math.max(size, 1), 100);
        Integer queryStatus = status;
        Long queryTeacherId = teacherId;
        if (currentUser.getRole() == UserRole.STUDENT.getCode()) {
            queryStatus = STATUS_ONLINE;
            queryTeacherId = null;
        } else if (currentUser.getRole() == UserRole.TEACHER.getCode()) {
            queryTeacherId = currentUser.getId();
        }
        return courseMapper.findPage((safePage - 1) * safeSize, safeSize, queryStatus, queryTeacherId).stream()
                .map(this::toResponse)
                .toList();
    }

    public CourseResponse detail(Long id) {
        Course course = requireCourse(id);
        AuthUser currentUser = currentUser();
        if (course.getStatus() != STATUS_ONLINE && !canManageCourse(currentUser, course)) {
            throw BusinessException.forbidden("无权查看该课程");
        }
        return toResponse(course);
    }

    public CourseResponse create(CourseCreateRequest request) {
        AuthUser currentUser = currentUser();
        assertTeacherOrAdmin(currentUser);
        requireText(request.getName(), "课程名称不能为空");
        int maxStudents = request.getMaxStudents() == null ? 100 : request.getMaxStudents();
        if (maxStudents <= 0) {
            throw BusinessException.badRequest("最大选课人数必须大于0");
        }
        int status = request.getStatus() == null ? STATUS_ONLINE : request.getStatus();
        assertValidStatus(status);
        Course course = new Course();
        course.setTeacherId(currentUser.getId());
        course.setName(request.getName());
        course.setDescription(request.getDescription());
        course.setCoverUrl(request.getCoverUrl());
        course.setMaxStudents(maxStudents);
        course.setStatus(status);
        courseMapper.insert(course);
        return detail(course.getId());
    }

    public CourseResponse update(Long id, CourseUpdateRequest request) {
        Course course = requireCourse(id);
        AuthUser currentUser = currentUser();
        if (!canManageCourse(currentUser, course)) {
            throw BusinessException.forbidden("无权修改该课程");
        }
        if (StringUtils.hasText(request.getName())) {
            course.setName(request.getName());
        }
        if (request.getDescription() != null) {
            course.setDescription(request.getDescription());
        }
        if (request.getCoverUrl() != null) {
            course.setCoverUrl(request.getCoverUrl());
        }
        if (request.getMaxStudents() != null) {
            if (request.getMaxStudents() <= 0) {
                throw BusinessException.badRequest("最大选课人数必须大于0");
            }
            if (course.getEnrolledCount() != null && request.getMaxStudents() < course.getEnrolledCount()) {
                throw BusinessException.badRequest("最大选课人数不能小于当前已选人数");
            }
            course.setMaxStudents(request.getMaxStudents());
        }
        courseMapper.update(course);
        return detail(id);
    }

    public CourseResponse updateStatus(Long id, CourseStatusUpdateRequest request) {
        Course course = requireCourse(id);
        AuthUser currentUser = currentUser();
        if (!canManageCourse(currentUser, course)) {
            throw BusinessException.forbidden("无权修改该课程");
        }
        assertValidStatus(request.getStatus());
        courseMapper.updateStatus(id, request.getStatus());
        return detail(id);
    }

    public void delete(Long id) {
        Course course = requireCourse(id);
        AuthUser currentUser = currentUser();
        if (!canManageCourse(currentUser, course)) {
            throw BusinessException.forbidden("无权删除该课程");
        }
        courseMapper.deleteById(id);
    }

    private Course requireCourse(Long id) {
        Course course = courseMapper.selectById(id);
        if (course == null) {
            throw BusinessException.notFound("课程不存在");
        }
        return course;
    }

    private boolean canManageCourse(AuthUser currentUser, Course course) {
        return currentUser.getRole() == UserRole.ADMIN.getCode()
                || (currentUser.getRole() == UserRole.TEACHER.getCode()
                && course.getTeacherId().equals(currentUser.getId()));
    }

    private void assertTeacherOrAdmin(AuthUser currentUser) {
        if (currentUser.getRole() != UserRole.TEACHER.getCode()
                && currentUser.getRole() != UserRole.ADMIN.getCode()) {
            throw BusinessException.forbidden("只有教师或管理员可以管理课程");
        }
    }

    private void assertValidStatus(Integer status) {
        if (status == null || (status != STATUS_OFFLINE && status != STATUS_ONLINE)) {
            throw BusinessException.badRequest("课程状态不合法");
        }
    }

    private AuthUser currentUser() {
        AuthUser currentUser = AuthContext.get();
        if (currentUser == null) {
            throw BusinessException.unauthorized("请先登录");
        }
        return currentUser;
    }

    private void requireText(String value, String message) {
        if (!StringUtils.hasText(value)) {
            throw BusinessException.badRequest(message);
        }
    }

    private CourseResponse toResponse(Course course) {
        CourseResponse response = new CourseResponse();
        response.setId(course.getId());
        response.setCode(course.getCode());
        response.setTeacherId(course.getTeacherId());
        response.setName(course.getName());
        response.setDescription(course.getDescription());
        response.setCoverUrl(course.getCoverUrl());
        response.setMaxStudents(course.getMaxStudents());
        response.setEnrolledCount(course.getEnrolledCount());
        response.setCredit(course.getCredit());
        response.setDept(course.getDept());
        response.setCategory(course.getCategory());
        response.setTags(course.getTags());
        response.setClassCount(course.getClassCount());
        response.setStatus(course.getStatus());
        response.setCreatedAt(course.getCreatedAt());
        return response;
    }

    public long countTotal() {
        return courseMapper.selectCount(null);
    }
}
