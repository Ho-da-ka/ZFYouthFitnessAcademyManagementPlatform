# ZF 青少年体能培训教务管理平台 后端接口文档

## 1. 基础信息
- 接口前缀: `/api/v1`
- 本地地址: `http://localhost:8080`
- 数据格式: `application/json`
- 日期格式:
  - `LocalDate`: `yyyy-MM-dd`，示例 `2026-02-14`
  - `LocalDateTime`: ISO-8601，示例 `2026-02-14T10:30:00`

## 2. 鉴权与权限
- 公开接口: `/api/v1/public/**`
- 其余 `/api/v1/**` 默认需要认证
- 当前认证方式: `HTTP Basic`
- 内置测试账号:
  - `admin / Admin@123` (角色 `ADMIN`)
  - `coach / Coach@123` (角色 `COACH`)
- 请求头示例:
  - `Authorization: Basic base64(admin:Admin@123)`
- 说明: 项目中存在 `Bearer` 令牌占位过滤器，但当前未建立完整 JWT 登录换 token 流程。

## 3. 统一返回结构
### 3.1 通用返回
```json
{
  "success": true,
  "message": "OK",
  "data": {}
}
```

### 3.2 分页返回 (`PageResponse<T>`)
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

### 3.3 常见错误码
- `400 Bad Request`: 参数校验失败
- `401 Unauthorized`: 未认证
- `403 Forbidden`: 角色权限不足
- `404 Not Found`: 资源不存在
- `409 Conflict`: 业务冲突（如编码重复）
- `500 Internal Server Error`: 服务内部异常

## 4. 枚举值
- `Gender`: `MALE`, `FEMALE`
- `StudentStatus`: `ACTIVE`, `INACTIVE`
- `CourseStatus`: `PLANNED`, `ONGOING`, `COMPLETED`, `CANCELLED`
- `AttendanceStatus`: `PRESENT`, `LATE`, `ABSENT`, `LEAVE`

## 5. 接口清单

### 5.1 健康检查
- 方法/路径: `GET /api/v1/public/ping`
- 权限: 无需登录
- 响应示例:
```json
{
  "success": true,
  "message": "OK",
  "data": {
    "service": "management-platform",
    "time": "2026-02-14T10:30:00",
    "status": "UP"
  }
}
```

### 5.2 学员管理
#### 5.2.1 创建学员
- 方法/路径: `POST /api/v1/students`
- 权限: `ADMIN`
- 请求体:
```json
{
  "studentNo": "S2026001",
  "name": "张三",
  "gender": "MALE",
  "birthDate": "2012-06-01",
  "guardianName": "张家长",
  "guardianPhone": "13800138000",
  "status": "ACTIVE",
  "remarks": "初始建档"
}
```
- 规则:
  - `studentNo`, `name`, `gender`, `birthDate` 必填
  - `studentNo` 不能重复，重复返回 `409`
  - `guardianPhone` 若填写，需匹配 `^[0-9+\\-]{6,20}$`
  - `status` 不传时默认 `ACTIVE`

#### 5.2.2 更新学员
- 方法/路径: `PUT /api/v1/students/{id}`
- 权限: `ADMIN`
- 路径参数: `id` 学员ID
- 请求体: 与创建类似，但 `status` 必填（`studentNo` 不可改）
- 规则: `guardianPhone` 若填写，需匹配 `^[0-9+\\-]{6,20}$`

#### 5.2.3 获取学员详情
- 方法/路径: `GET /api/v1/students/{id}`
- 权限: `ADMIN` / `COACH`

#### 5.2.4 分页查询学员
- 方法/路径: `GET /api/v1/students`
- 权限: `ADMIN` / `COACH`
- 查询参数:
  - `page` 默认 `0`
  - `size` 默认 `10`
  - `name` 可选，按姓名模糊查询
  - `status` 可选，`ACTIVE` / `INACTIVE`

