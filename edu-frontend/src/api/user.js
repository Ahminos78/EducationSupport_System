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
