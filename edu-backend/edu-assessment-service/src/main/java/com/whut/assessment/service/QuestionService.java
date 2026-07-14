package com.whut.assessment.service;

import com.whut.assessment.entity.Question;
import com.whut.assessment.mapper.QuestionMapper;
import com.whut.assessment.vo.QuestionResponse;
import com.whut.common.auth.AuthContext;
import com.whut.common.auth.AuthUser;
import com.whut.common.enums.UserRole;
import com.whut.common.exception.BusinessException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class QuestionService {

    private final QuestionMapper questionMapper;
    private final ExamService examService;

    public QuestionService(QuestionMapper questionMapper, ExamService examService) {
        this.questionMapper = questionMapper;
        this.examService = examService;
    }

    public List<QuestionResponse> listByExam(Long examId, boolean withAnswers) {
        AuthUser currentUser = currentUser();
        if (withAnswers && currentUser.getRole() != UserRole.TEACHER.getCode()
                && currentUser.getRole() != UserRole.ADMIN.getCode()) {
            throw BusinessException.forbidden("无权查看答案");
        }
        return questionMapper.findByExamId(examId).stream()
                .map(q -> toResponse(q, withAnswers))
                .toList();
    }

    public QuestionResponse create(Long examId, Integer type, String title,
                                    String options, String answer, Integer score, Integer sortOrder) {
        AuthUser currentUser = currentUser();
        if (currentUser.getRole() != UserRole.TEACHER.getCode()
                && currentUser.getRole() != UserRole.ADMIN.getCode()) {
            throw BusinessException.forbidden("只有教师可以添加题目");
        }
        examService.requireExam(examId);
        Question question = new Question();
        question.setExamId(examId);
        question.setType(type);
        question.setTitle(title);
        question.setOptions(options);
        question.setAnswer(answer);
        question.setScore(score == null ? 10 : score);
        question.setSortOrder(sortOrder == null ? 0 : sortOrder);
        questionMapper.insert(question);
        return toResponse(question, true);
    }

    public void update(Long id, Integer type, String title,
                        String options, String answer, Integer score, Integer sortOrder) {
        AuthUser currentUser = currentUser();
        if (currentUser.getRole() != UserRole.TEACHER.getCode()
                && currentUser.getRole() != UserRole.ADMIN.getCode()) {
            throw BusinessException.forbidden("只有教师可以修改题目");
        }
        Question question = questionMapper.selectById(id);
        if (question == null) {
            throw BusinessException.notFound("题目不存在");
        }
        if (type != null) question.setType(type);
        if (title != null) question.setTitle(title);
        if (options != null) question.setOptions(options);
        if (answer != null) question.setAnswer(answer);
        if (score != null) question.setScore(score);
        if (sortOrder != null) question.setSortOrder(sortOrder);
        questionMapper.updateById(question);
    }

    public void delete(Long id) {
        AuthUser currentUser = currentUser();
        if (currentUser.getRole() != UserRole.TEACHER.getCode()
                && currentUser.getRole() != UserRole.ADMIN.getCode()) {
            throw BusinessException.forbidden("只有教师可以删除题目");
        }
        Question question = questionMapper.selectById(id);
        if (question == null) {
            throw BusinessException.notFound("题目不存在");
        }
        questionMapper.deleteById(id);
    }

    private QuestionResponse toResponse(Question q, boolean withAnswer) {
        QuestionResponse r = new QuestionResponse();
        r.setId(q.getId());
        r.setExamId(q.getExamId());
        r.setType(q.getType());
        r.setTitle(q.getTitle());
        r.setOptions(q.getOptions());
        r.setScore(q.getScore());
        r.setSortOrder(q.getSortOrder());
        if (withAnswer) {
            r.setAnswer(q.getAnswer());
        }
        return r;
    }

    private AuthUser currentUser() {
        AuthUser u = AuthContext.get();
        if (u == null) throw BusinessException.unauthorized("请先登录");
        return u;
    }
}
