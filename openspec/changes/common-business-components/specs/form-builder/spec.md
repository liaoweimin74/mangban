## ADDED Requirements

### Requirement: FormBuilder SHALL render form fields based on field definitions
FormBuilder SHALL accept a `fields` prop defining each form field's type, label, prop name, placeholder, validation rules, and options. Each field type SHALL render the corresponding Element Plus form control.

#### Scenario: Render input field
- **GIVEN** a FormBuilder with fields containing `{ type: 'input', label: '用户名', prop: 'username', placeholder: '请输入用户名' }`
- **WHEN** the component renders
- **THEN** an el-form-item with label "用户名" and el-input with placeholder "请输入用户名" SHALL be rendered

#### Scenario: Render select field with options
- **GIVEN** a FormBuilder with fields containing `{ type: 'select', label: '角色', prop: 'roleIds', options: [{ label: '管理员', value: 1 }, { label: '普通用户', value: 2 }] }`
- **WHEN** the component renders
- **THEN** an el-select with the given options SHALL be rendered

#### Scenario: Render tree-select field
- **GIVEN** a FormBuilder with fields containing `{ type: 'tree-select', label: '组织机构', prop: 'orgId', treeProps: { data: [], props: { label: 'label', value: 'id', children: 'children' } } }`
- **WHEN** the component renders
- **THEN** an el-tree-select with the given treeProps SHALL be rendered

#### Scenario: Render switch field
- **GIVEN** a FormBuilder with fields containing `{ type: 'switch', label: '状态', prop: 'status' }`
- **WHEN** the component renders
- **THEN** an el-switch SHALL be rendered

#### Scenario: Render date-picker field
- **GIVEN** a FormBuilder with fields containing `{ type: 'date-picker', label: '创建时间', prop: 'createdAt' }`
- **WHEN** the component renders
- **THEN** an el-date-picker SHALL be rendered

#### Scenario: Render slot field for custom content
- **GIVEN** a FormBuilder with fields containing `{ type: 'slot', label: '自定义', prop: 'custom', slotName: 'customField' }`
- **WHEN** the parent provides `<template #customField><div>自定义内容</div></template>`
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
- **GIVEN** a FormBuilder with a field `{ type: 'input', label: '用户名', prop: 'username', rules: [{ required: true, message: '请输入用户名' }] }`
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