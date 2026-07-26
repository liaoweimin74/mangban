<!--
Spec for organization-management capability: 组织机构管理
-->

## ADDED Requirements

### Requirement: 组织机构树形查询
系统 SHALL 支持以树形结构查询组织机构列表。

#### Scenario: 查询组织机构树
- **WHEN** 管理员请求组织机构列表
- **THEN** 系统返回树形结构的组织机构数据，包含组织 ID、名称、编码、上级组织、排序、状态

#### Scenario: 查询根节点
- **WHEN** 管理员请求顶级组织机构
- **THEN** 系统返回所有顶级组织及其子组织树

### Requirement: 创建组织机构
系统 SHALL 支持管理员创建新的组织机构节点。

#### Scenario: 创建顶级组织
- **WHEN** 管理员创建一个顶级组织机构
- **THEN** 系统创建组织，parent_id 为空，返回创建成功

#### Scenario: 创建子组织
- **WHEN** 管理员选择一个上级组织后创建子组织
- **THEN** 系统创建组织，parent_id 指向上级组织，返回创建成功

#### Scenario: 组织编码已存在
- **WHEN** 管理员创建组织时填写的编码已存在
- **THEN** 系统返回 400 错误，提示"组织编码已存在"

### Requirement: 修改组织机构
系统 SHALL 支持管理员修改组织机构信息。

#### Scenario: 修改组织成功
- **WHEN** 管理员修改组织的名称、编码、排序、状态
- **THEN** 系统更新组织信息，返回修改成功

### Requirement: 删除组织机构
系统 SHALL 支持管理员删除组织机构节点。

#### Scenario: 删除无子组织的组织
- **WHEN** 管理员删除一个没有子组织的组织
- **THEN** 系统删除该组织，返回删除成功

#### Scenario: 删除有子组织的组织
- **WHEN** 管理员删除一个有子组织的组织
- **THEN** 系统返回 400 错误，提示"存在子组织，不能删除"

#### Scenario: 删除有用户的组织
- **WHEN** 管理员删除一个绑定了用户的组织
- **THEN** 系统返回 400 错误，提示"组织下存在用户，不能删除"