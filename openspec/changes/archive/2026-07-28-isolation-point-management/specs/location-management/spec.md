## ADDED Requirements

### Requirement: 装置层级树形查询
系统 SHALL 支持查询完整的工厂→装置→单元树形结构，以 parentId 自引用方式组织，按 sort_order 排序。

#### Scenario: 获取完整装置树
- **WHEN** 客户端请求 GET /api/locations/tree
- **THEN** 系统返回嵌套树形 JSON，根节点为所有工厂（type=FACTORY），子节点递归包含装置（type=PLANT）和单元（type=UNIT），按 sort_order 升序排列

#### Scenario: 空数据查询
- **WHEN** 数据库中无任何 location 记录
- **THEN** 系统返回空数组 []

---

### Requirement: 装置层级创建
系统 SHALL 支持创建工厂、装置或单元节点，创建时校验父级类型约束。

#### Scenario: 创建工厂节点
- **WHEN** 客户端 POST /api/locations，请求体包含 name="乙烯厂", type="FACTORY", parentId=null
- **THEN** 系统创建成功，返回新节点完整信息（含 id），parent_id 为 NULL

#### Scenario: 在工厂下创建装置
- **WHEN** 客户端 POST /api/locations，请求体包含 name="裂解装置", type="PLANT", parentId=<工厂id>
- **THEN** 系统校验 parentId 对应节点的 type 为 FACTORY，校验通过后创建成功

#### Scenario: 在装置下创建单元
- **WHEN** 客户端 POST /api/locations，请求体包含 name="分离单元", type="UNIT", parentId=<装置id>
- **THEN** 系统校验 parentId 对应节点的 type 为 PLANT，校验通过后创建成功

#### Scenario: 违反层级约束
- **WHEN** 客户端尝试在单元（type=UNIT）下创建子节点
- **THEN** 系统返回 400 错误，提示"单元下不可创建子节点"

#### Scenario: 必填字段缺失
- **WHEN** 客户端 POST /api/locations，请求体缺少 name 或 type 字段
- **THEN** 系统返回 400 错误，提示具体缺失字段

---

### Requirement: 装置层级更新
系统 SHALL 支持修改装置节点的名称、编码、排序和备注信息，不允许修改 type 字段。

#### Scenario: 更新节点信息
- **WHEN** 客户端 PUT /api/locations/{id}，请求体包含 name="新名称", sortOrder=2
- **THEN** 系统更新成功，返回更新后的节点信息

#### Scenario: 尝试修改 type
- **WHEN** 客户端 PUT /api/locations/{id}，请求体包含 type="FACTORY"（与原有 type 不同）
- **THEN** 系统忽略 type 字段或返回 400 错误

#### Scenario: 更新不存在的节点
- **WHEN** 客户端 PUT /api/locations/999999
- **THEN** 系统返回 404 错误

---

### Requirement: 装置层级删除
系统 SHALL 支持软删除装置节点，删除时校验是否有子节点或关联的隔离点。

#### Scenario: 删除无子节点的单元
- **WHEN** 客户端 DELETE /api/locations/{unitId}，该单元下无子节点且无关联隔离点
- **THEN** 系统软删除成功（is_deleted=1）

#### Scenario: 删除有子节点的装置
- **WHEN** 客户端 DELETE /api/locations/{plantId}，该装置下存在子节点（单元）
- **THEN** 系统返回 400 错误，提示"存在子节点，请先删除子节点"

#### Scenario: 删除有关联隔离点的单元
- **WHEN** 客户端 DELETE /api/locations/{unitId}，该单元下有关联的隔离点
- **THEN** 系统返回 400 错误，提示"存在关联隔离点，请先删除隔离点"

---

### Requirement: 装置层级分页查询
系统 SHALL 支持按条件分页查询装置节点列表。

#### Scenario: 按 type 筛选
- **WHEN** 客户端 GET /api/locations?type=PLANT&page=1&size=10
- **THEN** 系统返回 type=PLANT 的分页列表，含 total、page、size、rows

#### Scenario: 按名称模糊搜索
- **WHEN** 客户端 GET /api/locations?name=裂解&page=1&size=10
- **THEN** 系统返回名称包含"裂解"的节点分页列表