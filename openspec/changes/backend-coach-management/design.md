## Design

### Data Model
新增 `coaches` 表：
- `coach_code`：教练编号，唯一
- `name`：教练姓名，唯一
- `gender`
- `phone`
- `specialty`
- `status`
- `remarks`
- 审计字段 `created_at` / `updated_at`

### API Design
新增接口：
- `POST /api/v1/coaches`
- `PUT /api/v1/coaches/{id}`
- `DELETE /api/v1/coaches/{id}`
- `GET /api/v1/coaches/{id}`
- `GET /api/v1/coaches`
- `GET /api/v1/coaches/options`

### Consistency Rules
- 教练编号和姓名都必须唯一
- 更新教练姓名时，同步更新所有课程中的 `coachName`
- 删除教练前检查课程表；若存在引用，则返回冲突错误
- `options` 仅返回 `ACTIVE` 状态教练，供表单下拉使用

### Compatibility
课程接口仍沿用 `coachName` 字段，保持现有管理后台、小程序和文档调用兼容。