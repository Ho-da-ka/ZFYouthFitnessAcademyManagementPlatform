## ADDED Requirements

### Requirement: Attendance recording
系统 MUST 支持按课程与学员维度记录考勤，考勤状态至少包含 `PRESENT`、`LATE`、`ABSENT`、`LEAVE`。

#### Scenario: Record attendance
- **WHEN** 教练端或管理端提交课程-学员考勤记录
- **THEN** 系统 SHALL 保存该考勤并返回记录详情

### Requirement: Attendance query capability
系统 MUST 支持按学员、课程和日期范围查询考勤记录。

#### Scenario: Query attendance by student and date
- **WHEN** 客户端提交学员 ID 与起止日期查询条件
- **THEN** 系统 SHALL 返回符合条件的考勤记录集合
