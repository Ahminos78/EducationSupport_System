export const TOKEN_KEY = 'edu_token'
export const USER_KEY = 'edu_user'

export function getStoredToken() {
  return localStorage.getItem(TOKEN_KEY) || ''
}

export function setStoredToken(token) {
  localStorage.setItem(TOKEN_KEY, token)
}

export function getStoredUser() {
  const stored = localStorage.getItem(USER_KEY)
  if (!stored) {
    return null
  }
  try {
    return JSON.parse(stored)
  } catch {
    localStorage.removeItem(USER_KEY)
    return null
  }
}

export function setStoredUser(user) {
  localStorage.setItem(USER_KEY, JSON.stringify(user))
}

export function clearStoredAuth() {
  localStorage.removeItem(TOKEN_KEY)
  localStorage.removeItem(USER_KEY)
}
