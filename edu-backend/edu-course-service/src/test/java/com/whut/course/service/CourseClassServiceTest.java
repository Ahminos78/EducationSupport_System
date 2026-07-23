package com.whut.course.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.whut.common.exception.BusinessException;
import com.whut.course.dto.ScheduleSlotRequest;
import com.whut.course.entity.Course;
import com.whut.course.entity.CourseClass;
import com.whut.course.entity.CourseSchedule;
import com.whut.course.mapper.CourseClassMapper;
import com.whut.course.mapper.CourseMapper;
import com.whut.course.mapper.CourseScheduleMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CourseClassServiceTest {

    @Mock private CourseClassMapper courseClassMapper;
    @Mock private CourseScheduleMapper courseScheduleMapper;
    @Mock private CourseMapper courseMapper;

    private CourseClassService courseClassService;

    @BeforeEach
    void setUp() {
        courseClassService = new CourseClassService(courseClassMapper, courseScheduleMapper, courseMapper);
    }

    @Test
    void createClassSection_shouldCreateClassAndSchedule() {
        when(courseClassMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
        Course course = new Course();
        course.setId(10L);
        course.setName("数学");
        when(courseMapper.selectById(10L)).thenReturn(course);
        when(courseMapper.updateClassCount(10L)).thenReturn(1);

        ScheduleSlotRequest slot = new ScheduleSlotRequest();
        slot.setDayOfWeek(1);
        slot.setStartPeriod(1);
        slot.setEndPeriod(2);
        slot.setLocation("教学楼101");
        when(courseScheduleMapper.countConflicts(1, 1, 2, "教学楼101", 1L)).thenReturn(0);

        CourseClass result = courseClassService.createClassSection(10L, 1L, 50, List.of(slot));

        assertThat(result).isNotNull();
        assertThat(result.getCourseId()).isEqualTo(10L);
        assertThat(result.getTeacherId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("数学 第1班");
        assertThat(result.getMaxStudents()).isEqualTo(50);
        assertThat(result.getEnrolledCount()).isZero();

        verify(courseClassMapper).insert(any(CourseClass.class));
        verify(courseScheduleMapper).insert(any(CourseSchedule.class));
        verify(courseMapper).updateClassCount(10L);
    }

    @Test
    void createClassSection_withoutSlots_shouldCreateClassOnly() {
        when(courseClassMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(1L);
        Course course = new Course();
        course.setId(20L);
        course.setName("英语");
        when(courseMapper.selectById(20L)).thenReturn(course);
        when(courseMapper.updateClassCount(20L)).thenReturn(1);

        CourseClass result = courseClassService.createClassSection(20L, 1L, 60, null);

        assertThat(result.getName()).isEqualTo("英语 第2班");
        verify(courseScheduleMapper, never()).insert(any());
    }

    @Test
    void createScheduleSlots_incompleteData_shouldThrow() {
        ScheduleSlotRequest slot = new ScheduleSlotRequest();
        // dayOfWeek missing
        List<ScheduleSlotRequest> slots = List.of(slot);

        assertThatThrownBy(() -> courseClassService.createScheduleSlots(1L, 1L, slots))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("排课信息不完整");
    }

    @Test
    void createScheduleSlots_startAfterEnd_shouldThrow() {
        ScheduleSlotRequest slot = new ScheduleSlotRequest();
        slot.setDayOfWeek(1);
        slot.setStartPeriod(3);
        slot.setEndPeriod(1);
        slot.setLocation("教学楼101");
        List<ScheduleSlotRequest> slots = List.of(slot);

        assertThatThrownBy(() -> courseClassService.createScheduleSlots(1L, 1L, slots))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("开始节次不能大于结束节次");
    }

    @Test
    void createScheduleSlots_noLocation_shouldThrow() {
        ScheduleSlotRequest slot = new ScheduleSlotRequest();
        slot.setDayOfWeek(1);
        slot.setStartPeriod(1);
        slot.setEndPeriod(2);
        // location missing
        List<ScheduleSlotRequest> slots = List.of(slot);

        assertThatThrownBy(() -> courseClassService.createScheduleSlots(1L, 1L, slots))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("上课地点不能为空");
    }

    @Test
    void createScheduleSlots_conflict_shouldThrow() {
        ScheduleSlotRequest slot = new ScheduleSlotRequest();
        slot.setDayOfWeek(1);
        slot.setStartPeriod(1);
        slot.setEndPeriod(2);
        slot.setLocation("教学楼101");
        List<ScheduleSlotRequest> slots = List.of(slot);
        when(courseScheduleMapper.countConflicts(1, 1, 2, "教学楼101", 1L)).thenReturn(1);

        assertThatThrownBy(() -> courseClassService.createScheduleSlots(1L, 1L, slots))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("排课冲突");
    }

    @Test
    void createScheduleSlots_shouldSucceed() {
        ScheduleSlotRequest slot = new ScheduleSlotRequest();
        slot.setDayOfWeek(3);
        slot.setStartPeriod(5);
        slot.setEndPeriod(6);
        slot.setLocation("实验楼302");
        List<ScheduleSlotRequest> slots = List.of(slot);
        when(courseScheduleMapper.countConflicts(3, 5, 6, "实验楼302", 2L)).thenReturn(0);

        courseClassService.createScheduleSlots(1L, 2L, slots);

        ArgumentCaptor<CourseSchedule> captor = ArgumentCaptor.forClass(CourseSchedule.class);
        verify(courseScheduleMapper).insert(captor.capture());
        CourseSchedule cs = captor.getValue();
        assertThat(cs.getClassId()).isEqualTo(1L);
        assertThat(cs.getDayOfWeek()).isEqualTo(3);
        assertThat(cs.getStartPeriod()).isEqualTo(5);
        assertThat(cs.getEndPeriod()).isEqualTo(6);
        assertThat(cs.getStartWeek()).isEqualTo(1);
        assertThat(cs.getEndWeek()).isEqualTo(16);
        assertThat(cs.getWeekType()).isZero();
        assertThat(cs.getLocation()).isEqualTo("实验楼302");
    }

    @Test
    void deleteClassSection_notFound_shouldThrow() {
        when(courseClassMapper.selectById(99L)).thenReturn(null);

        assertThatThrownBy(() -> courseClassService.deleteClassSection(99L, 1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("教学班不存在");
    }

    @Test
    void deleteClassSection_notOwnTeacher_shouldThrow() {
        CourseClass cc = new CourseClass();
        cc.setId(1L);
        cc.setTeacherId(2L);
        cc.setDeleted(0);
        when(courseClassMapper.selectById(1L)).thenReturn(cc);

        assertThatThrownBy(() -> courseClassService.deleteClassSection(1L, 1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("只能删除自己的教学班");
    }

    @Test
    void deleteClassSection_hasEnrolledStudents_shouldThrow() {
        CourseClass cc = new CourseClass();
        cc.setId(1L);
        cc.setTeacherId(1L);
        cc.setEnrolledCount(5);
        cc.setCourseId(10L);
        when(courseClassMapper.selectById(1L)).thenReturn(cc);

        assertThatThrownBy(() -> courseClassService.deleteClassSection(1L, 1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("已有学生");
    }

    @Test
    void deleteClassSection_shouldSucceed() {
        CourseClass cc = new CourseClass();
        cc.setId(1L);
        cc.setTeacherId(1L);
        cc.setEnrolledCount(0);
        cc.setCourseId(10L);
        when(courseClassMapper.selectById(1L)).thenReturn(cc);
        when(courseMapper.updateClassCount(10L)).thenReturn(1);

        courseClassService.deleteClassSection(1L, 1L);

        verify(courseClassMapper).deleteById(1L);
        verify(courseMapper).updateClassCount(10L);
    }

    @Test
    void getClassesByCourse_shouldReturnListWithSchedule() {
        CourseClassMapper.CourseClassResponse clsRow = mock(CourseClassMapper.CourseClassResponse.class);
        when(clsRow.getId()).thenReturn(1L);
        when(clsRow.getCourseId()).thenReturn(10L);
        when(clsRow.getTeacherId()).thenReturn(1L);
        when(clsRow.getTeacherName()).thenReturn("张老师");
        when(clsRow.getName()).thenReturn("数学 第1班");
        when(clsRow.getMaxStudents()).thenReturn(50);
        when(clsRow.getEnrolledCount()).thenReturn(20);
        when(courseClassMapper.findByCourseId(10L)).thenReturn(List.of(clsRow));

        CourseScheduleMapper.ScheduleItem sRow = mock(CourseScheduleMapper.ScheduleItem.class);
        when(sRow.getId()).thenReturn(100L);
        when(sRow.getClassId()).thenReturn(1L);
        when(sRow.getDayOfWeek()).thenReturn(1);
        when(sRow.getStartPeriod()).thenReturn(1);
        when(sRow.getEndPeriod()).thenReturn(2);
        when(sRow.getStartWeek()).thenReturn(1);
        when(sRow.getEndWeek()).thenReturn(16);
        when(sRow.getWeekType()).thenReturn(0);
        when(sRow.getLocation()).thenReturn("教学楼101");
        when(courseScheduleMapper.findByClassId(1L)).thenReturn(List.of(sRow));

        List<CourseClassService.CourseClassWithSchedule> result = courseClassService.getClassesByCourse(10L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("数学 第1班");
        assertThat(result.get(0).getSchedule()).hasSize(1);
        assertThat(result.get(0).getSchedule().get(0).getLocation()).isEqualTo("教学楼101");
    }
}
