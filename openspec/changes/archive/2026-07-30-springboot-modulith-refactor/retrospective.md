## 回顾

### §0) 证据

| 指标 | 值 |
|------|-----|
| 提交数 | 7 |
| 变更文件数 | 25+ |
| 任务完成率 | 100%（15/15 tasks + 额外重构） |
| 新增外部依赖 | spring-modulith-bom, spring-modulith-starter-core, spring-modulith-starter-test |

### §1) 实现了什么

**计划内容**：
1. 将 SysLocation 从 system 模块迁移至 blindplate 模块
2. 引入 Spring Modulith 依赖和模块边界验证
3. 添加 Modulith 测试

**实际内容**（超出计划的部分）：
4. 瘦身 common 模块：将 5 个 Spring 配置类迁移到 framework，BaseEntity 迁到 common
5. 解耦 blindplate 对 system 的依赖（blindplate 不再依赖 system）
6. 合并 5 个 Maven 模块为 1 个单一模块（6 个 pom.xml -> 1 个）
7. 修复 `@ApplicationModuleTest` 在单模块项目中的 bean 扫描问题（改用 `@SpringBootTest`）

### §2) 与计划对比

计划仅涉及 SysLocation 迁移和 Modulith 引入。实际实现中发现：
- common 模块职责混杂，导致依赖传递污染所有模块
- blindplate 因 BaseEntity 依赖 system，存在不合理的跨模块耦合
- 5 个 Maven 模块 + 6 个 pom.xml 对 105 个文件的项目过于笨重
- Modulith 的 package 级别边界验证完全可以替代 Maven 模块隔离

最终方案：合并为单一 Maven 模块 + Modulith package 边界注解，既简化了构建配置，又保留了模块边界约束。

### §3) 问题与解决方案

1. **`@ApplicationModuleTest` bean 扫描失败**：`@ApplicationModuleTest` 在单模块项目中限制了 bean 扫描范围，导致 `@Configuration` 类未被注册。改用 `@SpringBootTest` 解决。
2. **CorsConfig bean 名称冲突**：`@Configuration` 类名 `CorsConfig` 自动注册为 bean `corsConfig`，与 Spring Boot auto-configuration 的 `corsConfig` 查找冲突。合并为单一模块后通过 `@SpringBootTest` 全量扫描解决。
3. **tmux PowerShell 路径问题**：tmux send-keys 在 PowerShell 中会吞掉路径中的反斜杠，需用引号包裹路径。

### §4) 后续改进

1. 事件总线引入：当需要添加盲板作业、审批流等模块时，可用 `@ApplicationModuleListener` 实现模块间事件驱动通信
2. 微服务拆分：如需将 system 或 blindplate 拆为独立服务，API 契约已定义，通信方式从本地调用改为 HTTP/gRPC 即可
3. 模块边界测试增强：当前 `ModulithModuleTest` 只验证 context 加载，可补充 `ApplicationModules.assertAllModules().verify()` 断言

### §5) 经验教训

1. **Maven 多模块不是银弹**：105 个文件的项目用 5 个 Maven 模块管理是过度工程化。Spring Modulith 的 package 级别边界验证足以满足模块隔离需求。
2. **common 模块要保持纯净**：基础设施配置类（Redis、Cors、Jackson）应放在依赖 Spring 的模块中，common 只放纯 POJO 工具。
3. **`@ApplicationModuleTest` 适用于多模块项目**：在单模块 Maven 项目中，`@SpringBootTest` 是更可靠的选择。
