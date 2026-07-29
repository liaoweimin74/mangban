# Proposal: 盲板模块拆分 + 前端目录重命名

## Why

当前盲板业务（IsolationPoint）的 controller/service/repository/entity 全部位于 mangban-system 模块中，与用户/角色/菜单/字典等系统管理功能耦合在同一模块。前端盲板页面放在 `views/process/` 目录下，目录名"process"无法反映业务含义。随着盲板业务复杂度增加，这种耦合导致模块边界模糊、维护困难。将盲板独立为 mangban-blindplate 模块、前端目录重命名为 blindplate，使代码结构与业务领域对齐。

## What Changes

**后端模块拆分**
- From: 盲板代码在 `mangban-system` 模块中，与系统管理代码混在一起
- To: 新建 `mangban-blindplate` Maven 子模块，迁移所有 IsolationPoint 相关代码
- Reason: 模块职责分离，盲板业务独立演进
- Impact: 需要调整 mangban-admin/pom.xml 添加依赖

**前端目录重命名**
- From: `views/process/` 目录，路由 `/process/*`
- To: `views/blindplate/` 目录，路由 `/blindplate/*`
- Reason: 目录名与业务含义一致
- Impact: 路由配置变更，需同步更新菜单数据中路由路径

## Capabilities

### New Capabilities
- `blindplate-module`: 盲板独立后端模块，包含 controller/service/repository/entity/dto/vo

### Modified Capabilities
- `blindplate-frontend`: 前端盲板页面从 `views/process/` 迁移到 `views/blindplate/`，路由路径更新

## Impact

- **后端代码迁移**: IsolationPointController/Service/Repository/Entity/DTO/VO 从 mangban-system 移到 mangban-blindplate
- **pom.xml 变更**: mangban-admin/pom.xml 新增 mangban-blindplate 依赖
- **前端重命名**: views/process/ → views/blindplate/，router/index.ts 路由路径更新
- **菜单数据**: 数据库中菜单表的路由路径需更新 /process → /blindplate
- **回滚计划**: 保留 mangban-system 中的盲板代码，切换回旧模块只需还原 pom.xml 和路由配置
- **受影响团队**: 后端开发需更新 IDE 模块配置；前端开发需更新 import 路径引用