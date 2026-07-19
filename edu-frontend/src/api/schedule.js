import request from '../utils/request'

export function getMySchedule(week) {
  return request.get('/courses/schedule/my', { params: { week } })
}

export function getMyScheduleAllWeeks() {
  return request.get('/courses/schedule/my/all')
}

export function getMaxWeek() {
  return request.get('/courses/schedule/max-week')
}

export function getTodaySchedule() {
  return request.get('/courses/schedule/today')
}
