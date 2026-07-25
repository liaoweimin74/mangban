<!--
Spec for user-auth capability: 用户认证与授权体系
-->

## ADDED Requirements

### Requirement: 用户登录验证
系统 SHALL 支持用户通过用户名和密码进行登录验证。

#### Scenario: 用户名密码正确时登录成功
- **WHEN** 用户输入正确的用户名和密码
- **THEN** 系统返回 JWT access_token 和 refresh_token，以及用户基本信息

#### Scenario: 用户名不存在时登录失败
- **WHEN** 用户输入不存在的用户名
- **THEN** 系统返回 401 错误，提示"用户名或密码错误"

#### Scenario: 密码错误时登录失败
- **WHEN** 用户输入正确的用户名但错误的密码
- **THEN** 系统返回 401 错误，提示"用户名或密码错误"

#### Scenario: 连续多次登录失败
- **WHEN** 用户在 30 分钟内连续 5 次登录失败
- **THEN** 系统锁定该账号 30 分钟，返回 429 错误

### Requirement: Token 管理
系统 SHALL 使用 JWT 双 Token 机制进行身份验证。

#### Scenario: Access Token 有效期内访问接口
- **WHEN** 用户在请求头中携带有效的 access_token
- **THEN** 系统正常处理请求并返回数据

#### Scenario: Access Token 过期时自动刷新
- **WHEN** 用户携带已过期的 access_token 请求接口
- **THEN** 系统返回 401 错误，前端自动使用 refresh_token 刷新

#### Scenario: Refresh Token 有效时刷新成功
- **WHEN** 用户携带有效的 refresh_token 调用刷新接口
- **THEN** 系统返回新的 access_token 和 refresh_token

#### Scenario: Refresh Token 过期时重新登录
- **WHEN** 用户携带已过期的 refresh_token 调用刷新接口
- **THEN** 系统返回 401 错误，前端跳转登录页

### Requirement: 用户退出登录
系统 SHALL 支持用户退出登录。

#### Scenario: 正常退出
- **WHEN** 已登录用户调用退出接口
- **THEN** 系统清除该用户的 Token 缓存，前端跳转登录页

#### Scenario: 退出后 Token 失效
- **WHEN** 已退出的用户使用原 Token 请求接口
- **THEN** 系统返回 401 错误

### Requirement: 获取当前用户信息
系统 SHALL 提供当前登录用户信息的获取接口。

#### Scenario: 获取用户基本信息
- **WHEN** 已登录用户请求个人信息
- **THEN** 系统返回用户基本信息、角色列表、权限标识列表

#### Scenario: 获取用户菜单树
- **WHEN** 已登录用户请求菜单树
- **THEN** 系统返回该用户有权限的菜单树结构（包含路由路径、组件路径、图标、排序）

### Requirement: 密码加密存储
系统 SHALL 使用 BCrypt 算法对用户密码进行加密存储。

#### Scenario: 密码存储
- **WHEN** 用户注册或修改密码
- **THEN** 系统使用 BCryptPasswordEncoder 加密后存储，不存储明文密码

#### Scenario: 密码验证
- **WHEN** 用户登录时验证密码
- **THEN** 系统使用 BCryptPasswordEncoder 匹配输入的密码与存储的加密密码