# ZF青少年体能培训教务管理平台（后端）

本项目是 `ZF Youth Fitness Academy Management Platform` 的后端服务，基于 Spring Boot 实现，面向管理后台与移动端提供统一 REST API。

## 当前技术方案
- Java 17
- Spring Boot 3.3.8
- Spring Web MVC
- MyBatis-Plus
- Spring Security
- Spring Data Redis
- MySQL
- Swagger OpenAPI Annotations

## 已实现能力
- 学员管理：创建、更新、详情、分页查询
- 课程管理：创建、更新、详情、分页查询
- 考勤管理：记录考勤、按学员/课程/日期范围查询
- 体测管理：录入体测记录、按学员时间线查询
- 训练记录：新增、更新、详情、按学员/课程/日期范围查询
- 安全基线：Spring Security + RBAC（`ADMIN` / `COACH`）
- 通用能力：统一响应结构、全局异常处理、参数校验

## 目录结构
```text
src/main/java/com/shuzi/managementplatform
|-- common
|   |-- api
|   |-- exception
|   `-- model
|-- config
|-- domain
|   |-- entity
|   |-- enums
|   |-- mapper
|   `-- service
|-- security
`-- web
    |-- controller
    `-- dto
```

## 本地启动（MySQL）
### 1. 准备环境
- JDK 17+
- MySQL 8.x
- 创建数据库：`management_platform`

```sql
CREATE DATABASE IF NOT EXISTS management_platform
  DEFAULT CHARACTER SET utf8mb4
  COLLATE utf8mb4_general_ci;
```

### 2. 修改数据库连接
编辑 `src/main/resources/application.properties`：
- `spring.datasource.url`
- `spring.datasource.username`
- `spring.datasource.password`

默认配置为本地 MySQL：
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/management_platform?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true&useSSL=false
spring.datasource.username=root
spring.datasource.password=123456
```

### 3. 启动项目
```powershell
cd Code/backend-service/ManagementPlatform
.\mvnw.cmd spring-boot:run
```

说明：
- 应用启动时会执行 `schema-mysql.sql` 初始化基础表结构。
- 当前不再使用 H2，开发和本地联调统一使用 MySQL。

## 认证与角色（开发态）
当前已切换为 JWT Bearer 认证，提供以下接口：
- `POST /api/v1/auth/login`
- `POST /api/v1/auth/refresh`
- `POST /api/v1/auth/logout`

默认系统用户（写入 `user_accounts` 表）：
- `admin / Admin@123`（角色：`ADMIN`）
- `coach / Coach@123`（角色：`COACH`）
- `student / Student@123`（角色：`STUDENT`）
- `parent / Parent@123`（角色：`PARENT`）

业务档案关联登录账户规则：
- 新增教练时自动创建登录用户：用户名 `coach_{coachCode}`，初始密码 `{coachCode}@123`
- 新增学员时自动创建登录用户：用户名 `student_{studentNo}`，初始密码 `{studentNo}@123`
- 密码以 BCrypt 哈希方式存储在数据库，不保存明文

密码管理接口：
- 本人修改密码：`POST /api/v1/auth/change-password`
- 管理员直接改密：`PUT /api/v1/user-accounts/password`
- 管理员重置初始密码：`POST /api/v1/user-accounts/reset-password`

## 核心接口
- 健康检查：`GET /api/v1/public/ping`
- 学员管理：`/api/v1/students`
- 课程管理：`/api/v1/courses`
- 考勤管理：`/api/v1/attendances`
- 体测管理：`/api/v1/fitness-tests`
- 训练记录：`/api/v1/training-records`

## 构建命令
```powershell
.\mvnw.cmd -DskipTests compile
.\mvnw.cmd package
```

## 文档与规范
- 接口文档：`docs/API接口文档.md`
- Postman 集合：`docs/ZFYouthFitnessAcademy-API.postman_collection.json`
- OpenSpec 变更目录：`openspec/changes/`
