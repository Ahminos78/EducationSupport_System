import request from '../utils/request'

export function getMyWarnings() {
  return request.get('/warnings/my')
}

export function getMyActiveWarnings() {
  return request.get('/warnings/my/active')
}

export function getMyActiveWarningCount() {
  return request.get('/warnings/my/count')
}
