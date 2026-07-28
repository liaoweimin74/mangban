## Design Summary

隔离点管理模块本期实现核心功能：装置层级结构（工厂→装置→单元树形管理）+ 隔离点台账 CRUD + 状态管理（通/盲、占用/锁定简单字段）+ 状态台账总览（筛选+状态颜色标记）。位置标注和二维码管理后续迭代。

### 数据模型
- `sys_location`：装置层级表（单表树形，parentId 自引用，type 字段区分 FACTORY/PLANT/UNIT）
- `sys_isolation_point`：隔离点台账表（关联 unit_id → sys_location.id，含介质/压力/温度/危害等级/点位类型/盲板规格/设备位号/管线号/通盲状态/占用状态）

### 后端
- 完全遵循现有模式：Entity 继承 BaseEntity，Repository 继承 JpaRepository + JpaSpecificationExecutor，Service 接口+实现，Controller RESTful，DTO/VO 用 Java Record
- LocationController (`/api/locations`) + IsolationPointController (`/api/isolation-points`)
- 包路径：`com.mangban.system.domain.entity`（沿用现有包）

### 前端
- 三个页面全部使用 SearchTable 业务组件（参考 UserPage/OrgPage 模式）
- 装置层级管理页：树形表格（`treeProps` + `showSearch: false`）
- 隔离点台账页：分页列表 + CRUD 弹窗
- 状态台账总览页：分页列表 + 状态列颜色标签（OPEN=绿/BLIND=红，OCCUPIED=橙/FREE=灰）

## Alternatives Considered

### 方案 A：完全复用业务组件（采用）
- **做法**：前端三个页面全部使用 SearchTable 组件，通过配置 searchFields/columns/formConfig/fetchApi 完成，不写自定义页面
- **优点**：开发效率最高，与现有代码风格完全一致，SearchTable 已内置分页/搜索/CRUD/权限控制/操作按钮
- **缺点**：复杂表单联动和异常高亮逻辑受限于 slot 机制
- **为何采用**：隔离点台账和状态总览本质是同一数据的不同视图，slot 足以处理颜色标记

### 方案 B：混合模式（未采用）
- **做法**：装置层级和台账用 SearchTable，状态总览页自定义表格
- **优点**：异常高亮/颜色标记更灵活
- **缺点**：不一致，开发量略多
- **为何未采用**：SearchTable slot 机制可满足需求，保持一致性更重要

## Agreed Approach

采用方案 A：完全复用现有 SearchTable + FormBuilder + ReferencePicker 业务组件，后端完全遵循现有分层模式。

### 层级结构实现
- 工厂/装置/单元用一张树形表（`sys_location`），parentId 自引用，type 字段区分层级
- 隔离点单独一张表（`sys_isolation_point`），unit_id 外键关联到单元级 location

### 状态管理
- 状态为简单字段（status: OPEN/BLIND，occupyStatus: OCCUPIED/FREE），PC 端直接编辑，不走审批流程

### 开发范围
- 装置层级结构 CRUD（树形管理）
- 隔离点台账 CRUD（含按装置/单元/介质/危害等级/状态筛选）
- 状态管理（通/盲状态变更 + 占用/释放）
- 状态台账总览（筛选 + 状态颜色标记）
- 不包含：位置标注、二维码管理、Excel 批量导入

## Key Decisions

1. **装置层级用单表树形**：避免多表关联复杂度，参考 SysOrganization 的 parentId 自引用模式
2. **隔离点挂在单元下**：unit_id 外键关联，不是挂在任意层级
3. **状态简单字段**：不走工作流，本期聚焦台账管理
4. **前端全用 SearchTable**：状态总览页通过 slot 实现颜色标记，不自定义页面
5. **后端包路径沿用**：放在 `mangban-system` 模块，不新建独立模块

## Open Questions

- 无