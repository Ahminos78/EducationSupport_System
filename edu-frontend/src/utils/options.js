export const ROLE_OPTIONS = [
  { label: '学生', value: 1 },
  { label: '教师', value: 2 },
  { label: '管理员', value: 3 },
]

export const COURSE_STATUS_OPTIONS = [
  { label: '下架', value: 0 },
  { label: '正常', value: 1 },
]

export const ENROLLMENT_STATUS_OPTIONS = [
  { label: '待审核', value: 0 },
  { label: '已选课', value: 1 },
  { label: '已退选', value: 2 },
  { label: '审核不通过', value: 4 },
]

export const DISCUSSION_STATUS_OPTIONS = [
  { label: '隐藏', value: 0 },
  { label: '正常', value: 1 },
]

export const ASSIGNMENT_STATUS_OPTIONS = [
  { label: '草稿', value: 0 },
  { label: '已发布', value: 1 },
  { label: '已截止', value: 2 },
]

export function roleLabel(value) {
  return ROLE_OPTIONS.find((item) => item.value === value)?.label || '未知'
}

export function courseStatusLabel(value) {
  return COURSE_STATUS_OPTIONS.find((item) => item.value === value)?.label || '未知'
}

export function enrollmentStatusLabel(value) {
  return ENROLLMENT_STATUS_OPTIONS.find((item) => item.value === value)?.label || '未知'
}

export function discussionStatusLabel(value) {
  return DISCUSSION_STATUS_OPTIONS.find((item) => item.value === value)?.label || '未知'
}

export function assignmentStatusLabel(value) {
  return ASSIGNMENT_STATUS_OPTIONS.find((item) => item.value === value)?.label || '未知'
}
