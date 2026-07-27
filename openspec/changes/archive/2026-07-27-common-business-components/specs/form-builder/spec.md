## ADDED Requirements

### 需求：FormBuilder 应根据字段定义渲染表单控�?FormBuilder 应接�?`fields` prop 定义每个表单字段的类型、标签、字段名、占位符、验证规则和选项。每个字段类型渲染对应的 Element Plus 表单控件�?
#### 场景：渲染输入框
- **给定** FormBuilder �?fields 包含 `{ type: 'input', label: '用户�?, prop: 'username', placeholder: '请输入用户名' }`
- **�?* 组件渲染�?- **那么** 应渲染一�?el-form-item（标�?用户�?）和一�?el-input（占位符"请输入用户名"�?
#### 场景：渲染带选项的下拉选择�?- **给定** FormBuilder �?fields 包含 `{ type: 'select', label: '角色', prop: 'roleIds', options: [{ label: '管理�?, value: 1 }, { label: '普通用�?, value: 2 }] }`
- **�?* 组件渲染�?- **那么** 应渲染一�?el-select 并包含给出的选项

#### 场景：渲染树形选择�?- **给定** FormBuilder �?fields 包含 `{ type: 'tree-select', label: '组织机构', prop: 'orgId', treeProps: { data: [], props: { label: 'label', value: 'id', children: 'children' } } }`
- **�?* 组件渲染�?- **那么** 应渲染一�?el-tree-select 并包含给出的 treeProps

#### 场景：渲染开�?- **给定** FormBuilder �?fields 包含 `{ type: 'switch', label: '状�?, prop: 'status' }`
- **�?* 组件渲染�?- **那么** 应渲染一�?el-switch

#### 场景：渲染日期选择�?- **给定** FormBuilder �?fields 包含 `{ type: 'date-picker', label: '创建时间', prop: 'createdAt' }`
- **�?* 组件渲染�?- **那么** 应渲染一�?el-date-picker

#### 场景：渲染文本域
- **给定** FormBuilder �?fields 包含 `{ type: 'textarea', label: '备注', prop: 'remark', placeholder: '请输入备�? }`
- **�?* 组件渲染�?- **那么** 应渲染一�?el-input type="textarea"

#### 场景：渲染单选按钮组
- **给定** FormBuilder �?fields 包含 `{ type: 'radio', label: '性别', prop: 'gender', options: [{ label: '�?, value: 1 }, { label: '�?, value: 2 }] }`
- **�?* 组件渲染�?- **那么** 应渲染一�?el-radio-group

#### 场景：渲染多选框�?- **给定** FormBuilder �?fields 包含 `{ type: 'checkbox', label: '爱好', prop: 'hobbies', options: [{ label: '阅读', value: 'reading' }, { label: '运动', value: 'sports' }] }`
- **�?* 组件渲染�?- **那么** 应渲染一�?el-checkbox-group

#### 场景：使用自定义插槽渲染字段
- **给定** FormBuilder �?fields 包含 `{ type: 'slot', label: '自定�?, prop: 'custom', slotName: 'customField' }`
- **�?* 父组件提�?`<template #customField="{ value, update }">` 内容�?- **那么** 该字段应渲染插槽内容

---

### 需求：FormBuilder 应支�?v-model 双向绑定
FormBuilder 通过 `modelValue` prop �?`update:modelValue` emit 实现 v-model 双向绑定。字段值变化时自动更新绑定数据�?
#### 场景：输入框值变化触发更�?- **给定** FormBuilder �?`modelValue` �?`{ username: '' }`，字段为 input 类型
- **�?* 用户输入"admin"�?- **那么** 组件�?emit 'update:modelValue'，新值为 `{ username: 'admin' }`

#### 场景：初始值正确显�?- **给定** FormBuilder �?`modelValue` �?`{ username: 'admin', status: 1 }`
- **�?* 组件渲染�?- **那么** 输入框应显示"admin"，开关应处于打开状�?
---

