## 1. 数据库变更

- [ ] 1.1 在 schema.sql 中添加 sys_location 表 DDL（字段：id, parent_id, name, code, type, sort_order, remark, is_deleted, created_by/at, updated_by/at）
- [ ] 1.2 在 schema.sql 中添加 sys_isolation_point 表 DDL（字段：id, unit_id, code, name, medium, pressure_rating, temperature_rating, hazard_level, point_type, blind_spec, equipment_tag, pipeline_no, status, occupy_status, remark, is_deleted, created_by/at, updated_by/at）
- [ ] 1.3 添加隔离点状态初始数据：status 默认 OPEN，occupy_status 默认 FREE

## 2. 后端 - 装置层级 (Location)

- [ ] 2.1 创建 SysLocation Entity（继承 BaseEntity，含 parentId 自引用 @ManyToOne，children 反向 @OneToMany）
- [ ] 2.2 创建 LocationRepository（JpaRepository + JpaSpecificationExecutor，含 findByParentIdIsNullOrderBySortOrder）
- [ ] 2.3 创建 LocationCreateRequest DTO（Record: parentId, name, code, type, sortOrder, remark，type 用 @NotBlank）
- [ ] 2.4 创建 LocationUpdateRequest DTO（Record: 所有字段可选）
- [ ] 2.5 创建 LocationVO（Record: id, parentId, name, code, type, sortOrder, remark, children, createdAt, updatedAt）
- [ ] 2.6 创建 LocationTreeNode VO（Record: id, parentId, name, code, type, sortOrder, children: List<LocationTreeNode>）
- [ ] 2.7 创建 LocationService 接口（getTree, list, getById, create, update, delete）
- [ ] 2.8 创建 LocationServiceImpl（树形查询递归构建、创建时校验 parent.type 层级约束、删除时校验子节点和关联隔离点）
- [ ] 2.9 创建 LocationController（GET /api/locations/tree, GET /api/locations, GET /api/locations/{id}, POST /api/locations, PUT /api/locations/{id}, DELETE /api/locations/{id}）

## 3. 后端 - 隔离点台账 (IsolationPoint)

- [ ] 3.1 创建 SysIsolationPoint Entity（继承 BaseEntity，@ManyToOne 关联 SysLocation unitId）
- [ ] 3.2 创建 IsolationPointRepository（JpaRepository + JpaSpecificationExecutor，含 findByCode）
- [ ] 3.3 创建 IsolationPointCreateRequest DTO（Record: unitId, code, name, medium, pressureRating, temperatureRating, hazardLevel, pointType, blindSpec, equipmentTag, pipelineNo, remark）
- [ ] 3.4 创建 IsolationPointUpdateRequest DTO（Record: 所有字段可选）
- [ ] 3.5 创建 IsolationPointStatusRequest DTO（Record: status）
- [ ] 3.6 创建 IsolationPointOccupyRequest DTO（Record: occupyStatus）
- [ ] 3.7 创建 IsolationPointVO（Record: id, unitId, unitName, plantName, factoryName, code, name, medium, pressureRating, temperatureRating, hazardLevel, pointType, blindSpec, equipmentTag, pipelineNo, status, occupyStatus, remark, createdAt, updatedAt）
- [ ] 3.8 创建 IsolationPointService 接口（list, getById, create, update, delete, updateStatus, updateOccupy）
- [ ] 3.9 创建 IsolationPointServiceImpl（创建时校验 unitId→type=UNIT + code 唯一，list 支持多条件 JpaSpecification 动态筛选，VO 中填充 unitName/plantName/factoryName 层级路径）
- [ ] 3.10 创建 IsolationPointController（GET /api/isolation-points, GET /api/isolation-points/{id}, POST /api/isolation-points, PUT /api/isolation-points/{id}, DELETE /api/isolation-points/{id}, PUT /api/isolation-points/{id}/status, PUT /api/isolation-points/{id}/occupy）

## 4. 前端 - 类型定义

- [ ] 4.1 创建 types/location.ts（Location, LocationTreeNode, LocationCreateForm, LocationUpdateForm, LocationQueryParams）
- [ ] 4.2 创建 types/isolation-point.ts（IsolationPoint, IsolationPointCreateForm, IsolationPointUpdateForm, IsolationPointQueryParams）

## 5. 前端 - API 封装

- [ ] 5.1 创建 api/location.ts（getLocationTree, getLocationList, getLocationById, createLocation, updateLocation, deleteLocation）
- [ ] 5.2 创建 api/isolation-point.ts（getIsolationPointList, getIsolationPointById, createIsolationPoint, updateIsolationPoint, deleteIsolationPoint, updateIsolationPointStatus, updateIsolationPointOccupy）

## 6. 前端 - 装置层级管理页

- [ ] 6.1 创建 views/process/LocationPage.vue（SearchTable 树形模式，searchFields 仅保留 name 模糊搜索，columns: name/code/type/sortOrder/remark，treeProps: rowKey=id, children=children，showSearch=none）
- [ ] 6.2 配置 LocationPage 的 formConfig（字段: parentId→tree-select 选父级, name→input, code→input, type→select(FACTORY/PLANT/UNIT), sortOrder→input-number, remark→textarea）
- [ ] 6.3 配置 LocationPage 的 actionButtons（编辑/删除，删除前确认，带 v-permission 权限控制）

## 7. 前端 - 隔离点台账页

- [ ] 7.1 创建 views/process/IsolationPointPage.vue（SearchTable 分页模式，searchFields: code/name/medium/hazardLevel/status/unitId，columns: code/name/unitName/medium/hazardLevel/status/occupyStatus）
- [ ] 7.2 配置 IsolationPointPage 的列渲染（status 列→el-tag 颜色，occupyStatus 列→el-tag 颜色）
- [ ] 7.3 配置 IsolationPointPage 的 formConfig（所有台账字段，unitId→tree-select 或 ReferencePicker）

## 8. 前端 - 状态台账总览页

- [ ] 8.1 创建 views/process/IsolationPointStatusPage.vue（复用 IsolationPointPage 模式，额外增加 occupyStatus 搜索字段，增加 plantId 搜索字段）
- [ ] 8.2 配置状态总览页的颜色标签（OPEN=绿/BLIND=红，OCCUPIED=橙/FREE=灰）

## 9. 前端 - 路由和菜单

- [ ] 9.1 在 router/index.ts 的 children 中添加 3 条路由（/process/locations, /process/isolation-points, /process/isolation-points/status）
- [ ] 9.2 在数据库 sys_menu 表中插入菜单记录（装置层级管理, 隔离点台账, 状态台账总览）和对应权限标识

## 10. 验证

- [ ] 10.1 启动后端服务，验证 Location CRUD 接口（curl 测试 tree/list/create/update/delete）
- [ ] 10.2 验证 IsolationPoint CRUD 接口（curl 测试 list/create/update/delete/status/occupy）
- [ ] 10.3 启动前端，验证 3 个页面渲染和 CRUD 操作
- [ ] 10.4 验证状态总览页筛选和颜色标签显示
- [ ] 10.5 运行前后端 lint/type check 确保无错误