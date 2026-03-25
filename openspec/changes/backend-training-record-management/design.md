## Overview
训练记录模块复用当前考勤、体测模块的实现风格，采用单表存储训练过程信息，通过 studentId 和 courseId 关联学员与课程。

## Data Model
- `student_id`: 学员ID，必填
- `course_id`: 课程ID，必填
- `training_date`: 训练日期，必填
- `training_content`: 训练内容，必填
- `duration_minutes`: 训练时长（分钟），必填，>= 1
- `intensity_level`: 强度等级，可选
- `performance_summary`: 训练反馈，可选
- `coach_comment`: 教练评语，可选

## API Design
- `POST /api/v1/training-records`
- `PUT /api/v1/training-records/{id}`
- `GET /api/v1/training-records/{id}`
- `GET /api/v1/training-records?studentId=&courseId=&startDate=&endDate=`

## Validation
- 创建与更新均校验学员、课程存在性
- 内容字段做长度约束，时长字段做最小值约束
- 查询按 `trainingDate desc, id desc` 返回
