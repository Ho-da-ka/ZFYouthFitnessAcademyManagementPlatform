## ADDED Requirements

### Requirement: Student profile CRUD
系统 MUST 提供学员档案的创建、更新、按 ID 查询和分页查询能力，并为每条学员记录维护唯一业务编号。

#### Scenario: Create student profile
- **WHEN** 管理端提交合法的学员基础信息（姓名、性别、出生日期、家长联系方式）
- **THEN** 系统 SHALL 创建学员档案并返回带唯一 ID 的结果

#### Scenario: Update student profile
- **WHEN** 管理端针对已存在学员提交更新请求
- **THEN** 系统 SHALL 持久化更新后的信息并返回最新档案

### Requirement: Student status management
系统 MUST 支持学员状态管理，至少包含 `ACTIVE`、`INACTIVE` 两种状态，用于标识在训与停训。

#### Scenario: Set student inactive
- **WHEN** 管理端将某学员状态调整为 `INACTIVE`
- **THEN** 系统 SHALL 在后续查询中返回该状态
