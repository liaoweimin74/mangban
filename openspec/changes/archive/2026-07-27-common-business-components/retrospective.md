# Retrospective: common-business-components

> Written: 2026-07-27
> Commit range: 13 commits (f5b4de3..76ab51d)
> Worktree: `.worktrees/common-business-components/`

---

## 0. Evidence

- **Commit range**: 13 commits
- **Diff size**: 3 新组件 + 类型定义 + 测试 + UserPageEx + 路由修改 + tasks/design/spec 文档
- **Tasks done**: 18/18 ✅
- **Active hours**: ~3h
- **Subagent dispatches**: 6+ deep agents for component implementation, 3+ explore agents
- **New external dependencies**: 无（仅使用已有 Element Plus）
- **Bugs encountered post-merge**: 无
- **OpenSpec validate state at archive**: verify 通过（1 CRITICAL → 修复后 0）
- **Test coverage signal**: 36/36 单元测试通过（FormBuilder 15, SearchTable 11, ReferencePicker 10），1 e2e 预存失败

---

## 1. Wins

- **声明式配置模式有效**：UserPageEx 用 ~140 行声明式配置替代了原 UserPage 的 200+ 行模板+逻辑混合代码
- **TDD 严格执行**：每个组件先写测试再实现，3 个组件共 36 个测试全部通过
- **actionButtons 合并逻辑**：formConfig 默认按钮 + 自定义按钮合并，避免覆盖丢失 CRUD 按钮
- **computed 响应式设计**：searchFields 和 formConfig 用 computed 包裹，异步数据加载后自动响应树/下拉选项

## 2. Misses

- **FormBuilder layout 首版未实现**：design 要求 single/double/grid 三种布局，初版只定义了 prop 未使用。verify 发现后修复，改用 render-field 内联组件 + el-row/el-col
- **ReferencePicker 未用 Teleport**：design 决策要求 Teleport，初版用 el-dialog 无 append-to-body。verify 修复后加上
- **SearchTable fetchList 未暴露**：UserPageEx 中 handleStatusChange 后需刷新列表，发现需在 SearchTable 添加 defineExpose

## 3. Plan deviations

- **Subagent 阻塞后改主代理顺序执行**：部分 deep subagent 超时阻塞，改用主代理顺序执行。后期组件实现仍有并行调度但失败率高
- **openspec 命令在 Windows PowerShell 环境不稳定**：openspec list/status/archive 等命令因 PowerShell 路径问题无法运行，改为手动文件操作

## 4. Skill / workflow compliance

| Skill | Used |
|-------|------|
| superpowers:brainstorming | ✓ (planning) |
| superpowers:writing-plans | ✓ (planning) |
| superpowers:using-git-worktrees | ✓ |
| superpowers:test-driven-development | ✓ (each component) |
| superpowers:verification-before-completion | ✓ (verify 2 passes) |
| superpowers:finishing-a-development-branch | ✓ (current) |

### Deliberately Skipped Skills

- superpowers:requesting-code-review — verify 替代审查
- superpowers:subagent-driven-development — subagent 阻塞率高，改主代理执行

## 5. Surprises

- **Windows 下 openspec CLI 不兼容**：多个命令因 PowerShell 路径拼接失败，需手动操作文件系统和 registry JSON
- **append-to-body 破坏 jsdom 测试**：el-dialog append-to-body 后弹窗渲染到 document.body，测试中的 wrapper.find 失效，需改为 document.body.querySelector
- **layout 实现迫使 FormBuilder 模板重构**：添加 el-row/el-col 后 v-if/v-else-if 链重复 3 次，改用 render-field 内联 h() 组件统一字段渲染

## 6. Promote candidates → long-term learning

- **声明式配置组件模式**：3 个组件（SearchTable / FormBuilder / ReferencePicker）可作为其他 CRUD 页面重构的模板
- **computed + ref 解决异步数据响应式**：当 props 依赖异步数据时，用 computed 包裹确保响应式更新，优于手动赋值