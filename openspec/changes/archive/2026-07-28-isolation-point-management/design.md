## Context

当前盲板管理系统基于 Spring Boot 3.5 + JPA + Vue3 + Element Plus 构建，已实现用户、角色、组织、菜单等基础管理功能。项目采用严格的分层架构：Controller → Service(接口) → ServiceImpl → Repository → Entity，DTO/VO 统一使用 Java Record，前端通过 SearchTable/FormBuilder/ReferencePicker 三个业务组件实现声明式页面开发。

隔离点管理是盲板作业系统的核心主数据模块，本期实现装置层级结构和隔离点台账的基础 CRUD 及状态管理。

## Goals / Non-Goals

**Goals:**
- 建立工厂→装置→单元三级树形层级结构，支持增删改查和树形展示
- 建立隔离点台账，包含编码、名称、介质、压力/温度等级、危害等级、盲板规格、关联设备等字段
- 实现隔离点通/盲状态和占用/锁定状态的维护
- 提供全厂状态台账总览视图，支持多维度筛选和状态颜色标记
- 完全遵循现有代码风格和架构模式，复用前端业务组件

**Non-Goals:**
- 不实现位置标注（三维模型/流程图标注）
- 不实现二维码生成/打印
- 不实现 Excel 批量导入
- 不实现状态工作流（审批流转）
- 不新增独立 Maven 模块（放在 mangban-system 下）

## Decisions

### 1. 装置层级用单表树形（parentId 自引用 + type 字段）
- **选择**：`sys_location` 表，parent_id 自引用，type 字段区分 FACTORY/PLANT/UNIT
- **替代方案**：四张独立表（factory/plant/unit/isolation_point）
- **理由**：PRD 中层级不超过 4 级，单表树形比多表关联更简单；参考 SysOrganization 的成熟模式；前端树形组件天然支持 parentId 数据结构；后续如需增减层级无需改表结构

### 2. 隔离点挂在单元下（unit_id → sys_location.id）
- **选择**：`sys_isolation_point.unit_id` 外键关联到 `sys_location.id`（约束 type=UNIT）
- **替代方案**：隔离点也可挂在装置或工厂下
- **理由**：PRD 明确"工厂→装置→单元→隔离点"层级；单元是最小管理粒度，隔离点挂在单元下符合物理实际

### 3. 状态为简单字段（非工作流）
- **选择**：`status`（OPEN/BLIND）和 `occupy_status`（OCCUPIED/FREE）为简单 VARCHAR 字段，通过 PUT 接口直接更新
- **替代方案**：状态变更走审批流程
- **理由**：本期聚焦台账管理，简化实现；PRD 明确"关注其基本台账与最终状态，不管理生命周期过程"；后续可升级为工作流

### 4. 前端全用 SearchTable 组件
- **选择**：三个页面（层级管理、台账、总览）全部使用 SearchTable，通过配置完成
- **替代方案**：状态总览页自定义表格
- **理由**：SearchTable slot 机制支持列自定义渲染（颜色标签）；保持代码一致性；减少开发量

### 5. 所有代码放在 mangban-system 模块
- **选择**：不新建独立 Maven 模块，Entity/Repository/Service/Controller 放在 mangban-system 下
- **替代方案**：新建 mangban-equipment 模块
- **理由**：隔离点管理属于核心业务；当前系统规模不大（9 个 Entity），暂不需要模块拆分；后续业务增长时可再独立

### 6. 状态变更记录
- **选择**：状态变更时记录到 `sys_isolation_point` 表的 remark 字段，暂不建独立的状态历史表
- **理由**：PRD 提到"支持状态变更历史追溯"，但本期先简化为 remark 追加；后续可单独建 `sys_isolation_point_status_log` 表

### 7. 后端 Java Record 的 DTO/VO 模式
- **选择**：创建/更新 DTO 用 Java Record 分离定义
- **理由**：遵循现有模式（OrganizationCreateRequest / OrganizationUpdateRequest 就是分离的）；创建有必填校验，更新全可选

## Risks / Trade-offs

- **[风险] 单表树形的深度限制**：parentId 自引用在深层嵌套时查询效率下降 → **缓解**：层级最多 4 级，JPA 懒加载 + 前端懒展开足够
- **[风险] SearchTable slot 复杂渲染限制**：状态总览页如需要复杂交互（如点击状态弹出详情），slot 可能不够 → **缓解**：本期只需颜色标签（简单 slot），后续如需复杂交互可降级为自定义表格
- **[风险] 状态变更无独立历史表**：remark 追加方式不利于精确追溯 → **缓解**：本期 remark 够用，后续建独立日志表成本低
- **[风险] unit_id 无数据库级外键约束**：JPA 关联可能产生脏数据 → **缓解**：Service 层校验 unit_id 对应的 location.type=UNIT；后续可加数据库外键