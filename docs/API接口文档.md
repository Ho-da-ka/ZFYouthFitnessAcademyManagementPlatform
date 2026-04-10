# ZF 青少年体能培训教务管理平台后端接口文档

## 1. 基础信息
- 接口前缀：`/api/v1`
- 本地地址：`http://localhost:8080`
- 数据格式：`application/json`
- 日期格式：
  - `LocalDate`：`yyyy-MM-dd`
  - `LocalDateTime`：ISO-8601（示例：`2026-04-10T20:30:00`）

## 2. 认证与权限

### 2.1 认证方式（当前）
- 使用 JWT Bearer Token：
  - `POST /api/v1/auth/login`
  - `POST /api/v1/auth/refresh`
  - `POST /api/v1/auth/logout`
  - `POST /api/v1/auth/change-password`
- 请求头示例：
  - `Authorization: Bearer <accessToken>`

### 2.2 Refresh Token 持久化
- Refresh Token 已持久化到数据库表：`refresh_tokens`
- 关键行为：
  - 登录发放 refresh token（写库）
  - 刷新时旧 token 一次性消费（读后删）
  - 退出时 token 主动失效（删库）
  - 过期 token 自动清理

### 2.3 公开接口
- `GET /api/v1/public/ping`

### 2.4 角色权限摘要
- `ADMIN`：
  - 学员/课程/教练管理写操作
  - 账户密码管理（管理员改密、重置）
- `ADMIN` / `COACH`：
  - 学员/课程/教练查询
  - 考勤、体测、训练记录管理
- `PARENT`：
  - `/api/v1/parent/**` 家长端数据访问

### 2.5 默认账号（开发环境）
- `admin / Admin@123`（`ADMIN`）
- `coach / Coach@123`（`COACH`）
- `student / Student@123`（`STUDENT`）
- `parent / Parent@123`（`PARENT`）

## 3. 统一返回结构
```json
{
  "success": true,
  "message": "OK",
  "data": {}
}
```

分页返回（`PageResponse<T>`）：
```json
{
  "success": true,
  "message": "OK",
  "data": {
    "content": [],
    "page": 0,
    "size": 10,
    "totalElements": 0,
    "totalPages": 0
  }
}
```

## 4. 通用错误码
- `400` 参数校验失败
- `401` 未认证或 token 失效
- `403` 无权限
- `404` 资源不存在
- `409` 业务冲突（重复、容量不足、被引用等）
- `500` 服务内部异常

## 5. 枚举
- `Gender`：`MALE` / `FEMALE`
- `StudentStatus`：`ACTIVE` / `INACTIVE`
- `CoachStatus`：`ACTIVE` / `INACTIVE`
- `CourseStatus`：`PLANNED` / `ONGOING` / `COMPLETED` / `CANCELLED`
- `AttendanceStatus`：`PRESENT` / `LATE` / `ABSENT` / `LEAVE`

## 6. 鉴权相关接口

### 6.1 登录
- `POST /api/v1/auth/login`
- 请求体：
```json
{
  "username": "admin",
  "password": "Admin@123"
}
```
- 响应 `data` 关键字段：
  - `tokenType`
  - `accessToken`
  - `accessTokenExpiresIn`
  - `refreshToken`
  - `username`
  - `role`

### 6.2 刷新 Token
- `POST /api/v1/auth/refresh`
- 请求体：
```json
{
  "refreshToken": "..."
}
```

### 6.3 退出登录
- `POST /api/v1/auth/logout`
- 请求体（可选）：
```json
{
  "refreshToken": "..."
}
```

### 6.4 本人修改密码
- `POST /api/v1/auth/change-password`
- 需登录

### 6.5 管理员密码管理
- `PUT /api/v1/user-accounts/password`
- `POST /api/v1/user-accounts/reset-password`
- 仅 `ADMIN`

## 7. 管理端核心接口

### 7.1 学员
- `POST /api/v1/students`（`ADMIN`）
- `PUT /api/v1/students/{id}`（`ADMIN`）
- `DELETE /api/v1/students/{id}`（`ADMIN`）
- `GET /api/v1/students/{id}`（`ADMIN`/`COACH`）
- `GET /api/v1/students`（`ADMIN`/`COACH`，分页）

### 7.2 教练
- `POST /api/v1/coaches`（`ADMIN`）
- `PUT /api/v1/coaches/{id}`（`ADMIN`）
- `DELETE /api/v1/coaches/{id}`（`ADMIN`）
- `GET /api/v1/coaches/{id}`（`ADMIN`/`COACH`）
- `GET /api/v1/coaches`（`ADMIN`/`COACH`，分页）
- `GET /api/v1/coaches/options`（`ADMIN`/`COACH`）

### 7.3 课程
- `POST /api/v1/courses`（`ADMIN`）
- `PUT /api/v1/courses/{id}`（`ADMIN`）
- `GET /api/v1/courses/{id}`（`ADMIN`/`COACH`）
- `GET /api/v1/courses`（`ADMIN`/`COACH`，分页）

### 7.4 考勤 / 体测 / 训练
- 考勤：
  - `POST /api/v1/attendances`
  - `GET /api/v1/attendances`
  - `DELETE /api/v1/attendances/{id}`
- 体测：
  - `POST /api/v1/fitness-tests`
  - `GET /api/v1/fitness-tests`
- 训练：
  - `POST /api/v1/training-records`
  - `PUT /api/v1/training-records/{id}`
  - `GET /api/v1/training-records/{id}`
  - `GET /api/v1/training-records`
- 上述接口均为 `ADMIN` / `COACH`

## 8. 家长端接口（`/api/v1/parent`）

### 8.1 我的孩子
- `GET /api/v1/parent/children`

### 8.2 可预约课程
- `GET /api/v1/parent/courses`
- 返回课程容量摘要：`capacity`、`bookedCount`、`availableCount`

### 8.3 预约管理
- `GET /api/v1/parent/bookings`
- `POST /api/v1/parent/bookings`
```json
{
  "studentId": 1,
  "courseId": 1,
  "remark": "周末体验"
}
```
- `DELETE /api/v1/parent/bookings/{id}`

### 8.4 签到
- `GET /api/v1/parent/checkins`
- `POST /api/v1/parent/checkins`
```json
{
  "bookingId": 1,
  "attendanceDate": "2026-04-10",
  "note": "家长手动签到"
}
```

### 8.5 体测记录
- `GET /api/v1/parent/fitness-tests`
- 可选参数：`studentId`

### 8.6 站内消息
- `GET /api/v1/parent/messages`
- `PATCH /api/v1/parent/messages/{id}/read`
- 兼容小程序：
  - `POST /api/v1/parent/messages/{id}/read`

## 9. 相关文档
- README：`README.md`
- Postman 集合：`docs/ZFYouthFitnessAcademy-API.postman_collection.json`
- OpenSpec：`openspec/changes/`

