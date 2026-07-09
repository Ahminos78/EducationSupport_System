# API 接口契约

版本：v1.0  
日期：2026-07-09  
适用范围：第一阶段用户与权限模块

## 1. 通用约定

前端默认通过网关访问后端：

```text
http://localhost:8080/api
```

开发调试时可直连用户服务：

```text
http://localhost:8010/api
```

当前网关只负责统一入口、跨域处理和路由转发，不解析 Token，不做权限判断。
`Authorization` 请求头由网关透传给后端服务，具体认证和角色校验仍由各业务服务完成。

开发阶段网关路由约定：

| 请求前缀 | 目标服务 | 当前状态 |
| --- | --- | --- |
| `/api/users/**` | `edu-user-service` | 已联调，默认直连 `http://127.0.0.1:8010` |
| `/api/courses/**` | `edu-course-service` | 已开发，默认路由 `lb://edu-course-service` |
| `/api/enrollments/**` | `edu-enrollment-service` | 已开发，默认路由 `lb://edu-enrollment-service` |
| `/api/interactions/**` | `edu-interaction-service` | 已开发，默认路由 `lb://edu-interaction-service` |
| `/api/assessments/**` | `edu-assessment-service` | 预留 |
| `/api/ai/**` | `edu-ai-service` | 预留 |

所有接口统一返回：

```json
{
  "code": 200,
  "message": "success",
  "data": {}
}
```

常用错误码：

| code | 含义 |
| ---: | --- |
| 400 | 请求参数错误 |
| 401 | 未登录或 Token 无效 |
| 403 | 无权限 |
| 404 | 资源不存在 |
| 500 | 服务器内部错误 |

## 2. 认证约定

除登录接口和公开用户信息接口外，其余接口都需要 Token。

请求头：

```http
Authorization: Bearer <token>
```

开发阶段 Token 存储：

```text
localStorage
```

角色编码：

| 编码 | 枚举 | 说明 |
| ---: | --- | --- |
| 1 | `STUDENT` | 学生 |
| 2 | `TEACHER` | 教师 |
| 3 | `ADMIN` | 管理员 |

## 3. 用户接口

### 3.1 登录

```http
POST /api/users/login
```

是否需要 Token：否

请求体：

```json
{
  "username": "admin",
  "password": "admin123"
}
```

成功响应：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "token": "jwt-token",
    "user": {
      "id": 1,
      "username": "admin",
      "nickname": "系统管理员",
      "role": 3,
      "createdAt": "2026-07-09T10:00:00"
    }
  }
}
```

### 3.2 获取当前用户

```http
GET /api/users/me
```

是否需要 Token：是

成功响应：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "username": "admin",
    "nickname": "系统管理员",
    "role": 3,
    "createdAt": "2026-07-09T10:00:00"
  }
}
```

### 3.3 查询公开用户信息

```http
GET /api/users/{id}/public
```

是否需要 Token：否

成功响应：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "nickname": "系统管理员",
    "role": 3
  }
}
```

### 3.4 管理员分页查询用户

```http
GET /api/users/page?page=1&size=10
```

是否需要 Token：是  
允许角色：`ADMIN`

成功响应：

```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 1,
      "username": "admin",
      "nickname": "系统管理员",
      "role": 3,
      "createdAt": "2026-07-09T10:00:00"
    }
  ]
}
```

### 3.5 管理员新增用户

```http
POST /api/users
```

是否需要 Token：是  
允许角色：`ADMIN`

请求体：

```json
{
  "username": "student01",
  "password": "123456",
  "nickname": "学生一",
  "role": 1
}
```

成功响应：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 2,
    "username": "student01",
    "nickname": "学生一",
    "role": 1,
    "createdAt": "2026-07-09T10:05:00"
  }
}
```

### 3.6 管理员编辑用户

```http
PUT /api/users/{id}
```

是否需要 Token：是  
允许角色：`ADMIN`

请求体：

```json
{
  "password": "new-password",
  "nickname": "学生一",
  "role": 1
}
```

说明：

- `password` 可为空，为空时不修改密码
- `nickname` 可为空，为空时不修改昵称
- `role` 可为空，为空时不修改角色

### 3.7 管理员删除用户

```http
DELETE /api/users/{id}
```

是否需要 Token：是  
允许角色：`ADMIN`

成功响应：

```json
{
  "code": 200,
  "message": "success",
  "data": null
}
```

