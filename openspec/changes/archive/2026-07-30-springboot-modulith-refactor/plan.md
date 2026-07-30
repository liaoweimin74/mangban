# Spring Boot Modulith 重构实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 将项目从 Maven 多模块架构升级为 Spring Modulith 模块化架构，实现模块边界编译期验证和一键启动调试。

**Architecture:** 保留现有 5 个 Maven 模块不变，将 SysLocation 从 system 迁移至 blindplate 消除跨模块依赖，然后通过 `@ApplicationModule` 标注 package 边界，Modulith 在编译期自动验证。common 和 framework 标记为 OPEN 模块，system 和 blindplate 使用 named interface 模式。

**Tech Stack:** Spring Boot 3.5.0, Spring Modulith 1.4.x, Java 21, Maven, JPA, MySQL

## Global Constraints

- 保留现有 Maven 多模块结构（5 个 module 不变）
- common 和 framework 标记为 OPEN 模块
- system 和 blindplate 使用 named interface 模式
- 所有 package 声明和 import 引用必须同步修改
- 每个阶段完成后必须 `mvn compile` 验证

---

### Task 1: 迁移 SysLocation Entity 和 Repository

**Files:**
- Create: `blindplate/src/main/java/com/mangban/blindplate/domain/entity/SysLocation.java`
- Create: `blindplate/src/main/java/com/mangban/blindplate/repository/SysLocationRepository.java`
- Delete: `system/src/main/java/com/mangban/system/domain/entity/SysLocation.java`
- Delete: `system/src/main/java/com/mangban/system/repository/SysLocationRepository.java`

**Interfaces:**
- Consumes: 无
- Produces: `SysLocation` Entity 和 `SysLocationRepository` 在 blindplate 模块中可用

- [ ] **Step 1: 在 blindplate 创建 SysLocation.java**

将 `system/.../SysLocation.java` 复制到 `blindplate/.../SysLocation.java`，修改 package 为 `com.mangban.blindplate.domain.entity`，import 中 `BaseEntity` 的引用改为 `com.mangban.system.domain.entity.BaseEntity`（BaseEntity 仍留在 system 模块）。

```java
package com.mangban.blindplate.domain.entity;

import com.mangban.system.domain.entity.BaseEntity;
import jakarta.persistence.*;
// ... 其余代码与 system 版本一致
```

- [ ] **Step 2: 在 blindplate 创建 SysLocationRepository.java**

```java
package com.mangban.blindplate.repository;

import com.mangban.blindplate.domain.entity.SysLocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import java.util.List;

public interface SysLocationRepository extends JpaRepository<SysLocation, Long>,
        JpaSpecificationExecutor<SysLocation> {
    List<SysLocation> findByParentIdOrderBySortOrder(Long parentId);
    List<SysLocation> findByParentIdIsNullOrderBySortOrder();
}
```

- [ ] **Step 3: 删除 system 模块中的 SysLocation.java 和 SysLocationRepository.java**

```bash
rm "system/src/main/java/com/mangban/system/domain/entity/SysLocation.java"
rm "system/src/main/java/com/mangban/system/repository/SysLocationRepository.java"
```

- [ ] **Step 4: 编译验证**

```bash
mvn compile -q
```
Expected: 编译通过

- [ ] **Step 5: 提交**

```bash
git add -A && git commit -m "phase1: migrate SysLocation entity and repository to blindplate"
```

---

### Task 2: 迁移 LocationService 和 LocationServiceImpl

**Files:**
- Create: `blindplate/src/main/java/com/mangban/blindplate/service/LocationService.java`
- Create: `blindplate/src/main/java/com/mangban/blindplate/service/impl/LocationServiceImpl.java`
- Delete: `system/src/main/java/com/mangban/system/service/LocationService.java`
- Delete: `system/src/main/java/com/mangban/system/service/impl/LocationServiceImpl.java`

**Interfaces:**
- Consumes: `SysLocation`, `SysLocationRepository` (blindplate 模块内)
- Produces: `LocationService` 接口在 blindplate 模块中可用

- [ ] **Step 1: 在 blindplate 创建 LocationService.java**

