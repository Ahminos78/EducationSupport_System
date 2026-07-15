import { ref, computed } from 'vue'
import { useAuthStore } from '../../../stores/auth'
import { listMyEnrollments, listCourseEnrollments } from '../../../api/enrollment'
import { listCourses } from '../../../api/course'
import { listAssignments, listExams, listAssignmentSubmissions } from '../../../api/assessment'

const STORAGE_KEY = 'edu_notifications_read'

const notifications = ref([])
const readIds = ref(new Set())
const loading = ref(false)

const unreadCount = computed(() => notifications.value.filter((n) => !readIds.value.has(n.id)).length)

const visibleNotifications = computed(() => {
  return notifications.value
    .filter((n) => !readIds.value.has(n.id))
    .sort((a, b) => {
      const aDeadline = a.sortDeadline || Infinity
      const bDeadline = b.sortDeadline || Infinity
      if (aDeadline !== bDeadline) return aDeadline - bDeadline
      const aTime = a.sortTime || 0
      const bTime = b.sortTime || 0
      return bTime - aTime
    })
})

function loadReadIds() {
  try {
    const saved = JSON.parse(localStorage.getItem(STORAGE_KEY) || '[]')
    readIds.value = new Set(saved)
  } catch {
    readIds.value = new Set()
  }
}

function saveReadIds() {
  localStorage.setItem(STORAGE_KEY, JSON.stringify([...readIds.value]))
}

function markAsRead(id) {
  readIds.value.add(id)
  saveReadIds()
}

function markAllRead() {
  notifications.value.forEach((n) => readIds.value.add(n.id))
  saveReadIds()
}

function formatTime(dateStr) {
  if (!dateStr) return ''
  const d = new Date(dateStr)
  const pad = (n) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`
}

function daysDiff(dateStr) {
  if (!dateStr) return Infinity
  const now = new Date()
  const target = new Date(dateStr)
  return (target - now) / (1000 * 60 * 60 * 24)
}

async function generateStudentNotifications() {
  const newNotifs = []
  const approved = (await listMyEnrollments() || []).filter((e) => e.status === 1)
  if (!approved.length) return newNotifs

  const courseIds = [...new Set(approved.map((e) => e.courseId))]

  const [assignmentLists, examLists] = await Promise.all([
    Promise.all(courseIds.map((id) => listAssignments(id).catch(() => []))),
    Promise.all(courseIds.map((id) => listExams(id).catch(() => []))),
  ])

  const courseNames = new Map(approved.map((e) => [e.courseId, e.courseName]))

  assignmentLists.flat().forEach((a) => {
    const name = a.courseName || courseNames.get(a.courseId) || '未知课程'
    if (daysDiff(a.createdAt) <= 3 && daysDiff(a.createdAt) >= -3) {
      newNotifs.push({
        id: `assignment_new_${a.id}`,
        title: `${name}发布了新作业：${a.title}`,
        time: formatTime(a.publishedAt || a.createdAt),
        sortDeadline: Infinity,
        sortTime: new Date(a.publishedAt || a.createdAt).getTime(),
      })
    }
    if (daysDiff(a.deadline) > 0 && daysDiff(a.deadline) <= 3) {
      newNotifs.push({
        id: `assignment_deadline_${a.id}`,
        title: `${name}「${a.title}」即将截止`,
        time: formatTime(a.deadline),
        sortDeadline: new Date(a.deadline).getTime(),
        sortTime: new Date(a.deadline).getTime(),
      })
    }
  })

  examLists.flat().forEach((e) => {
    const name = e.courseName || courseNames.get(e.courseId) || '未知课程'
    if (daysDiff(e.createdAt) <= 3 && daysDiff(e.createdAt) >= -3) {
      newNotifs.push({
        id: `exam_new_${e.id}`,
        title: `${name}发布了新考试：${e.title}`,
        time: formatTime(e.createdAt),
        sortDeadline: e.startTime ? new Date(e.startTime).getTime() : Infinity,
        sortTime: new Date(e.createdAt).getTime(),
      })
    }
  })

  return newNotifs
}

async function generateTeacherNotifications() {
  const newNotifs = []
  const authStore = useAuthStore()
  const user = authStore.user
  if (!user) return newNotifs

  const courses = await listCourses({ page: 1, size: 100, teacherId: user.id }).catch(() => [])
  const courseList = Array.isArray(courses) ? courses : []
  if (!courseList.length) return newNotifs

  const [enrollmentLists, assignmentListsPerCourse] = await Promise.all([
    Promise.all(courseList.map((c) => listCourseEnrollments(c.id).catch(() => []))),
    Promise.all(courseList.map((c) => listAssignments(c.id).catch(() => []))),
  ])

  enrollmentLists.flat().filter((e) => e.status === 0).forEach((e) => {
    newNotifs.push({
      id: `enrollment_pending_${e.id}`,
      title: `${e.courseName}有新的选课申请：${e.studentName}`,
      time: formatTime(e.appliedAt),
      sortDeadline: Infinity,
      sortTime: new Date(e.appliedAt).getTime(),
    })
  })

  for (let i = 0; i < courseList.length; i++) {
    const assignments = assignmentListsPerCourse[i] || []
    for (const a of assignments) {
      const submissions = await listAssignmentSubmissions(a.id).catch(() => [])
      const ungraded = (submissions || []).filter((s) => s.gradingStatus === 0)
      if (ungraded.length > 0) {
        newNotifs.push({
          id: `submission_ungraded_${a.id}`,
          title: `${a.courseName}「${a.title}」有${ungraded.length}份待批改作业`,
          time: formatTime(ungraded[0].submittedAt),
          sortDeadline: a.deadline ? new Date(a.deadline).getTime() : Infinity,
          sortTime: new Date(ungraded[0].submittedAt).getTime(),
        })
      }
    }
  }

  return newNotifs
}

async function refreshNotifications() {
  const authStore = useAuthStore()
  const user = authStore.user
  if (!user) return

  loading.value = true
  try {
    const role = user.role
    let newNotifs = []
    if (role === 1) {
      newNotifs = await generateStudentNotifications()
    } else if (role === 2) {
      newNotifs = await generateTeacherNotifications()
    }
    notifications.value = newNotifs
  } catch {
    notifications.value = []
  } finally {
    loading.value = false
  }
}

loadReadIds()

export function useNotifications() {
  return {
    notifications,
    visibleNotifications,
    readIds,
    unreadCount,
    loading,
    markAsRead,
    markAllRead,
    refreshNotifications,
  }
}
