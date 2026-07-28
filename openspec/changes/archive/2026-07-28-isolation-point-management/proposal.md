## Why

当前盲板管理系统已具备用户、角色、组织、菜单等基础功能，但核心业务模块尚未建立。隔离点作为盲板作业的基础数据——所有盲板安装/拆除操作都围绕隔离点展开。缺少隔离点台账，后续的工艺处置方案编制、隔离方案编制、盲板作业管理等模块都无法落地。本期优先建设隔离点管理模块，建立工厂→装置→单元→隔离点的层级数据基础，为后续业务模块提供数据支撑。

## What Changes

新增隔离点管理模块，包含以下变更：

**装置层级结构管理**
- From: 系统中无工厂/装置/单元数据结构
- To: 新增 `sys_location` 表，支持工厂→装置→单元三级树形层级管理（单表 parentId 自引用）
- Reason: 隔离点必须挂在装置层级下，层级结构是所有后续模块的基础
- Impact: 非破坏性新增，不影响现有功能

**隔离点台账管理**
- From: 系统中无隔离点数据
- To: 新增 `sys_isolation_point` 表，支持隔离点基本档案的增删改查，关联到单元级 location
- Reason: 隔离点台账是盲板作业的核心主数据
- Impact: 非破坏性新增

**隔离点状态管理**
- From: 无状态管理能力
- To: 隔离点支持通/盲状态（OPEN/BLIND）和占用状态（OCCUPIED/FREE）的维护，状态变更记录
- Reason: 状态是现场作业决策的关键依据
- Impact: 非破坏性新增

**状态台账总览**
- From: 无全局状态视图
- To: 新增状态台账总览页面，支持按装置、介质类型、危害等级、占用状态筛选，状态列颜色标记（OPEN=绿、BLIND=红、OCCUPIED=橙、FREE=灰）
- Reason: 管理人员需要全局视角监控全厂隔离点状态
- Impact: 非破坏性新增

## Capabilities

### New Capabilities

- `location-management`: 装置层级结构管理——工厂→装置→单元树形结构的增删改查，支持树形展示和层级选择
- `isolation-point-crud`: 隔离点台账管理——隔离点基本档案（编码、名称、所属单元、介质、压力/温度等级、危害等级、点位类型、适配盲板规格、关联设备位号/管线号）的增删改查，支持按装置/单元/介质/危害等级/状态筛选
- `isolation-point-status`: 隔离点状态管理——通/盲状态变更、占用/释放操作，状态变更记录
- `isolation-point-overview`: 状态台账总览——全厂隔离点状态一览，按装置、介质类型、危害等级、占用状态筛选，异常状态颜色高亮

### Modified Capabilities

无——本变更为全新模块，不修改现有 capability。

## Impact

### 后端影响
- 新增 `sys_location` 和 `sys_isolation_point` 两张表（需在 `schema.sql` 中添加 DDL）
- 新增 2 个 Entity、2 个 Repository、2 个 Service 接口+实现、2 个 Controller
- 新增 DTO/VO 各约 4-6 个（创建/更新/查询/树节点/列表）
- 无需修改现有代码，无需新增 Maven 依赖

### 前端影响
- 新增 3 个页面组件（`views/process/LocationPage.vue`、`IsolationPointPage.vue`、`IsolationPointStatusPage.vue`）
- 新增 2 个 API 文件（`api/location.ts`、`api/isolation-point.ts`）
- 新增类型定义文件（`types/location.ts`、`types/isolation-point.ts`）
- 路由新增 3 条（`/process/locations`、`/process/isolation-points`、`/process/isolation-points/status`）
- 数据库菜单表需添加对应菜单和权限记录

### 回滚计划
- 后端：删除两张新表（DROP TABLE），删除新增的 Java 文件即可，无现有代码修改
- 前端：删除新增的页面/API/类型文件，移除路由配置中的 3 条路由
- 风险：低——纯新增模块，无破坏性变更

### 影响团队
- 后端开发：新增模块，不影响现有接口
- 前端开发：新增页面，不影响现有页面
- 测试：需对新接口和新页面进行功能测试
- 运维：需执行 DDL 建表