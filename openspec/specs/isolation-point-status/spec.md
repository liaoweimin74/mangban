# isolation-point-status Specification

## Purpose
TBD - created by archiving change isolation-point-management. Update Purpose after archive.
## Requirements
### Requirement: 隔离点通盲状态变更
系统 SHALL 支持变更隔离点的通/盲状态。

#### Scenario: 将通板改为盲板
- **WHEN** 客户端 PUT /api/isolation-points/{id}/status，请求体包含 status="BLIND"
- **THEN** 系统更新隔离点状态为 BLIND，返回更新后的隔离点信息，status 字段为 BLIND

#### Scenario: 将盲板改为通板
- **WHEN** 客户端 PUT /api/isolation-points/{id}/status，请求体包含 status="OPEN"
- **THEN** 系统更新隔离点状态为 OPEN，返回更新后的隔离点信息

#### Scenario: 无效状态值
- **WHEN** 客户端 PUT /api/isolation-points/{id}/status，请求体包含 status="INVALID"
- **THEN** 系统返回 400 错误，提示"无效状态值，仅支持 OPEN 或 BLIND"

#### Scenario: 状态变更隔离点不存在
- **WHEN** 客户端 PUT /api/isolation-points/999999/status
- **THEN** 系统返回 404 错误

---

### Requirement: 隔离点占用状态变更
系统 SHALL 支持变更隔离点的占用/释放状态。

#### Scenario: 占用隔离点
- **WHEN** 客户端 PUT /api/isolation-points/{id}/occupy，请求体包含 occupyStatus="OCCUPIED"
- **THEN** 系统更新隔离点占用状态为 OCCUPIED，返回更新后的隔离点信息

#### Scenario: 释放隔离点
- **WHEN** 客户端 PUT /api/isolation-points/{id}/occupy，请求体包含 occupyStatus="FREE"
- **THEN** 系统更新隔离点占用状态为 FREE，返回更新后的隔离点信息

#### Scenario: 无效占用状态值
- **WHEN** 客户端 PUT /api/isolation-points/{id}/occupy，请求体包含 occupyStatus="BUSY"
- **THEN** 系统返回 400 错误，提示"无效占用状态值，仅支持 OCCUPIED 或 FREE"

#### Scenario: 占用状态变更隔离点不存在
- **WHEN** 客户端 PUT /api/isolation-points/999999/occupy
- **THEN** 系统返回 404 错误

---

### Requirement: 状态变更记录
系统 SHALL 在隔离点状态变更时记录变更信息。

#### Scenario: 状态变更时记录备注
- **WHEN** 隔离点状态或占用状态发生变更
- **THEN** 系统在隔离点的 remark 字段追加状态变更记录（包含变更前状态、变更后状态、变更时间）

