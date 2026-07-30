# Spring Modulith 模块化架构

## 概述

将项目从传统的 Maven 多模块架构升级为 Spring Modulith 模块化架构，在保留现有 Maven 模块结构的前提下，通过 package 级别的编译期验证强制模块边界，实现一键启动调试，并为未来微服务拆分建立 API 契约。

## 需求

### Requirement: SysLocation 迁移至 blindplate 模块

将 `SysLocation`（工厂→装置→单元位置层级）从 `system` 模块迁移至 `blindplate` 模块，消除跨模块直接 Repository 访问。

#### Scenario: SysLocation 文件从 system 迁移到 blindplate
WHEN 将 `SysLocation` Entity、Repository、Service、Controller 及相关 DTO/VO 从 `com.mangban.system` 包迁移至 `com.mangban.blindplate` 包
THEN `mvn compile` 编译通过
AND `IsolationPointServiceImpl` 中对 `SysLocationRepository` 的引用改为同模块调用
AND `system` 模块不再包含任何 SysLocation 相关文件

#### Scenario: 位置管理 API 正常运行
WHEN 调用位置管理的 tree/create/update/delete 接口
THEN 请求路由到 `blindplate.controller.LocationController`
AND 返回数据格式与迁移前一致

### Requirement: Spring Modulith 模块边界验证

引入 Spring Modulith，通过 `@ApplicationModule` 标注各模块 package 边界，编译期自动验证跨模块访问。

#### Scenario: 编译期验证模块边界
WHEN 执行 `mvn compile`
THEN Modulith 验证器检查各模块的 package 访问
AND `blindplate` 模块不能访问 `system` 模块的 `repository` 或 `internal` 包
AND `system` 模块不能访问 `blindplate` 模块的 `repository` 或 `internal` 包
AND 违反边界时报编译错误

#### Scenario: common 和 framework 标记为 OPEN
WHEN 其他模块访问 `common` 或 `framework` 的任何包
THEN Modulith 验证通过（OPEN 模块允许任意访问）

#### Scenario: system 和 blindplate 使用 named interface
WHEN 外部模块访问 `system` 或 `blindplate` 的 `api` 包
THEN Modulith 验证通过
WHEN 外部模块访问 `system` 或 `blindplate` 的非 `api` 包（如 `repository`、`domain`）
THEN Modulith 验证失败，报编译错误

### Requirement: 一键启动调试

引入 Spring Modulith 后保持单一部署单元，支持 `mvn spring-boot:run` 一键启动。

#### Scenario: 一键启动
WHEN 在项目根目录执行 `mvn spring-boot:run -pl admin -am`
THEN Spring Boot 启动成功
AND 所有模块的 Bean 被正确扫描和注入
AND API 端点可正常访问

### Requirement: Modulith 测试（可选）

添加 `spring-modulith-starter-test` 依赖，编写模块结构验证测试。

#### Scenario: 模块结构验证测试通过
WHEN 执行 `@ApplicationModuleTest` 标注的测试类
THEN 测试自动验证所有模块的边界约束
AND 测试通过时无违规报告
AND 测试失败时输出违规详情（哪个模块访问了哪个不允许的包）