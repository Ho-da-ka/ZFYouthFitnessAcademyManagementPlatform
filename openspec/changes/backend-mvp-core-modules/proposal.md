## Why

当前后端工程仅包含 Spring Boot 启动骨架，尚未承载项目概述文档中定义的核心教务业务能力。为支撑移动端与管理后台并行开发，需要先落地可运行、可扩展的后端 MVP 接口与数据模型。

## What Changes

- 新增教务核心域模型与 REST API：学员、课程、考勤、体能测试记录。
- 新增基础工程能力：统一响应、参数校验、异常处理、分页查询。
- 新增最小安全能力骨架：JWT/RBAC 配置占位与鉴权拦截预留（不实现完整登录流程）。
- 新增后端基础配置：面向 MySQL/Redis 的配置项模板与本地开发配置结构。
- 新增 OpenAPI 文档支持，便于移动端与后台联调。

## Capabilities

### New Capabilities

- `student-management`: 管理学员档案，支持创建、更新、查询、分页检索和状态管理。
- `course-management`: 管理课程信息与排课基础字段，支持创建、更新、查询与分页。
- `attendance-management`: 记录学员课程签到与出勤状态，支持按学员/课程/日期查询。
- `fitness-assessment`: 记录体能测试结果并支持按学员时间线查询。
- `auth-rbac-baseline`: 提供 JWT 与 RBAC 的最小化后端骨架与接口访问控制预留能力。

### Modified Capabilities

- 无

## Impact

- 代码范围：`src/main/java/com/shuzi/managementplatform/**` 新增多层模块（controller/service/repository/entity/dto）。
- 接口范围：新增 `/api/v1/students`、`/api/v1/courses`、`/api/v1/attendances`、`/api/v1/fitness-tests` 等端点。
- 依赖变化：新增 JPA、Validation、MySQL、OpenAPI 相关依赖。
- 配置变化：`application.properties` 新增数据库、Redis、JPA 和文档相关配置项。
