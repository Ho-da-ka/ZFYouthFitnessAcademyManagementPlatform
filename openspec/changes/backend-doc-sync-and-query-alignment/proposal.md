## Why

本轮联调后，后端行为与接口文档出现了偏差：`GET /api/v1/fitness-tests` 已支持不传 `studentId` 查询全量记录，但文档仍描述为必填。该差异会导致前端联调和测试用例理解不一致。

## What Changes

- 同步修订后端接口文档，明确 `studentId` 为可选查询参数。
- 同步修订 Postman/Apifox 导入集合，标注 `studentId` 的可选语义。
- 在 OpenSpec 中补充本次文档与行为对齐变更，形成可追溯记录。

## Capabilities

### Modified Capabilities

- `fitness-assessment`: 体测记录查询从“必须按学员过滤”调整为“可选按学员过滤”。
- `api-integration-docs`: 联调文档必须反映接口真实行为，避免前后端约定漂移。

## Impact

- 文档更新：`docs/API接口文档.md`、`docs/ZFYouthFitnessAcademy-API.postman_collection.json`、`README.md`
- 行为对齐：前端可在不传 `studentId` 时加载全量体测数据。