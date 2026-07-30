## 1. 迁移 SysLocation 到 blindplate 模块

- [x] 1.1 将 `SysLocation.java` Entity 从 `com.mangban.system.domain.entity` 迁移到 `com.mangban.blindplate.domain.entity`，修改 package 声明
- [x] 1.2 将 `SysLocationRepository.java` 从 `com.mangban.system.repository` 迁移到 `com.mangban.blindplate.repository`，修改 package 声明
- [x] 1.3 将 `LocationService.java` 接口从 `com.mangban.system.service` 迁移到 `com.mangban.blindplate.service`，修改 package 声明
- [x] 1.4 将 `LocationServiceImpl.java` 从 `com.mangban.system.service.impl` 迁移到 `com.mangban.blindplate.service.impl`，修改 package 声明和 import 引用
- [x] 1.5 将 `LocationController.java` 从 `com.mangban.system.controller` 迁移到 `com.mangban.blindplate.controller`，修改 package 声明
- [x] 1.6 将 `LocationCreateRequest.java` 和 `LocationUpdateRequest.java` DTO 从 `com.mangban.system.domain.dto` 迁移到 `com.mangban.blindplate.domain.dto`，修改 package 声明
- [x] 1.7 将 `LocationTreeNode.java` VO 从 `com.mangban.system.domain.vo` 迁移到 `com.mangban.blindplate.domain.vo`，修改 package 声明
- [x] 1.8 修改 `IsolationPointServiceImpl.java` 中的 import 引用，将 `com.mangban.system.*` 改为 `com.mangban.blindplate.*`
- [x] 1.9 删除 `system` 模块中已迁移的 SysLocation 相关源文件
- [x] 1.10 运行 `mvn compile` 验证编译通过

## 2. 引入 Spring Modulith 依赖

- [x] 2.1 在根 `pom.xml` 的 `<dependencyManagement>` 中添加 `spring-modulith-bom` 依赖管理
- [x] 2.2 在 `admin/pom.xml` 中添加 `spring-modulith-starter-core` 依赖
- [x] 2.3 在 `com.mangban.common` 包添加 `package-info.java`，标注为 OPEN 模块
- [x] 2.4 在 `com.mangban.framework` 包添加 `package-info.java`，标注为 OPEN 模块
- [x] 2.5 在 `com.mangban.system` 包添加 `package-info.java`，标注模块边界
- [x] 2.6 在 `com.mangban.blindplate` 包添加 `package-info.java`，标注模块边界
- [x] 2.7 运行 `mvn compile` 验证 Modulith 编译期验证通过

## 3. 添加 Modulith 测试（可选）

- [x] 3.1 在 `admin/pom.xml` 中添加 `spring-modulith-starter-test` 依赖（test scope）
- [x] 3.2 在 `admin` 模块的测试目录创建 `ModulithModuleTest.java` 模块结构验证测试
- [x] 3.3 运行 `mvn test` 验证测试通过