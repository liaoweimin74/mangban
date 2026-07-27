# reference-picker Specification

## Purpose
TBD - created by archiving change common-business-components. Update Purpose after archive.
## Requirements
### Requirement:ReferencePicker 应渲染输入框，点击后弹出数据选择弹窗
ReferencePicker 应渲染一�?el-input（只读），显示选中项的 displayField 值。点击输入框时打开弹窗，加载数据表格�?
#### 场景：点击输入框打开弹窗并加载数�?- **给定** ReferencePicker 有有效的 fetchApi
- **�?* 用户点击输入框时
- **那么** 应打开弹窗，fetchApi 被调用（page=1, size=10），表格显示返回的数�?
#### 场景：未选择时显示占位符
- **给定** ReferencePicker �?`placeholder="请选择用户"`
- **�?* 组件渲染且未选择值时
- **那么** 输入框应显示占位�?请选择用户"

#### 场景：已选择时显�?displayField �?- **给定** ReferencePicker �?`modelValue=1`、`valueField='id'`、`displayField='nickname'`
- **�?* 组件渲染�?- **那么** 输入框应显示选中�?nickname �?
---

### Requirement:ReferencePicker 应在弹窗内提供搜索输入框和查询表�?弹窗顶部应包含一个搜索输入框和搜索按钮，中间为数据表格，底部为分页组件�?
#### 场景：搜索关键词过滤数据
- **给定** ReferencePicker 弹窗已打开
- **�?* 用户输入搜索关键词并点击搜索按钮�?- **那么** fetchApi 应以 page=1 �?keyword 参数被调�?
#### 场景：弹窗内分页正常工作
- **给定** ReferencePicker 弹窗已打开，数据超过一�?- **�?* 用户切换页码�?- **那么** fetchApi 应以更新�?page 参数被调�?
#### 场景：弹窗内表格列根�?columns 配置渲染
- **给定** ReferencePicker �?columns �?`[{ prop: 'username', label: '用户�? }, { prop: 'email', label: '邮箱' }]`
- **�?* 弹窗打开�?- **那么** 表格应显�?用户�?�?邮箱"两列

---

### Requirement:ReferencePicker 应支持单选和多选模�?ReferencePicker 应接�?`mode` prop，值为 `'single'` �?`'multiple'`。单选模式点击行选中并关闭弹窗，多选模式行前有复选框，需确认按钮关闭弹窗�?
#### 场景：单选点击行选中并关闭弹�?- **给定** ReferencePicker �?`mode="single"`，弹窗已打开
- **�?* 用户点击某行�?- **那么** modelValue 应更新为该行�?valueField 值，弹窗关闭，输入框显示该行�?displayField �?
#### 场景：多选模式下勾选并确认
- **给定** ReferencePicker �?`mode="multiple"`，弹窗已打开
- **�?* 用户勾选多行并点击"确定"�?- **那么** modelValue 应更新为选中行的 valueField 值数组，弹窗关闭，输入框显示选中项的 displayField 值（�? / "分隔�?
#### 场景：取消按钮关闭弹窗不改变�?- **给定** ReferencePicker 弹窗已打开
- **�?* 用户点击"取消"�?- **那么** 弹窗应关闭，当前值不�?
---

### Requirement:ReferencePicker 应支�?valueField �?displayField 配置
ReferencePicker 应接�?`valueField`（绑定到 v-model 的字段）�?`displayField`（输入框显示的字段）�?
#### 场景：自定义值和显示字段
- **给定** ReferencePicker �?`valueField="id"`、`displayField="nickname"`、`modelValue=1`
- **�?* 组件渲染且数据加载后
- **那么** 输入框应显示 id=1 对应�?nickname �?
---

### Requirement:ReferencePicker 应在弹窗打开时自动加载数�?弹窗打开时，ReferencePicker 应自动调�?fetchApi，默认参数为 page=1, size=10�?
#### 场景：弹窗打开时自动获取数�?- **给定** ReferencePicker �?fetchApi
- **�?* 弹窗打开�?- **那么** fetchApi 应被调用，参数为 `{ page: 1, size: 10 }`L display the fetched rows

#### Scenario: Pagination inside dialog
- **GIVEN** a ReferencePicker with fetchApi returning total=25
- **WHEN** the dialog renders
- **THEN** pagination SHALL be rendered at the bottom of the dialog

---

### Requirement:ReferencePicker 应支持清除选中�?ReferencePicker �?`clearable` prop �?true 时，输入框右侧应显示清除图标，点击后清空选中值�?
#### 场景：清除选中�?- **给定** ReferencePicker �?`clearable=true`，已选择�?- **�?* 用户点击输入框的清除图标�?- **那么** modelValue 应更新为 null（single）或 []（multiple），输入框清�

