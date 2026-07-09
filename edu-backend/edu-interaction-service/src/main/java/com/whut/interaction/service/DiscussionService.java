package com.whut.interaction.service;

import com.whut.common.auth.AuthContext;
import com.whut.common.auth.AuthUser;
import com.whut.common.enums.UserRole;
import com.whut.common.exception.BusinessException;
import com.whut.interaction.dto.DiscussionStatusUpdateRequest;
import com.whut.interaction.dto.DiscussionUpdateRequest;
import com.whut.interaction.dto.ReplyCreateRequest;
import com.whut.interaction.dto.TopicCreateRequest;
import com.whut.interaction.entity.CourseSnapshot;
import com.whut.interaction.entity.Discussion;
import com.whut.interaction.mapper.DiscussionMapper;
import com.whut.interaction.vo.DiscussionResponse;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class DiscussionService {

    private static final int COURSE_ONLINE = 1;
    private static final int STATUS_HIDDEN = 0;
    private static final int STATUS_NORMAL = 1;

    private final DiscussionMapper discussionMapper;

    public DiscussionService(DiscussionMapper discussionMapper) {
        this.discussionMapper = discussionMapper;
    }

    public List<DiscussionResponse> topics(Long courseId, int page, int size) {
        CourseSnapshot course = requireCourse(courseId);
        AuthUser currentUser = currentUser();
        boolean includeHidden = canManageCourse(currentUser, course);
        int safePage = Math.max(page, 1);
        int safeSize = Math.min(Math.max(size, 1), 100);
        return discussionMapper.findTopics(courseId, (safePage - 1) * safeSize, safeSize, includeHidden).stream()
                .map(this::toResponse)
                .toList();
    }

    public DiscussionResponse detail(Long topicId) {
        Discussion discussion = requireDiscussion(topicId);
        if (discussion.getParentId() != null) {
            throw BusinessException.badRequest("该内容不是主题帖");
        }
        ensureVisibleOrManageable(discussion);
        return toResponse(requireResponse(topicId));
    }

    public List<DiscussionResponse> replies(Long topicId, int page, int size) {
        Discussion topic = requireDiscussion(topicId);
        if (topic.getParentId() != null) {
            throw BusinessException.badRequest("该内容不是主题帖");
        }
        CourseSnapshot course = requireCourse(topic.getCourseId());
        AuthUser currentUser = currentUser();
        boolean includeHidden = canManageCourse(currentUser, course);
        int safePage = Math.max(page, 1);
        int safeSize = Math.min(Math.max(size, 1), 100);
        return discussionMapper.findReplies(topicId, (safePage - 1) * safeSize, safeSize, includeHidden).stream()
                .map(this::toResponse)
                .toList();
    }

    public DiscussionResponse createTopic(TopicCreateRequest request) {
        AuthUser currentUser = currentUser();
        if (request.getCourseId() == null) {
            throw BusinessException.badRequest("课程ID不能为空");
        }
        requireAvailableCourse(request.getCourseId());
        requireText(request.getTitle(), "标题不能为空");
        requireText(request.getContent(), "内容不能为空");
        Discussion discussion = new Discussion();
        discussion.setCourseId(request.getCourseId());
        discussion.setAuthorId(currentUser.getId());
        discussion.setTitle(request.getTitle());
        discussion.setContent(request.getContent());
        discussion.setStatus(STATUS_NORMAL);
        discussionMapper.insert(discussion);
        return toResponse(requireResponse(discussion.getId()));
    }

    public DiscussionResponse createReply(Long topicId, ReplyCreateRequest request) {
        AuthUser currentUser = currentUser();
        Discussion topic = requireDiscussion(topicId);
        if (topic.getParentId() != null) {
            throw BusinessException.badRequest("只能回复主题帖");
        }
        if (topic.getStatus() != STATUS_NORMAL) {
            throw BusinessException.badRequest("主题帖已隐藏，不能回复");
        }
        requireAvailableCourse(topic.getCourseId());
        requireText(request.getContent(), "内容不能为空");
        Discussion reply = new Discussion();
        reply.setCourseId(topic.getCourseId());
        reply.setParentId(topicId);
        reply.setAuthorId(currentUser.getId());
        reply.setContent(request.getContent());
        reply.setStatus(STATUS_NORMAL);
        discussionMapper.insert(reply);
        return toResponse(requireResponse(reply.getId()));
    }

    public DiscussionResponse update(Long id, DiscussionUpdateRequest request) {
        Discussion discussion = requireDiscussion(id);
        AuthUser currentUser = currentUser();
        if (!canEdit(currentUser, discussion)) {
            throw BusinessException.forbidden("无权编辑该讨论内容");
        }
        if (discussion.getParentId() == null && StringUtils.hasText(request.getTitle())) {
            discussion.setTitle(request.getTitle());
        }
        if (StringUtils.hasText(request.getContent())) {
            discussion.setContent(request.getContent());
        }
        discussionMapper.update(discussion);
        return toResponse(requireResponse(id));
    }

    public DiscussionResponse updateStatus(Long id, DiscussionStatusUpdateRequest request) {
        Discussion discussion = requireDiscussion(id);
        AuthUser currentUser = currentUser();
        CourseSnapshot course = requireCourse(discussion.getCourseId());
        if (!canManageCourse(currentUser, course)) {
            throw BusinessException.forbidden("无权修改该讨论状态");
        }
        assertValidStatus(request.getStatus());
        discussionMapper.updateStatus(id, request.getStatus());
        return toResponse(requireResponse(id));
    }

    public void delete(Long id) {
        Discussion discussion = requireDiscussion(id);
        AuthUser currentUser = currentUser();
        if (!canDelete(currentUser, discussion)) {
            throw BusinessException.forbidden("无权删除该讨论内容");
        }
        discussionMapper.deleteById(id);
    }

    private void ensureVisibleOrManageable(Discussion discussion) {
        if (discussion.getStatus() == STATUS_NORMAL) {
            return;
        }
        CourseSnapshot course = requireCourse(discussion.getCourseId());
        if (!canManageCourse(currentUser(), course)) {
            throw BusinessException.forbidden("无权查看该讨论内容");
        }
    }

    private boolean canEdit(AuthUser currentUser, Discussion discussion) {
        if (currentUser.getRole() == UserRole.ADMIN.getCode()) {
            return true;
        }
        if (discussion.getAuthorId().equals(currentUser.getId())) {
            return true;
        }
        return canManageCourse(currentUser, requireCourse(discussion.getCourseId()));
    }

    private boolean canDelete(AuthUser currentUser, Discussion discussion) {
        return canEdit(currentUser, discussion);
    }

    private boolean canManageCourse(AuthUser currentUser, CourseSnapshot course) {
        return currentUser.getRole() == UserRole.ADMIN.getCode()
                || (currentUser.getRole() == UserRole.TEACHER.getCode()
                && course.getTeacherId().equals(currentUser.getId()));
    }

    private CourseSnapshot requireAvailableCourse(Long courseId) {
        CourseSnapshot course = requireCourse(courseId);
        if (course.getStatus() != COURSE_ONLINE) {
            throw BusinessException.badRequest("课程未开放讨论");
        }
        return course;
    }

    private CourseSnapshot requireCourse(Long courseId) {
        CourseSnapshot course = discussionMapper.findCourseById(courseId);
        if (course == null || (course.getDeleted() != null && course.getDeleted() == 1)) {
            throw BusinessException.notFound("课程不存在");
        }
        return course;
    }

    private Discussion requireDiscussion(Long id) {
        Discussion discussion = discussionMapper.selectById(id);
        if (discussion == null) {
            throw BusinessException.notFound("讨论内容不存在");
        }
        return discussion;
    }

    private DiscussionMapper.DiscussionResponseRow requireResponse(Long id) {
        DiscussionMapper.DiscussionResponseRow response = discussionMapper.findResponseById(id);
        if (response == null) {
            throw BusinessException.notFound("讨论内容不存在");
        }
        return response;
    }

    private void assertValidStatus(Integer status) {
        if (status == null || (status != STATUS_HIDDEN && status != STATUS_NORMAL)) {
            throw BusinessException.badRequest("讨论状态不合法");
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

    private DiscussionResponse toResponse(DiscussionMapper.DiscussionResponseRow row) {
        DiscussionResponse response = new DiscussionResponse();
        response.setId(row.getId());
        response.setCourseId(row.getCourseId());
        response.setCourseName(row.getCourseName());
        response.setParentId(row.getParentId());
        response.setAuthorId(row.getAuthorId());
        response.setTitle(row.getTitle());
        response.setContent(row.getContent());
        response.setStatus(row.getStatus());
        response.setCreatedAt(row.getCreatedAt());
        response.setUpdatedAt(row.getUpdatedAt());
        return response;
    }
}
