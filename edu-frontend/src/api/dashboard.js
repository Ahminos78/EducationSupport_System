import request from '../utils/request'

/**
 * 获取学生工作台统计数据
 * 接口：从选课服务获取我的选课，从作业服务获取我的提交，从课程服务搜索
 */
export function getStudentStats() {
  return request.get('/enrollments/my').then((enrollments) => {
    const total = enrollments?.length || 0
    // 后端暂无"进行中的课程"状态过滤，暂用总数
    return { enrolledCourses: total, activeCourses: total }
  })
}

/**
 * 获取我的待完成作业数（学生视角）
 */
export function getMySubmissions() {
  return request.get('/assessments/submissions/my').then((submissions) => {
    return { submitted: submissions?.length || 0 }
  })
}

/**
 * 获取教师工作台统计数据
 */
export function getTeacherStats(teacherId) {
  return request.get('/courses/page', { params: { page: 1, size: 999, teacherId } }).then((courses) => {
    const list = courses?.records || courses || []
    const count = list.length
    // 从课程列表简单统计，真实场景需要后端聚合
    return { courseCount: count }
  })
}

/**
 * 获取工作台统计数据（按角色聚合）
 */
export function getDashboardStats(role) {
  if (role === 1) {
    // 学生：并行调用选课和提交记录
    return Promise.all([
      request.get('/enrollments/my'),
      request.get('/assessments/submissions/my'),
    ]).then(([enrollments, submissions]) => {
      const enrolledList = enrollments || []
      const subList = submissions || []
      const pendingSubs = subList.filter((s) => s.score === null || s.score === undefined)
      return {
        enrolledCourses: enrolledList.length,
        activeCourses: enrolledList.filter((e) => e.status === 1).length || enrolledList.length,
        pendingAssignments: pendingSubs.length,
        avgScore: 0, // 后端暂无计算接口，暂为 0
      }
    })
  }

  if (role === 2) {
    // 教师：获取课程列表
    return request.get('/courses/page', { params: { page: 1, size: 999 } }).then((res) => {
      const list = res?.records || res || []
      return {
        courseCount: list.length,
        studentCount: 0, // 暂无接口统计选课学生数
        pendingGrading: 0,
        examCount: 0,
      }
    })
  }

  // 管理员：获取平台概览（暂无专有接口，返回静态）
  return Promise.resolve({
    totalCourses: 0,
    teacherCount: 0,
    studentCount: 0,
    onlineCount: 0,
  })
}
