## Why
课题任务书和开题报告都将训练记录管理列为核心业务流程，但当前后端仅实现了学员、课程、考勤和体测，训练过程数据尚未纳入统一管理。

## What Changes
- 新增训练记录表 `training_records`
- 新增训练记录 CRUD / 查询接口
- 同步 README、接口文档和 Postman 集合

## Impact
- 后端新增训练记录领域模型、DTO、Service、Controller、Mapper
- 数据库初始化脚本需要新增训练记录表结构
- 管理后台可基于该接口实现训练记录录入与查询
