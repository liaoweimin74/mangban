# Retrospective: blindplate-module-split

## 计划 vs 实际

| 计划 | 实际 |
|------|------|
| 创建 `mangban-blindplate` 模块 | 创建 `blindplate` 模块（去掉了 mangban- 前缀） |
| 清理 `mangban-system` 盲板代码 | 完成 |
| 前端 `views/process/` → `views/blindplate/` | 完成 |
| 路由 `/process/` → `/blindplate/` | 完成 |
| 菜单数据更新 | data.sql 中同步更新了 path/component/permission |

## 执行偏差

- **模块命名**：`mangban-blindplate` → `blindplate`（去掉 mangban- 前缀），后续又将所有模块的 `mangban-` 前缀一并去掉
- **跨模块依赖**：`blindplate` 仍然依赖 `system`（因为引用了 `SysLocation`），这是已知 trade-off，需后续将 `SysLocation` 移到 `common` 或独立为 `location` 模块
- **WSL 网络问题**：前后端多次因为 WSL 进程管理问题需要重新启动，与变更本身无关

## 验证结果

- `mvn test`：BUILD SUCCESS（5 模块全部通过）
- `vitest run`：44 tests passed，1 个预存 e2e 失败（Playwright 环境问题）
- 前后端手动启动后盲板页面可正常访问

## 改进建议

1. `SysLocation` 应移到 `common` 模块或独立为 `location` 模块，消除 `blindplate` 对 `system` 的依赖
2. data.sql 中菜单数据路径变更应与代码目录重命名同步进行，避免手动 UPDATE 被 INSERT IGNORE 覆盖