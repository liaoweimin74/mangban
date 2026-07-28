<template>
  <div class="search-table">
    <!-- 搜索栏 -->
    <el-card v-if="showSearch" style="margin-bottom: 16px">
      <el-form :inline="true" :model="query" @submit.prevent>
        <el-form-item v-for="field in searchFields" :key="field.prop" :label="field.label">
          <el-input
            v-if="field.type === 'input'"
            v-model="query[field.prop]"
            :placeholder="field.placeholder"
            clearable
          />
          <el-select
            v-else-if="field.type === 'select'"
            v-model="query[field.prop]"
            :placeholder="field.placeholder"
            clearable
            :style="field.style || 'width: 180px'"
          >
            <el-option
              v-for="opt in field.options"
              :key="String(opt.value)"
              :label="opt.label"
              :value="opt.value"
            />
          </el-select>
          <el-tree-select
            v-else-if="field.type === 'tree-select'"
            v-model="query[field.prop]"
            v-bind="field.treeProps"
            :placeholder="field.placeholder"
            clearable
            :style="field.style || 'width: 200px'"
            check-strictly
          />
          <el-date-picker
            v-else-if="field.type === 'date-picker'"
            v-model="query[field.prop]"
            :placeholder="field.placeholder"
          />
          <el-date-picker
            v-else-if="field.type === 'date-range'"
            v-model="query[field.prop]"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
          />
        </el-form-item>
        <el-form-item>
          <div style="display: flex; gap: 8px; margin-left: auto">
            <el-button type="primary" :icon="Search" circle @click="handleSearch" />
            <el-button :icon="Refresh" circle @click="handleReset" />
            <el-button v-if="showExport" :icon="Download" :loading="exportLoading" circle @click="handleExport" />
          </div>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 表格卡片 -->
    <el-card>
      <div style="margin-bottom: 12px; display: flex; align-items: center; gap: 8px">
        <slot />
        <el-button
          v-if="formConfig"
          type="primary"
          :icon="Plus"
          v-permission="formConfig.createPermission"
          @click="handleCreate"
        >
          新增
        </el-button>
      </div>

      <el-table :data="list" v-loading="loading" border :size="tableSize" @row-click="(row: any, col: any, evt: Event) => emit('row-click', row, col, evt)">
        <el-table-column
          v-for="col in columns"
          :key="col.prop || col.label"
          :prop="col.prop"
          :label="col.label"
          :width="col.width"
          :min-width="col.minWidth"
          :align="col.align"
          :fixed="col.fixed"
          :formatter="col.formatter"
        >
          <template #default="{ row, column, $index }" v-if="col.slotName && $slots[col.slotName]">
            <slot :name="col.slotName" :row="row" :column="column" :$index="$index" />
          </template>
        </el-table-column>

        <!-- 操作列 -->
        <el-table-column
          v-if="resolvedActionButtons.length"
          label="操作"
          :width="actionColumnWidth"
          fixed="right"
        >
          <template #default="{ row }">
            <div style="display: inline-flex; align-items: center; gap: 4px; white-space: nowrap">
              <template v-for="btn in visibleButtons" :key="btn.label">
                <el-popconfirm v-if="btn.confirm" :title="btn.confirm" @confirm="btn.onClick(row)">
                  <template #reference>
                    <el-button text
                      size="small"                      
                      :type="btn.type"
                      :class="btn.class"
                      v-permission="btn.permission"
                    >
                      {{ btn.label }}
                    </el-button>
                  </template>
                </el-popconfirm>
                <el-button
                  v-else
                  size="small"
                  :type="btn.type || 'text'"
                  v-permission="btn.permission"
                  @click="btn.onClick(row)"
                >
                  {{ btn.label }}
                </el-button>
              </template>

              <el-dropdown v-if="dropdownButtons.length" trigger="click">
                <el-button size="small" text>
                  <div style="display: flex; align-items: center; gap: 2px">
                    <span>更多</span>
                    <el-icon><ArrowDown /></el-icon>
                  </div>
                </el-button>
                <template #dropdown>
                  <el-dropdown-menu>
                    <template v-for="btn in dropdownButtons" :key="btn.label">
                      <el-dropdown-item v-if="!btn.confirm" @click="btn.onClick(row)">
                        {{ btn.label }}
                      </el-dropdown-item>
                      <el-dropdown-item v-else>
                        <el-popconfirm :title="btn.confirm" @confirm="btn.onClick(row)">
                          <template #reference>
                            <span>{{ btn.label }}</span>
                          </template>
                        </el-popconfirm>
                      </el-dropdown-item>
                    </template>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <div v-if="total > 0" style="margin-top: 16px; display: flex; justify-content: flex-end">
        <el-pagination
          v-model:current-page="query.page"
          v-model:page-size="query.size"
          :total="total"
          :page-sizes="pageSizes"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="fetchList()"
          @current-change="fetchList()"
        />
      </div>
    </el-card>

    <!-- 表单弹窗 -->
    <el-dialog
      v-if="formConfig"
      v-model="dialogVisible"
      :title="dialogTitle"
      :width="formConfig.dialogWidth || '500px'"
      :close-on-click-modal="false"
      @close="handleDialogClose"
    >
      <FormBuilder
        ref="formRef"
        v-model="formData"
        :fields="formConfig.fields"
        :layout="formConfig.layout"
        :label-width="formConfig.labelWidth || '80px'"
      />
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="formLoading" @click="handleDialogSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { Search, Refresh, Download, Plus, ArrowDown } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import type { SearchTableProps, ActionButton, QueryParams } from './types'
import FormBuilder from './FormBuilder.vue'

