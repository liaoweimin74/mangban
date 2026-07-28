# Retrospective: isolation-point-management

> Written: 2026-07-29
> Commit range: e2fbeba (HEAD ~ 8 commits from fork point)
> Worktree: .worktrees/isolation-point-management/
> Branch: feature/isolation-point-management

## 0. Evidence

- **Commits**: 8 (from fork point)
- **Tasks done**: 35/35
- **New external dependencies**: none
- **Bugs encountered**: none
- **Test coverage**: 44 passed, 3 test files

## 1. Wins

- 完整实现隔离点管理模块：装置层级树形管理、隔离点台账 CRUD、状态管理、状态台账总览
- 后端遵循现有分层模式（Controller→Service→Repository→Entity），DTO/VO 全部使用 Java Record
- 前端全部使用 SearchTable 业务组件，零自定义页面，维护性高
- SearchTable 组件布局优化：查询栏/工具栏/表头/分页固定，数据区独立滚动
- vite 添加 usePolling 解决 WSL 下 HMR 不刷新问题
- 44 个前端测试全部通过

## 2. Misses

- 多个前端页面间的交互确认（审批/销票页面的关联）尚未深入
- SearchTable 滚动布局的样式调整有反复，template 和 style 的同步修改不够一次到位

## 3. Plan deviations

无显著偏差。

## 4. Skill / workflow compliance

| Skill | Used |
|---|---|
| brainstorming | ✓ |
| writing-plans | ✓ |
| using-git-worktrees | ✓ |
| subagent-driven-development | ✓ |
| finishing-a-development-branch | ✓ |

## 5. Surprises

- vite HMR 在 WSL 下不刷新，需要 usePolling 配置

## 6. Promote candidates

- WSL + vite HMR 问题：vite.config.ts 加 server.watch.usePolling: true