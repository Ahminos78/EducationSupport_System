package com.whut.assessment.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.whut.assessment.dto.SubmissionCreateRequest;
import com.whut.assessment.dto.SubmissionGradeRequest;
import com.whut.assessment.entity.Assignment;
import com.whut.assessment.entity.Submission;
import com.whut.assessment.mapper.SubmissionMapper;
import com.whut.assessment.vo.SubmissionResponse;
import com.whut.common.auth.AuthContext;
import com.whut.common.auth.AuthUser;
import com.whut.common.enums.UserRole;
import com.whut.common.exception.BusinessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class SubmissionService {

    private final SubmissionMapper submissionMapper;
    private final AssignmentService assignmentService;

    public SubmissionService(SubmissionMapper submissionMapper, AssignmentService assignmentService) {
        this.submissionMapper = submissionMapper;
        this.assignmentService = assignmentService;
    }

    @Transactional
    public SubmissionResponse submit(Long assignmentId, SubmissionCreateRequest request) {
        AuthUser currentUser = currentUser();
        if (currentUser.getRole() != UserRole.STUDENT.getCode()) {
            throw BusinessException.forbidden("只有学生可以提交作业");
        }
        Assignment assignment = assignmentService.requireAssignment(assignmentId);
        assignmentService.assertApprovedStudent(assignment.getCourseId(), currentUser);
        if (!assignmentService.canSubmit(assignment)) {
            throw BusinessException.badRequest("作业未发布或已截止");
        }
        requireText(request.getContent(), "提交内容不能为空");
        Submission existing = findByAssignmentAndStudent(assignmentId, currentUser.getId());
        if (existing == null) {
            Submission submission = new Submission();
            submission.setAssignmentId(assignmentId);
            submission.setStudentId(currentUser.getId());
            submission.setContent(request.getContent());
            submission.setAttachmentUrl(request.getAttachmentUrl());
            submissionMapper.insert(submission);
            return detail(submission.getId());
        }
        existing.setContent(request.getContent());
        existing.setAttachmentUrl(request.getAttachmentUrl());
        submissionMapper.updateContent(existing);
        return detail(existing.getId());
    }

    public List<SubmissionResponse> mySubmissions() {
        AuthUser currentUser = currentUser();
        if (currentUser.getRole() != UserRole.STUDENT.getCode()) {
            throw BusinessException.forbidden("只有学生可以查看自己的提交记录");
        }
        return submissionMapper.findByStudentId(currentUser.getId()).stream()
                .map(this::toResponse)
                .toList();
    }

    public List<SubmissionResponse> assignmentSubmissions(Long assignmentId) {
        AuthUser currentUser = currentUser();
        Assignment assignment = assignmentService.requireAssignment(assignmentId);
        if (!assignmentService.canManageAssignment(currentUser, assignment)) {
            throw BusinessException.forbidden("无权查看该作业提交");
        }
        return submissionMapper.findByAssignmentId(assignmentId).stream()
                .map(this::toResponse)
                .toList();
    }

    public SubmissionResponse detail(Long id) {
        Submission submission = requireSubmission(id);
        AuthUser currentUser = currentUser();
        Assignment assignment = assignmentService.requireAssignment(submission.getAssignmentId());
        if (currentUser.getRole() == UserRole.STUDENT.getCode()
                && !submission.getStudentId().equals(currentUser.getId())) {
            throw BusinessException.forbidden("无权查看该提交记录");
        }
        if (currentUser.getRole() != UserRole.STUDENT.getCode()
                && !assignmentService.canManageAssignment(currentUser, assignment)) {
            throw BusinessException.forbidden("无权查看该提交记录");
        }
        return toResponse(requireResponse(id));
    }

    public SubmissionResponse grade(Long id, SubmissionGradeRequest request) {
        Submission submission = requireSubmission(id);
        AuthUser currentUser = currentUser();
        Assignment assignment = assignmentService.requireAssignment(submission.getAssignmentId());
        if (!assignmentService.canManageAssignment(currentUser, assignment)) {
            throw BusinessException.forbidden("无权批改该作业");
        }
        if (request.getScore() == null || request.getScore() < 0 || request.getScore() > assignment.getFullScore()) {
            throw BusinessException.badRequest("分数必须在 0 到满分之间");
        }
        submissionMapper.grade(id, request.getScore(), request.getTeacherComment());
        return detail(id);
    }

    private Submission requireSubmission(Long id) {
        Submission submission = submissionMapper.selectById(id);
        if (submission == null) {
            throw BusinessException.notFound("提交记录不存在");
        }
        return submission;
    }

    private Submission findByAssignmentAndStudent(Long assignmentId, Long studentId) {
        return submissionMapper.selectOne(new LambdaQueryWrapper<Submission>()
                .eq(Submission::getAssignmentId, assignmentId)
                .eq(Submission::getStudentId, studentId));
    }

    private SubmissionMapper.SubmissionResponseRow requireResponse(Long id) {
        SubmissionMapper.SubmissionResponseRow row = submissionMapper.findResponseById(id);
        if (row == null) {
            throw BusinessException.notFound("提交记录不存在");
        }
        return row;
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

    private SubmissionResponse toResponse(SubmissionMapper.SubmissionResponseRow row) {
        SubmissionResponse response = new SubmissionResponse();
        response.setId(row.getId());
        response.setAssignmentId(row.getAssignmentId());
        response.setAssignmentTitle(row.getAssignmentTitle());
        response.setCourseId(row.getCourseId());
        response.setCourseName(row.getCourseName());
        response.setStudentId(row.getStudentId());
        response.setContent(row.getContent());
        response.setAttachmentUrl(row.getAttachmentUrl());
        response.setScore(row.getScore());
        response.setTeacherComment(row.getTeacherComment());
        response.setAiComment(row.getAiComment());
        response.setSubmittedAt(row.getSubmittedAt());
        response.setGradedAt(row.getGradedAt());
        return response;
    }
}
