import request from '../utils/request'

export function listCourses(params) {
  return request.get('/courses/page', { params })
}

export function getCourse(id) {
  return request.get(`/courses/${id}`)
}

export function createCourse(data) {
  return request.post('/courses', data)
}

export function updateCourse(id, data) {
  return request.put(`/courses/${id}`, data)
}

export function updateCourseStatus(id, status) {
  return request.put(`/courses/${id}/status`, { status })
}

export function deleteCourse(id) {
  return request.delete(`/courses/${id}`)
}

export function countCourses() {
  return request.get('/courses/count')
}
