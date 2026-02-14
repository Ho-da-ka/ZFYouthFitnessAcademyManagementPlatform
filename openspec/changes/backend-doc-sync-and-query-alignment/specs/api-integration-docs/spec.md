## ADDED Requirements

### Requirement: API docs must match runtime behavior
系统的联调文档 MUST 与运行时接口行为保持一致。

#### Scenario: Optional query parameter documented
- **WHEN** 某查询参数在接口层声明为 `required=false`
- **THEN** 文档 SHALL 明确该参数为可选，并给出不传时的行为说明

#### Scenario: Collection and markdown consistency
- **WHEN** Markdown 文档更新了接口约定
- **THEN** Postman/Apifox 导入集合 SHALL 同步更新同一约定