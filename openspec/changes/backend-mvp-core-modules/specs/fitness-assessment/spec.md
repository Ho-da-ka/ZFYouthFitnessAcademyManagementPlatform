## ADDED Requirements

### Requirement: Fitness test recording
系统 MUST 支持记录学员体能测试数据，至少包含测试日期、测试项目、测试值、单位和评语。

#### Scenario: Add fitness test result
- **WHEN** 教练端提交某学员的体测记录
- **THEN** 系统 SHALL 保存记录并返回可查询的记录 ID

### Requirement: Fitness timeline query
系统 MUST 支持按学员时间线查询体测记录，并按测试日期倒序返回。

#### Scenario: Query fitness timeline
- **WHEN** 客户端请求某学员体测时间线
- **THEN** 系统 SHALL 返回按日期倒序排列的体测记录
