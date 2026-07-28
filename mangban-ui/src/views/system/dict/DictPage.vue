<script setup lang="ts">
import { ref } from 'vue'
import { SearchTable } from '@/components/business'
import type { SearchField, TableColumn, ActionButton, FormConfig } from '@/components/business/types'
import { getDictTypeList, createDictType, updateDictType, deleteDictType, getDictDataList, createDictData, updateDictData, deleteDictData } from '@/api/dict'
import type { DictTypeVO, DictDataVO } from '@/types/dict'

const typeTableRef = ref()
const dataTableRef = ref()
const selectedType = ref<DictTypeVO | null>(null)

// ========== 字典类型 ==========
const typeSearchFields: SearchField[] = [
  { type: 'input', label: '字典名称', prop: 'dictName', placeholder: '输入名称' },
  { type: 'input', label: '字典编码', prop: 'dictCode', placeholder: '输入编码' },
]

const typeColumns: TableColumn[] = [
  { prop: 'dictName', label: '字典名称', minWidth: 150 },
  { prop: 'dictCode', label: '字典编码', width: 150 },
  { prop: 'remark', label: '备注', minWidth: 200 },
  { prop: 'createTime', label: '创建时间', width: 170 },
]

async function typeFetchApi(params: any) {
  const res = await getDictTypeList(params)
  return { rows: res.data.rows, total: res.data.total }
}

const typeFormConfig: FormConfig<DictTypeVO> = {
  fields: [
    { type: 'input', label: '字典名称', prop: 'dictName', rules: [{ required: true, message: '请输入字典名称', trigger: 'blur' }] },
    { type: 'input', label: '字典编码', prop: 'dictCode', rules: [{ required: true, message: '请输入字典编码', trigger: 'blur' }] },
    { type: 'input', label: '备注', prop: 'remark' },
  ],
  createApi: createDictType,
  updateApi: (id, data) => updateDictType(id as number, data),
  deleteApi: deleteDictType,
  getApi: async (id) => {
    const res = await getDictTypeList({ page: 1, size: 999 })
    return res.data.rows.find((r: DictTypeVO) => r.id === id) || null
  },
  dialogTitle: { create: '新增字典类型', edit: '编辑字典类型' },
}

const typeActionButtons: ActionButton[] = [
  {
    label: '启用/停用', size: 'small', type: 'text',
    onClick: async (row: DictTypeVO) => {
      await updateDictType(row.id, { status: row.status === 1 ? 0 : 1 } as any)
      typeTableRef.value?.fetchList()
    },
  },
]

function handleTypeRowClick(row: DictTypeVO) {
  selectedType.value = row
  dataTableRef.value?.fetchList()
}

// ========== 字典数据 ==========
const dataSearchFields: SearchField[] = [
  { type: 'input', label: '标签', prop: 'label', placeholder: '输入标签' },
  { type: 'input', label: '值', prop: 'value', placeholder: '输入值' },
]

const dataColumns: TableColumn[] = [
  { prop: 'label', label: '标签', minWidth: 150 },
  { prop: 'value', label: '值', width: 150 },
  { prop: 'sortOrder', label: '排序', width: 80 },
  { prop: 'createTime', label: '创建时间', width: 170 },
]

const dataFormConfig: FormConfig<DictDataVO> = {
  fields: [
    { type: 'input', label: '标签', prop: 'label', rules: [{ required: true, message: '请输入标签', trigger: 'blur' }] },
    { type: 'input', label: '值', prop: 'value', rules: [{ required: true, message: '请输入值', trigger: 'blur' }] },
    { type: 'input-number', label: '排序', prop: 'sortOrder' },
  ],
  createApi: async (data: any) => createDictData({ ...data, dictCode: selectedType.value?.dictCode }),
  updateApi: (id, data) => updateDictData(id as number, data),
  deleteApi: deleteDictData,
  getApi: async (id) => {
    if (!selectedType.value) return null
    const res = await getDictDataList(selectedType.value.dictCode)
    const list = res.data as any[]
    return list.find((d: any) => d.id === id) || null
  },
  dialogTitle: { create: '新增字典项', edit: '编辑字典项' },
}

const dataActionButtons: ActionButton[] = [
  {
    label: '启用/停用', size: 'small', type: 'text',
    onClick: async (row: DictDataVO) => {
      await updateDictData(row.id, { status: row.status === 1 ? 0 : 1 } as any)
      dataTableRef.value?.fetchList()
    },
  },
]
</script>

<template>
  <div style="display: flex; gap: 12px; height: calc(100vh - 140px)">
    <!-- 左侧：字典类型 -->
    <el-card style="width: 400px; flex-shrink: 0">
      <template #header><span style="font-weight: bold; font-size: 14px">字典类型</span></template>
      <SearchTable
        ref="typeTableRef"
        :search-fields="typeSearchFields"
        :columns="typeColumns"
        :action-buttons="typeActionButtons"
        :fetch-api="typeFetchApi"
        :form-config="typeFormConfig"
        @row-click="handleTypeRowClick"
      />
    </el-card>

    <!-- 右侧：字典数据 -->
    <el-card style="flex: 1" v-if="selectedType">
      <template #header>
        <span style="font-weight: bold; font-size: 14px">字典数据 - {{ selectedType.dictName }}</span>
      </template>
      <SearchTable
        ref="dataTableRef"
        :search-fields="dataSearchFields"
        :columns="dataColumns"
        :action-buttons="dataActionButtons"
        :fetch-api="async (p: any) => {
          if (!selectedType) return { rows: [], total: 0 }
          const res = await getDictDataList(selectedType.dictCode)
          let list = (res.data as any[]) || []
          if (p.label) list = list.filter((d: any) => d.label?.includes(p.label))
          if (p.value) list = list.filter((d: any) => d.value?.includes(p.value))
          const total = list.length
          const start = ((p.page || 1) - 1) * (p.size || 10)
          return { rows: list.slice(start, start + (p.size || 10)), total }
        }"
        :form-config="dataFormConfig"
      />
    </el-card>

    <div v-else style="flex: 1; display: flex; align-items: center; justify-content: center; color: #999">
      点击左侧字典类型查看字典数据
    </div>
  </div>
</template>