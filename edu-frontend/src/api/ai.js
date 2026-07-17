import request from '@/utils/request'

/**
 * AI 对话相关 API
 */

/** 发送对话消息 */
export function chatSend(data) {
  return request({
    url: '/ai/chat',
    method: 'post',
    data,
  })
}

/** 清除会话历史 */
export function clearSession(sessionId) {
  return request({
    url: '/ai/chat/session',
    method: 'delete',
    params: { sessionId },
  })
}

/** AI 服务健康检查 */
export function getAiHealth() {
  return request({
    url: '/ai/health',
    method: 'get',
  })
}

/** 构建知识库演示 */
export function buildKnowledgeDemo() {
  return request({
    url: '/ai/knowledge/demo',
    method: 'post',
  })
}

/** 知识库检索 */
export function ragSearch(data) {
  return request({
    url: '/ai/rag/search',
    method: 'post',
    data,
  })
}
