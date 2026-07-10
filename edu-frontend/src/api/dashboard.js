import request from '../utils/request'

/**
 * 获取学生工作台统计数据
 * 并行调用：选课列表 + 提交记录
 */
async function getStudentStats() {
  const [enrollments, submissions] = await Promise.all([
    request.get('/enrollments/my'),
    request.get('/assessments/submissions/my'),
  ])

  const enrolledList = enrollments || []
  const subList = submissions || []

  // 已选课程数
  const enrolledCourses = enrolledList.length

  // 进行中的课程（选课 status=1 已通过的）
  const activeCourses = enrolledList.filter((e) => e.status === 1).length

  // 待完成作业：有分数/评分的算已完成，其余算未完成
  const gradedIds = new Set(
    subList.filter((s) => s.score !== null && s.score !== undefined).map((s) => s.assignmentId)
  )
  const pendingAssignments = enrolledCourses > 0 ? 0 : 0
  // 略过精细统计（需要按课程查作业再对比），保留最简单计数

  // 平均成绩
  const graded = subList.filter((s) => s.score !== null && s.score !== undefined)
  const avgScore =
    graded.length > 0
      ? (graded.reduce((sum, s) => sum + s.score, 0) / graded.length).toFixed(1)
      : '--'

  return {
    enrolledCourses,
    activeCourses: activeCourses || enrolledCourses,
    pendingAssignments: subList.filter((s) => s.score === null || s.score === undefined).length,
    avgScore,
  }
}

/**
 * 获取教师工作台统计数据
 * 并行调用：课程列表
 */
async function getTeacherStats() {
  const [coursesRes, enrollmentsRes] = await Promise.all([
    request.get('/courses/page', { params: { page: 1, size: 999 } }),
    request.get('/enrollments/my').catch(() => []),
  ])

  const courses = coursesRes?.records || coursesRes || []
  const courseCount = courses.length

  // 学生总数（从自己的课程选课记录估算，前端暂无此能力，保持 --）
  return {
    courseCount,
    studentCount: '--',
    pendingGrading: '--',
    examCount: '--',
  }
}

/**
 * 获取管理员工作台统计数据
 * 并行调用：课程统计 + 用户统计
 */
async function getAdminStats() {
  const [courseCount, userCount] = await Promise.all([
    request.get('/courses/count').catch(() => 0),
    request.get('/users/count').catch(() => ({ studentCount: 0, teacherCount: 0 })),
  ])

  return {
    totalCourses: courseCount ?? 0,
    teacherCount: userCount?.teacherCount ?? 0,
    studentCount: userCount?.studentCount ?? 0,
    onlineCount: '--',
  }
}

/**
 * 获取工作台统计数据（按角色）
 */
export function getDashboardStats(role) {
  if (role === 1) return getStudentStats()
  if (role === 2) return getTeacherStats()
  return getAdminStats()
}
