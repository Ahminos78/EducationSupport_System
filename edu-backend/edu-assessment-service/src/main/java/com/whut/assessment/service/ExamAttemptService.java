package com.whut.assessment.service;

import com.whut.assessment.dto.ExamAttemptGradeRequest;
import com.whut.assessment.dto.ExamAttemptSubmitRequest;
import com.whut.assessment.entity.Exam;
import com.whut.assessment.entity.ExamAttempt;
import com.whut.assessment.mapper.ExamAttemptMapper;
import com.whut.assessment.vo.ExamAttemptResponse;
import com.whut.common.auth.AuthContext;
import com.whut.common.auth.AuthUser;
import com.whut.common.enums.UserRole;
import com.whut.common.exception.BusinessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ExamAttemptService {

    private static final int EXAM_PUBLISHED = 1;
    private static final int ATTEMPT_IN_PROGRESS = 0;

    private final ExamAttemptMapper examAttemptMapper;
    private final ExamService examService;

    public ExamAttemptService(ExamAttemptMapper examAttemptMapper, ExamService examService) {
        this.examAttemptMapper = examAttemptMapper;
        this.examService = examService;
    }

    @Transactional
    public ExamAttemptResponse start(Long examId) {
        AuthUser currentUser = requireStudent();
        Exam exam = examService.requireExam(examId);
        examService.assertApprovedStudent(exam.getCourseId(), currentUser);
        assertExamOpen(exam);
        ExamAttempt existing = examAttemptMapper.findByExamAndStudent(examId, currentUser.getId());
        if (existing != null) {
            return toResponse(requireResponse(existing.getId()));
        }
        ExamAttempt attempt = new ExamAttempt();
        attempt.setExamId(examId);
        attempt.setStudentId(currentUser.getId());
        attempt.setStatus(ATTEMPT_IN_PROGRESS);
        examAttemptMapper.insert(attempt);
        return toResponse(requireResponse(attempt.getId()));
    }

    @Transactional
    public ExamAttemptResponse submit(Long examId, ExamAttemptSubmitRequest request) {
        AuthUser currentUser = requireStudent();
        Exam exam = examService.requireExam(examId);
        examService.assertApprovedStudent(exam.getCourseId(), currentUser);
        assertExamOpen(exam);
        if (!StringUtils.hasText(request.getAnswerContent())) {
            throw BusinessException.badRequest("考试作答内容不能为空");
        }
        ExamAttempt attempt = examAttemptMapper.findByExamAndStudent(examId, currentUser.getId());
        if (attempt == null) {
            throw BusinessException.badRequest("请先开始考试");
        }
        if (attempt.getStatus() != ATTEMPT_IN_PROGRESS) {
            throw BusinessException.badRequest("考试已交卷，不能重复提交");
        }
        examAttemptMapper.submit(attempt.getId(), request.getAnswerContent());
        return toResponse(requireResponse(attempt.getId()));
    }

    public List<ExamAttemptResponse> myAttempts() {
        AuthUser currentUser = requireStudent();
        return examAttemptMapper.findByStudentId(currentUser.getId()).stream()
                .map(this::toResponse)
                .toList();
    }

    public List<ExamAttemptResponse> examAttempts(Long examId) {
        AuthUser currentUser = currentUser();
        Exam exam = examService.requireExam(examId);
        if (!examService.canManageExam(currentUser, exam)) {
            throw BusinessException.forbidden("无权查看该考试的参与记录");
        }
        return examAttemptMapper.findByExamId(examId).stream()
                .map(this::toResponse)
                .toList();
    }

    public ExamAttemptResponse detail(Long id) {
        ExamAttempt attempt = requireAttempt(id);
        AuthUser currentUser = currentUser();
        Exam exam = examService.requireExam(attempt.getExamId());
        if (currentUser.getRole() == UserRole.STUDENT.getCode()
                && !attempt.getStudentId().equals(currentUser.getId())) {
            throw BusinessException.forbidden("无权查看该考试记录");
        }
        if (currentUser.getRole() != UserRole.STUDENT.getCode()
                && !examService.canManageExam(currentUser, exam)) {
            throw BusinessException.forbidden("无权查看该考试记录");
        }
        return toResponse(requireResponse(id));
    }

    public ExamAttemptResponse grade(Long id, ExamAttemptGradeRequest request) {
        ExamAttempt attempt = requireAttempt(id);
        AuthUser currentUser = currentUser();
        Exam exam = examService.requireExam(attempt.getExamId());
        if (!examService.canManageExam(currentUser, exam)) {
            throw BusinessException.forbidden("无权批改该考试");
        }
        if (attempt.getStatus() == ATTEMPT_IN_PROGRESS) {
            throw BusinessException.badRequest("学生尚未交卷");
        }
        if (request.getScore() == null || request.getScore() < 0 || request.getScore() > exam.getFullScore()) {
            throw BusinessException.badRequest("分数必须在 0 到考试满分之间");
        }
        examAttemptMapper.grade(id, request.getScore(), request.getTeacherComment());
        return detail(id);
    }

    private void assertExamOpen(Exam exam) {
        LocalDateTime now = LocalDateTime.now();
        if (exam.getStatus() != EXAM_PUBLISHED
                || now.isBefore(exam.getStartTime())
                || now.isAfter(exam.getEndTime())) {
            throw BusinessException.badRequest("考试尚未开始或已经结束");
        }
    }

    private ExamAttempt requireAttempt(Long id) {
        ExamAttempt attempt = examAttemptMapper.selectById(id);
        if (attempt == null) {
            throw BusinessException.notFound("考试参与记录不存在");
        }
        return attempt;
    }

    private ExamAttemptMapper.ExamAttemptResponseRow requireResponse(Long id) {
        ExamAttemptMapper.ExamAttemptResponseRow row = examAttemptMapper.findResponseById(id);
        if (row == null) {
            throw BusinessException.notFound("考试参与记录不存在");
        }
        return row;
    }

    private AuthUser requireStudent() {
        AuthUser currentUser = currentUser();
        if (currentUser.getRole() != UserRole.STUDENT.getCode()) {
            throw BusinessException.forbidden("只有学生可以参加考试");
        }
        return currentUser;
    }

    private AuthUser currentUser() {
        AuthUser currentUser = AuthContext.get();
        if (currentUser == null) {
            throw BusinessException.unauthorized("请先登录");
        }
        return currentUser;
    }

    private ExamAttemptResponse toResponse(ExamAttemptMapper.ExamAttemptResponseRow row) {
        ExamAttemptResponse response = new ExamAttemptResponse();
        response.setId(row.getId());
        response.setExamId(row.getExamId());
        response.setExamTitle(row.getExamTitle());
        response.setCourseId(row.getCourseId());
        response.setCourseName(row.getCourseName());
        response.setStudentId(row.getStudentId());
        response.setStudentName(row.getStudentName());
        response.setAnswerContent(row.getAnswerContent());
        response.setStatus(row.getStatus());
        response.setScore(row.getScore());
        response.setTeacherComment(row.getTeacherComment());
        response.setStartedAt(row.getStartedAt());
        response.setSubmittedAt(row.getSubmittedAt());
        response.setGradedAt(row.getGradedAt());
        return response;
    }
}
