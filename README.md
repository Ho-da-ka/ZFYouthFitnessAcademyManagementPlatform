# ZF青少年体能培训教务管理平台（后端）

本项目是 `ZF Youth Fitness Academy Management Platform` 的后端服务，基于 Spring Boot 实现，面向移动端与管理后台提供统一 REST API。

## 已实现能力（MVP）

- 学员管理：创建、更新、详情、分页查询
- 课程管理：创建、更新、详情、分页查询
- 考勤管理：记录考勤、按学员/课程/日期范围查询
- 体测管理：录入体测记录、按学员时间线查询
- 安全基线：Spring Security + RBAC（`ADMIN` / `COACH`）
- 公共能力：统一响应结构、全局异常处理、参数校验
- 接口文档注解：控制器级 `@Tag` 与方法级 `@Operation`

## 技术栈

- Java 17
- Spring Boot 4.0.2
- Spring Web MVC
- Spring Data JPA
- Spring Security
- Spring Data Redis
- MySQL 驱动（生产）+ H2（本地默认）
- OpenAPI Annotations（Swagger）
- Maven Wrapper

## 目录结构

```text
src/main/java/com/shuzi/managementplatform
|-- common
|   |-- api
|   |-- exception
|   `-- model
|-- domain
|   |-- entity
|   |-- enums
|   |-- repository
|   `-- service
|-- security
`-- web
    |-- controller
    `-- dto
```

## 快速启动

### 1. 环境准备

- 安装 JDK 17+
- 推荐设置 `JAVA_HOME` 到 JDK 17，例如：

```powershell
$env:JAVA_HOME="C:\Program Files\Java\jdk-17"
$env:Path="$env:JAVA_HOME\bin;$env:Path"
```

### 2. 启动项目

```powershell
cd Code/backend-service/ManagementPlatform
.\mvnw.cmd spring-boot:run
```

默认使用 H2 内存数据库，便于本地快速联调。  
如需切换 MySQL，可使用 `application-mysql.properties` 并按实际账号修改配置。

## 认证与角色

当前采用开发态内存用户（HTTP Basic）：

- `admin / Admin@123`（角色：`ADMIN`）
- `coach / Coach@123`（角色：`COACH`）

说明：JWT 已预留占位过滤器与提供器，完整登录发 token 流程将在后续变更实现。

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
`GET /api/v1/fitness-tests?studentId=`

## OpenAPI 接口分组说明

当前以控制器 `Tag` 进行分组：

- `Public`：公共健康检查接口
- `Student`：学员档案管理接口
- `Course`：课程与排课基础管理接口
- `Attendance`：考勤记录接口
- `FitnessTest`：体能测试记录接口

## 构建与测试

```powershell
.\mvnw.cmd test
.\mvnw.cmd package
```

## Spec 工作流

本仓库已按 OpenSpec 初始化，当前开发变更位于：

- `openspec/changes/backend-mvp-core-modules/`

