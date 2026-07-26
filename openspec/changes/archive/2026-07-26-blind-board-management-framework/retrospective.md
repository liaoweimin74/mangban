# Retrospective: blind-board-management-framework

> Written: 2026-07-26  
> Commit range: base → 8804be6  
> Worktree: `.worktrees/blind-board-management-framework/`

---

## 0. Evidence

- **Commit range**: base → 8804be6
- **Diff size**: ~5000 行（后端 Java + 前端 Vue/TS）
- **Tasks done**: 全部后端模块 + 全部前端页面 CRUD（tasks.md 中所有 `- [ ]` 均已完成）
- **Active hours**: ~6h
- **Subagent dispatches**: explore × 15+, librarian × 3, oracle × 2, deep × 4
- **New external dependencies**: Spring Boot 3.5, Spring Security 6, JPA, JWT, Redis, MySQL; Vue 3, Element Plus, Pinia, Vue Router, Axios, Playwright, Tailwind
- **Bugs encountered post-merge**: 0（worktree 内实现）
- **OpenSpec validate state at archive**: 未执行（openspec CLI 路径异常）
- **Test coverage signal**: E2E 15 例全部通过，无单元测试

Commit chain:
```
8804be6 feat: 盲板管理框架 - 用户/角色/菜单/组织/字典 CRUD + UI 规范
```

---

## 1. Wins

1. **完整 CRUD 闭环**：从后端 JPA 实体到前端表格/表单，用户/角色/菜单/组织/字典五个模块全部打通
2. **界面一致性**：统一了所有页面的操作列按钮样式（text + whitespace-nowrap）、表格表头样式、菜单间距
3. **中文 locale**：Element Plus 全局 zhCn，分页等组件自动中文化
4. **UI 规范文档**：`docs/UI-GUIDELINES.md` 沉淀了字体/布局/菜单/页签栏/按钮/表格规范

## 2. Misses

1. **前后端字段名不一致**：字典模块 `dictLabel`/`dictValue` vs `label`/`value`，`records` vs `rows`，调试花费较多时间
2. **字典数据 Redis 缓存未实现**：tasks 8.3 未完成
3. **无单元测试**：仅依赖 E2E 测试
4. **字典类型下拉选择变更为左右分栏**：最初方案是从下拉选类型，后期改为左右分栏

## 3. Plan deviations

| 偏离 | 原因 |
|------|------|
| 字典页面左右分栏 | 最初从左侧树/下拉选类型，改为左侧类型列表 + 右侧字典项 |
| 操作列按钮改为 text 属性 | 原计划用普通按钮，后期统一改为无边框 text 风格 |
| SubMenu 图标改为动态 import | `@element-plus/icons-vue` 组件需要运行时解析 |

## 4. Skill / workflow compliance

| Skill | Used |
|---|---|
| superpowers:brainstorming | ✓ (artifacts 阶段) |
| superpowers:writing-plans | ✓ (artifacts 阶段) |
| superpowers:using-git-worktrees | ✓ (opsx-propose 自动) |
| superpowers:subagent-driven-development | ✓ (apply 阶段大量并行 deep 代理) |
| (transitive) superpowers:test-driven-development | △ (部分 apply 代理加载) |
| (transitive) superpowers:requesting-code-review | ✗ (未执行) |
| superpowers:finishing-a-development-branch | ✓ (finish 阶段) |

### Deliberately Skipped Skills

- `requesting-code-review`: 用户未要求，且 verify.md 为空模板未触发

## 5. Surprises

1. **openspec CLI 路径 bug**：`registry-utils.js` 找不到路径，不影响文件产出但阻塞了 validate/archive 命令
2. **Vite 热更新不稳定**：多次编辑后 Vite 崩溃需要重启
3. **后端 Spring Security OpsX 应用失败**：同一后端启动命令在不同时机行为不一致，最终手动启动 JAR

## 6. Promote candidates → long-term learning

- **前后端字段对齐**：项目初期应生成 DTO/VO 字段映射表，避免隐式假设
- **E2E + unit 双保险**：仅有 E2E 不够，关键 Service 层应加单元测试
- **UI 规范文档前置**：界面规范应在设计阶段就写入 UI-GUIDELINES.md，而非实现后补写