const props = withDefaults(defineProps<SearchTableProps>(), {
  defaultPageSize: 10,
  pageSizes: () => [10, 20, 50],
  showExport: false,
  exportLoading: false,
  maxVisibleButtons: 3,
  showSearch: true,
  tableSize: 'default',
})

const emit = defineEmits<{
  search: [params: QueryParams]
  reset: []
  export: [params: QueryParams]
  'row-click': [row: any, column: any, event: Event]
}>()

const loading = ref(false)
const list = ref<any[]>([])
const total = ref(0)

const query = reactive<QueryParams>({ page: 1, size: props.defaultPageSize })
const initialQuery = ref<Record<string, any>>({})

// 表单弹窗状态
const dialogVisible = ref(false)
const dialogTitle = ref('')
const isEdit = ref(false)
const editId = ref<number | string>(0)
const formData = ref<Record<string, any>>({})
const formLoading = ref(false)
const formRef = ref()

// 操作列宽度
const actionColumnWidth = computed(() => {
  const total = resolvedActionButtons.value.length
  const visible = Math.min(total, props.maxVisibleButtons)
  const hasMore = total > props.maxVisibleButtons
  // 每按钮约 70px + 更多约 80px
  return (visible * 70 + (hasMore ? 80 : 0) + 20) + 'px'
})

const resolvedActionButtons = computed<ActionButton[]>(() => {
  const defaults = props.formConfig ? getDefaultActions() : []
  if (props.actionButtons !== undefined) {
    // 合并：自定义按钮追加到默认按钮后面
    return [...defaults, ...props.actionButtons]
  }
  return defaults
})

function getDefaultActions(): ActionButton[] {
  const btns: ActionButton[] = []
  if (props.formConfig?.updateApi) {
    btns.push({
      label: '编辑',
      type: 'text',
      size: 'small',
      permission: props.formConfig.editPermission,
      onClick: (row) => handleEdit(row),
    })
  }
  if (props.formConfig?.deleteApi) {
    btns.push({
      label: '删除',
      type: 'text',
      size: 'small',
      confirm: '确定删除该记录吗？',
      permission: props.formConfig.deletePermission,
      onClick: (row) => handleDelete(row),
    })
  }
  return btns
}

const visibleButtons = computed(() =>
  resolvedActionButtons.value.slice(0, props.maxVisibleButtons),
)
const dropdownButtons = computed(() =>
  resolvedActionButtons.value.slice(props.maxVisibleButtons),
)

function initSearchDefaults() {
  const defaults: Record<string, any> = {}
  for (const field of props.searchFields) {
    if (field.defaultValue !== undefined) {
      defaults[field.prop] = field.defaultValue
    }
  }
  initialQuery.value = { page: 1, size: props.defaultPageSize, ...defaults }
  Object.assign(query, initialQuery.value)
}

onMounted(() => {
  initSearchDefaults()
  fetchList()
})

async function fetchList() {
  loading.value = true
  try {
    const res = await props.fetchApi({ ...query })
    list.value = res.rows
    total.value = res.total
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  query.page = 1
  fetchList()
}

function handleReset() {
  Object.keys(query).forEach((key) => delete query[key])
  Object.assign(query, initialQuery.value)
  fetchList()
}

function handleExport() {
  emit('export', { ...query })
}

// --- CRUD ---
function handleCreate() {
  isEdit.value = false
  editId.value = 0
  formData.value = {}
  dialogTitle.value = props.formConfig?.dialogTitle?.create || '新增'
  dialogVisible.value = true
}

async function handleEdit(row: any) {
  if (props.formConfig?.beforeEdit) {
    const ok = await props.formConfig.beforeEdit(row)
    if (ok === false) return
  }
  isEdit.value = true
  editId.value = row.id
  dialogTitle.value = props.formConfig?.dialogTitle?.edit || '编辑'

  if (props.formConfig?.getApi) {
    formLoading.value = true
    try {
      const res = await props.formConfig.getApi(row.id)
      formData.value = { ...res }
    } finally {
      formLoading.value = false
    }
  } else {
    formData.value = { ...row }
  }
  dialogVisible.value = true
}

async function handleDelete(row: any) {
  if (props.formConfig?.beforeDelete) {
    const ok = await props.formConfig.beforeDelete(row)
    if (ok === false) return
  }
  try {
    await props.formConfig?.deleteApi?.(row.id)
    ElMessage.success('删除成功')
    props.formConfig?.afterDelete?.()
    fetchList()
  } catch {
    // http 拦截器统一处理
  }
}

async function handleDialogSubmit() {
  const valid = await formRef.value?.validate()
  if (!valid) return

  formLoading.value = true
  try {
    if (isEdit.value) {
      await props.formConfig?.updateApi?.(editId.value, formData.value)
      props.formConfig?.afterUpdate?.(formData.value)
    } else {
      await props.formConfig?.createApi?.(formData.value)
      props.formConfig?.afterCreate?.(formData.value)
    }
    ElMessage.success(isEdit.value ? '更新成功' : '创建成功')
    dialogVisible.value = false
    fetchList()
  } finally {
    formLoading.value = false
  }
}

function handleDialogClose() {
  formData.value = {}
}

defineExpose({ fetchList })
</script>