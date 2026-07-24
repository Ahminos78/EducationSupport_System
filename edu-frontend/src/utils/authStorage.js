export const TOKEN_KEY = 'edu_token'
export const USER_KEY = 'edu_user'

export function getStoredToken() {
  return sessionStorage.getItem(TOKEN_KEY) || ''
}

export function setStoredToken(token) {
  sessionStorage.setItem(TOKEN_KEY, token)
}

export function getStoredUser() {
  const stored = sessionStorage.getItem(USER_KEY)
  if (!stored) {
    return null
  }
  try {
    return JSON.parse(stored)
  } catch {
    sessionStorage.removeItem(USER_KEY)
    return null
  }
}

export function setStoredUser(user) {
  sessionStorage.setItem(USER_KEY, JSON.stringify(user))
}

export function clearStoredAuth() {
  sessionStorage.removeItem(TOKEN_KEY)
  sessionStorage.removeItem(USER_KEY)
}