### 5.3 课程管理
#### 5.3.1 创建课程
- 方法/路径: `POST /api/v1/courses`
- 权限: `ADMIN`
- 请求体:
```json
{
  "courseCode": "C2026001",
  "name": "青少年体能基础班",
  "courseType": "体能训练",
  "coachName": "李教练",
  "venue": "A馆1号场",
  "startTime": "2026-02-20T19:00:00",
  "durationMinutes": 90,
  "status": "PLANNED",
  "description": "每周二四晚间"
}
```
- 规则:
  - `courseCode`, `name`, `courseType`, `coachName`, `venue`, `startTime`, `durationMinutes` 必填
  - `courseCode` 不能重复，重复返回 `409`
  - `durationMinutes >= 1`
  - `status` 不传时默认 `PLANNED`

#### 5.3.2 更新课程
- 方法/路径: `PUT /api/v1/courses/{id}`
- 权限: `ADMIN`
- 路径参数: `id` 课程ID
- 请求体: 与创建类似，但 `status` 必填（`courseCode` 不可改）

#### 5.3.3 获取课程详情
- 方法/路径: `GET /api/v1/courses/{id}`
- 权限: `ADMIN` / `COACH`

#### 5.3.4 分页查询课程
- 方法/路径: `GET /api/v1/courses`
- 权限: `ADMIN` / `COACH`
- 查询参数:
  - `page` 默认 `0`
  - `size` 默认 `10`
  - `name` 可选，按课程名模糊查询
  - `status` 可选，`PLANNED` / `ONGOING` / `COMPLETED` / `CANCELLED`

### 5.4 考勤管理
#### 5.4.1 新增考勤记录
- 方法/路径: `POST /api/v1/attendances`
- 权限: `ADMIN` / `COACH`
- 请求体:
```json
{
  "studentId": 1,
  "courseId": 1,
  "attendanceDate": "2026-02-14",
  "status": "PRESENT",
  "note": "到课正常"
}
```
- 规则:
  - `studentId`, `courseId`, `attendanceDate`, `status` 必填
  - `studentId` / `courseId` 对应资源不存在会返回 `404`

#### 5.4.2 查询考勤记录
- 方法/路径: `GET /api/v1/attendances`
- 权限: `ADMIN` / `COACH`
- 查询参数（均可选）:
  - `studentId`
  - `courseId`
  - `startDate`
  - `endDate`
- 返回: `List<AttendanceResponse>`（按 `attendanceDate`、`id` 倒序）

### 5.5 体测管理
#### 5.5.1 新增体测记录
- 方法/路径: `POST /api/v1/fitness-tests`
- 权限: `ADMIN` / `COACH`
- 请求体:
```json
{
  "studentId": 1,
  "testDate": "2026-02-14",
  "itemName": "50米跑",
  "testValue": 8.72,
  "unit": "s",
  "comment": "较上月提升"
}
```
- 规则:
  - `studentId`, `testDate`, `itemName`, `testValue`, `unit` 必填
  - `testValue > 0`
  - `studentId` 不存在返回 `404`

#### 5.5.2 查询体测记录
- 方法/路径: `GET /api/v1/fitness-tests`
- 权限: `ADMIN` / `COACH`
- 查询参数（可选）:
  - `studentId`：按学员筛选；不传时返回全量体测记录
- 返回: `List<FitnessTestResponse>`（按 `testDate`、`id` 倒序）

## 6. 响应对象字段摘要
### 6.1 StudentResponse
`id`, `studentNo`, `name`, `gender`, `birthDate`, `guardianName`, `guardianPhone`, `status`, `remarks`, `createdAt`, `updatedAt`

### 6.2 CourseResponse
`id`, `courseCode`, `name`, `courseType`, `coachName`, `venue`, `startTime`, `durationMinutes`, `status`, `description`, `createdAt`, `updatedAt`

### 6.3 AttendanceResponse
`id`, `studentId`, `studentName`, `courseId`, `courseName`, `attendanceDate`, `status`, `note`, `createdAt`, `updatedAt`

### 6.4 FitnessTestResponse
`id`, `studentId`, `studentName`, `testDate`, `itemName`, `testValue`, `unit`, `comment`, `createdAt`, `updatedAt`
