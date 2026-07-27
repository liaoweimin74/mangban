## ADDED Requirements

### Requirement: ReferencePicker SHALL render as an input field that opens a selection dialog on click
ReferencePicker SHALL render an el-input with a placeholder and a click handler that opens a dialog overlay for data selection.

#### Scenario: Click opens selection dialog
- **GIVEN** a ReferencePicker component
- **WHEN** the user clicks on the input field
- **THEN** a dialog SHALL open containing a search bar and a data table

#### Scenario: Display selected value in input
- **GIVEN** a ReferencePicker with modelValue set to `{ id: 1, nickname: '管理员' }` and displayField="nickname"
- **WHEN** the component renders
- **THEN** the input SHALL display "管理员"

---

### Requirement: ReferencePicker SHALL support search query in the selection dialog
ReferencePicker SHALL render a search input at the top of the selection dialog. When the user types and submits, the component SHALL call fetchApi with the search keyword.

#### Scenario: Search triggers data fetch
- **GIVEN** a ReferencePicker with a fetchApi prop
- **WHEN** the user types "admin" in the dialog search input and clicks search
- **THEN** fetchApi SHALL be called with `{ keyword: 'admin', page: 1, size: 10 }`

---

### Requirement: ReferencePicker SHALL display selectable data in a table within the dialog
ReferencePicker SHALL render an el-table inside the selection dialog, displaying data returned by fetchApi. The table columns SHALL be configurable via a `columns` prop.

#### Scenario: Table displays fetched data
- **GIVEN** a ReferencePicker with fetchApi returning `[{ id: 1, nickname: '管理员' }, { id: 2, nickname: '普通用户' }]`
- **WHEN** the dialog opens and data loads
- **THEN** the table SHALL display the fetched rows

#### Scenario: Pagination inside dialog
- **GIVEN** a ReferencePicker with fetchApi returning total=25
- **WHEN** the dialog renders
- **THEN** pagination SHALL be rendered at the bottom of the dialog

---

### Requirement: ReferencePicker SHALL support single and multiple selection modes
ReferencePicker SHALL accept a `mode` prop with values `'single'` or `'multiple'`. In single mode, clicking a row selects it and closes the dialog. In multiple mode, rows have checkboxes and a confirm button closes the dialog.

#### Scenario: Single selection closes dialog
- **GIVEN** a ReferencePicker with `mode="single"`
- **WHEN** the user clicks a row in the dialog table
- **THEN** the dialog SHALL close and the selected value SHALL be emitted via v-model

#### Scenario: Multiple selection with confirm
- **GIVEN** a ReferencePicker with `mode="multiple"`
- **WHEN** the user checks multiple rows and clicks confirm
- **THEN** the dialog SHALL close and the array of selected values SHALL be emitted via v-model

#### Scenario: Cancel button closes dialog without selection
- **GIVEN** an open ReferencePicker dialog
- **WHEN** the user clicks cancel
- **THEN** the dialog SHALL close without changing the current value

---

### Requirement: ReferencePicker SHALL support valueField and displayField configuration
ReferencePicker SHALL accept `valueField` (the field whose value is bound to v-model) and `displayField` (the field shown in the input).

#### Scenario: Custom value and display fields
- **GIVEN** a ReferencePicker with `valueField="id"` and `displayField="nickname"` and modelValue=1
- **WHEN** the component renders and data loads
- **THEN** the input SHALL display the nickname corresponding to id=1

---

### Requirement: ReferencePicker SHALL auto-load data on dialog open with default params
When the selection dialog opens, ReferencePicker SHALL automatically call fetchApi with default pagination params.

#### Scenario: Auto-fetch on dialog open
- **GIVEN** a ReferencePicker with fetchApi
- **WHEN** the dialog opens
- **THEN** fetchApi SHALL be called with `{ page: 1, size: 10 }`