## ADDED Requirements

### Requirement: Coach CRUD and listing
系统 MUST 提供教练档案的创建、更新、删除、按 ID 查询与分页查询能力。

#### Scenario: Create coach
- **WHEN** 管理员提交合法教练信息
- **THEN** 系统 SHALL 创建教练档案并返回教练详情

#### Scenario: List coaches with filters
- **WHEN** 客户端按姓名或状态查询教练列表
- **THEN** 系统 SHALL 返回符合条件的分页结果

#### Scenario: Delete unreferenced coach
- **WHEN** 管理员删除一个未被课程引用的教练
- **THEN** 系统 SHALL 删除该教练并返回成功响应

### Requirement: Coach data consistency with courses
系统 MUST 维护教练档案与课程教练名称之间的一致性。

#### Scenario: Rename coach
- **WHEN** 管理员修改教练姓名
- **THEN** 系统 SHALL 同步更新所有使用该姓名的课程 `coachName`

#### Scenario: Delete referenced coach
- **WHEN** 管理员尝试删除仍被课程引用的教练
- **THEN** 系统 SHALL 返回 `409 Conflict` 并阻止删除

### Requirement: Active coach options
系统 MUST 提供仅包含在职教练的选择接口，供前端表单使用。

#### Scenario: Query coach options
- **WHEN** 客户端请求教练选项列表
- **THEN** 系统 SHALL 返回所有 `ACTIVE` 状态教练