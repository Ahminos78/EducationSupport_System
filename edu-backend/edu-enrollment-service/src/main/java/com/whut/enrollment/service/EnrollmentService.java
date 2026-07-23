package com.whut.enrollment.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.whut.common.auth.AuthContext;
import com.whut.common.auth.AuthUser;
import com.whut.common.enums.EnrollmentStatus;
import com.whut.common.enums.UserRole;
import com.whut.common.exception.BusinessException;
import com.whut.enrollment.dto.EnrollmentCreateRequest;
import com.whut.enrollment.dto.EnrollmentReviewRequest;
import com.whut.enrollment.entity.CourseSnapshot;
import com.whut.enrollment.entity.Enrollment;
import com.whut.enrollment.mapper.EnrollmentMapper;
import com.whut.enrollment.vo.EnrollmentResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import com.whut.enrollment.mapper.GradeComponentMapper;
import com.whut.enrollment.mapper.StudentGradeMapper;
import com.whut.enrollment.service.WarningService;
import com.whut.enrollment.entity.GradeComponent;
import com.whut.enrollment.entity.StudentGrade;
import com.whut.enrollment.vo.CourseStudyScoreResponse;
import com.whut.enrollment.vo.CourseStudyScoreResponse.ComponentScoreItem;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class EnrollmentService {

    private static final int COURSE_ONLINE = 1;

    private final EnrollmentMapper enrollmentMapper;
    private final GradeComponentMapper gradeComponentMapper;
    private final StudentGradeMapper studentGradeMapper;
    private final WarningService warningService;

    public EnrollmentService(EnrollmentMapper enrollmentMapper,
                             GradeComponentMapper gradeComponentMapper,
                             StudentGradeMapper studentGradeMapper,
                             WarningService warningService) {
        this.enrollmentMapper = enrollmentMapper;
        this.gradeComponentMapper = gradeComponentMapper;
        this.studentGradeMapper = studentGradeMapper;
        this.warningService = warningService;
    }

    @Transactional
    public EnrollmentResponse apply(EnrollmentCreateRequest request) {
        AuthUser currentUser = currentUser();
        if (currentUser.getRole() != UserRole.STUDENT.getCode()) {
            throw BusinessException.forbidden("只有学生可以申请选课");
        }
        if (request.getCourseId() == null || request.getClassId() == null) {
            throw BusinessException.badRequest("课程ID和教学班ID不能为空");
        }
        CourseSnapshot course = requireAvailableCourse(request.getCourseId());
        EnrollmentMapper.ClassSnapshot classSection = requireClass(request.getClassId());
        if (!classSection.getCourseId().equals(request.getCourseId())) {
            throw BusinessException.badRequest("教学班不属于该课程");
        }
        if (classSection.getEnrolledCount() != null && classSection.getMaxStudents() != null
                && classSection.getEnrolledCount() >= classSection.getMaxStudents()) {
            throw BusinessException.badRequest("该教学班人数已满");
        }
        Enrollment existing = findByClassAndStudent(request.getClassId(), currentUser.getId());
        if (existing != null && existing.getStatus() != EnrollmentStatus.REJECTED.getCode()) {
            throw BusinessException.badRequest("已选过该教学班");
        }
        List<EnrollmentMapper.ScheduleSlot> newSlots = enrollmentMapper.findScheduleSlotsByClassId(request.getClassId());
        List<EnrollmentMapper.ScheduleSlot> existingSlots = getStudentScheduleSlots(currentUser.getId());
        List<String> conflicts = detectConflicts(newSlots, existingSlots);
        if (!conflicts.isEmpty()) {
            throw BusinessException.badRequest("时间冲突：" + String.join("；", conflicts));
        }
        Enrollment enrollment = new Enrollment();
        enrollment.setCourseId(request.getCourseId());
        enrollment.setClassId(request.getClassId());
        enrollment.setStudentId(currentUser.getId());
        enrollment.setStatus(EnrollmentStatus.PENDING.getCode());
        enrollment.setApplyReason(request.getApplyReason());
        enrollmentMapper.insert(enrollment);
        return getOwnEnrollment(enrollment.getId());
    }

    public List<EnrollmentResponse> myEnrollments() {
        AuthUser currentUser = currentUser();
        if (currentUser.getRole() != UserRole.STUDENT.getCode()) {
            throw BusinessException.forbidden("只有学生可以查看自己的选课记录");
        }
        return enrollmentMapper.findByStudentId(currentUser.getId()).stream()
                .map(this::toResponse)
                .toList();
    }

    public List<EnrollmentResponse> courseEnrollments(Long courseId, Long classId, Integer status) {
        AuthUser currentUser = currentUser();
        CourseSnapshot course = requireCourse(courseId);
        if (!canManageCourse(currentUser, course)) {
            throw BusinessException.forbidden("无权查看该课程选课记录");
        }
        assertValidStatusWhenPresent(status);
        return enrollmentMapper.findByCourseId(courseId, classId, status).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public EnrollmentResponse approve(Long id, EnrollmentReviewRequest request) {
        Enrollment enrollment = requireEnrollment(id);
        AuthUser currentUser = currentUser();
        assertCanManageClass(currentUser, enrollment.getCourseId(), enrollment.getClassId());
        if (enrollment.getStatus() != EnrollmentStatus.PENDING.getCode()) {
            throw BusinessException.badRequest("只能通过待审核的选课申请");
        }
        if (enrollment.getClassId() != null) {
            if (enrollmentMapper.increaseClassEnrollment(enrollment.getClassId()) == 0) {
                throw BusinessException.badRequest("教学班人数已满或教学班不可选");
            }
        }
        enrollmentMapper.increaseCourseEnrollment(enrollment.getCourseId());
        enrollmentMapper.updateReviewStatus(id, EnrollmentStatus.APPROVED.getCode(), reviewComment(request));
        return getEnrollmentForManager(id);
    }

    @Transactional
    public EnrollmentResponse reject(Long id, EnrollmentReviewRequest request) {
        Enrollment enrollment = requireEnrollment(id);
        AuthUser currentUser = currentUser();
        assertCanManageClass(currentUser, enrollment.getCourseId(), enrollment.getClassId());
        if (enrollment.getStatus() != EnrollmentStatus.PENDING.getCode()) {
            throw BusinessException.badRequest("只能拒绝待审核的选课申请");
        }
        enrollmentMapper.updateReviewStatus(id, EnrollmentStatus.REJECTED.getCode(), reviewComment(request));
        return getEnrollmentForManager(id);
    }

    @Transactional
    public EnrollmentResponse removeByTeacher(Long id) {
        Enrollment enrollment = requireEnrollment(id);
        AuthUser currentUser = currentUser();
        assertCanManageClass(currentUser, enrollment.getCourseId(), enrollment.getClassId());
        if (enrollment.getStatus() != EnrollmentStatus.APPROVED.getCode()) {
            throw BusinessException.badRequest("只能移出已通过审核的学生");
        }
        enrollmentMapper.drop(id);
        if (enrollment.getClassId() != null) {
            enrollmentMapper.decreaseClassEnrollment(enrollment.getClassId());
        }
        enrollmentMapper.decreaseCourseEnrollment(enrollment.getCourseId());
        return getEnrollmentForManager(id);
    }

    @Transactional
    public EnrollmentResponse drop(Long id) {
        AuthUser currentUser = currentUser();
        Enrollment enrollment = requireEnrollment(id);
        if (currentUser.getRole() != UserRole.STUDENT.getCode() || !enrollment.getStudentId().equals(currentUser.getId())) {
            throw BusinessException.forbidden("只能退选自己的课程");
        }
        if (enrollment.getStatus() != EnrollmentStatus.APPROVED.getCode()
                && enrollment.getStatus() != EnrollmentStatus.PENDING.getCode()) {
            throw BusinessException.badRequest("当前状态不能退选");
        }
        if (enrollment.getStatus() == EnrollmentStatus.PENDING.getCode()) {
            enrollmentMapper.physicallyDelete(id);
            return null;
        }
        enrollmentMapper.drop(id);
        if (enrollment.getClassId() != null) {
            enrollmentMapper.decreaseClassEnrollment(enrollment.getClassId());
        }
        enrollmentMapper.decreaseCourseEnrollment(enrollment.getCourseId());
        return getOwnEnrollment(id);
    }

    public CourseStudyScoreResponse getStudyScore(Long courseId) {
        AuthUser currentUser = currentUser();
        if (currentUser.getRole() != 1) {
            throw BusinessException.forbidden("只有学生可以查看学情成绩");
        }
        // 1. Find enrollment for this student + course
        Enrollment enrollment = findByCourseAndStudent(courseId, currentUser.getId());
        if (enrollment == null || enrollment.getStatus() != 1) {
            return null;
        }
        // 2. Get grade components
        List<GradeComponent> components = gradeComponentMapper.findByCourseId(courseId);
        if (components == null || components.isEmpty()) {
            return null;
        }
        // 3. Count total assignments/exams and completed ones
        int totalAssignments = enrollmentMapper.countAssignments(courseId);
        int totalExams = enrollmentMapper.countExams(courseId);
        int totalTasks = totalAssignments + totalExams;
        List<Integer> submissionScores = enrollmentMapper.findSubmissionScores(courseId, currentUser.getId());
        List<Integer> examScores = enrollmentMapper.findExamScores(courseId, currentUser.getId());
        int completedAssignments = submissionScores.size();
        int completedExams = examScores.size();
        int completedTasks = completedAssignments + completedExams;

        // 3.5 预加载按类型分类的考试成绩
        List<Integer> quizScores = enrollmentMapper.findExamScoresByType(courseId, currentUser.getId(), "quiz");
        List<Integer> midtermScores = enrollmentMapper.findExamScoresByType(courseId, currentUser.getId(), "midterm");
        List<Integer> finalScores = enrollmentMapper.findExamScoresByType(courseId, currentUser.getId(), "final");

        // 4. Calculate each component score
        List<ComponentScoreItem> componentItems = new ArrayList<>();
        BigDecimal totalWeightedScore = BigDecimal.ZERO;
        BigDecimal totalWeight = BigDecimal.ZERO;
        for (GradeComponent comp : components) {
            BigDecimal componentScore = null;
            String name = comp.getName();
            if (name != null && (name.contains("作业") || name.contains("Assignment") || name.contains("assignment"))) {
                // 平时作业 = 作业平均分 + 测验平均分（合并计算）
                double assignmentAvg = submissionScores.stream().mapToInt(Integer::intValue).average().orElse(0);
                double quizAvg = quizScores.stream().mapToInt(Integer::intValue).average().orElse(0);
                boolean hasAssignment = !submissionScores.isEmpty();
                boolean hasQuiz = !quizScores.isEmpty();
                if (hasAssignment || hasQuiz) {
                    double combined = 0;
                    int count = 0;
                    if (hasAssignment) { combined += assignmentAvg; count++; }
                    if (hasQuiz) { combined += quizAvg; count++; }
                    componentScore = BigDecimal.valueOf(combined / count);
                }
            } else if (name != null && name.contains("期中")) {
                // 期中考试成绩取平均分
                if (!midtermScores.isEmpty()) {
                    double avg = midtermScores.stream().mapToInt(Integer::intValue).average().orElse(0);
                    componentScore = BigDecimal.valueOf(avg);
                }
            } else if (name != null && name.contains("期末")) {
                // 期末考试成绩取平均分
                if (!finalScores.isEmpty()) {
                    double avg = finalScores.stream().mapToInt(Integer::intValue).average().orElse(0);
                    componentScore = BigDecimal.valueOf(avg);
                }
            } else if (name != null && (name.contains("考试") || name.contains("Exam") || name.contains("exam"))) {
                // 兜底：通用"考试"仍取所有考试的最新成绩（向后兼容）
                if (!examScores.isEmpty()) {
                    componentScore = BigDecimal.valueOf(examScores.get(examScores.size() - 1));
                }
            } else {
                // Other components: check tb_student_grade
                if (enrollment != null && enrollment.getId() != null) {
                    StudentGrade sg = studentGradeMapper.selectOne(
                            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<StudentGrade>()
                                    .eq(StudentGrade::getEnrollmentId, enrollment.getId())
                                    .eq(StudentGrade::getComponentId, comp.getId()));
                    if (sg != null && sg.getScore() != null) {
                        componentScore = sg.getScore();
                    }
                }
            }
            if (componentScore != null) {
                componentItems.add(new ComponentScoreItem(comp.getName(), comp.getWeight(), componentScore, comp.getMaxScore()));
                BigDecimal weight = comp.getWeight() != null ? comp.getWeight() : BigDecimal.ONE;
                totalWeightedScore = totalWeightedScore.add(componentScore.multiply(weight));
                totalWeight = totalWeight.add(weight);
            } else {
                componentItems.add(new ComponentScoreItem(comp.getName(), comp.getWeight(), null, comp.getMaxScore()));
            }
        }
        // 5. Calculate final weighted score
        BigDecimal finalScore = null;
        String gradeLetter = null;
        Integer passed = null;
        if (totalWeight.compareTo(BigDecimal.ZERO) > 0) {
            finalScore = totalWeightedScore.divide(totalWeight, 1, RoundingMode.HALF_UP);
            double scoreVal = finalScore.doubleValue();
            if (scoreVal >= 90) gradeLetter = "A";
            else if (scoreVal >= 80) gradeLetter = "B";
            else if (scoreVal >= 70) gradeLetter = "C";
            else if (scoreVal >= 60) gradeLetter = "D";
            else gradeLetter = "F";
            passed = scoreVal >= 60 ? 1 : 0;
        }
        // 根据成绩自动创建/更新学业预警
        if (enrollment != null && enrollment.getId() != null) {
            warningService.evaluateAndCreateWarning(
                    currentUser.getId(), courseId, enrollment.getId(), finalScore);
        }
        // 6. Build response
        CourseStudyScoreResponse response = new CourseStudyScoreResponse();
        response.setFinalScore(finalScore);
        response.setGradeLetter(gradeLetter);
        response.setPassed(passed);
        response.setCompletedTasks(completedTasks);
        response.setTotalTasks(totalTasks);
        response.setCompletedAssignments(completedAssignments);
        response.setTotalAssignments(totalAssignments);
        response.setCompletedExams(completedExams);
        response.setTotalExams(totalExams);
        response.setCompletionPercent(totalTasks > 0 ? completedTasks * 100 / totalTasks : 0);
        response.setComponentScores(componentItems);
        return response;
    }

    // ── 教学班选课 + 时间冲突检测 ────────────────────────────────

    public List<EnrollmentMapper.ScheduleSlot> getStudentScheduleSlots(Long studentId) {
        List<Long> classIds = enrollmentMapper.findEnrolledClassIds(studentId);
        List<EnrollmentMapper.ScheduleSlot> allSlots = new ArrayList<>();
        for (Long classId : classIds) {
            allSlots.addAll(enrollmentMapper.findScheduleSlotsByClassId(classId));
        }
        return allSlots;
    }

    public List<String> detectConflicts(List<EnrollmentMapper.ScheduleSlot> newSlots,
                                        List<EnrollmentMapper.ScheduleSlot> existingSlots) {
        List<String> conflicts = new ArrayList<>();
        for (EnrollmentMapper.ScheduleSlot newSlot : newSlots) {
            for (EnrollmentMapper.ScheduleSlot existingSlot : existingSlots) {
                if (newSlot.getDayOfWeek().equals(existingSlot.getDayOfWeek())
                        && periodsOverlap(newSlot.getStartPeriod(), newSlot.getEndPeriod(),
                                          existingSlot.getStartPeriod(), existingSlot.getEndPeriod())
                        && weekOverlap(newSlot.getStartWeek(), newSlot.getEndWeek(),
                                       existingSlot.getStartWeek(), existingSlot.getEndWeek(),
                                       newSlot.getWeekType(), existingSlot.getWeekType())) {
                    String dayName = switch (newSlot.getDayOfWeek()) {
                        case 1 -> "周一"; case 2 -> "周二"; case 3 -> "周三";
                        case 4 -> "周四"; case 5 -> "周五"; case 6 -> "周六"; default -> "周日";
                    };
                    conflicts.add(dayName + " 第" + newSlot.getStartPeriod() + "-" + newSlot.getEndPeriod() + "节");
                }
            }
        }
        return conflicts.stream().distinct().toList();
    }

    public List<String> checkConflict(Long classId) {
        AuthUser currentUser = currentUser();
        if (currentUser.getRole() != UserRole.STUDENT.getCode()) {
            throw BusinessException.forbidden("只有学生可以检查时间冲突");
        }
        if (classId == null) {
            throw BusinessException.badRequest("教学班ID不能为空");
        }
        List<EnrollmentMapper.ScheduleSlot> newSlots = enrollmentMapper.findScheduleSlotsByClassId(classId);
        List<EnrollmentMapper.ScheduleSlot> existingSlots = getStudentScheduleSlots(currentUser.getId());
        return detectConflicts(newSlots, existingSlots);
    }

    private boolean periodsOverlap(int s1, int e1, int s2, int e2) {
        return s1 <= e2 && s2 <= e1;
    }

    private boolean weekOverlap(int s1, int e1, int s2, int e2, Integer type1, Integer type2) {
        if (s1 > e2 || s2 > e1) return false;
        if ((type1 == null || type1 == 0) && (type2 == null || type2 == 0)) return true;
        int t1 = type1 != null ? type1 : 0;
        int t2 = type2 != null ? type2 : 0;
        if (t1 == 0 || t2 == 0) return true;
        if (t1 == t2) return true;
        for (int w = Math.max(s1, s2); w <= Math.min(e1, e2); w++) {
            boolean w1 = t1 == 0 || (t1 == 1 && w % 2 == 1) || (t1 == 2 && w % 2 == 0);
            boolean w2 = t2 == 0 || (t2 == 1 && w % 2 == 1) || (t2 == 2 && w % 2 == 0);
            if (w1 && w2) return true;
        }
        return false;
    }

    private EnrollmentResponse getOwnEnrollment(Long id) {
        Enrollment enrollment = requireEnrollment(id);
        AuthUser currentUser = currentUser();
        if (!enrollment.getStudentId().equals(currentUser.getId())) {
            throw BusinessException.forbidden("无权查看该选课记录");
        }
        return findEnrollmentResponse(enrollment);
    }

    private EnrollmentResponse getEnrollmentForManager(Long id) {
        Enrollment enrollment = requireEnrollment(id);
        return findEnrollmentResponse(enrollment);
    }

    private EnrollmentResponse findEnrollmentResponse(Enrollment enrollment) {
        List<EnrollmentMapper.EnrollmentResponseRow> rows;
        if (enrollment.getClassId() != null) {
            rows = enrollmentMapper.findByCourseId(enrollment.getCourseId(), enrollment.getClassId(), null);
        } else {
            rows = enrollmentMapper.findByCourseId(enrollment.getCourseId(), null, null);
        }
        return rows.stream()
                .filter(item -> item.getId().equals(enrollment.getId()))
                .findFirst()
                .map(this::toResponse)
                .orElseThrow(() -> BusinessException.notFound("选课记录不存在"));
    }

    private Enrollment requireEnrollment(Long id) {
        Enrollment enrollment = enrollmentMapper.selectById(id);
        if (enrollment == null) {
            throw BusinessException.notFound("选课记录不存在");
        }
        return enrollment;
    }

    private Enrollment findByClassAndStudent(Long classId, Long studentId) {
        return enrollmentMapper.selectOne(new LambdaQueryWrapper<Enrollment>()
                .eq(Enrollment::getClassId, classId)
                .eq(Enrollment::getStudentId, studentId));
    }

    private Enrollment findByCourseAndStudent(Long courseId, Long studentId) {
        return enrollmentMapper.selectOne(new LambdaQueryWrapper<Enrollment>()
                .eq(Enrollment::getCourseId, courseId)
                .eq(Enrollment::getStudentId, studentId)
                .eq(Enrollment::getStatus, EnrollmentStatus.APPROVED.getCode()));
    }

    private EnrollmentMapper.ClassSnapshot requireClass(Long classId) {
        EnrollmentMapper.ClassSnapshot classSection = enrollmentMapper.findClassById(classId);
        if (classSection == null) {
            throw BusinessException.notFound("教学班不存在");
        }
        return classSection;
    }

    private CourseSnapshot requireAvailableCourse(Long courseId) {
        CourseSnapshot course = requireCourse(courseId);
        if (course.getDeleted() != null && course.getDeleted() == 1) {
            throw BusinessException.notFound("课程不存在");
        }
        if (course.getStatus() != COURSE_ONLINE) {
            throw BusinessException.badRequest("课程未开放选课");
        }
        return course;
    }

    private CourseSnapshot requireCourse(Long courseId) {
        if (courseId == null) {
            throw BusinessException.badRequest("课程ID不能为空");
        }
        CourseSnapshot course = enrollmentMapper.findCourseById(courseId);
        if (course == null || (course.getDeleted() != null && course.getDeleted() == 1)) {
            throw BusinessException.notFound("课程不存在");
        }
        return course;
    }

    private boolean canManageCourse(AuthUser currentUser, CourseSnapshot course) {
        return currentUser.getRole() == UserRole.ADMIN.getCode()
                || (currentUser.getRole() == UserRole.TEACHER.getCode()
                && (course.getTeacherId().equals(currentUser.getId())
                || enrollmentMapper.countClassesByTeacher(course.getId(), currentUser.getId()) > 0));
    }

    private void assertCanManageClass(AuthUser currentUser, Long courseId, Long classId) {
        if (currentUser.getRole() == UserRole.ADMIN.getCode()) return;
        if (currentUser.getRole() == UserRole.TEACHER.getCode()) {
            if (classId == null) {
                throw BusinessException.forbidden("无权管理该课程的学生");
            }
            EnrollmentMapper.ClassSnapshot cls = enrollmentMapper.findClassById(classId);
            if (cls == null || !cls.getTeacherId().equals(currentUser.getId())) {
                throw BusinessException.forbidden("只能管理自己教学班的学生");
            }
            return;
        }
        throw BusinessException.forbidden("无权操作");
    }

    private void assertValidStatusWhenPresent(Integer status) {
        if (status == null) {
            return;
        }
        for (EnrollmentStatus enrollmentStatus : EnrollmentStatus.values()) {
            if (enrollmentStatus.getCode() == status) {
                return;
            }
        }
        throw BusinessException.badRequest("选课状态不合法");
    }

    private String reviewComment(EnrollmentReviewRequest request) {
        return request == null ? null : request.getReviewComment();
    }

    private AuthUser currentUser() {
        AuthUser currentUser = AuthContext.get();
        if (currentUser == null) {
            throw BusinessException.unauthorized("请先登录");
        }
        return currentUser;
    }

    private EnrollmentResponse toResponse(EnrollmentMapper.EnrollmentResponseRow row) {
        EnrollmentResponse response = new EnrollmentResponse();
        response.setId(row.getId());
        response.setCourseId(row.getCourseId());
        response.setCourseName(row.getCourseName());
        response.setClassId(row.getClassId());
        response.setClassName(row.getClassName());
        response.setStudentId(row.getStudentId());
        response.setStudentName(row.getStudentName());
        response.setStatus(row.getStatus());
        response.setApplyReason(row.getApplyReason());
        response.setReviewComment(row.getReviewComment());
        response.setAppliedAt(row.getAppliedAt());
        response.setReviewedAt(row.getReviewedAt());
        return response;
    }
}
