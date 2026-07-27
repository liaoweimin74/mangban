## ADDED Requirements

### Requirement: SearchTable SHALL render a filter/search bar at the top
SearchTable SHALL render a filter/search bar at the top based on the `searchFields` prop definition. Each field type SHALL render the corresponding Element Plus form control.

#### Scenario: Render text input field
- **GIVEN** a SearchTable with `searchFields` containing `{ type: 'input', label: '用户名', prop: 'username' }`
- **WHEN** the component mounts
- **THEN** the search bar SHALL display an el-form-item with label "用户名" and an el-input bound to query.username

#### Scenario: Render select field
- **GIVEN** a SearchTable with `searchFields` containing `{ type: 'select', label: '状态', prop: 'status', options: [{ label: '启用', value: 1 }, { label: '停用', value: 0 }] }`
- **WHEN** the component mounts
- **THEN** the search bar SHALL display an el-select with the given options

#### Scenario: Render tree-select field
- **GIVEN** a SearchTable with `searchFields` containing `{ type: 'tree-select', label: '组织机构', prop: 'orgId', treeProps: { data: [], props: { label: 'label', value: 'id' } } }`
- **WHEN** the component mounts
- **THEN** the search bar SHALL display an el-tree-select with the given treeProps

---

### Requirement: SearchTable SHALL provide search, reset, and export buttons on the right side of the filter bar
SearchTable SHALL render search (el-button type=primary with icon Search), reset (el-button with icon Refresh), and export (el-button with icon Download) buttons aligned to the right side of the filter bar.

#### Scenario: Search button triggers query
- **GIVEN** a SearchTable with a fetchApi prop
- **WHEN** the user clicks the search button
- **THEN** SearchTable SHALL set page to 1 and call fetchApi with the current query params

#### Scenario: Reset button clears filters and re-queries
- **GIVEN** a SearchTable with filled search fields
- **WHEN** the user clicks the reset button
- **THEN** SearchTable SHALL clear all query field values, set page to 1, and call fetchApi

#### Scenario: Export button emits export event
- **GIVEN** a SearchTable
- **WHEN** the user clicks the export button
- **THEN** SearchTable SHALL emit an 'export' event with the current query params

---

### Requirement: SearchTable SHALL render a data table in the middle area
SearchTable SHALL render an el-table with columns defined by the `columns` prop. Each column SHALL support prop binding, custom label, width, formatter function, and custom template via slot.

#### Scenario: Render simple text columns
- **GIVEN** a SearchTable with `columns` containing `[{ prop: 'username', label: '用户名', width: 120 }, { prop: 'email', label: '邮箱', minWidth: 160 }]`
- **WHEN** the table renders with data
- **THEN** each row SHALL display the corresponding prop value in the column

#### Scenario: Render action column with buttons
- **GIVEN** a SearchTable with `actionButtons` containing `[{ label: '编辑', type: 'primary', permission: 'system:user:edit', onClick: (row) => handleEdit(row) }]`
- **WHEN** the table renders
- **THEN** the last column SHALL contain action buttons per row

#### Scenario: Custom column template via slot
- **GIVEN** a SearchTable with a column that has `slotName: 'status'`
- **WHEN** the parent provides `<template #status="{ row }">` content
- **THEN** the column SHALL render the slot content instead of the raw prop value

---

### Requirement: SearchTable SHALL render pagination at the bottom
SearchTable SHALL render el-pagination at the bottom, showing total count, page size selector (10/20/50), prev/next buttons, and page number buttons.

#### Scenario: Pagination triggers re-fetch on change
- **GIVEN** a SearchTable with fetchApi returning total=100
- **WHEN** the user clicks page 2
- **THEN** SearchTable SHALL set page to 2 and call fetchApi with the updated page param

#### Scenario: Page size change triggers re-fetch
- **GIVEN** a SearchTable with default pageSize=10
- **WHEN** the user selects 20 from page size dropdown
- **THEN** SearchTable SHALL set size to 20, set page to 1, and call fetchApi

---

### Requirement: SearchTable SHALL support loading state
SearchTable SHALL show el-table v-loading during fetchApi execution.

#### Scenario: Loading indicator shown during fetch
- **GIVEN** a SearchTable with fetchApi that takes 500ms to resolve
- **WHEN** the component is fetching data
- **THEN** the table SHALL display a loading overlay

---

### Requirement: SearchTable SHALL support toolbar slot above the table
SearchTable SHALL provide a default slot between the filter bar and the table for toolbar buttons (e.g. "新增用户").

#### Scenario: Toolbar slot renders custom content
- **GIVEN** a SearchTable with `<template #default><el-button>新增用户</el-button></template>`
- **WHEN** the component renders
- **THEN** the toolbar area SHALL contain the "新增用户" button

---

### Requirement: SearchTable SHALL accept fetchApi as a required function prop
SearchTable SHALL require a `fetchApi` prop that is a function `(params: QueryParams) => Promise<{ rows: T[]; total: number }>`. The component SHALL call it automatically on mount and on search/reset/pagination change.

#### Scenario: Auto-fetch on mount
- **GIVEN** a SearchTable with a valid fetchApi
- **WHEN** the component mounts
- **THEN** fetchApi SHALL be called with default params (page=1, size=10)

#### Scenario: fetchApi receives correct params
- **GIVEN** a SearchTable with search fields and fetchApi
- **WHEN** the user fills username and clicks search
- **THEN** fetchApi SHALL be called with `{ page: 1, size: 10, username: 'admin' }`