## ADDED Requirements

### Requirement: Course CRUD and listing
系统 MUST 提供课程的创建、更新、按 ID 查询与分页查询能力，课程需包含名称、类型、教练、场地、计划开始时间与时长字段。

#### Scenario: Create course
- **WHEN** 管理端提交合法课程信息
- **THEN** 系统 SHALL 创建课程并返回课程详情

#### Scenario: List courses with pagination
- **WHEN** 客户端请求课程列表并提供分页参数
- **THEN** 系统 SHALL 返回分页结果及总记录数

### Requirement: Course status control
系统 MUST 支持课程状态管理，至少包含 `PLANNED`、`ONGOING`、`COMPLETED`、`CANCELLED`。

#### Scenario: Change course status
- **WHEN** 管理端更新课程状态
- **THEN** 系统 SHALL 按请求更新并在查询结果中反映新状态
