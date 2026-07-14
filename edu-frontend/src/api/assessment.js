import request from '../utils/request'

export function listAssignments(courseId) {
  return request.get(`/assessments/assignments/course/${courseId}`)
}

export function createAssignment(data) {
  return request.post('/assessments/assignments', data)
}

export function updateAssignment(id, data) {
  return request.put(`/assessments/assignments/${id}`, data)
}

export function updateAssignmentStatus(id, status) {
  return request.put(`/assessments/assignments/${id}/status`, { status })
}

export function deleteAssignment(id) {
  return request.delete(`/assessments/assignments/${id}`)
}

export function submitAssignment(assignmentId, data) {
  return request.post(`/assessments/assignments/${assignmentId}/submissions`, data)
}

export function listMySubmissions() {
  return request.get('/assessments/submissions/my')
}

export function listAssignmentSubmissions(assignmentId) {
  return request.get(`/assessments/assignments/${assignmentId}/submissions`)
}

export function gradeSubmission(id, data) {
  return request.put(`/assessments/submissions/${id}/grade`, data)
}

export function listExams(courseId) {
  return request.get(`/assessments/exams/course/${courseId}`)
}

export function createExam(data) {
  return request.post('/assessments/exams', data)
}

export function listMyExamAttempts() {
  return request.get('/assessments/exam-attempts/my')
}
