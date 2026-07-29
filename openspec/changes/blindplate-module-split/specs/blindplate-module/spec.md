## ADDED Requirements

### Requirement: 盲板后端独立模块 SHALL 包含完整的 IsolationPoint CRUD
新建 `mangban-blindplate` Maven 子模块，将 mangban-system 中所有 IsolationPoint 相关代码完整迁移，确保 API 接口路径 `/api/isolation-points/*` 不变。

#### Scenario: 新模块编译通过
- **WHEN** 执行 `mvn -pl mangban-blindplate compile`
- **THEN** 编译成功，无错误

#### Scenario: API 接口路径不变
- **WHEN** 发送 GET /api/isolation-points 请求
- **THEN** 返回 200 状态码，响应格式与迁移前一致

### Requirement: 新模块 SHALL 依赖 mangban-common 和 mangban-framework
`mangban-blindplate` 的 pom.xml 中配置对 `mangban-common` 和 `mangban-framework` 的依赖。

#### Scenario: 依赖解析成功
- **WHEN** 执行 `mvn -pl mangban-blindplate -am dependency:resolve`
- **THEN** 所有依赖解析成功，无错误

### Requirement: mangban-admin SHALL 新增依赖 mangban-blindplate
`mangban-admin/pom.xml` 中新增 `<dependency>` 引用 `mangban-blindplate`，同时保留对 `mangban-system` 的依赖。

#### Scenario: 启动成功
- **WHEN** 执行 `mvn -pl mangban-admin spring-boot:run`
- **THEN** 应用启动正常，无 ClassNotFoundException

### Requirement: 父 POM SHALL 注册新模块
`mangban/pom.xml` 的 `<modules>` 列表新增 `<module>mangban-blindplate</module>`。

#### Scenario: 构建全部模块成功
- **WHEN** 执行 `mvn compile`
- **THEN** 所有模块（含 mangban-blindplate）编译成功

### Requirement: mangban-system SHALL 删除所有 IsolationPoint 相关代码
迁移后删除 `mangban-system` 中 IsolationPointController.java、SysIsolationPoint.java、SysIsolationPointRepository.java、IsolationPointService.java/Impl.java、所有 IsolationPoint DTO/VO 文件。

#### Scenario: mangban-system 无盲板代码残留
- **WHEN** 在 mangban-system 源码中搜索 "IsolationPoint"
- **THEN** 搜索结果为空