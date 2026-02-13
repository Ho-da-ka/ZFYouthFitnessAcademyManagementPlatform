# ZF青少年体能培训教务管理平台

本仓库为 **ZF青少年体能培训教务管理平台** 的后端服务（Spring Boot）。

## 项目简介

该后端服务面向青少年体能培训场景，目标是支撑以下核心能力：

- 学员档案管理
- 课程与排课管理
- 考勤与训练数据管理
- 面向移动端（家长/学员）的数据服务
- 面向管理后台的业务接口服务

## 技术栈

- Java 17
- Spring Boot 4.0.2
- Spring Web MVC
- Spring Data Redis
- MySQL（运行时驱动）
- Maven Wrapper（`mvnw`、`mvnw.cmd`）

## 当前目录结构

```text
ManagementPlatform
|-- src
|   |-- main
|   |   |-- java/com/shuzi/managementplatform
|   |   |   `-- ManagementPlatformApplication.java
|   |   `-- resources
|   |       `-- application.properties
|   `-- test
|       `-- java/com/shuzi/managementplatform
|           `-- ManagementPlatformApplicationTests.java
|-- pom.xml
|-- mvnw
`-- mvnw.cmd
```

## 快速开始

### 1. 环境要求

- JDK 17 及以上
- 可访问 Maven 仓库网络（首次构建需要下载依赖）

### 2. 本地启动（Windows PowerShell）

```powershell
cd Code/backend-service/ManagementPlatform
.\mvnw.cmd spring-boot:run
```

### 3. 本地启动（macOS/Linux）

```bash
cd Code/backend-service/ManagementPlatform
./mvnw spring-boot:run
```

当前应用名配置位于 `src/main/resources/application.properties`：

```properties
spring.application.name=ManagementPlatform
```

## 构建与测试

```powershell
.\mvnw.cmd clean test
.\mvnw.cmd clean package
```

## 规划模块

- 认证与权限（JWT + RBAC）
- 学员管理
- 教练管理
- 课程与排课管理
- 考勤管理
- 体能测试与成长档案
- 消息通知
- 文件存储集成（OSS）

## 分支与远程

- 默认分支：`main`
- 推荐远程名：`origin`

## 许可证

当前暂未声明许可证。