```java
package com.mangban.blindplate.service;

import com.mangban.blindplate.domain.dto.LocationCreateRequest;
import com.mangban.blindplate.domain.dto.LocationUpdateRequest;
import com.mangban.blindplate.domain.vo.LocationTreeNode;
import java.util.List;

public interface LocationService {
    List<LocationTreeNode> tree();
    LocationTreeNode create(LocationCreateRequest request);
    LocationTreeNode update(Long id, LocationUpdateRequest request);
    void delete(Long id);
}
```

- [ ] **Step 2: 在 blindplate 创建 LocationServiceImpl.java**

```java
package com.mangban.blindplate.service.impl;

import com.mangban.blindplate.domain.entity.SysLocation;
import com.mangban.blindplate.domain.dto.LocationCreateRequest;
import com.mangban.blindplate.domain.dto.LocationUpdateRequest;
import com.mangban.blindplate.domain.vo.LocationTreeNode;
import com.mangban.blindplate.repository.SysLocationRepository;
import com.mangban.blindplate.service.LocationService;
import com.mangban.common.exception.BusinessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.List;

@Service
public class LocationServiceImpl implements LocationService {
    private final SysLocationRepository locationRepository;

    public LocationServiceImpl(SysLocationRepository locationRepository) {
        this.locationRepository = locationRepository;
    }

    @Override
    public List<LocationTreeNode> tree() {
        List<SysLocation> roots = locationRepository.findByParentIdIsNullOrderBySortOrder();
        List<LocationTreeNode> result = new ArrayList<>();
        for (SysLocation root : roots) {
            result.add(toTreeNode(root));
        }
        return result;
    }

    private LocationTreeNode toTreeNode(SysLocation location) {
        LocationTreeNode node = new LocationTreeNode(
            location.getId(), location.getName(), location.getType(),
            location.getCode(), location.getSortOrder(), location.getParentId(),
            new ArrayList<>()
        );
        List<SysLocation> children = locationRepository.findByParentIdOrderBySortOrder(location.getId());
        for (SysLocation child : children) {
            node.getChildren().add(toTreeNode(child));
        }
        return node;
    }

    // ... create/update/delete 方法与 system 版本一致，仅 package 引用改为 blindplate
}
```

- [ ] **Step 3: 删除 system 模块中的 LocationService.java 和 LocationServiceImpl.java**

```bash
rm "system/src/main/java/com/mangban/system/service/LocationService.java"
rm "system/src/main/java/com/mangban/system/service/impl/LocationServiceImpl.java"
```

- [ ] **Step 4: 编译验证**

```bash
mvn compile -q
```
Expected: 编译通过

- [ ] **Step 5: 提交**

```bash
git add -A && git commit -m "phase1: migrate LocationService and impl to blindplate"
```

---

### Task 3: 迁移 LocationController 和 DTO/VO

**Files:**
- Create: `blindplate/src/main/java/com/mangban/blindplate/controller/LocationController.java`
- Create: `blindplate/src/main/java/com/mangban/blindplate/domain/dto/LocationCreateRequest.java`
- Create: `blindplate/src/main/java/com/mangban/blindplate/domain/dto/LocationUpdateRequest.java`
- Create: `blindplate/src/main/java/com/mangban/blindplate/domain/vo/LocationTreeNode.java`
- Delete: 对应的 system 模块源文件（4 个）

- [ ] **Step 1: 在 blindplate 创建 LocationController.java**

```java
package com.mangban.blindplate.controller;

import com.mangban.blindplate.domain.dto.LocationCreateRequest;
import com.mangban.blindplate.domain.dto.LocationUpdateRequest;
import com.mangban.blindplate.domain.vo.LocationTreeNode;
import com.mangban.blindplate.service.LocationService;
import com.mangban.common.domain.R;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/locations")
public class LocationController {
    private final LocationService locationService;

    public LocationController(LocationService locationService) {
        this.locationService = locationService;
    }

    @GetMapping("/tree")
    public R<List<LocationTreeNode>> tree() {
        return R.ok(locationService.tree());
    }

    @PostMapping
    public R<LocationTreeNode> create(@Valid @RequestBody LocationCreateRequest request) {
        return R.ok(locationService.create(request));
    }

    @PutMapping("/{id}")
    public R<LocationTreeNode> update(@PathVariable Long id,
                                       @Valid @RequestBody LocationUpdateRequest request) {
        return R.ok(locationService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        locationService.delete(id);
        return R.ok();
    }
}
```

