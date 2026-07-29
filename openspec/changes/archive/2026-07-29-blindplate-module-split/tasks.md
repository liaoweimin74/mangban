## 1. 后端模块创建（mangban-blindplate）

- [ ] 1.1 创建 `mangban-blindplate/` 目录结构（src/main/java/com/mangban/blindplate/controller/、service/、domain/entity/、domain/dto/、domain/vo/、repository/）
- [ ] 1.2 创建 `mangban-blindplate/pom.xml`，依赖 mangban-common 和 mangban-framework
- [ ] 1.3 父 POM `mangban/pom.xml` 的 modules 新增 `<module>mangban-blindplate</module>`
- [ ] 1.4 复制 IsolationPointController.java 到 mangban-blindplate/controller/，修改包名为 `com.mangban.blindplate.controller`
- [ ] 1.5 复制 SysIsolationPoint.java 到 mangban-blindplate/domain/entity/，修改包名为 `com.mangban.blindplate.domain.entity`
- [ ] 1.6 复制 SysIsolationPointRepository.java 到 mangban-blindplate/repository/，修改包名为 `com.mangban.blindplate.repository`
- [ ] 1.7 复制 IsolationPointService.java 和 IsolationPointServiceImpl.java 到 mangban-blindplate/service/，修改包名为 `com.mangban.blindplate.service`
- [ ] 1.8 复制所有 IsolationPoint DTO 到 mangban-blindplate/domain/dto/，修改包名
- [ ] 1.9 复制 IsolationPointVO.java 到 mangban-blindplate/domain/vo/，修改包名
- [ ] 1.10 mangban-admin/pom.xml 新增 mangban-blindplate 依赖
- [ ] 1.11 运行 `mvn compile` 验证编译成功

## 2. 清理 mangban-system 盲板代码

- [ ] 2.1 删除 mangban-system 中 IsolationPointController.java
- [ ] 2.2 删除 mangban-system 中 SysIsolationPoint.java
- [ ] 2.3 删除 mangban-system 中 SysIsolationPointRepository.java
- [ ] 2.4 删除 mangban-system 中 IsolationPointService.java 和 IsolationPointServiceImpl.java
- [ ] 2.5 删除 mangban-system 中所有 IsolationPoint DTO 文件
- [ ] 2.6 删除 mangban-system 中 IsolationPointVO.java
- [ ] 2.7 运行 `mvn compile` 验证无编译错误

## 3. 前端目录重命名

- [ ] 3.1 将 `views/process/` 目录重命名为 `views/blindplate/`
- [ ] 3.2 更新 `router/index.ts` 中路由路径 `/process/` → `/blindplate/` 和 import 路径
- [ ] 3.3 搜索 mangban-ui/src 中所有 `views/process` 引用，确认无残留
- [ ] 3.4 运行 `npx vitest run` 验证前端测试通过

## 4. 菜单数据更新

- [ ] 4.1 编写 SQL 更新脚本 UPDATE menu SET route_path = REPLACE(route_path, '/process/', '/blindplate/')
- [ ] 4.2 确认 SQL 执行后无旧路径残留