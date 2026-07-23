package com.whut.assessment.service;

import com.whut.assessment.dto.ExamCreateRequest;
import com.whut.assessment.dto.ExamStatusUpdateRequest;
import com.whut.assessment.dto.ExamWithQuestionsRequest;
import com.whut.assessment.entity.CourseSnapshot;
import com.whut.assessment.entity.Exam;
import com.whut.assessment.entity.Question;
import com.whut.assessment.mapper.ExamMapper;
import com.whut.assessment.mapper.ExamAttemptMapper;
import com.whut.assessment.mapper.QuestionMapper;
import com.whut.assessment.vo.ExamResponse;
import com.whut.common.auth.AuthContext;
import com.whut.common.auth.AuthUser;
import com.whut.common.enums.UserRole;
import com.whut.common.exception.BusinessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class ExamService {

    private static final int STATUS_DRAFT = 0;
    private static final int STATUS_PUBLISHED = 1;

    private final ExamMapper examMapper;
    private final QuestionMapper questionMapper;
    private final ExamAttemptMapper examAttemptMapper;

    public ExamService(ExamMapper examMapper, QuestionMapper questionMapper,
                       ExamAttemptMapper examAttemptMapper) {
        this.examMapper = examMapper;
        this.questionMapper = questionMapper;
        this.examAttemptMapper = examAttemptMapper;
    }

    public List<ExamResponse> listByCourse(Long courseId) {
        CourseSnapshot course = requireCourse(courseId);
        AuthUser currentUser = currentUser();
        assertCourseAccess(currentUser, course);
        boolean includeDraft = canManageCourse(currentUser, course);
        return examMapper.findByCourseId(courseId, includeDraft).stream()
                .map(this::toResponse)
                .toList();
    }

    public ExamResponse detail(Long id) {
        Exam exam = requireExam(id);
        AuthUser currentUser = currentUser();
        CourseSnapshot course = requireCourse(exam.getCourseId());
        assertCourseAccess(currentUser, course);
        if (exam.getStatus() == STATUS_DRAFT && !canManageExam(currentUser, exam)) {
            throw BusinessException.forbidden("无权查看该考试");
        }
        return toResponse(requireResponse(id));
    }

    public ExamResponse create(ExamCreateRequest request) {
        AuthUser currentUser = currentUser();
        if (currentUser.getRole() != UserRole.TEACHER.getCode()
                && currentUser.getRole() != UserRole.ADMIN.getCode()) {
            throw BusinessException.forbidden("只有教师或管理员可以发布考试");
        }
        if (request.getCourseId() == null) {
            throw BusinessException.badRequest("课程ID不能为空");
        }
        CourseSnapshot course = requireCourse(request.getCourseId());
        if (!canManageCourse(currentUser, course)) {
            throw BusinessException.forbidden("无权在该课程发布考试");
        }
        if (!StringUtils.hasText(request.getTitle())) {
            throw BusinessException.badRequest("考试标题不能为空");
        }
        if (request.getStartTime() == null || request.getEndTime() == null
                || !request.getEndTime().isAfter(request.getStartTime())) {
            throw BusinessException.badRequest("考试结束时间必须晚于开始时间");
        }
        int fullScore = 100;
        int duration = request.getDuration() == null ? 60 : request.getDuration();
        if (duration <= 0) {
            throw BusinessException.badRequest("考试时长必须大于0");
        }
        String examType = validateExamType(request.getType());
        int status = request.getStatus() == null ? STATUS_PUBLISHED : request.getStatus();
        if (status != STATUS_DRAFT && status != STATUS_PUBLISHED) {
            throw BusinessException.badRequest("考试状态不合法");
        }
        Exam exam = new Exam();
        exam.setCourseId(request.getCourseId());
        exam.setTeacherId(currentUser.getId());
        exam.setTitle(request.getTitle());
        exam.setDescription(request.getDescription());
        exam.setStartTime(request.getStartTime());
        exam.setEndTime(request.getEndTime());
        exam.setFullScore(fullScore);
        exam.setType(examType);
        exam.setDuration(duration);
        exam.setStatus(status);
        examMapper.insert(exam);
        return toResponse(requireResponse(exam.getId()));
    }

    @Transactional
    public ExamResponse createWithQuestions(ExamWithQuestionsRequest request) {
        AuthUser currentUser = currentUser();
        if (currentUser.getRole() != UserRole.TEACHER.getCode()
                && currentUser.getRole() != UserRole.ADMIN.getCode()) {
            throw BusinessException.forbidden("只有教师或管理员可以发布考试");
        }
        if (request.getCourseId() == null) {
            throw BusinessException.badRequest("课程ID不能为空");
        }
        CourseSnapshot course = requireCourse(request.getCourseId());
        if (!canManageCourse(currentUser, course)) {
            throw BusinessException.forbidden("无权在该课程发布考试");
        }
        if (!StringUtils.hasText(request.getTitle())) {
            throw BusinessException.badRequest("考试标题不能为空");
        }
        if (request.getQuestions() == null || request.getQuestions().isEmpty()) {
            throw BusinessException.badRequest("考试至少需要一道题目");
        }
        int fullScore = 100;
        int duration = request.getDuration() == null ? 60 : request.getDuration();
        int status = request.getStatus() == null ? 1 : request.getStatus();
        String examType = validateExamType(request.getType());

        Exam exam = new Exam();
        exam.setCourseId(request.getCourseId());
        exam.setTeacherId(currentUser.getId());
        exam.setTitle(request.getTitle());
        exam.setDescription(request.getDescription());
        exam.setStartTime(request.getStartTime());
        exam.setEndTime(request.getEndTime());
        exam.setFullScore(fullScore);
        exam.setType(examType);
        exam.setDuration(duration);
        exam.setStatus(status);
        examMapper.insert(exam);

        int questionCount = request.getQuestions().size();
        int perScore = fullScore / questionCount;
        int remainder = fullScore % questionCount;
        for (int i = 0; i < questionCount; i++) {
            ExamWithQuestionsRequest.QuestionItem qi = request.getQuestions().get(i);
            Question q = new Question();
            q.setExamId(exam.getId());
            q.setType(qi.getType());
            q.setTitle(qi.getTitle());
            q.setOptions(qi.getOptions());
            q.setAnswer(qi.getAnswer());
            q.setScore(perScore + (i < remainder ? 1 : 0));
            q.setSortOrder(qi.getSortOrder() == null ? i + 1 : qi.getSortOrder());
            questionMapper.insert(q);
        }
        return detail(exam.getId());
    }

    @Transactional
    public ExamResponse updateWithQuestions(Long id, ExamWithQuestionsRequest request) {
        AuthUser currentUser = currentUser();
        Exam exam = requireExam(id);
        if (!canManageExam(currentUser, exam)) {
            throw BusinessException.forbidden("无权修改该考试");
        }
        assertQuestionsEditable(id);
        if (request.getTitle() != null) exam.setTitle(request.getTitle());
        if (request.getDescription() != null) exam.setDescription(request.getDescription());
        if (request.getStartTime() != null) exam.setStartTime(request.getStartTime());
        if (request.getEndTime() != null) exam.setEndTime(request.getEndTime());
        exam.setFullScore(100);
        if (request.getDuration() != null) exam.setDuration(request.getDuration());
        if (request.getStatus() != null) exam.setStatus(request.getStatus());
        if (request.getType() != null) exam.setType(validateExamType(request.getType()));
        examMapper.updateById(exam);
        questionMapper.deleteByExamId(id);
        if (request.getQuestions() != null && !request.getQuestions().isEmpty()) {
            int questionCount = request.getQuestions().size();
            int perScore = 100 / questionCount;
            int remainder = 100 % questionCount;
            for (int i = 0; i < questionCount; i++) {
                ExamWithQuestionsRequest.QuestionItem qi = request.getQuestions().get(i);
                Question q = new Question();
                q.setExamId(id);
                q.setType(qi.getType());
                q.setTitle(qi.getTitle());
                q.setOptions(qi.getOptions());
                q.setAnswer(qi.getAnswer());
                q.setScore(perScore + (i < remainder ? 1 : 0));
                q.setSortOrder(qi.getSortOrder() == null ? i + 1 : qi.getSortOrder());
                questionMapper.insert(q);
            }
        }
        return detail(id);
    }

    public void delete(Long id) {
        AuthUser currentUser = currentUser();
        Exam exam = requireExam(id);
        if (!canManageExam(currentUser, exam)) {
            throw BusinessException.forbidden("无权删除该考试");
        }
        examMapper.deleteById(id);
    }

    public ExamResponse updateStatus(Long id, ExamStatusUpdateRequest request) {
        AuthUser currentUser = currentUser();
        Exam exam = requireExam(id);
        assertCanManageExam(currentUser, exam);
        if (request.getStatus() == null || (request.getStatus() != STATUS_DRAFT
                && request.getStatus() != STATUS_PUBLISHED && request.getStatus() != 2)) {
            throw BusinessException.badRequest("考试状态不合法");
        }
        examMapper.updateStatus(id, request.getStatus());
        return detail(id);
    }

    Exam requireExam(Long id) {
        Exam exam = examMapper.selectById(id);
        if (exam == null) {
            throw BusinessException.notFound("考试不存在");
        }
        return exam;
    }

    boolean canManageExam(AuthUser currentUser, Exam exam) {
        if (currentUser.getRole() == UserRole.ADMIN.getCode()) return true;
        if (currentUser.getRole() == UserRole.TEACHER.getCode()) {
            CourseSnapshot course = examMapper.findCourseById(exam.getCourseId());
            return course != null && canManageCourse(currentUser, course);
        }
        return false;
    }

    void assertExamAccess(AuthUser currentUser, Exam exam) {
        CourseSnapshot course = requireCourse(exam.getCourseId());
        assertCourseAccess(currentUser, course);
        if (exam.getStatus() == STATUS_DRAFT && !canManageExam(currentUser, exam)) {
            throw BusinessException.forbidden("无权查看该考试");
        }
    }

    void assertCanManageExam(AuthUser currentUser, Exam exam) {
        if (!canManageExam(currentUser, exam)) {
            throw BusinessException.forbidden("无权管理该考试");
        }
    }

    void assertQuestionsEditable(Long examId) {
        if (examAttemptMapper.countByExamId(examId) > 0) {
            throw BusinessException.badRequest("考试已有学生作答，不能再修改试卷题目");
        }
    }

    void assertApprovedStudent(Long courseId, AuthUser currentUser) {
        if (currentUser.getRole() != UserRole.STUDENT.getCode()
                || !examMapper.isApprovedStudent(courseId, currentUser.getId())) {
            throw BusinessException.forbidden("只有已通过选课审核的学生可以访问该课程考试");
        }
    }

    private void assertCourseAccess(AuthUser currentUser, CourseSnapshot course) {
        if (canManageCourse(currentUser, course)) {
            return;
        }
        assertApprovedStudent(course.getId(), currentUser);
    }

    private boolean canManageCourse(AuthUser currentUser, CourseSnapshot course) {
        return currentUser.getRole() == UserRole.ADMIN.getCode()
                || (currentUser.getRole() == UserRole.TEACHER.getCode()
                && (course.getTeacherId().equals(currentUser.getId())
                || examMapper.countClassesByTeacher(course.getId(), currentUser.getId()) > 0));
    }

    private CourseSnapshot requireCourse(Long courseId) {
        CourseSnapshot course = examMapper.findCourseById(courseId);
        if (course == null || (course.getDeleted() != null && course.getDeleted() == 1)) {
            throw BusinessException.notFound("课程不存在");
        }
        return course;
    }

    private ExamMapper.ExamResponseRow requireResponse(Long id) {
        ExamMapper.ExamResponseRow row = examMapper.findResponseById(id);
        if (row == null) {
            throw BusinessException.notFound("考试不存在");
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

    private ExamResponse toResponse(ExamMapper.ExamResponseRow row) {
        ExamResponse response = new ExamResponse();
        response.setId(row.getId());
        response.setCourseId(row.getCourseId());
        response.setCourseName(row.getCourseName());
        response.setTeacherId(row.getTeacherId());
        response.setTitle(row.getTitle());
        response.setDescription(row.getDescription());
        response.setStartTime(row.getStartTime());
        response.setEndTime(row.getEndTime());
        response.setFullScore(row.getFullScore());
        response.setType(row.getType());
        response.setDuration(row.getDuration());
        response.setStatus(row.getStatus());
        response.setCreatedAt(row.getCreatedAt());
        return response;
    }

    private String validateExamType(String type) {
        if (type == null) return "quiz";
        String t = type.toLowerCase();
        if (!"quiz".equals(t) && !"midterm".equals(t) && !"final".equals(t)) {
            throw BusinessException.badRequest("考试类型不合法，仅支持 quiz/midterm/final");
        }
        return t;
    }
}