- [ ] **Step 2: 在 blindplate 创建 DTO 和 VO 文件**

`LocationCreateRequest.java`:
```java
package com.mangban.blindplate.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record LocationCreateRequest(
    @NotBlank String name,
    @NotBlank String type,
    String code,
    Integer sortOrder,
    Long parentId
) {}
```

`LocationUpdateRequest.java`:
```java
package com.mangban.blindplate.domain.dto;

public record LocationUpdateRequest(
    String name,
    String type,
    String code,
    Integer sortOrder,
    Long parentId
) {}
```

`LocationTreeNode.java`:
```java
package com.mangban.blindplate.domain.vo;

import java.util.List;

public record LocationTreeNode(
    Long id,
    String name,
    String type,
    String code,
    Integer sortOrder,
    Long parentId,
    List<LocationTreeNode> children
) {}
```

- [ ] **Step 3: 删除 system 模块中的对应文件**

```bash
rm "system/src/main/java/com/mangban/system/controller/LocationController.java"
rm "system/src/main/java/com/mangban/system/domain/dto/LocationCreateRequest.java"
rm "system/src/main/java/com/mangban/system/domain/dto/LocationUpdateRequest.java"
rm "system/src/main/java/com/mangban/system/domain/vo/LocationTreeNode.java"
```

- [ ] **Step 4: 编译验证**

```bash
mvn compile -q
```
Expected: 编译通过

- [ ] **Step 5: 提交**

```bash
git add -A && git commit -m "phase1: migrate LocationController and DTO/VO to blindplate"
```

---

### Task 4: 修改 IsolationPointServiceImpl 中的引用

**Files:**
- Modify: `blindplate/src/main/java/com/mangban/blindplate/service/impl/IsolationPointServiceImpl.java`

**Interfaces:**
- Consumes: `SysLocation`, `SysLocationRepository` 现在在 blindplate 模块内
- Produces: 编译通过，无跨模块引用

- [ ] **Step 1: 修改 IsolationPointServiceImpl.java 中的 import**

将：
```java
import com.mangban.system.domain.entity.SysLocation;
import com.mangban.system.repository.SysLocationRepository;
```
改为：
```java
import com.mangban.blindplate.domain.entity.SysLocation;
import com.mangban.blindplate.repository.SysLocationRepository;
```

同时删除 `SysLocationRepository` 字段声明和构造器参数中的 `SysLocationRepository` 注入（如果之前是直接注入的，改为通过 `LocationService` 调用）。但 `IsolationPointServiceImpl` 目前直接使用了 `SysLocationRepository` 的 `findById()` 和 `findByParentIdOrderBySortOrder()` 方法。由于现在 `SysLocation` 和 `SysLocationRepository` 都在 blindplate 模块内，可以直接注入，无需改为 API 调用。

- [ ] **Step 2: 编译验证**

```bash
mvn compile -q
```
Expected: 编译通过

- [ ] **Step 3: 提交**

```bash
git add -A && git commit -m "phase1: update IsolationPointServiceImpl imports for SysLocation relocation"
```

---

### Task 5: 清理 system 模块 pom.xml 和空包

**Files:**
- Modify: 如有必要，清理 system 模块中因迁移变空的目录层级

- [ ] **Step 1: 检查 system 模块中是否还有残留的 SysLocation 引用**

```bash
grep -r "SysLocation\|LocationService\|LocationController" system/src/main/java/ --include="*.java" || echo "无残留引用"
```

- [ ] **Step 2: 检查 system/pom.xml 中 blindplate 的依赖**

如果 `system/pom.xml` 中有对 `blindplate` 的依赖（当前没有），保持不变。当前 system 的依赖是 `framework`。

- [ ] **Step 3: 编译验证**

```bash
mvn compile -q
```
Expected: 编译通过

- [ ] **Step 4: 提交**

```bash
git add -A && git commit -m "phase1: cleanup system module after SysLocation migration"
```

---

### Task 6: 添加 Spring Modulith BOM 依赖管理

**Files:**
- Modify: `pom.xml`（根目录）

- [ ] **Step 1: 在根 pom.xml 的 `<dependencyManagement>` 中添加 Modulith BOM**

