import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { listCourses } from '../../../api/course'
import { listMyEnrollments } from '../../../api/enrollment'

export function useScheduleData() {
  const enrollments = ref([])
  const courseMap = ref(new Map())
  const loading = ref(false)

  async function loadBaseData() {
    loading.value = true
    try {
      const [enrollmentList, courseList] = await Promise.all([
        listMyEnrollments(),
        listCourses({ page: 1, size: 100 }),
      ])
      const approved = (enrollmentList || []).filter(e => e.status === 1)
      enrollments.value = approved
      courseMap.value = new Map(
        (courseList || []).filter(c => approved.some(e => e.courseId === c.id)).map(c => [c.id, c])
      )
      return approved
    } catch (error) {
      ElMessage.error(error.message || '数据加载失败')
      return []
    } finally {
      loading.value = false
    }
  }

  return { enrollments, courseMap, loading, loadBaseData }
}