## 4. 课程接口

课程状态编码：

| 编码 | 说明 |
| ---: | --- |
| 0 | 下架 |
| 1 | 正常 |

### 4.1 分页查询课程

```http
GET /api/courses/page?page=1&size=10&status=1&teacherId=2
```

是否需要 Token：是  
允许角色：`STUDENT`、`TEACHER`、`ADMIN`

说明：

- 学生固定只能查看正常课程
- 教师固定只能查看自己创建的课程
- 管理员可按 `status`、`teacherId` 查询全部课程

成功响应：

```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 1,
      "teacherId": 2,
      "name": "Java Web 开发",
      "description": "课程简介",
      "coverUrl": "https://example.com/cover.png",
      "maxStudents": 100,
      "enrolledCount": 0,
      "status": 1,
      "createdAt": "2026-07-09T10:00:00"
    }
  ]
}
```

### 4.2 查询课程详情

```http
GET /api/courses/{id}
```

是否需要 Token：是

### 4.3 创建课程

```http
POST /api/courses
```

是否需要 Token：是  
允许角色：`TEACHER`、`ADMIN`

请求体：

```json
{
  "name": "Java Web 开发",
  "description": "课程简介",
  "coverUrl": "https://example.com/cover.png",
  "maxStudents": 100,
  "status": 1
}
```

说明：

- `teacherId` 由当前登录用户决定，前端不传
- `maxStudents` 为空时默认 100
- `status` 为空时默认 1

### 4.4 编辑课程

```http
PUT /api/courses/{id}
```

是否需要 Token：是  
允许角色：`TEACHER`、`ADMIN`

请求体：

```json
{
  "name": "Java Web 开发",
  "description": "新的课程简介",
  "coverUrl": "https://example.com/cover.png",
  "maxStudents": 120
}
```

说明：

- 教师只能编辑自己的课程
- 管理员可以编辑任意课程
- `maxStudents` 不能小于当前已选人数

### 4.5 修改课程状态

```http
PUT /api/courses/{id}/status
```

是否需要 Token：是  
允许角色：`TEACHER`、`ADMIN`

请求体：

```json
{
  "status": 0
}
```

### 4.6 删除课程

```http
DELETE /api/courses/{id}
```

是否需要 Token：是  
允许角色：`TEACHER`、`ADMIN`

说明：

- 使用逻辑删除
- 教师只能删除自己的课程
- 管理员可以删除任意课程

## 5. 选课接口

选课状态编码：

| 编码 | 枚举 | 说明 |
| ---: | --- | --- |
| 0 | `PENDING` | 待审核 |
| 1 | `APPROVED` | 已选课 |
| 2 | `DROPPED` | 已退选 |
| 4 | `REJECTED` | 审核不通过 |

### 5.1 学生申请选课

```http
POST /api/enrollments
```

是否需要 Token：是  
允许角色：`STUDENT`

请求体：

```json
{
  "courseId": 1,
  "applyReason": "希望学习 Java Web"
}
```

说明：

- 默认创建 `PENDING` 状态记录
- 同一学生同一课程只能存在一条非拒绝状态的选课记录

### 5.2 查询我的选课

```http
GET /api/enrollments/my
```

是否需要 Token：是  
允许角色：`STUDENT`

成功响应：

```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 1,
      "courseId": 1,
      "courseName": "Java Web 开发",
      "studentId": 3,
      "status": 0,
      "applyReason": "希望学习 Java Web",
      "reviewComment": null,
      "appliedAt": "2026-07-09T10:00:00",
      "reviewedAt": null
    }
  ]
}
```

### 5.3 查询课程选课记录

```http
GET /api/enrollments/course/{courseId}?status=0
```

是否需要 Token：是  
允许角色：`TEACHER`、`ADMIN`

说明：

- 教师只能查看自己课程的选课记录
- 管理员可以查看任意课程的选课记录

### 5.4 通过选课申请

```http
PUT /api/enrollments/{id}/approve
```

是否需要 Token：是  
允许角色：`TEACHER`、`ADMIN`

请求体：

```json
{
  "reviewComment": "通过"
}
```

说明：

- 只能审核 `PENDING` 状态记录
- 通过后状态变为 `APPROVED`
- 通过后课程 `enrolledCount` 增加 1
- 课程人数已满时不能通过

### 5.5 拒绝选课申请

```http
PUT /api/enrollments/{id}/reject
```

