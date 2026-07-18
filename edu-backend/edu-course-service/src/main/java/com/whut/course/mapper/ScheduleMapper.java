package com.whut.course.mapper;

import com.whut.course.vo.ScheduleResponse;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ScheduleMapper {

    @Select("""
            SELECT
                c.id AS courseId,
                c.name AS courseName,
                c.code AS courseCode,
                c.credit,
                c.category,
                c.description,
                coalesce(nullif(teacher.nickname, ''), teacher.username) AS teacherName,
                teacher.id AS teacherId,
                cc.id AS classId,
                cc.name AS className,
                s.location,
                s.day_of_week AS dayOfWeek,
                s.start_period AS startPeriod,
                s.end_period AS endPeriod,
                s.start_week AS startWeek,
                s.end_week AS endWeek,
                s.week_type AS weekType
            FROM tb_enrollment e
            JOIN tb_course_class cc ON cc.id = e.class_id
            JOIN tb_course c ON c.id = e.course_id
            JOIN tb_course_schedule s ON s.class_id = cc.id
            LEFT JOIN tb_user teacher ON teacher.id = cc.teacher_id
            WHERE e.student_id = #{studentId}
              AND e.status = 1
              AND c.deleted = 0
              AND s.start_week <= #{week}
              AND s.end_week >= #{week}
            ORDER BY s.day_of_week, s.start_period
            """)
    List<ScheduleResponse> findMySchedule(@Param("studentId") Long studentId, @Param("week") int week);

    @Select("""
            SELECT
                c.id AS courseId,
                c.name AS courseName,
                c.code AS courseCode,
                c.credit,
                c.category,
                c.description,
                coalesce(nullif(teacher.nickname, ''), teacher.username) AS teacherName,
                teacher.id AS teacherId,
                cc.id AS classId,
                cc.name AS className,
                s.location,
                s.day_of_week AS dayOfWeek,
                s.start_period AS startPeriod,
                s.end_period AS endPeriod,
                s.start_week AS startWeek,
                s.end_week AS endWeek,
                s.week_type AS weekType
            FROM tb_enrollment e
            JOIN tb_course_class cc ON cc.id = e.class_id
            JOIN tb_course c ON c.id = e.course_id
            JOIN tb_course_schedule s ON s.class_id = cc.id
            LEFT JOIN tb_user teacher ON teacher.id = cc.teacher_id
            WHERE e.student_id = #{studentId}
              AND e.status = 1
              AND c.deleted = 0
            ORDER BY s.day_of_week, s.start_period
            """)
    List<ScheduleResponse> findMyScheduleAllWeeks(@Param("studentId") Long studentId);

    @Select("""
            SELECT MAX(s.end_week) AS maxWeek
            FROM tb_enrollment e
            JOIN tb_course_class cc ON cc.id = e.class_id
            JOIN tb_course_schedule s ON s.class_id = cc.id
            WHERE e.student_id = #{studentId}
              AND e.status = 1
            """)
    Integer findMaxWeek(@Param("studentId") Long studentId);
}
