## ADDED Requirements

### Requirement: 隔离点创建
系统 SHALL 支持创建隔离点台账记录，创建时校验 unit_id 必须对应 type=UNIT 的 location。

#### Scenario: 正常创建隔离点
- **WHEN** 客户端 POST /api/isolation-points，请求体包含完整字段（code, name, unitId, medium, pressureRating, temperatureRating, hazardLevel, pointType, blindSpec, equipmentTag, pipelineNo）
- **THEN** 系统创建成功，返回隔离点完整信息（含 id），默认 status=OPEN, occupyStatus=FREE

#### Scenario: unitId 不是单元
- **WHEN** 客户端 POST /api/isolation-points，unitId 对应的 location.type 不是 UNIT
- **THEN** 系统返回 400 错误，提示"隔离点必须挂在单元下"

#### Scenario: 编码重复
- **WHEN** 客户端 POST /api/isolation-points，code 与已有隔离点重复
- **THEN** 系统返回 400 错误，提示"编码已存在"

#### Scenario: 必填字段缺失
- **WHEN** 客户端 POST /api/isolation-points，code 或 name 缺失
- **THEN** 系统返回 400 错误，提示具体缺失字段

---

### Requirement: 隔离点分页查询
系统 SHALL 支持按多条件筛选分页查询隔离点列表。

#### Scenario: 无条件分页查询
- **WHEN** 客户端 GET /api/isolation-points?page=1&size=20
- **THEN** 系统返回第 1 页隔离点列表，含 total、page、size、rows，rows 中包含 unitName（关联的单元名称）

#### Scenario: 按装置筛选（通过 location 层级）
- **WHEN** 客户端 GET /api/isolation-points?plantId=<装置id>&page=1&size=20
- **THEN** 系统返回该装置下所有单元关联的隔离点列表

#### Scenario: 按介质筛选
- **WHEN** 客户端 GET /api/isolation-points?medium=蒸汽&page=1&size=20
- **THEN** 系统返回介质为"蒸汽"的隔离点列表

#### Scenario: 按危害等级筛选
- **WHEN** 客户端 GET /api/isolation-points?hazardLevel=A&page=1&size=20
- **THEN** 系统返回危害等级为 A 的隔离点列表

#### Scenario: 按状态筛选
- **WHEN** 客户端 GET /api/isolation-points?status=BLIND&page=1&size=20
- **THEN** 系统返回当前状态为盲板的隔离点列表

#### Scenario: 多条件组合筛选
- **WHEN** 客户端 GET /api/isolation-points?unitId=<单元id>&status=OPEN&hazardLevel=B&page=1&size=20
- **THEN** 系统返回同时满足所有条件的隔离点列表

#### Scenario: 按编码模糊搜索
- **WHEN** 客户端 GET /api/isolation-points?code=IP-&page=1&size=20
- **THEN** 系统返回编码包含"IP-"的隔离点列表

---

### Requirement: 隔离点详情查询
系统 SHALL 支持按 ID 查询隔离点详细信息。

#### Scenario: 查询存在的隔离点
- **WHEN** 客户端 GET /api/isolation-points/{id}
- **THEN** 系统返回隔离点完整信息，含 unitName、plantName、factoryName（关联的完整层级路径）

#### Scenario: 查询不存在的隔离点
- **WHEN** 客户端 GET /api/isolation-points/999999
- **THEN** 系统返回 404 错误

#### Scenario: 查询已删除的隔离点
- **WHEN** 客户端 GET /api/isolation-points/{deletedId}
- **THEN** 系统返回 404 错误（软删除记录不返回）

---

### Requirement: 隔离点更新
系统 SHALL 支持更新隔离点台账信息。

#### Scenario: 更新基本信息
- **WHEN** 客户端 PUT /api/isolation-points/{id}，请求体包含更新后的字段（name, medium, pressureRating 等）
- **THEN** 系统更新成功，返回更新后的隔离点信息

#### Scenario: 更新关联单元
- **WHEN** 客户端 PUT /api/isolation-points/{id}，请求体包含 unitId=<新单元id>
- **THEN** 系统校验新 unitId 对应 type=UNIT，更新成功

#### Scenario: 更新不存在的隔离点
- **WHEN** 客户端 PUT /api/isolation-points/999999
- **THEN** 系统返回 404 错误

---

### Requirement: 隔离点删除
系统 SHALL 支持软删除隔离点。

#### Scenario: 删除隔离点
- **WHEN** 客户端 DELETE /api/isolation-points/{id}
- **THEN** 系统软删除成功（is_deleted=1），后续查询不再返回该记录

#### Scenario: 删除不存在的隔离点
- **WHEN** 客户端 DELETE /api/isolation-points/999999
- **THEN** 系统返回 404 错误