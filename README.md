# ZF青少年体能培训教务管理平台（后端）

本项目是 `ZF Youth Fitness Academy Management Platform` 的后端服务，基于 Spring Boot 实现，面向移动端与管理后台提供统一 REST API。

## 当前技术方案

- Java 17
- Spring Boot 4.0.2
- Spring Web MVC
- MyBatis-Plus（ORM 与分页）
- Spring Security
- Spring Data Redis
- MySQL（本地与生产统一）
- OpenAPI 注解（`@Tag` / `@Operation`）

## 已实现能力（MVP）

- 学员管理：创建、更新、详情、分页查询
- 课程管理：创建、更新、详情、分页查询
- 考勤管理：记录考勤、按学员/课程/日期范围查询
- 体测管理：录入体测记录、按学员时间线查询
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
- 应用启动时会执行 `schema-mysql.sql` 初始化基础表结构（`CREATE TABLE IF NOT EXISTS`）。
- 不再使用 H2 数据库。

## 认证与角色（开发态）

当前使用 HTTP Basic 内存用户：

- `admin / Admin@123`（角色：`ADMIN`）
- `coach / Coach@123`（角色：`COACH`）

JWT 目前为占位骨架，后续会补完整登录发 token 流程。

## 核心接口

- 公共健康检查  
`GET /api/v1/public/ping`

- 学员管理  
`POST /api/v1/students`  
`PUT /api/v1/students/{id}`  
`GET /api/v1/students/{id}`  
`GET /api/v1/students?page=0&size=10&name=&status=`

- 课程管理  
`POST /api/v1/courses`  
`PUT /api/v1/courses/{id}`  
`GET /api/v1/courses/{id}`  
`GET /api/v1/courses?page=0&size=10&name=&status=`

- 考勤管理  
`POST /api/v1/attendances`  
`GET /api/v1/attendances?studentId=&courseId=&startDate=&endDate=`

- 体测管理  
`POST /api/v1/fitness-tests`  
`GET /api/v1/fitness-tests?studentId=`（`studentId` 可选，不传则返回全部）

## OpenAPI 分组

- `Public`：公共接口
- `Student`：学员管理
- `Course`：课程管理
- `Attendance`：考勤管理
- `FitnessTest`：体测管理

## 构建命令

```powershell
.\mvnw.cmd -DskipTests compile
.\mvnw.cmd package
```

## Spec 工作流

本仓库 OpenSpec 变更目录：

- `openspec/changes/backend-mvp-core-modules/`
