## MODIFIED Requirements

### Requirement: Fitness timeline query
系统 MUST 支持查询体测记录，并允许按学员可选过滤。

#### Scenario: Query fitness timeline without student filter
- **WHEN** 客户端调用 `GET /api/v1/fitness-tests` 且未传 `studentId`
- **THEN** 系统 SHALL 返回按 `testDate`、`id` 倒序的全量体测记录

#### Scenario: Query fitness timeline with student filter
- **WHEN** 客户端调用 `GET /api/v1/fitness-tests` 且传入 `studentId`
- **THEN** 系统 SHALL 仅返回该学员体测记录，且按 `testDate`、`id` 倒序