### 需求：FormBuilder 应支持布局配置
FormBuilder 应通过 `layout` prop 支持三种布局模式：single（单列）、double（双列）、grid（栅格，指定列数）�?
#### 场景：单列布局
- **给定** FormBuilder �?`layout="single"`，有 4 个字�?- **�?* 组件渲染�?- **那么** 每个字段应独占一行，单列显示

#### 场景：双列布局
- **给定** FormBuilder �?`layout="double"`，有 4 个字�?- **�?* 组件渲染�?- **那么** 字段应分 2 列显示，�?2 �?
---

### 需求：FormBuilder 应支持表单验�?FormBuilder 应集�?Element Plus 表单验证。带�?`rules` prop 的字段在提交时会被验证�?
#### 场景：必填字段验�?- **给定** FormBuilder 的字段为 `{ type: 'input', label: '用户�?, prop: 'username', rules: [{ required: true, message: '请输入用户名' }] }`
- **�?* 表单在用户名为空时提�?- **那么** 验证应失败，提示消息"请输入用户名"

---

### 需求：FormBuilder 应通过 ref 暴露 validate �?resetFields 方法
FormBuilder 应暴�?`validate()`、`validateField(prop)`、`resetFields()`、`clearValidate()` 方法，父组件通过模板 ref 调用�?
#### 场景：验证所有字�?- **给定** 父组件通过模板 ref `formRef` 引用 FormBuilder
- **�?* 父组件调�?`formRef.validate()` �?- **那么** 所有字段应被验证，返回 Promise<boolean>

#### 场景：重置所有字�?- **给定** FormBuilder 的表单数据已填写
- **�?* 父组件调�?`formRef.resetFields()` �?- **那么** 所有字段应重置为初始�?
---

### 需求：FormBuilder 应支持字�?onChange 回调，在值变更时拦截或接�?每个 FormField 应接受可选的 `onChange` 函数 `(newVal: any, oldVal: any, formData: Record<string, any>) => boolean | Promise<boolean>`。当回调返回 `false` �?Promise 解析�?`false` 时，字段值应回退到旧值。否则正常更新�?
#### 场景：onChange 返回 false，拒绝变�?- **给定** FormBuilder 的字段为 `{ type: 'input', label: '编码', prop: 'code', onChange: (newVal) => newVal.length <= 10 }`
- **�?* 用户输入超过 10 个字符时
- **那么** 字段值不应更新，保持旧�?
#### 场景：onChange 返回 true，接受变�?- **给定** FormBuilder 的字段为 `{ type: 'input', label: '编码', prop: 'code', onChange: (newVal) => newVal.length <= 10 }`
- **�?* 用户输入 5 个字符时
- **那么** 字段值应更新为新�?
#### 场景：onChange 返回 Promise<false>，异步拒�?- **给定** FormBuilder 的字段为 `{ type: 'select', label: '组织', prop: 'orgId', onChange: async (newVal) => { const res = await checkOrg(newVal); return res.valid } }`
- **�?* 用户选择一个组织，异步校验返回 `{ valid: false }` �?- **那么** 字段值不应更�?
#### 场景：onChange 接收 oldVal �?formData 上下�?- **给定** FormBuilder 的字段为 `[{ prop: 'type', ... }, { prop: 'category', onChange: (newVal, oldVal, formData) => formData.type === 'A' }]`
- **�?* 用户�?type �?'A' 时修�?category
- **那么** onChange 应通过 formData 接收�?type 的当前值ender slot field for custom content
- **GIVEN** a FormBuilder with fields containing `{ type: 'slot', label: '自定�?, prop: 'custom', slotName: 'customField' }`
- **WHEN** the parent provides `<template #customField><div>自定义内�?/div></template>`
- **THEN** the slot content SHALL be rendered in place of the form control

---

### Requirement: FormBuilder SHALL support v-model two-way binding
FormBuilder SHALL accept `modelValue` prop and emit `update:modelValue` event for v-model binding.

