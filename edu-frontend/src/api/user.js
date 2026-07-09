import request from '../utils/request'

export function loginApi(data) {
  return request.post('/users/login', data)
}

export function currentUserApi() {
  return request.get('/users/me')
}
