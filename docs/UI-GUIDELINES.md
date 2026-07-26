# 界面规范 (UI Guidelines)

所有前端页面位于 `mangban-ui/src/`，使用 Vue 3 + Element Plus + Tailwind CSS 构建。

## 整体布局（上下结构）

- **顶部标题栏**：整行，h-14，左侧 Logo(32x32 工业蓝底 MB 图标) + "盲板管理系统" + 分隔线 + 面包屑；右侧用户头像 + 昵称下拉菜单
- **下方区域**：左侧菜单 + 右侧内容区
  - 菜单宽度：展开 224px(w-56)，折叠 64px(w-16)，过渡 300ms
  - 菜单背景：bg-gray-50，右边框 border-r border-gray-200
- **最小/最大宽度**：min-w-[1024px] max-w-[1920px] mx-auto

## 字体

- 全局默认 14px，字体族 `"Microsoft YaHei", "PingFang SC", system-ui`，文字色 `#374151`
- Element Plus locale 设为中文（`zhCn`）
- 菜单字体 14px（全局 CSS 覆盖 `el-menu-item` / `el-sub-menu__title`）
- 菜单项统一高度 40px，行高 40px

## 菜单

- 首页为固定菜单项，使用内联 SVG 图标
- 动态菜单从 `authStore.menus` 获取，过滤掉 path=`/dashboard` 避免重复
- 图标通过 `import * as Icons from '@element-plus/icons-vue'` 动态解析
- 子菜单每级缩进 14px（一个中文字符）
- 折叠态字体保持 14px

## 页签栏

- 高度 h-10，灰底 bg-gray-50
- 当前选中页签：白底 + 顶部橙色标记(border-t-2 border-t-safety-500)
- 未选中：text-gray-500，hover:text-gray-700
- 首页页签不可关闭

## 操作列按钮

- 统一使用 `text` 属性（无边框无背景），size="small"
- 禁止换行：`class="flex items-center gap-1 whitespace-nowrap"`
- 颜色：编辑 primary / 删除 danger / 新增子级 success

## 表格

- 表头：bg-[#f8f9fb] text-[#6b7280] font-medium
- 分页：Element Plus 默认样式（中文 locale）

## 主题色

```css
--industrial-50: #eff6ff ~ --industrial-600: #1a56db
--safety-50: #fffbeb ~ --safety-600: #f59e0b
```
