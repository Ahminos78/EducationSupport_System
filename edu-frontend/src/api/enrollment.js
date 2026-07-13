import request from '../utils/request'

export function applyEnrollment(data) {
  return request.post('/enrollments', data)
}

export function listMyEnrollments() {
  return request.get('/enrollments/my')
}

export function listCourseEnrollments(courseId, params) {
  return request.get(`/enrollments/course/${courseId}`, { params })
}

export function approveEnrollment(id, data = {}) {
  return request.put(`/enrollments/${id}/approve`, data)
}

export function rejectEnrollment(id, data = {}) {
  return request.put(`/enrollments/${id}/reject`, data)
}

export function dropEnrollment(id) {
  return request.put(`/enrollments/${id}/drop`)
}

export function removeEnrollment(id) {
  return request.put(`/enrollments/${id}/remove`)
}
