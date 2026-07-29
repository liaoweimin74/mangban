## ADDED Requirements

### Requirement: 前端盲板页面 SHALL 从 views/process/ 迁移到 views/blindplate/
将 `mangban-ui/src/views/process/` 目录（含 IsolationPointManagePage.vue 和 IsolationPointStatusPage.vue）重命名为 `mangban-ui/src/views/blindplate/`，所有 import 引用同步更新。

#### Scenario: 目录重命名成功
- **WHEN** 执行 `ls mangban-ui/src/views/blindplate/`
- **THEN** 列出 IsolationPointManagePage.vue 和 IsolationPointStatusPage.vue

#### Scenario: 无旧目录引用残留
- **WHEN** 在 mangban-ui/src 中搜索 `views/process`
- **THEN** 搜索结果为空

### Requirement: 前端路由 SHALL 将 /process/* 变更为 /blindplate/*
`router/index.ts` 中 `/process/isolation-points` → `/blindplate/isolation-points`，`/process/isolation-points/status` → `/blindplate/isolation-points/status`，component import 路径同步更新。

#### Scenario: 路由配置正确
- **WHEN** 访问 `/blindplate/isolation-points`
- **THEN** 渲染 IsolationPointManagePage.vue

#### Scenario: 旧路由返回 404
- **WHEN** 访问 `/process/isolation-points`
- **THEN** 返回 404

### Requirement: 菜单数据 SHALL 同步更新路由路径
执行 SQL UPDATE 语句更新数据库菜单表中 route_path 字段，将 `/process/` 前缀替换为 `/blindplate/`。

#### Scenario: 菜单数据更新
- **WHEN** 查询菜单表 WHERE route_path LIKE '/process/%'
- **THEN** 返回 0 条记录

#### Scenario: 新菜单路径生效
- **WHEN** 查询菜单表 WHERE route_path LIKE '/blindplate/%'
- **THEN** 返回对应菜单记录