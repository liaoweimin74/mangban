# 经验教训

## 多模块 Maven 项目：改依赖模块后 spring-boot:run 不生效

**问题**：`mangban-admin` 依赖 `mangban-system`，修改 `mangban-system` 代码后 `mvn spring-boot:run` 不会重新打包依赖模块的 class。

**根因**：`spring-boot:run` 只编译 admin 模块本身，依赖的 system 模块 class 来自本地 Maven 仓库（`~/.m2`），不会自动刷新。

**正确流程**：

```powershell
# 1. 安装依赖模块到本地仓库（编译 + install）
cd mangban
mvn install -pl mangban-system -DskipTests

# 2. 再启动
cd mangban-admin
$env:SPRING_PROFILES_ACTIVE="dev"
mvn spring-boot:run
```

**或者一次性编译所有：**

```powershell
cd mangban
mvn compile -pl mangban-system -am   # -am = also-make，编译 system 及其依赖
mvn install -pl mangban-system -DskipTests  # install 到本地仓库
cd mangban-admin
mvn spring-boot:run
```

**判断标准**：改了 `mangban-common` / `mangban-system` 等非 admin 模块 → 必须先 `mvn install`。

