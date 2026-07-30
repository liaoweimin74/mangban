## Design Summary

本项目（石化厂盲板与工艺隔离完整性管理系统）当前采用 Maven 多模块架构（5 个模块：common、framework、system、blindplate、admin），运行调试需编译多个 jar 包，且未来可能拆分为微服务。本次引入 Spring Modulith 实现模块化重构。

**核心改动**：
1. 将 `SysLocation`（工厂→装置→单元位置层级）从 `system` 模块迁移至 `blindplate` 模块
2. 引入 Spring Modulith 依赖，通过 package 级别的编译期验证强制模块边界
3. 保留现有 Maven 多模块结构不变，Modulith 模块边界基于 Java package 层级定义

## Alternatives Considered

### 方案 A：全面引入 Spring Modulith（推荐）
- **做法**：迁移 SysLocation 到 blindplate → 添加 spring-modulith-starter-core 依赖 → 配置模块边界验证 → 添加测试
- **优点**：
  - 编译期自动校验模块边界，违规直接报错
  - 单一部署单元，`mvn spring-boot:run` 一键启动调试
  - 模块间强制 API 契约，为微服务拆分铺路
  - 可渐进引入事件总线解耦
- **缺点**：需要重构跨模块调用（约 30 行代码）
- **为何胜出**：当前代码量小、架构清晰，是引入 Modulith 的最佳时机

### 方案 B：仅加 Modulith 验证，不重构
- **做法**：添加 Modulith 依赖，配置验证，但不迁移 SysLocation，不重构跨模块调用
- **优点**：改动最小，半天内完成
- **缺点**：违规会一直存在，验证形同虚设；调试不便问题未解决
- **为何未采用**：不重构就没有实际收益

### 方案 C：保持现状，未来直接拆微服务
- **做法**：不做任何改动
- **优点**：零成本
- **缺点**：调试不便持续存在；拆微服务时需在无边界约束的单体上硬拆，风险大
- **为何未采用**：当前是最佳重构时机，代码量增长后成本翻倍

## Agreed Approach

采用方案 A，分三阶段实施：

**第一阶段：迁移 SysLocation 到 blindplate**
- 将 system 模块中所有 SysLocation 相关文件（Entity、Repository、Service、Controller、DTO、VO）整体迁移到 blindplate
- 修改 package 声明和 import 引用
- 消除 blindplate 对 system 的唯一跨模块依赖

**第二阶段：引入 Spring Modulith 依赖 + 配置边界验证**
- 在根 pom.xml 添加 spring-modulith-bom
- 在 admin 启动模块添加 spring-modulith-starter-core
- 用 @ApplicationModule 标注各模块 package 边界
- common 和 framework 标记为 OPEN，system 和 blindplate 使用 named interface 模式

**第三阶段：添加 Modulith 测试（可选）**
- 添加 spring-modulith-starter-test 依赖
- 编写模块验证测试和场景测试

## Key Decisions

| 决策 | 结论 |
|------|------|
| SysLocation 归属 | 属于 blindplate（位置是盲板业务的基础主数据，不是组织架构） |
| Maven 模块结构 | 保留现有 5 模块不变，Modulith 模块边界基于 package |
| common/framework | 标记为 OPEN 模块，不做边界约束 |
| system/blindplate | 使用 named interface 模式（`*.api` 包对外） |
| 跨模块通信 | 通过 API 接口，不直接访问 Repository |

## Open Questions

无。设计已确认。