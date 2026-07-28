# Design: SearchTable 搜索栏/按钮/字体/事件增强

## 1. showSearch — 搜索栏显隐控制

```
SearchTableProps 新增:
  showSearch?: boolean  // 默认 true

模板变更:
  <el-card v-if="showSearch" style="margin-bottom: 16px">
    <!-- 搜索栏内容 -->
  </el-card>
```

**受影响页面**:
- MenuPage: `:show-search="false"`（树形表格无需搜索）
- OrgPage: `:show-search="false"`（同上）
- 其余页面不传，默认 true，行为不变

---

## 2. 搜索按钮纯图标化

```
当前:  <el-button type="primary" :icon="Search">搜索</el-button>
改为:  <el-button type="primary" :icon="Search" circle />

当前:  <el-button :icon="Refresh">重置</el-button>
改为:  <el-button :icon="Refresh" circle />

当前:  <el-button v-if="showExport" :icon="Download" :loading="exportLoading">导出</el-button>
改为:  <el-button v-if="showExport" :icon="Download" :loading="exportLoading" circle />
```

使用 `circle` 属性让按钮变成纯圆形图标按钮。Element Plus 的 circle 按钮会去掉 padding，只显示图标。

**权衡**：circle 按钮无 tooltip，用户可能不知道图标含义。但搜索/重置是标准图标（放大镜/刷新），行业惯例可接受。如果需要，后续可加 `el-tooltip` 包裹。

---

## 3. tableSize — 表格尺寸控制

```
SearchTableProps 新增:
  tableSize?: 'small' | 'default' | 'large'  // 默认 'default'

模板变更:
  <el-table :data="list" :size="tableSize" ...>
```

直接透传给 Element Plus 的 `el-table` size prop。三个档位：
- `small`: 紧凑行高 ~32px，字体稍小
- `default`: 标准行高 ~48px
- `large`: 宽松行高 ~56px

---

## 4. 事件转发 — row-click 联动修复

```
defineEmits 新增:
  'row-click': [row: any, column: any, event: Event]

模板变更:
  <el-table @row-click="(row, col, evt) => emit('row-click', row, col, evt)" ...>
```

DictPage 的 `@row-click="handleTypeRowClick"` 在事件转发后即可正常触发，右侧字典数据表格随之显示。

**不转发所有事件**：只转发 `row-click`。el-table 有十几个事件，按需添加，避免 API 膨胀。未来如需其他事件（selection-change、sort-change 等），按同样模式逐个添加。