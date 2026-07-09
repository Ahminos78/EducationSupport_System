import request from '../utils/request'

export function listTopics(courseId, params) {
  return request.get(`/interactions/courses/${courseId}/topics`, { params })
}

export function getTopic(topicId) {
  return request.get(`/interactions/topics/${topicId}`)
}

export function listReplies(topicId, params) {
  return request.get(`/interactions/topics/${topicId}/replies`, { params })
}

export function createTopic(data) {
  return request.post('/interactions/topics', data)
}

export function createReply(topicId, data) {
  return request.post(`/interactions/topics/${topicId}/replies`, data)
}

export function updateDiscussion(id, data) {
  return request.put(`/interactions/${id}`, data)
}

export function updateDiscussionStatus(id, status) {
  return request.put(`/interactions/${id}/status`, { status })
}

export function deleteDiscussion(id) {
  return request.delete(`/interactions/${id}`)
}