是否需要 Token：是  
允许角色：`TEACHER`、`ADMIN`

请求体：

```json
{
  "reviewComment": "人数已满"
}
```

### 5.6 学生退课

```http
PUT /api/enrollments/{id}/drop
```

是否需要 Token：是  
允许角色：`STUDENT`

说明：

- 学生只能退选自己的课程
- `APPROVED` 状态退课后课程 `enrolledCount` 减少 1
- `PENDING` 状态也允许撤销为 `DROPPED`

## 6. 论坛接口

讨论状态编码：

| 编码 | 说明 |
| ---: | --- |
| 0 | 隐藏 |
| 1 | 正常 |

### 6.1 查询课程主题帖

```http
GET /api/interactions/courses/{courseId}/topics?page=1&size=10
```

是否需要 Token：是  
允许角色：`STUDENT`、`TEACHER`、`ADMIN`

说明：

- 普通用户只能看到正常状态主题帖
- 课程教师和管理员可以看到隐藏主题帖

成功响应：

```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 1,
      "courseId": 1,
      "courseName": "Java Web 开发",
      "parentId": null,
      "authorId": 3,
      "title": "作业提交问题",
      "content": "请问第一次作业什么时候截止？",
      "status": 1,
      "createdAt": "2026-07-09T10:00:00",
      "updatedAt": "2026-07-09T10:00:00"
    }
  ]
}
```

### 6.2 查询主题帖详情

```http
GET /api/interactions/topics/{topicId}
```

是否需要 Token：是  
允许角色：`STUDENT`、`TEACHER`、`ADMIN`

说明：

- `topicId` 必须是主题帖 ID，即 `parentId = null`

### 6.3 查询主题帖回复

```http
GET /api/interactions/topics/{topicId}/replies?page=1&size=20
```

是否需要 Token：是  
允许角色：`STUDENT`、`TEACHER`、`ADMIN`

### 6.4 发布主题帖

```http
POST /api/interactions/topics
```

是否需要 Token：是  
允许角色：`STUDENT`、`TEACHER`、`ADMIN`

请求体：

```json
{
  "courseId": 1,
  "title": "作业提交问题",
  "content": "请问第一次作业什么时候截止？"
}
```

说明：

- 课程必须存在且处于正常状态
- 第一版不强制校验学生是否已选课

### 6.5 回复主题帖

```http
POST /api/interactions/topics/{topicId}/replies
```

是否需要 Token：是  
允许角色：`STUDENT`、`TEACHER`、`ADMIN`

请求体：

```json
{
  "content": "老师说本周五截止。"
}
```

说明：

- 只能回复正常状态的主题帖
- 回复记录的 `parentId` 为主题帖 ID

### 6.6 编辑讨论内容

```http
PUT /api/interactions/{id}
```

是否需要 Token：是  
允许角色：`STUDENT`、`TEACHER`、`ADMIN`

请求体：

```json
{
  "title": "新的标题",
  "content": "新的内容"
}
```

说明：

- 作者本人可以编辑自己的主题帖或回复
- 课程教师可以编辑自己课程下的讨论内容
- 管理员可以编辑任意讨论内容
- 回复不使用 `title` 字段

### 6.7 修改讨论状态

```http
PUT /api/interactions/{id}/status
```

是否需要 Token：是  
允许角色：课程教师、`ADMIN`

请求体：

```json
{
  "status": 0
}
```

说明：

- 课程教师只能管理自己课程下的讨论内容
- 管理员可以管理任意讨论内容

### 6.8 删除讨论内容

```http
DELETE /api/interactions/{id}
```

是否需要 Token：是  
允许角色：`STUDENT`、`TEACHER`、`ADMIN`

说明：

- 使用逻辑删除
- 作者本人可以删除自己的主题帖或回复
- 课程教师可以删除自己课程下的讨论内容
- 管理员可以删除任意讨论内容

## 7. 前端调用要求

前端 Axios 应：

- 设置基础地址为 `http://localhost:8080/api`
- 登录成功后保存 `token` 和 `user`
- 请求拦截器自动添加 `Authorization`
- 响应 `401` 时清理登录态并跳转登录页
- 根据 `user.role` 控制菜单显示

## 8. 后端修改接口规则

后端如修改以下内容，必须同步更新本文档：

- URL
- HTTP 方法
- 请求体字段
- 返回字段
- 权限要求
- 错误码含义
