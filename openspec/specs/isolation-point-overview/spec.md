# isolation-point-overview Specification

## Purpose
TBD - created by archiving change isolation-point-management. Update Purpose after archive.
## Requirements
### Requirement: 状态台账筛选查询
系统 SHALL 支持按多维度筛选全厂隔离点状态一览。

#### Scenario: 按装置筛选
- **WHEN** 客户端 GET /api/isolation-points?plantId=<装置id>&page=1&size=50
- **THEN** 系统返回该装置下所有隔离点的状态信息

#### Scenario: 按介质类型筛选
- **WHEN** 客户端 GET /api/isolation-points?medium=氢气&page=1&size=50
- **THEN** 系统返回介质为"氢气"的所有隔离点状态

#### Scenario: 按危害等级筛选
- **WHEN** 客户端 GET /api/isolation-points?hazardLevel=A&page=1&size=50
- **THEN** 系统返回危害等级为 A 的所有隔离点状态

#### Scenario: 按占用状态筛选
- **WHEN** 客户端 GET /api/isolation-points?occupyStatus=OCCUPIED&page=1&size=50
- **THEN** 系统返回当前被占用的所有隔离点状态

#### Scenario: 多条件组合筛选
- **WHEN** 客户端 GET /api/isolation-points?plantId=<装置id>&status=BLIND&occupyStatus=OCCUPIED&page=1&size=50
- **THEN** 系统返回同时满足所有条件的隔离点状态列表

---

### Requirement: 状态颜色标记
前端页面 SHALL 对不同的状态值使用颜色标签进行区分。

#### Scenario: 通板状态显示
- **WHEN** 隔离点 status 为 OPEN
- **THEN** 前端显示绿色标签"通板"

#### Scenario: 盲板状态显示
- **WHEN** 隔离点 status 为 BLIND
- **THEN** 前端显示红色标签"盲板"

#### Scenario: 已占用状态显示
- **WHEN** 隔离点 occupyStatus 为 OCCUPIED
- **THEN** 前端显示橙色标签"已占用"

#### Scenario: 空闲状态显示
- **WHEN** 隔离点 occupyStatus 为 FREE
- **THEN** 前端显示灰色标签"空闲"

---

### Requirement: 状态总览列表展示
前端页面 SHALL 以表格形式展示全厂隔离点状态一览，包含关键字段。

#### Scenario: 列表字段展示
- **WHEN** 用户访问状态台账总览页面
- **THEN** 表格展示以下列：编码、名称、所属工厂、所属装置、所属单元、介质、危害等级、通盲状态（颜色标签）、占用状态（颜色标签）、操作按钮

