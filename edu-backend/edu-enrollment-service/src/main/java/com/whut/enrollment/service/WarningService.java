package com.whut.enrollment.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.whut.common.auth.AuthContext;
import com.whut.common.auth.AuthUser;
import com.whut.common.exception.BusinessException;
import com.whut.enrollment.entity.AcademicWarning;
import com.whut.enrollment.mapper.AcademicWarningMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class WarningService {

    private final AcademicWarningMapper warningMapper;

    public WarningService(AcademicWarningMapper warningMapper) {
        this.warningMapper = warningMapper;
    }

    /**
     * 根据最终成绩自动创建或更新预警。
     * finalScore < 60 时触发，否则关闭已有预警。
     */
    @Transactional
    public void evaluateAndCreateWarning(Long studentId, Long courseId, Long enrollmentId, BigDecimal finalScore) {
        if (finalScore == null) {
            return;
        }

        // 分数 < 60 → 创建/更新预警
        if (finalScore.compareTo(BigDecimal.valueOf(60)) < 0) {
            // 检查是否已有未处理的预警
            int existing = warningMapper.countActiveByStudentAndCourse(studentId, courseId, 2);
            if (existing > 0) {
                // 已有预警，更新成绩
                AcademicWarning existingWarn = warningMapper.selectOne(
                        new LambdaQueryWrapper<AcademicWarning>()
                                .eq(AcademicWarning::getStudentId, studentId)
                                .eq(AcademicWarning::getCourseId, courseId)
                                .eq(AcademicWarning::getWarningType, 2)
                                .lt(AcademicWarning::getStatus, 2)
                                .orderByDesc(AcademicWarning::getCreatedAt)
                                .last("LIMIT 1"));
                if (existingWarn != null) {
                    existingWarn.setCurrentScore(finalScore);
                    existingWarn.setSeverity(determineSeverity(finalScore));
                    existingWarn.setDescription(
                            String.format("课程最终成绩 %.1f 分，未达到 60 分及格线。", finalScore));
                    warningMapper.updateById(existingWarn);
                    return;
                }
            }

            // 创建新预警
            AcademicWarning warning = new AcademicWarning();
            warning.setStudentId(studentId);
            warning.setCourseId(courseId);
            warning.setEnrollmentId(enrollmentId);
            warning.setWarningType(2); // 期末预警
            warning.setSeverity(determineSeverity(finalScore));
            warning.setCurrentScore(finalScore);
            warning.setThresholdScore(BigDecimal.valueOf(60));
            warning.setDescription(
                    String.format("课程最终成绩 %.1f 分，未达到 60 分及格线。", finalScore));
            warning.setStatus(0); // 未处理
            warningMapper.insert(warning);
        } else {
            // 分数 >= 60，关闭该课程未处理的预警
            List<AcademicWarning> activeWarnings = warningMapper.selectList(
                    new LambdaQueryWrapper<AcademicWarning>()
                            .eq(AcademicWarning::getStudentId, studentId)
                            .eq(AcademicWarning::getCourseId, courseId)
                            .lt(AcademicWarning::getStatus, 2));
            for (AcademicWarning w : activeWarnings) {
                w.setStatus(2); // 已处理
                w.setProcessComment("成绩已达标，自动关闭");
                warningMapper.updateById(w);
            }
        }
    }

    /** 获取当前学生的预警列表 */
    public List<AcademicWarningMapper.WarningRow> getMyWarnings() {
        AuthUser user = currentUser();
        return warningMapper.findByStudentId(user.getId());
    }

    /** 获取当前学生的活跃预警（未处理 + 已通知） */
    public List<AcademicWarningMapper.WarningRow> getMyActiveWarnings() {
        AuthUser user = currentUser();
        return warningMapper.findActiveByStudentId(user.getId());
    }

    /** 获取活跃预警数量 */
    public int getMyActiveWarningCount() {
        AuthUser user = currentUser();
        Long cnt = warningMapper.selectCount(
                new LambdaQueryWrapper<AcademicWarning>()
                        .eq(AcademicWarning::getStudentId, user.getId())
                        .lt(AcademicWarning::getStatus, 2));
        return cnt != null ? cnt.intValue() : 0;
    }

    /** 根据分数决定严重程度 */
    private int determineSeverity(BigDecimal score) {
        if (score == null) return 1;
        double val = score.doubleValue();
        if (val < 30) return 3;  // 非常严重
        if (val < 45) return 2;  // 严重
        return 1;                 // 一般
    }

    private AuthUser currentUser() {
        AuthUser user = AuthContext.get();
        if (user == null) {
            throw BusinessException.unauthorized("请先登录");
        }
        return user;
    }
}