```xml
<dependency>
    <groupId>org.springframework.modulith</groupId>
    <artifactId>spring-modulith-bom</artifactId>
    <version>1.4.0</version>
    <scope>import</scope>
    <type>pom</type>
</dependency>
```

插入到现有 `<dependencyManagement><dependencies>` 中，放在 `jjwt` 依赖后面。

- [ ] **Step 2: 编译验证**

```bash
mvn compile -q
```
Expected: 编译通过

- [ ] **Step 3: 提交**

```bash
git add -A && git commit -m "phase2: add spring-modulith-bom dependency management"
```

---

### Task 7: 在 admin 模块添加 Modulith 核心依赖

**Files:**
- Modify: `admin/pom.xml`

- [ ] **Step 1: 在 admin/pom.xml 中添加 spring-modulith-starter-core 依赖**

```xml
<dependency>
    <groupId>org.springframework.modulith</groupId>
    <artifactId>spring-modulith-starter-core</artifactId>
</dependency>
```

- [ ] **Step 2: 编译验证**

```bash
mvn compile -q
```
Expected: 编译通过

- [ ] **Step 3: 提交**

```bash
git add -A && git commit -m "phase2: add spring-modulith-starter-core to admin module"
```

---

### Task 8: 添加模块边界 package-info.java

**Files:**
- Create: `common/src/main/java/com/mangban/common/package-info.java`
- Create: `framework/src/main/java/com/mangban/framework/package-info.java`
- Create: `system/src/main/java/com/mangban/system/package-info.java`
- Create: `blindplate/src/main/java/com/mangban/blindplate/package-info.java`

- [ ] **Step 1: 创建 common 的 package-info.java**

```java
@ApplicationModule(displayName = "基础设施",
    type = ApplicationModule.Type.OPEN)
package com.mangban.common;

import org.springframework.modulith.ApplicationModule;
```

- [ ] **Step 2: 创建 framework 的 package-info.java**

```java
@ApplicationModule(displayName = "安全框架",
    type = ApplicationModule.Type.OPEN)
package com.mangban.framework;

import org.springframework.modulith.ApplicationModule;
```

- [ ] **Step 3: 创建 system 的 package-info.java**

```java
@ApplicationModule(displayName = "系统管理模块",
    allowedDependencies = {"common", "framework"})
package com.mangban.system;

import org.springframework.modulith.ApplicationModule;
```

- [ ] **Step 4: 创建 blindplate 的 package-info.java**

```java
@ApplicationModule(displayName = "核心业务模块",
    allowedDependencies = {"common", "framework", "system"})
package com.mangban.blindplate;

import org.springframework.modulith.ApplicationModule;
```

- [ ] **Step 5: 编译验证**

```bash
mvn compile -q
```
Expected: 编译通过，无 Modulith 违规报告

- [ ] **Step 6: 提交**

```bash
git add -A && git commit -m "phase2: add module boundary annotations via package-info.java"
```

---

### Task 9: 添加 Modulith 测试（可选）

**Files:**
- Modify: `admin/pom.xml`
- Create: `admin/src/test/java/com/mangban/ModulithModuleTest.java`

- [ ] **Step 1: 在 admin/pom.xml 中添加 spring-modulith-starter-test 依赖**

```xml
<dependency>
    <groupId>org.springframework.modulith</groupId>
    <artifactId>spring-modulith-starter-test</artifactId>
    <scope>test</scope>
</dependency>
```

- [ ] **Step 2: 创建模块结构验证测试**

```java
package com.mangban;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.test.ApplicationModuleTest;

@ApplicationModuleTest
class ModulithModuleTest {
    @Test
    void verifyModuleStructure() {
        // Modulith 自动验证所有模块的边界约束
        // 如果任何模块访问了不允许的包，此测试会失败
    }
}
```

- [ ] **Step 3: 运行测试验证**

```bash
mvn test -pl admin -am -q
```
Expected: 测试通过，无违规报告

- [ ] **Step 4: 提交**

```bash
git add -A && git commit -m "phase3: add Modulith module verification test"
```

---

### 验证清单

- [ ] 项目编译通过（`mvn compile -q`）
- [ ] 项目测试通过（`mvn test -q`）
- [ ] `mvn spring-boot:run -pl admin -am` 一键启动成功
- [ ] API 端点（如 `/api/locations/tree`）可正常访问
- [ ] Modulith 无模块边界违规报告