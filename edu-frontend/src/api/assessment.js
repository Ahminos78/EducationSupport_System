import request from '../utils/request'

export function listAssignments(courseId) {
  return request.get(`/assessments/assignments/course/${courseId}`)
}

export function getAssignment(id) {
  return request.get(`/assessments/assignments/${id}`)
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

export function listAssignmentAttachments(assignmentId) {
  return request.get(`/assessments/assignments/${assignmentId}/attachments`)
}

export function listSubmissionAttachments(submissionId) {
  return request.get(`/assessments/submissions/${submissionId}/attachments`)
}

export function uploadSubmissionAttachment(submissionId, file) {
  const formData = new FormData()
  formData.append('file', file)
  return request.post(`/assessments/submissions/${submissionId}/attachments`, formData)
}

export function uploadAssignmentAttachment(assignmentId, file) {
  const formData = new FormData()
  formData.append('file', file)
  return request.post(`/assessments/assignments/${assignmentId}/attachments`, formData)
}

export function downloadAttachment(url) {
  return request.get(url, { responseType: 'blob' })
}

export function getExamDetail(id) {
  return request.get(`/assessments/exams/${id}`)
}

export function listExamQuestions(examId, withAnswers = false) {
  return request.get(`/assessments/exams/${examId}/questions`, { params: { withAnswers } })
}

export function startExamAttempt(examId) {
  return request.post(`/assessments/exams/${examId}/attempts/start`)
}

export function submitExamAttempt(examId, data) {
  return request.put(`/assessments/exams/${examId}/attempts/submit`, data)
}

export function createExamWithQuestions(data) {
  return request.post('/assessments/exams/with-questions', data)
}

export function deleteExam(id) {
  return request.delete(`/assessments/exams/${id}`)
}

export function updateExamWithQuestions(id, data) {
  return request.put(`/assessments/exams/${id}/with-questions`, data)
}

export function autoGenerateQuestions(courseId, count = 10) {
  return request.get(`/assessments/courses/${courseId}/auto-questions`, { params: { count } })
}

export function listExamAttempts(examId) {
  return request.get(`/assessments/exams/${examId}/attempts`)
}

export function getExamAttemptDetail(id) {
  return request.get(`/assessments/exam-attempts/${id}`)
}
