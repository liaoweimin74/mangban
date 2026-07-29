# 盲板模块拆分 Implementation Plan

> **For agentic workers:** Use superpowers:subagent-driven-development to implement this plan task-by-task.

**Goal:** 后端将盲板业务从 mangban-system 分离为独立模块 mangban-blindplate，前端将 views/process/ 重命名为 views/blindplate/

**Architecture:** 后端新建 Maven 子模块（依赖 mangban-common + mangban-framework），迁移所有 IsolationPoint 代码并修改包名；前端目录重命名 + 路由路径更新 + 菜单数据同步

**Tech Stack:** Maven + Spring Boot 3.5 + JPA + Vue 3 + TypeScript + Element Plus

---

## Task 1: 后端模块创建（mangban-blindplate）

- [ ] **Step 1:** 创建目录结构 `mangban-blindplate/src/main/java/com/mangban/blindplate/{controller,service,domain/{entity,dto,vo},repository}`
- [ ] **Step 2:** 创建 `mangban-blindplate/pom.xml`（parent 指向 mangban，依赖 mangban-common + mangban-framework）
- [ ] **Step 3:** 父 POM `mangban/pom.xml` 的 modules 添加 mangban-blindplate
- [ ] **Step 4:** 复制 IsolationPointController.java → mangban-blindplate/controller/，包名改为 `com.mangban.blindplate.controller`
- [ ] **Step 5:** 复制 SysIsolationPoint.java → mangban-blindplate/domain/entity/，包名改为 `com.mangban.blindplate.domain.entity`
- [ ] **Step 6:** 复制 SysIsolationPointRepository.java → mangban-blindplate/repository/，包名改为 `com.mangban.blindplate.repository`
- [ ] **Step 7:** 复制 IsolationPointService.java + IsolationPointServiceImpl.java → mangban-blindplate/service/，包名改为 `com.mangban.blindplate.service`
- [ ] **Step 8:** 复制所有 IsolationPoint DTO → mangban-blindplate/domain/dto/，修改包名
- [ ] **Step 9:** 复制 IsolationPointVO.java → mangban-blindplate/domain/vo/，修改包名
- [ ] **Step 10:** mangban-admin/pom.xml 添加 mangban-blindplate 依赖
- [ ] **Commit:** `mvn compile` 验证编译通过后提交 "feat: 创建 mangban-blindplate 模块，迁移盲板代码"

## Task 2: 清理 mangban-system 盲板代码

- [ ] **Step 1:** 删除 mangban-system 中所有 IsolationPoint 相关文件（controller/entity/repository/service/impl/dto/vo）
- [ ] **Commit:** `mvn compile` 验证无编译错误后提交 "refactor: 从 mangban-system 删除盲板相关代码"

## Task 3: 前端目录重命名

- [ ] **Step 1:** `git mv mangban-ui/src/views/process mangban-ui/src/views/blindplate`
- [ ] **Step 2:** 修改 `router/index.ts` 中路由路径 `/process/` → `/blindplate/`，import 路径同步更新
- [ ] **Step 3:** 搜索 mangban-ui/src 中所有残留的 `views/process` 引用并修复
- [ ] **Commit:** `npx vitest run` 测试通过后提交 "refactor: 前端 views/process 重命名为 blindplate"

## Task 4: 菜单数据更新

- [ ] **Step 1:** 编写并执行 SQL: `UPDATE menu SET route_path = REPLACE(route_path, '/process/', '/blindplate/')`
- [ ] **Commit:** 提交 "chore: 同步更新菜单表路由路径 /process/ → /blindplate/"