#### Scenario: v-model binds form data
- **GIVEN** a parent component with `const form = reactive({ username: 'admin' })` and `<FormBuilder v-model="form" :fields="fields" />`
- **WHEN** the component renders
- **THEN** the username input SHALL display "admin"

#### Scenario: Input change updates model
- **GIVEN** a FormBuilder with v-model
- **WHEN** the user types "newuser" in the username input
- **THEN** the model's username property SHALL be updated to "newuser"

---

### Requirement: FormBuilder SHALL support layout configuration
FormBuilder SHALL accept a `layout` prop that controls field layout: `'single'` (one column), `'double'` (two columns), or a responsive grid definition.

#### Scenario: Single column layout
- **GIVEN** a FormBuilder with `layout="single"` and 4 fields
- **WHEN** the component renders
- **THEN** each field SHALL be rendered in its own row, one column

#### Scenario: Double column layout
- **GIVEN** a FormBuilder with `layout="double"` and 4 fields
- **WHEN** the component renders
- **THEN** fields SHALL be rendered in 2 columns, 2 rows

---

### Requirement: FormBuilder SHALL support form validation
FormBuilder SHALL integrate with Element Plus form validation. Fields with `rules` prop SHALL be validated on submit trigger.

#### Scenario: Required field validation
- **GIVEN** a FormBuilder with a field `{ type: 'input', label: '用户�?, prop: 'username', rules: [{ required: true, message: '请输入用户名' }] }`
- **WHEN** the form is submitted with empty username
- **THEN** validation SHALL fail with message "请输入用户名"

---

### Requirement: FormBuilder SHALL expose validate and reset methods via ref
FormBuilder SHALL expose `validate()` and `resetFields()` methods accessible through template ref.

#### Scenario: Validate all fields
- **GIVEN** a FormBuilder with a template ref `formRef`
- **WHEN** the parent calls `formRef.validate()`
- **THEN** all fields SHALL be validated and a Promise<boolean> SHALL be returned

#### Scenario: Reset all fields
- **GIVEN** a FormBuilder with filled form data
- **WHEN** the parent calls `formRef.resetFields()`
- **THEN** all fields SHALL be reset to their initial values

---

### Requirement: FormBuilder SHALL support onChange callback per field to intercept or accept value changes
Each `FormField` SHALL accept an optional `onChange` function `(newVal: any, oldVal: any, formData: Record<string, any>) => boolean | Promise<boolean>`. When the callback returns `false` or a Promise resolving to `false`, the field value SHALL revert to the old value. Otherwise the value SHALL update normally.

#### Scenario: onChange returns false, value is rejected
- **GIVEN** a FormBuilder with a field `{ type: 'input', label: '编码', prop: 'code', onChange: (newVal) => newVal.length <= 10 }`
- **WHEN** the user types a value longer than 10 characters
- **THEN** the field value SHALL NOT be updated and the old value SHALL be preserved

#### Scenario: onChange returns true, value is accepted
- **GIVEN** a FormBuilder with a field `{ type: 'input', label: '编码', prop: 'code', onChange: (newVal) => newVal.length <= 10 }`
- **WHEN** the user types a value of 5 characters
- **THEN** the field value SHALL be updated to the new value

#### Scenario: onChange returns Promise<false>, value is rejected asynchronously
- **GIVEN** a FormBuilder with a field `{ type: 'select', label: '组织', prop: 'orgId', onChange: async (newVal) => { const res = await checkOrg(newVal); return res.valid } }`
- **WHEN** the user selects an org and the async check returns `{ valid: false }`
- **THEN** the field value SHALL NOT be updated

#### Scenario: onChange receives oldVal and formData context
- **GIVEN** a FormBuilder with fields `[{ prop: 'type', ... }, { prop: 'category', onChange: (newVal, oldVal, formData) => formData.type === 'A' }]`
- **WHEN** the user changes category while type is 'A'
- **THEN** onChange SHALL receive the current value of type via formData