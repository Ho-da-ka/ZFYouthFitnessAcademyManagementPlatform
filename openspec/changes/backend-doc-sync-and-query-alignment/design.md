## Context

现有后端接口已支持 `studentId` 可选，但文档仍按“必填”表达。该问题不涉及数据模型和业务规则重构，重点是规范接口契约描述。

## Decisions

### Decision 1: 文档契约以运行时行为为准
- **Rationale**: 联调工具和文字文档应反映当前实际接口行为，减少误判为后端故障的概率。
- **Alternative**: 回滚后端代码继续强制 `studentId` 必填；该方案会破坏已优化的前端交互。

### Decision 2: 同步维护人类可读文档与导入集合
- **Rationale**: 团队同时使用 Markdown 文档与 Postman/Apifox，必须双向一致。
- **Alternative**: 只改其中一份文档，后续仍会出现认知分叉。

## Risks / Trade-offs

- [Risk] 后续代码再次调整查询参数后未同步文档。  
  **Mitigation**: 将“文档同步”纳入变更 tasks 检查项。

## Migration Plan

1. 更新 API 文档中体测查询参数描述。  
2. 更新 Postman 集合中 `studentId` 参数注释为可选。  
3. 在 README 的接口总览中补充可选说明。  
4. 通过 OpenSpec 记录变更并关闭任务。