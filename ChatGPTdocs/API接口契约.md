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
| `/api/courses/**` | `edu-course-service` | 预留 |
| `/api/enrollments/**` | `edu-enrollment-service` | 预留 |
| `/api/interactions/**` | `edu-interaction-service` | 预留 |
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

## 4. 前端调用要求

前端 Axios 应：

- 设置基础地址为 `http://localhost:8080/api`
- 登录成功后保存 `token` 和 `user`
- 请求拦截器自动添加 `Authorization`
- 响应 `401` 时清理登录态并跳转登录页
- 根据 `user.role` 控制菜单显示

## 5. 后端修改接口规则

后端如修改以下内容，必须同步更新本文档：

- URL
- HTTP 方法
- 请求体字段
- 返回字段
- 权限要求
- 错误码含义
