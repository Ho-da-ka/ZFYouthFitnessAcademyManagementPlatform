## ADDED Requirements

### Requirement: Protected API baseline
系统 MUST 提供受保护接口的统一鉴权入口，未认证请求访问受保护接口时返回 401。

#### Scenario: Reject unauthenticated request
- **WHEN** 客户端未携带有效认证信息访问受保护接口
- **THEN** 系统 SHALL 返回 401 Unauthorized

### Requirement: Role-based authorization baseline
系统 MUST 支持基于角色的接口访问控制基础能力，至少区分 `ADMIN` 与 `COACH` 角色的访问边界。

#### Scenario: Reject forbidden role
- **WHEN** 角色权限不足的用户访问受限接口
- **THEN** 系统 SHALL 返回 403 Forbidden
