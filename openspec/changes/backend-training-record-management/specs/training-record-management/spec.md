## ADDED Requirements
### Requirement: Training Record Management API
系统 SHALL 提供训练记录的新增、更新、详情与条件查询能力，用于记录青少年学员每次训练过程与反馈。

#### Scenario: Create training record
- **WHEN** 管理员或教练提交合法训练记录
- **THEN** 系统返回创建成功的训练记录结果

#### Scenario: Update training record
- **WHEN** 管理员或教练更新已有训练记录
- **THEN** 系统返回更新后的训练记录结果

#### Scenario: Search training records
- **WHEN** 调用方按学员、课程或日期范围查询训练记录
- **THEN** 系统返回按训练日期倒序排序的记录列表
