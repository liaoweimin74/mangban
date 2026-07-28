# 隔离点管理模块 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 实现隔离点管理模块核心功能——装置层级结构（Factory→Plant→Unit 树形管理）+ 隔离点台账 CRUD + 状态管理 + 状态台账总览。

**Architecture:** 后端完全遵循现有分层模式（Controller→Service→Repository→Entity，DTO/VO 用 Java Record），前端全部使用 SearchTable 业务组件，通过配置声明式实现列表、搜索、表单 CRUD。

**Tech Stack:** Spring Boot 3.5 + JPA + MySQL 8 + Vue 3 + TypeScript + Element Plus + Pinia

## Global Constraints

- 所有实体继承 `BaseEntity`（id, isDeleted, createdBy/At, updatedBy/At）
- 软删除：isDeleted=1，所有查询过滤 isDeleted=0
- DTO/VO 使用 Java Record、Controller 返回 `R<T>` 包装
- 前端所有页面使用 SearchTable 组件，不写自定义页面
- 状态颜色：OPEN=绿(#67C23A)、BLIND=红(#F56C6C)、OCCUPIED=橙(#E6A23C)、FREE=灰(#909399)
- schema.sql 表名使用 `sys_` 前缀，引擎 InnoDB，字符集 utf8mb4
- API 路径使用 kebab-case 复数形式

---

### Task 1: 数据库 DDL

**Files:**
- Create: DDL 追加到 `mangban-admin/src/main/resources/schema.sql`

**Interfaces:**
- Produces: `sys_location` 表结构, `sys_isolation_point` 表结构

- [ ] **Step 1: 在 schema.sql 末尾追加 sys_location 表 DDL**

```sql
-- 装置层级表
CREATE TABLE IF NOT EXISTS sys_location (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    parent_id BIGINT COMMENT '父级ID，NULL=根节点',
    name VARCHAR(100) NOT NULL COMMENT '名称',
    code VARCHAR(50) NOT NULL COMMENT '编码',
    type VARCHAR(20) NOT NULL COMMENT '类型：FACTORY/PLANT/UNIT',
    sort_order INT DEFAULT 0 COMMENT '排序',
    remark VARCHAR(500) COMMENT '备注',
    is_deleted INT DEFAULT 0,
    created_by VARCHAR(50),
    created_at DATETIME,
    updated_by VARCHAR(50),
    updated_at DATETIME,
    UNIQUE KEY uk_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='装置层级';
```

- [ ] **Step 2: 在 schema.sql 末尾追加 sys_isolation_point 表 DDL**

```sql
-- 隔离点台账表
CREATE TABLE IF NOT EXISTS sys_isolation_point (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    unit_id BIGINT NOT NULL COMMENT '所属单元ID',
    code VARCHAR(50) NOT NULL COMMENT '编码',
    name VARCHAR(100) NOT NULL COMMENT '名称',
    medium VARCHAR(50) COMMENT '介质',
    pressure_rating VARCHAR(50) COMMENT '压力等级',
    temperature_rating VARCHAR(50) COMMENT '温度等级',
    hazard_level VARCHAR(20) COMMENT '危害等级',
    point_type VARCHAR(50) COMMENT '点位类型',
    blind_spec VARCHAR(100) COMMENT '适配盲板规格',
    equipment_tag VARCHAR(50) COMMENT '关联设备位号',
    pipeline_no VARCHAR(50) COMMENT '关联管线号',
    status VARCHAR(20) DEFAULT 'OPEN' COMMENT '通盲状态：OPEN/BLIND',
    occupy_status VARCHAR(20) DEFAULT 'FREE' COMMENT '占用状态：OCCUPIED/FREE',
    remark VARCHAR(500) COMMENT '备注',
    is_deleted INT DEFAULT 0,
    created_by VARCHAR(50),
    created_at DATETIME,
    updated_by VARCHAR(50),
    updated_at DATETIME,
    UNIQUE KEY uk_code (code),
    KEY idx_unit_id (unit_id),
    KEY idx_status (status),
    KEY idx_hazard_level (hazard_level)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='隔离点台账';
```

---

### Task 2: 后端 - SysLocation Entity + Repository

**Files:**
- Create: `mangban-system/src/main/java/com/mangban/system/domain/entity/SysLocation.java`
- Create: `mangban-system/src/main/java/com/mangban/system/repository/SysLocationRepository.java`

**Interfaces:**
- Produces: `SysLocation` entity, `SysLocationRepository`

- [ ] **Step 1: 创建 SysLocation.java**

```java
package com.mangban.system.domain.entity;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "sys_location")
public class SysLocation extends BaseEntity {
    @Column(name = "parent_id")
    private Long parentId;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 50, unique = true)
    private String code;

    @Column(nullable = false, length = 20)
    private String type;

    @Column(name = "sort_order")
    private Integer sortOrder = 0;

    @Column(length = 500)
    private String remark;

    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id", insertable = false, updatable = false)
    @OrderBy("sortOrder ASC")
    private List<SysLocation> children = new ArrayList<>();

    // getters/setters
    public Long getParentId() { return parentId; }
    public void setParentId(Long parentId) { this.parentId = parentId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    public List<SysLocation> getChildren() { return children; }
    public void setChildren(List<SysLocation> children) { this.children = children; }
}
```

- [ ] **Step 2: 创建 SysLocationRepository.java**

```java
package com.mangban.system.repository;

import com.mangban.system.domain.entity.SysLocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import java.util.List;

public interface SysLocationRepository extends JpaRepository<SysLocation, Long>,
        JpaSpecificationExecutor<SysLocation> {
    List<SysLocation> findByParentIdOrderBySortOrder(Long parentId);
    List<SysLocation> findByParentIdIsNullOrderBySortOrder();
    long countByParentId(Long parentId);
    List<SysLocation> findByParentIdIn(List<Long> parentIds);
    long countByParentIdAndIsDeleted(Long parentId, Integer isDeleted);
}
```

---

### Task 3: 后端 - Location DTO/VO

**Files:**
- Create: `mangban-system/src/main/java/com/mangban/system/domain/dto/LocationCreateRequest.java`
- Create: `mangban-system/src/main/java/com/mangban/system/domain/dto/LocationUpdateRequest.java`
- Create: `mangban-system/src/main/java/com/mangban/system/domain/vo/LocationTreeNode.java`

**Interfaces:**
- Produces: DTO/VO records

- [ ] **Step 1: 创建 LocationCreateRequest.java**

```java
package com.mangban.system.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LocationCreateRequest(
        Long parentId,
        @NotBlank @Size(max = 100) String name,
        @NotBlank @Size(max = 50) String code,
        @NotBlank @Size(max = 20) String type,
        Integer sortOrder,
        @Size(max = 500) String remark) {
}
```

- [ ] **Step 2: 创建 LocationUpdateRequest.java**

```java
package com.mangban.system.domain.dto;

import jakarta.validation.constraints.Size;

public record LocationUpdateRequest(
        Long parentId,
        @Size(max = 100) String name,
        @Size(max = 50) String code,
        Integer sortOrder,
        @Size(max = 500) String remark) {
}
```

- [ ] **Step 3: 创建 LocationTreeNode.java**

```java
package com.mangban.system.domain.vo;

import java.util.List;

public record LocationTreeNode(
        Long id,
        Long parentId,
        String name,
        String code,
        String type,
        Integer sortOrder,
        String remark,
        List<LocationTreeNode> children) {
}
```

---

### Task 4: 后端 - Location Service + Controller

**Files:**
- Create: `mangban-system/src/main/java/com/mangban/system/service/LocationService.java`
- Create: `mangban-system/src/main/java/com/mangban/system/service/impl/LocationServiceImpl.java`
- Create: `mangban-system/src/main/java/com/mangban/system/controller/LocationController.java`

**Interfaces:**
- Consumes: `SysLocationRepository`, `LocationCreateRequest`, `LocationUpdateRequest`, `LocationTreeNode`
- Produces: REST API at `/api/locations/*`

- [ ] **Step 1: 创建 LocationService.java**

```java
package com.mangban.system.service;

import com.mangban.system.domain.dto.LocationCreateRequest;
import com.mangban.system.domain.dto.LocationUpdateRequest;
import com.mangban.system.domain.vo.LocationTreeNode;
import java.util.List;

public interface LocationService {
    List<LocationTreeNode> tree();
    LocationTreeNode create(LocationCreateRequest request);
    LocationTreeNode update(Long id, LocationUpdateRequest request);
    void delete(Long id);
}
```

- [ ] **Step 2: 创建 LocationServiceImpl.java**

```java
package com.mangban.system.service.impl;

import com.mangban.common.constant.GlobalConstant;
import com.mangban.common.exception.BusinessException;
import com.mangban.system.domain.dto.LocationCreateRequest;
import com.mangban.system.domain.dto.LocationUpdateRequest;
import com.mangban.system.domain.entity.SysLocation;
import com.mangban.system.domain.vo.LocationTreeNode;
import com.mangban.system.repository.SysLocationRepository;
import com.mangban.system.service.LocationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class LocationServiceImpl implements LocationService {
    private final SysLocationRepository locationRepository;

    public LocationServiceImpl(SysLocationRepository locationRepository) {
        this.locationRepository = locationRepository;
    }

    @Override
    public List<LocationTreeNode> tree() {
        List<SysLocation> roots = locationRepository.findByParentIdIsNullOrderBySortOrder();
        return roots.stream()
                .filter(l -> l.getIsDeleted() == 0)
                .map(this::toTreeNode)
                .collect(Collectors.toList());
    }

    private LocationTreeNode toTreeNode(SysLocation location) {
        List<SysLocation> children = locationRepository.findByParentIdOrderBySortOrder(location.getId());
        List<LocationTreeNode> childNodes = children.stream()
                .filter(c -> c.getIsDeleted() == 0)
                .map(this::toTreeNode)
                .collect(Collectors.toList());
        return new LocationTreeNode(
                location.getId(), location.getParentId(),
                location.getName(), location.getCode(), location.getType(),
                location.getSortOrder(), location.getRemark(),
                childNodes.isEmpty() ? null : childNodes);
    }

    private void validateParentType(String childType, Long parentId) {
        if (parentId == null) {
            // 根节点必须是 FACTORY
            if (!"FACTORY".equals(childType)) {
                throw new BusinessException("根节点必须是工厂类型");
            }
            return;
        }
        SysLocation parent = locationRepository.findById(parentId)
                .orElseThrow(() -> new BusinessException("父节点不存在"));
        if ("FACTORY".equals(childType) && !"FACTORY".equals(parent.getType())) {
            throw new BusinessException("工厂节点必须为根节点");
        }
        if ("PLANT".equals(childType) && !"FACTORY".equals(parent.getType())) {
            throw new BusinessException("装置必须挂在工厂下");
        }
        if ("UNIT".equals(childType) && !"PLANT".equals(parent.getType())) {
            throw new BusinessException("单元必须挂在装置下");
        }
    }

    @Override
    @Transactional
    public LocationTreeNode create(LocationCreateRequest request) {
        validateParentType(request.type(), request.parentId());
        SysLocation location = new SysLocation();
        location.setParentId(request.parentId());
        location.setName(request.name());
        location.setCode(request.code());
        location.setType(request.type());
        location.setSortOrder(request.sortOrder() != null ? request.sortOrder() : 0);
        location.setRemark(request.remark());
        location = locationRepository.save(location);
        return toTreeNode(location);
    }

    @Override
    @Transactional
    public LocationTreeNode update(Long id, LocationUpdateRequest request) {
        SysLocation location = locationRepository.findById(id)
                .orElseThrow(() -> new BusinessException("位置节点不存在"));
        if (StringUtils.hasText(request.name())) location.setName(request.name());
        if (StringUtils.hasText(request.code())) location.setCode(request.code());
        if (request.sortOrder() != null) location.setSortOrder(request.sortOrder());
        if (StringUtils.hasText(request.remark())) location.setRemark(request.remark());
        location = locationRepository.save(location);
        return toTreeNode(location);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!locationRepository.existsById(id)) {
            throw new BusinessException("位置节点不存在");
        }
        if (locationRepository.countByParentIdAndIsDeleted(id, 0) > 0) {
            throw new BusinessException("存在子节点，无法删除");
        }
        SysLocation location = locationRepository.findById(id).orElseThrow();
        location.setIsDeleted(GlobalConstant.DELETED_YES);
        locationRepository.save(location);
    }
}
```

- [ ] **Step 3: 创建 LocationController.java**

```java
package com.mangban.system.controller;

import com.mangban.common.domain.R;
import com.mangban.system.domain.dto.LocationCreateRequest;
import com.mangban.system.domain.dto.LocationUpdateRequest;
import com.mangban.system.domain.vo.LocationTreeNode;
import com.mangban.system.service.LocationService;
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
    public R<LocationTreeNode> update(@PathVariable Long id, @Valid @RequestBody LocationUpdateRequest request) {
        return R.ok(locationService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        locationService.delete(id);
        return R.ok();
    }
}
```

---

### Task 5: 后端 - SysIsolationPoint Entity + Repository

**Files:**
- Create: `mangban-system/src/main/java/com/mangban/system/domain/entity/SysIsolationPoint.java`
- Create: `mangban-system/src/main/java/com/mangban/system/repository/SysIsolationPointRepository.java`

**Interfaces:**
- Produces: `SysIsolationPoint` entity, `SysIsolationPointRepository`

- [ ] **Step 1: 创建 SysIsolationPoint.java**

```java
package com.mangban.system.domain.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "sys_isolation_point")
public class SysIsolationPoint extends BaseEntity {
    @Column(name = "unit_id", nullable = false)
    private Long unitId;

    @Column(nullable = false, length = 50, unique = true)
    private String code;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 50)
    private String medium;

    @Column(name = "pressure_rating", length = 50)
    private String pressureRating;

    @Column(name = "temperature_rating", length = 50)
    private String temperatureRating;

    @Column(name = "hazard_level", length = 20)
    private String hazardLevel;

    @Column(name = "point_type", length = 50)
    private String pointType;

    @Column(name = "blind_spec", length = 100)
    private String blindSpec;

    @Column(name = "equipment_tag", length = 50)
    private String equipmentTag;

    @Column(name = "pipeline_no", length = 50)
    private String pipelineNo;

    @Column(length = 20)
    private String status = "OPEN";

    @Column(name = "occupy_status", length = 20)
    private String occupyStatus = "FREE";

    @Column(length = 500)
    private String remark;

    // getters/setters
    public Long getUnitId() { return unitId; }
    public void setUnitId(Long unitId) { this.unitId = unitId; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getMedium() { return medium; }
    public void setMedium(String medium) { this.medium = medium; }
    public String getPressureRating() { return pressureRating; }
    public void setPressureRating(String pressureRating) { this.pressureRating = pressureRating; }
    public String getTemperatureRating() { return temperatureRating; }
    public void setTemperatureRating(String temperatureRating) { this.temperatureRating = temperatureRating; }
    public String getHazardLevel() { return hazardLevel; }
    public void setHazardLevel(String hazardLevel) { this.hazardLevel = hazardLevel; }
    public String getPointType() { return pointType; }
    public void setPointType(String pointType) { this.pointType = pointType; }
    public String getBlindSpec() { return blindSpec; }
    public void setBlindSpec(String blindSpec) { this.blindSpec = blindSpec; }
    public String getEquipmentTag() { return equipmentTag; }
    public void setEquipmentTag(String equipmentTag) { this.equipmentTag = equipmentTag; }
    public String getPipelineNo() { return pipelineNo; }
    public void setPipelineNo(String pipelineNo) { this.pipelineNo = pipelineNo; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getOccupyStatus() { return occupyStatus; }
    public void setOccupyStatus(String occupyStatus) { this.occupyStatus = occupyStatus; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
}
```

- [ ] **Step 2: 创建 SysIsolationPointRepository.java**

```java
package com.mangban.system.repository;

import com.mangban.system.domain.entity.SysIsolationPoint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import java.util.Optional;

public interface SysIsolationPointRepository extends JpaRepository<SysIsolationPoint, Long>,
        JpaSpecificationExecutor<SysIsolationPoint> {
    Optional<SysIsolationPoint> findByCode(String code);
    long countByUnitIdAndIsDeleted(Long unitId, Integer isDeleted);
}
```

---

### Task 6: 后端 - IsolationPoint DTO/VO

**Files:**
- Create: `mangban-system/src/main/java/com/mangban/system/domain/dto/IsolationPointCreateRequest.java`
- Create: `mangban-system/src/main/java/com/mangban/system/domain/dto/IsolationPointUpdateRequest.java`
- Create: `mangban-system/src/main/java/com/mangban/system/domain/dto/IsolationPointStatusRequest.java`
- Create: `mangban-system/src/main/java/com/mangban/system/domain/dto/IsolationPointOccupyRequest.java`
- Create: `mangban-system/src/main/java/com/mangban/system/domain/vo/IsolationPointVO.java`

- [ ] **Step 1: 创建 IsolationPointCreateRequest.java**

```java
package com.mangban.system.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record IsolationPointCreateRequest(
        @NotNull Long unitId,
        @NotBlank @Size(max = 50) String code,
        @NotBlank @Size(max = 100) String name,
        @Size(max = 50) String medium,
        @Size(max = 50) String pressureRating,
        @Size(max = 50) String temperatureRating,
        @Size(max = 20) String hazardLevel,
        @Size(max = 50) String pointType,
        @Size(max = 100) String blindSpec,
        @Size(max = 50) String equipmentTag,
        @Size(max = 50) String pipelineNo,
        @Size(max = 500) String remark) {
}
```

- [ ] **Step 2: 创建 IsolationPointUpdateRequest.java**

```java
package com.mangban.system.domain.dto;

import jakarta.validation.constraints.Size;

public record IsolationPointUpdateRequest(
        Long unitId,
        @Size(max = 50) String code,
        @Size(max = 100) String name,
        @Size(max = 50) String medium,
        @Size(max = 50) String pressureRating,
        @Size(max = 50) String temperatureRating,
        @Size(max = 20) String hazardLevel,
        @Size(max = 50) String pointType,
        @Size(max = 100) String blindSpec,
        @Size(max = 50) String equipmentTag,
        @Size(max = 50) String pipelineNo,
        @Size(max = 500) String remark) {
}
```

- [ ] **Step 3: 创建 IsolationPointStatusRequest.java**

```java
package com.mangban.system.domain.dto;

import jakarta.validation.constraints.NotBlank;

public record IsolationPointStatusRequest(
        @NotBlank String status) {
}
```

- [ ] **Step 4: 创建 IsolationPointOccupyRequest.java**

```java
package com.mangban.system.domain.dto;

import jakarta.validation.constraints.NotBlank;

public record IsolationPointOccupyRequest(
        @NotBlank String occupyStatus) {
}
```

- [ ] **Step 5: 创建 IsolationPointVO.java**

```java
package com.mangban.system.domain.vo;

import java.time.LocalDateTime;

public record IsolationPointVO(
        Long id,
        Long unitId,
        String unitName,
        String plantName,
        String factoryName,
        String code,
        String name,
        String medium,
        String pressureRating,
        String temperatureRating,
        String hazardLevel,
        String pointType,
        String blindSpec,
        String equipmentTag,
        String pipelineNo,
        String status,
        String occupyStatus,
        String remark,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {
}
```

---

### Task 7: 后端 - IsolationPoint Service + Controller

**Files:**
- Create: `mangban-system/src/main/java/com/mangban/system/service/IsolationPointService.java`
- Create: `mangban-system/src/main/java/com/mangban/system/service/impl/IsolationPointServiceImpl.java`
- Create: `mangban-system/src/main/java/com/mangban/system/controller/IsolationPointController.java`

- [ ] **Step 1: 创建 IsolationPointService.java**

```java
package com.mangban.system.service;

import com.mangban.common.domain.PageResult;
import com.mangban.system.domain.dto.*;
import com.mangban.system.domain.vo.IsolationPointVO;

public interface IsolationPointService {
    PageResult<IsolationPointVO> list(Long unitId, Long plantId, String code, String name, String medium,
                                      String hazardLevel, String status, String occupyStatus,
                                      int page, int size);
    IsolationPointVO getById(Long id);
    IsolationPointVO create(IsolationPointCreateRequest request);
    IsolationPointVO update(Long id, IsolationPointUpdateRequest request);
    void delete(Long id);
    IsolationPointVO updateStatus(Long id, IsolationPointStatusRequest request);
    IsolationPointVO updateOccupy(Long id, IsolationPointOccupyRequest request);
}
```

- [ ] **Step 2: 创建 IsolationPointServiceImpl.java**

```java
package com.mangban.system.service.impl;

import com.mangban.common.constant.GlobalConstant;
import com.mangban.common.domain.PageResult;
import com.mangban.common.exception.BusinessException;
import com.mangban.system.domain.dto.*;
import com.mangban.system.domain.entity.SysIsolationPoint;
import com.mangban.system.domain.entity.SysLocation;
import com.mangban.system.domain.vo.IsolationPointVO;
import com.mangban.system.repository.SysIsolationPointRepository;
import com.mangban.system.repository.SysLocationRepository;
import com.mangban.system.service.IsolationPointService;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class IsolationPointServiceImpl implements IsolationPointService {
    private final SysIsolationPointRepository isolationPointRepository;
    private final SysLocationRepository locationRepository;

    public IsolationPointServiceImpl(SysIsolationPointRepository isolationPointRepository,
                                     SysLocationRepository locationRepository) {
        this.isolationPointRepository = isolationPointRepository;
        this.locationRepository = locationRepository;
    }

    @Override
    public PageResult<IsolationPointVO> list(Long unitId, Long plantId, String code, String name,
                                              String medium, String hazardLevel, String status,
                                              String occupyStatus, int page, int size) {
        Specification<SysIsolationPoint> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("isDeleted"), 0));
            if (unitId != null) predicates.add(cb.equal(root.get("unitId"), unitId));
            if (plantId != null) {
                // 通过 location 层级找到 plant 下所有 unit
                List<SysLocation> units = findUnitsByPlant(plantId);
                predicates.add(root.get("unitId").in(units.stream().map(SysLocation::getId).toList()));
            }
            if (StringUtils.hasText(code)) predicates.add(cb.like(root.get("code"), "%" + code + "%"));
            if (StringUtils.hasText(name)) predicates.add(cb.like(root.get("name"), "%" + name + "%"));
            if (StringUtils.hasText(medium)) predicates.add(cb.equal(root.get("medium"), medium));
            if (StringUtils.hasText(hazardLevel)) predicates.add(cb.equal(root.get("hazardLevel"), hazardLevel));
            if (StringUtils.hasText(status)) predicates.add(cb.equal(root.get("status"), status));
            if (StringUtils.hasText(occupyStatus)) predicates.add(cb.equal(root.get("occupyStatus"), occupyStatus));
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        Page<SysIsolationPoint> p = isolationPointRepository.findAll(spec,
                PageRequest.of(page - 1, size, Sort.by(Sort.Direction.ASC, "id")));
        return new PageResult<>(p.getTotalElements(), page, size,
                p.getContent().stream().map(this::toVO).toList());
    }

    private List<SysLocation> findUnitsByPlant(Long plantId) {
        List<SysLocation> units = new ArrayList<>();
        List<SysLocation> children = locationRepository.findByParentIdOrderBySortOrder(plantId);
        for (SysLocation child : children) {
            if ("UNIT".equals(child.getType()) && child.getIsDeleted() == 0) {
                units.add(child);
            }
        }
        return units;
    }

    private IsolationPointVO toVO(SysIsolationPoint ip) {
        String unitName = null, plantName = null, factoryName = null;
        Optional<SysLocation> unitOpt = locationRepository.findById(ip.getUnitId());
        if (unitOpt.isPresent()) {
            SysLocation unit = unitOpt.get();
            unitName = unit.getName();
            if (unit.getParentId() != null) {
                Optional<SysLocation> plantOpt = locationRepository.findById(unit.getParentId());
                if (plantOpt.isPresent()) {
                    SysLocation plant = plantOpt.get();
                    plantName = plant.getName();
                    if (plant.getParentId() != null) {
                        Optional<SysLocation> factoryOpt = locationRepository.findById(plant.getParentId());
                        factoryOpt.ifPresent(f -> factoryName = f.getName());
                    }
                }
            }
        }
        return new IsolationPointVO(ip.getId(), ip.getUnitId(), unitName, plantName, factoryName,
                ip.getCode(), ip.getName(), ip.getMedium(), ip.getPressureRating(),
                ip.getTemperatureRating(), ip.getHazardLevel(), ip.getPointType(),
                ip.getBlindSpec(), ip.getEquipmentTag(), ip.getPipelineNo(),
                ip.getStatus(), ip.getOccupyStatus(), ip.getRemark(),
                ip.getCreatedAt(), ip.getUpdatedAt());
    }

    @Override
    public IsolationPointVO getById(Long id) {
        SysIsolationPoint ip = isolationPointRepository.findById(id)
                .orElseThrow(() -> new BusinessException("隔离点不存在"));
        return toVO(ip);
    }

    @Override
    @Transactional
    public IsolationPointVO create(IsolationPointCreateRequest request) {
        // 校验 unitId 必须是 UNIT
        SysLocation unit = locationRepository.findById(request.unitId())
                .orElseThrow(() -> new BusinessException("所属单元不存在"));
        if (!"UNIT".equals(unit.getType())) {
            throw new BusinessException("隔离点必须挂在单元下");
        }
        // 校验编码唯一
        if (isolationPointRepository.findByCode(request.code()).isPresent()) {
            throw new BusinessException("编码已存在");
        }
        SysIsolationPoint ip = new SysIsolationPoint();
        ip.setUnitId(request.unitId());
        ip.setCode(request.code());
        ip.setName(request.name());
        ip.setMedium(request.medium());
        ip.setPressureRating(request.pressureRating());
        ip.setTemperatureRating(request.temperatureRating());
        ip.setHazardLevel(request.hazardLevel());
        ip.setPointType(request.pointType());
        ip.setBlindSpec(request.blindSpec());
        ip.setEquipmentTag(request.equipmentTag());
        ip.setPipelineNo(request.pipelineNo());
        ip.setRemark(request.remark());
        ip = isolationPointRepository.save(ip);
        return toVO(ip);
    }

    @Override
    @Transactional
    public IsolationPointVO update(Long id, IsolationPointUpdateRequest request) {
        SysIsolationPoint ip = isolationPointRepository.findById(id)
                .orElseThrow(() -> new BusinessException("隔离点不存在"));
        if (request.unitId() != null) {
            SysLocation unit = locationRepository.findById(request.unitId())
                    .orElseThrow(() -> new BusinessException("所属单元不存在"));
            if (!"UNIT".equals(unit.getType())) {
                throw new BusinessException("隔离点必须挂在单元下");
            }
            ip.setUnitId(request.unitId());
        }
        if (StringUtils.hasText(request.code())) {
            // 如果编码变了，检查唯一性
            if (!request.code().equals(ip.getCode())) {
                if (isolationPointRepository.findByCode(request.code()).isPresent()) {
                    throw new BusinessException("编码已存在");
                }
            }
            ip.setCode(request.code());
        }
        if (StringUtils.hasText(request.name())) ip.setName(request.name());
        if (StringUtils.hasText(request.medium())) ip.setMedium(request.medium());
        if (StringUtils.hasText(request.pressureRating())) ip.setPressureRating(request.pressureRating());
        if (StringUtils.hasText(request.temperatureRating())) ip.setTemperatureRating(request.temperatureRating());
        if (StringUtils.hasText(request.hazardLevel())) ip.setHazardLevel(request.hazardLevel());
        if (StringUtils.hasText(request.pointType())) ip.setPointType(request.pointType());
        if (StringUtils.hasText(request.blindSpec())) ip.setBlindSpec(request.blindSpec());
        if (StringUtils.hasText(request.equipmentTag())) ip.setEquipmentTag(request.equipmentTag());
        if (StringUtils.hasText(request.pipelineNo())) ip.setPipelineNo(request.pipelineNo());
        if (StringUtils.hasText(request.remark())) ip.setRemark(request.remark());
        ip = isolationPointRepository.save(ip);
        return toVO(ip);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        SysIsolationPoint ip = isolationPointRepository.findById(id)
                .orElseThrow(() -> new BusinessException("隔离点不存在"));
        ip.setIsDeleted(GlobalConstant.DELETED_YES);
        isolationPointRepository.save(ip);
    }

    @Override
    @Transactional
    public IsolationPointVO updateStatus(Long id, IsolationPointStatusRequest request) {
        if (!List.of("OPEN", "BLIND").contains(request.status())) {
            throw new BusinessException("无效状态值，仅支持 OPEN 或 BLIND");
        }
        SysIsolationPoint ip = isolationPointRepository.findById(id)
                .orElseThrow(() -> new BusinessException("隔离点不存在"));
        ip.setStatus(request.status());
        ip = isolationPointRepository.save(ip);
        return toVO(ip);
    }

    @Override
    @Transactional
    public IsolationPointVO updateOccupy(Long id, IsolationPointOccupyRequest request) {
        if (!List.of("OCCUPIED", "FREE").contains(request.occupyStatus())) {
            throw new BusinessException("无效占用状态值，仅支持 OCCUPIED 或 FREE");
        }
        SysIsolationPoint ip = isolationPointRepository.findById(id)
                .orElseThrow(() -> new BusinessException("隔离点不存在"));
        ip.setOccupyStatus(request.occupyStatus());
        ip = isolationPointRepository.save(ip);
        return toVO(ip);
    }
}
```

- [ ] **Step 3: 创建 IsolationPointController.java**

```java
package com.mangban.system.controller;

import com.mangban.common.domain.PageResult;
import com.mangban.common.domain.R;
import com.mangban.system.domain.dto.*;
import com.mangban.system.domain.vo.IsolationPointVO;
import com.mangban.system.service.IsolationPointService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/isolation-points")
public class IsolationPointController {
    private final IsolationPointService isolationPointService;

    public IsolationPointController(IsolationPointService isolationPointService) {
        this.isolationPointService = isolationPointService;
    }

    @GetMapping
    public R<PageResult<IsolationPointVO>> list(
            @RequestParam(required = false) Long unitId,
            @RequestParam(required = false) Long plantId,
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String medium,
            @RequestParam(required = false) String hazardLevel,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String occupyStatus,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return R.ok(isolationPointService.list(unitId, plantId, code, name, medium,
                hazardLevel, status, occupyStatus, page, size));
    }

    @GetMapping("/{id}")
    public R<IsolationPointVO> getById(@PathVariable Long id) {
        return R.ok(isolationPointService.getById(id));
    }

    @PostMapping
    public R<IsolationPointVO> create(@Valid @RequestBody IsolationPointCreateRequest request) {
        return R.ok(isolationPointService.create(request));
    }

    @PutMapping("/{id}")
    public R<IsolationPointVO> update(@PathVariable Long id, @Valid @RequestBody IsolationPointUpdateRequest request) {
        return R.ok(isolationPointService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        isolationPointService.delete(id);
        return R.ok();
    }

    @PutMapping("/{id}/status")
    public R<IsolationPointVO> updateStatus(@PathVariable Long id,
                                             @Valid @RequestBody IsolationPointStatusRequest request) {
        return R.ok(isolationPointService.updateStatus(id, request));
    }

    @PutMapping("/{id}/occupy")
    public R<IsolationPointVO> updateOccupy(@PathVariable Long id,
                                              @Valid @RequestBody IsolationPointOccupyRequest request) {
        return R.ok(isolationPointService.updateOccupy(id, request));
    }
}
```

---

### Task 8: 前端 - 类型定义

**Files:**
- Create: `mangban-ui/src/types/location.ts`
- Create: `mangban-ui/src/types/isolation-point.ts`

- [ ] **Step 1: 创建 types/location.ts**

```ts
export interface LocationTreeNode {
  id: number
  parentId: number | null
  name: string
  code: string
  type: 'FACTORY' | 'PLANT' | 'UNIT'
  sortOrder: number
  remark: string
  children: LocationTreeNode[] | null
}

export interface LocationCreateForm {
  parentId?: number | null
  name: string
  code: string
  type: string
  sortOrder?: number
  remark?: string
}

export interface LocationUpdateForm {
  name?: string
  code?: string
  sortOrder?: number
  remark?: string
}
```

- [ ] **Step 2: 创建 types/isolation-point.ts**

```ts
export interface IsolationPointVO {
  id: number
  unitId: number
  unitName: string
  plantName: string
  factoryName: string
  code: string
  name: string
  medium: string
  pressureRating: string
  temperatureRating: string
  hazardLevel: string
  pointType: string
  blindSpec: string
  equipmentTag: string
  pipelineNo: string
  status: string
  occupyStatus: string
  remark: string
  createdAt: string
  updatedAt: string
}

export interface IsolationPointCreateForm {
  unitId: number
  code: string
  name: string
  medium?: string
  pressureRating?: string
  temperatureRating?: string
  hazardLevel?: string
  pointType?: string
  blindSpec?: string
  equipmentTag?: string
  pipelineNo?: string
  remark?: string
}

export interface IsolationPointUpdateForm {
  unitId?: number
  code?: string
  name?: string
  medium?: string
  pressureRating?: string
  temperatureRating?: string
  hazardLevel?: string
  pointType?: string
  blindSpec?: string
  equipmentTag?: string
  pipelineNo?: string
  remark?: string
}

export interface IsolationPointQueryParams {
  unitId?: number
  plantId?: number
  code?: string
  name?: string
  medium?: string
  hazardLevel?: string
  status?: string
  occupyStatus?: string
  page?: number
  size?: number
}
```

---

### Task 9: 前端 - API 封装

**Files:**
- Create: `mangban-ui/src/api/location.ts`
- Create: `mangban-ui/src/api/isolation-point.ts`

- [ ] **Step 1: 创建 api/location.ts**

```ts
import http from '@/utils/http'
import type { R } from '@/types/common'
import type { LocationTreeNode, LocationCreateForm, LocationUpdateForm } from '@/types/location'

export function getLocationTree() {
  return http.get<any, R<LocationTreeNode[]>>('/locations/tree')
}

export function createLocation(data: LocationCreateForm) {
  return http.post<any, R<LocationTreeNode>>('/locations', data)
}

export function updateLocation(id: number, data: LocationUpdateForm) {
  return http.put<any, R<LocationTreeNode>>(`/locations/${id}`, data)
}

export function deleteLocation(id: number) {
  return http.delete<any, R<null>>(`/locations/${id}`)
}
```

- [ ] **Step 2: 创建 api/isolation-point.ts**

```ts
import http from '@/utils/http'
import type { R } from '@/types/common'
import type { PageResult } from '@/types/common'
import type { IsolationPointVO, IsolationPointCreateForm, IsolationPointUpdateForm, IsolationPointQueryParams } from '@/types/isolation-point'

export function getIsolationPointList(params: IsolationPointQueryParams) {
  return http.get<any, R<PageResult<IsolationPointVO>>>('/isolation-points', { params })
}

export function getIsolationPointById(id: number) {
  return http.get<any, R<IsolationPointVO>>(`/isolation-points/${id}`)
}

export function createIsolationPoint(data: IsolationPointCreateForm) {
  return http.post<any, R<IsolationPointVO>>('/isolation-points', data)
}

export function updateIsolationPoint(id: number, data: IsolationPointUpdateForm) {
  return http.put<any, R<IsolationPointVO>>(`/isolation-points/${id}`, data)
}

export function deleteIsolationPoint(id: number) {
  return http.delete<any, R<null>>(`/isolation-points/${id}`)
}

export function updateIsolationPointStatus(id: number, status: string) {
  return http.put<any, R<IsolationPointVO>>(`/isolation-points/${id}/status`, { status })
}

export function updateIsolationPointOccupy(id: number, occupyStatus: string) {
  return http.put<any, R<IsolationPointVO>>(`/isolation-points/${id}/occupy`, { occupyStatus })
}
```

---

### Task 10: 前端 - 装置层级管理页

**Files:**
- Create: `mangban-ui/src/views/process/LocationPage.vue`

- [ ] **Step 1: 创建 LocationPage.vue（树形表格模式，参考 OrgPage.vue）**

```vue
<script setup lang="ts">
import { ref, computed } from 'vue'
import { SearchTable } from '@/components/business'
import type { SearchField, TableColumn, ActionButton, FormConfig } from '@/components/business/types'
import { getLocationTree, createLocation, updateLocation, deleteLocation } from '@/api/location'
import type { LocationTreeNode } from '@/types/location'

const searchTableRef = ref()
const list = ref<LocationTreeNode[]>([])

const typeOptions = [
  { label: '工厂', value: 'FACTORY' },
  { label: '装置', value: 'PLANT' },
  { label: '单元', value: 'UNIT' },
]

const searchFields: SearchField[] = []

const columns: TableColumn[] = [
  { prop: 'name', label: '名称', minWidth: 200 },
  { prop: 'code', label: '编码', width: 160 },
  {
    label: '类型', width: 100, align: 'center',
    formatter: (_r: any, _c: any, v: string) =>
      ({ FACTORY: '工厂', PLANT: '装置', UNIT: '单元' }[v] || v),
  },
  { prop: 'sortOrder', label: '排序', width: 80, align: 'center' },
]

async function fetchApi(_params: any) {
  const res = await getLocationTree()
  list.value = res.data
  return { rows: res.data, total: res.data.length }
}

function handleAddChild(parentId: number) {
  searchTableRef.value?.openFormDialog({ parentId, sortOrder: 0 })
}

function handleAddRoot() {
  searchTableRef.value?.openFormDialog({ sortOrder: 0 })
}

const actionButtons: ActionButton[] = [
  { label: '新增子节点', size: 'small', type: 'text', onClick: (row: LocationTreeNode) => handleAddChild(row.id) },
]

const formConfig = computed<FormConfig<LocationTreeNode>>(() => ({
  fields: [
    {
      type: 'tree-select', label: '上级节点', prop: 'parentId',
      placeholder: '选择上级（空=根工厂）',
      treeProps: { data: list.value, props: { label: 'name', value: 'id', children: 'children' } },
    },
    { type: 'input', label: '名称', prop: 'name', rules: [{ required: true, message: '请输入名称', trigger: 'blur' }] },
    { type: 'input', label: '编码', prop: 'code', rules: [{ required: true, message: '请输入编码', trigger: 'blur' }] },
    { type: 'select', label: '类型', prop: 'type', options: typeOptions, rules: [{ required: true, message: '请选择类型', trigger: 'change' }] },
    { type: 'input', label: '排序', prop: 'sortOrder' },
    { type: 'textarea', label: '备注', prop: 'remark' },
  ],
  createApi: createLocation,
  updateApi: (id, data) => updateLocation(id as number, data),
  deleteApi: deleteLocation,
  getApi: async (id) => {
    const res = await getLocationTree()
    return findNode(res.data, id)
  },
  dialogTitle: { create: '新增节点', edit: '编辑节点' },
}))

function findNode(tree: LocationTreeNode[], id: number): LocationTreeNode | null {
  for (const node of tree) {
    if (node.id === id) return node
    if (node.children) {
      const found = findNode(node.children, id)
      if (found) return found
    }
  }
  return null
}
</script>

<template>
  <SearchTable
    ref="searchTableRef"
    :search-fields="searchFields"
    :columns="columns"
    :action-buttons="actionButtons"
    :fetch-api="fetchApi"
    :form-config="formConfig"
    :tree-props="{ rowKey: 'id', children: 'children', defaultExpandAll: true }"
    :show-search="false"
  />
</template>
```

---

### Task 11: 前端 - 隔离点台账页

**Files:**
- Create: `mangban-ui/src/views/process/IsolationPointPage.vue`
- Modify: `mangban-ui/src/types/common.ts`（检查 PageResult 是否已定义）

- [ ] **Step 1: 创建 IsolationPointPage.vue**

```vue
<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { SearchTable } from '@/components/business'
import type { SearchField, TableColumn, ActionButton, FormConfig } from '@/components/business/types'
import { getIsolationPointList, createIsolationPoint, updateIsolationPoint, deleteIsolationPoint, getIsolationPointById, updateIsolationPointStatus, updateIsolationPointOccupy } from '@/api/isolation-point'
import { getLocationTree } from '@/api/location'
import type { IsolationPointVO } from '@/types/isolation-point'
import type { LocationTreeNode } from '@/types/location'

const searchTableRef = ref()
const locationTree = ref<LocationTreeNode[]>([])

onMounted(async () => {
  const res = await getLocationTree()
  locationTree.value = res.data
})

const hazardLevelOptions = [
  { label: '全部', value: undefined },
  { label: 'A级', value: 'A' },
  { label: 'B级', value: 'B' },
  { label: 'C级', value: 'C' },
]

const statusOptions = [
  { label: '全部', value: undefined },
  { label: '通板', value: 'OPEN' },
  { label: '盲板', value: 'BLIND' },
]

const searchFields = computed<SearchField[]>(() => [
  { type: 'input', label: '编码', prop: 'code', placeholder: '输入编码' },
  { type: 'input', label: '名称', prop: 'name', placeholder: '输入名称' },
  {
    type: 'tree-select', label: '所属单元', prop: 'unitId', placeholder: '选择单元',
    treeProps: { data: locationTree.value, props: { label: 'name', value: 'id', children: 'children' } },
  },
  { type: 'input', label: '介质', prop: 'medium', placeholder: '输入介质' },
  { type: 'select', label: '危害等级', prop: 'hazardLevel', options: hazardLevelOptions },
  { type: 'select', label: '状态', prop: 'status', options: statusOptions },
])

const columns: TableColumn[] = [
  { prop: 'code', label: '编码', width: 140 },
  { prop: 'name', label: '名称', minWidth: 140 },
  { prop: 'unitName', label: '所属单元', width: 120 },
  { prop: 'medium', label: '介质', width: 100 },
  { prop: 'hazardLevel', label: '危害等级', width: 90, align: 'center' },
  {
    label: '通盲状态', width: 100, align: 'center', slotName: 'status',
  },
  {
    label: '占用状态', width: 100, align: 'center', slotName: 'occupyStatus',
  },
  { prop: 'createdAt', label: '创建时间', width: 170 },
]

const formConfig = computed<FormConfig<IsolationPointVO>>(() => ({
  fields: [
    {
      type: 'tree-select', label: '所属单元', prop: 'unitId', placeholder: '选择单元',
      rules: [{ required: true, message: '请选择所属单元', trigger: 'change' }],
      treeProps: { data: locationTree.value, props: { label: 'name', value: 'id', children: 'children' } },
    },
    { type: 'input', label: '编码', prop: 'code', placeholder: '请输入编码', rules: [{ required: true, message: '请输入编码', trigger: 'blur' }] },
    { type: 'input', label: '名称', prop: 'name', placeholder: '请输入名称', rules: [{ required: true, message: '请输入名称', trigger: 'blur' }] },
    { type: 'input', label: '介质', prop: 'medium', placeholder: '请输入介质' },
    { type: 'input', label: '压力等级', prop: 'pressureRating', placeholder: '请输入压力等级' },
    { type: 'input', label: '温度等级', prop: 'temperatureRating', placeholder: '请输入温度等级' },
    { type: 'select', label: '危害等级', prop: 'hazardLevel', options: hazardLevelOptions.filter(o => o.value) },
    { type: 'input', label: '点位类型', prop: 'pointType', placeholder: '请输入点位类型' },
    { type: 'input', label: '适配盲板规格', prop: 'blindSpec', placeholder: '请输入盲板规格' },
    { type: 'input', label: '关联设备位号', prop: 'equipmentTag', placeholder: '请输入设备位号' },
    { type: 'input', label: '关联管线号', prop: 'pipelineNo', placeholder: '请输入管线号' },
    { type: 'textarea', label: '备注', prop: 'remark' },
  ],
  createApi: createIsolationPoint,
  updateApi: (id, data) => updateIsolationPoint(id as number, data),
  deleteApi: deleteIsolationPoint,
  getApi: (id) => getIsolationPointById(id as number).then(r => r.data),
  dialogTitle: { create: '新增隔离点', edit: '编辑隔离点' },
}))

const actionButtons: ActionButton[] = [
  {
    label: '设为盲板', size: 'small', type: 'text', confirm: '确定设为盲板吗？',
    onClick: async (row: IsolationPointVO) => {
      await updateIsolationPointStatus(row.id, 'BLIND')
      searchTableRef.value?.fetchList()
    },
  },
  {
    label: '设为通板', size: 'small', type: 'text', confirm: '确定设为通板吗？',
    onClick: async (row: IsolationPointVO) => {
      await updateIsolationPointStatus(row.id, 'OPEN')
      searchTableRef.value?.fetchList()
    },
  },
  {
    label: '占用', size: 'small', type: 'text', confirm: '确定占用该隔离点吗？',
    onClick: async (row: IsolationPointVO) => {
      await updateIsolationPointOccupy(row.id, 'OCCUPIED')
      searchTableRef.value?.fetchList()
    },
  },
  {
    label: '释放', size: 'small', type: 'text', confirm: '确定释放该隔离点吗？',
    onClick: async (row: IsolationPointVO) => {
      await updateIsolationPointOccupy(row.id, 'FREE')
      searchTableRef.value?.fetchList()
    },
  },
]

async function fetchApi(params: any) {
  const res = await getIsolationPointList(params)
  return { rows: res.data.rows, total: res.data.total }
}
</script>

<template>
  <SearchTable
    ref="searchTableRef"
    :search-fields="searchFields"
    :columns="columns"
    :action-buttons="actionButtons"
    :fetch-api="fetchApi"
    :form-config="formConfig"
  >
    <template #status="{ row }">
      <el-tag :type="row.status === 'OPEN' ? 'success' : 'danger'" size="small">
        {{ row.status === 'OPEN' ? '通板' : '盲板' }}
      </el-tag>
    </template>
    <template #occupyStatus="{ row }">
      <el-tag :type="row.occupyStatus === 'OCCUPIED' ? 'warning' : 'info'" size="small">
        {{ row.occupyStatus === 'OCCUPIED' ? '已占用' : '空闲' }}
      </el-tag>
    </template>
  </SearchTable>
</template>
```

---

### Task 12: 前端 - 状态台账总览页

**Files:**
- Create: `mangban-ui/src/views/process/IsolationPointStatusPage.vue`

- [ ] **Step 1: 创建 IsolationPointStatusPage.vue**

```vue
<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { SearchTable } from '@/components/business'
import type { SearchField, TableColumn, FormConfig } from '@/components/business/types'
import { getIsolationPointList } from '@/api/isolation-point'
import { getLocationTree } from '@/api/location'
import type { IsolationPointVO } from '@/types/isolation-point'
import type { LocationTreeNode } from '@/types/location'

const locationTree = ref<LocationTreeNode[]>([])

onMounted(async () => {
  const res = await getLocationTree()
  locationTree.value = res.data
})

const searchFields = computed<SearchField[]>(() => [
  {
    type: 'tree-select', label: '所属装置', prop: 'plantId', placeholder: '选择装置',
    treeProps: { data: locationTree.value, props: { label: 'name', value: 'id', children: 'children' } },
  },
  { type: 'input', label: '介质', prop: 'medium', placeholder: '输入介质' },
  { type: 'select', label: '危害等级', prop: 'hazardLevel', options: [
    { label: '全部', value: undefined },
    { label: 'A级', value: 'A' },
    { label: 'B级', value: 'B' },
    { label: 'C级', value: 'C' },
  ]},
  { type: 'select', label: '通盲状态', prop: 'status', options: [
    { label: '全部', value: undefined },
    { label: '通板', value: 'OPEN' },
    { label: '盲板', value: 'BLIND' },
  ]},
  { type: 'select', label: '占用状态', prop: 'occupyStatus', options: [
    { label: '全部', value: undefined },
    { label: '已占用', value: 'OCCUPIED' },
    { label: '空闲', value: 'FREE' },
  ]},
])

const columns: TableColumn[] = [
  { prop: 'code', label: '编码', width: 140 },
  { prop: 'name', label: '名称', minWidth: 140 },
  { prop: 'factoryName', label: '所属工厂', width: 120 },
  { prop: 'plantName', label: '所属装置', width: 120 },
  { prop: 'unitName', label: '所属单元', width: 120 },
  { prop: 'medium', label: '介质', width: 100 },
  { prop: 'hazardLevel', label: '危害等级', width: 90, align: 'center' },
  { label: '通盲状态', width: 100, align: 'center', slotName: 'status' },
  { label: '占用状态', width: 100, align: 'center', slotName: 'occupyStatus' },
]

const formConfig = computed<FormConfig<IsolationPointVO>>(() => ({
  fields: [],
  createApi: undefined as any,
  updateApi: undefined as any,
  deleteApi: undefined as any,
  getApi: undefined as any,
}))

async function fetchApi(params: any) {
  const res = await getIsolationPointList(params)
  return { rows: res.data.rows, total: res.data.total }
}
</script>

<template>
  <SearchTable
    :search-fields="searchFields"
    :columns="columns"
    :fetch-api="fetchApi"
    :form-config="formConfig"
  >
    <template #status="{ row }">
      <el-tag :type="row.status === 'OPEN' ? 'success' : 'danger'" size="small">
        {{ row.status === 'OPEN' ? '通板' : '盲板' }}
      </el-tag>
    </template>
    <template #occupyStatus="{ row }">
      <el-tag :type="row.occupyStatus === 'OCCUPIED' ? 'warning' : 'info'" size="small">
        {{ row.occupyStatus === 'OCCUPIED' ? '已占用' : '空闲' }}
      </el-tag>
    </template>
  </SearchTable>
</template>
```

---

### Task 13: 前端 - 路由配置

**Files:**
- Modify: `mangban-ui/src/router/index.ts`

- [ ] **Step 1: 在 router/index.ts 的 children 中添加 3 条路由**

在 `profile` 路由之后，`404` 路由之前插入：

```ts
{
  path: 'process/locations',
  name: 'LocationManagement',
  component: () => import('@/views/process/LocationPage.vue'),
  meta: { title: '装置层级管理' }
},
{
  path: 'process/isolation-points',
  name: 'IsolationPointManagement',
  component: () => import('@/views/process/IsolationPointPage.vue'),
  meta: { title: '隔离点台账' }
},
{
  path: 'process/isolation-points/status',
  name: 'IsolationPointStatus',
  component: () => import('@/views/process/IsolationPointStatusPage.vue'),
  meta: { title: '状态台账总览' }
},
```

---

### Task 14: 编译验证

**Files:**
- None (build commands)

- [ ] **Step 1: 编译后端验证**

```bash
cd mangban && mvn compile -q
```

Expected: BUILD SUCCESS

- [ ] **Step 2: 编译前端验证**

```bash
cd mangban-ui && npx vue-tsc --noEmit
```

Expected: 0 errors