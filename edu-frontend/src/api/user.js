import request from '../utils/request'

export function loginApi(data) {
  return request.post('/users/login', data)
}

export function registerUser(data) {
  return request.post('/users/register', data)
}

export function currentUserApi() {
  return request.get('/users/me')
}

// 返回格式: { records: [...], total, current, pages }
export function listUsers(params) {
  return request.get('/users/page', { params })
}

export function createUser(data) {
  return request.post('/users', data)
}

export function updateUser(id, data) {
  return request.put(`/users/${id}`, data)
}

export function deleteUser(id) {
  return request.delete(`/users/${id}`)
}

export function countUsers() {
  return request.get('/users/count')
}

export function listQuickLoginAccounts() {
  return request.get('/users/quick-login')
}

export function updateMyProfile(data) {
  return request.put('/users/me/profile', data)
}

export function changeMyPassword(data) {
  return request.put('/users/me/password', data)
}

export function uploadMyAvatar(data) {
  return request.post('/users/me/avatar', data)
}

export function forgotPasswordVerify(data) {
  return request.post('/users/forgot-password/verify', data)
}

export function forgotPasswordReset(data) {
  return request.post('/users/forgot-password/reset', data)
}
