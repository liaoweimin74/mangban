<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { SearchTable } from '@/components/business'
import type { SearchField, TableColumn, ActionButton } from '@/components/business/types'
import { getIsolationPointList, createIsolationPoint, updateIsolationPoint, deleteIsolationPoint, getIsolationPointById, updateIsolationPointStatus, updateIsolationPointOccupy } from '@/api/isolation-point'
import { getLocationTree } from '@/api/location'
import type { IsolationPointVO } from '@/types/isolation-point'
import type { LocationTreeNode } from '@/types/location'

const searchTableRef = ref()
const locationTree = ref<LocationTreeNode[]>([])

onMounted(async () => {
  const res = await getLocationTree()
  locationTree.value = res.data
})

const hazardLevelOptions = [
  { label: '全部', value: undefined },
  { label: 'A级', value: 'A' },
  { label: 'B级', value: 'B' },
  { label: 'C级', value: 'C' },
]

const statusOptions = [
  { label: '全部', value: undefined },
  { label: '通板', value: 'OPEN' },
  { label: '盲板', value: 'BLIND' },
]

const searchFields = computed<SearchField[]>(() => [
  { type: 'input', label: '编码', prop: 'code', placeholder: '输入编码' },
  { type: 'input', label: '名称', prop: 'name', placeholder: '输入名称' },
  {
    type: 'tree-select', label: '所属单元', prop: 'unitId', placeholder: '选择单元',
    treeProps: { data: locationTree.value, props: { label: 'name', value: 'id', children: 'children' } },
  },
  { type: 'input', label: '介质', prop: 'medium', placeholder: '输入介质' },
  { type: 'select', label: '危害等级', prop: 'hazardLevel', options: hazardLevelOptions },
  { type: 'select', label: '状态', prop: 'status', options: statusOptions },
])

const columns: TableColumn[] = [
  { prop: 'code', label: '编码', width: 140 },
  { prop: 'name', label: '名称', minWidth: 140 },
  { prop: 'unitName', label: '所属单元', width: 120 },
  { prop: 'medium', label: '介质', width: 100 },
  { prop: 'hazardLevel', label: '危害等级', width: 90, align: 'center' },
  {
    label: '通盲状态', width: 100, align: 'center', slotName: 'status',
  },
  {
    label: '占用状态', width: 100, align: 'center', slotName: 'occupyStatus',
  },
  { prop: 'createdAt', label: '创建时间', width: 170 },
]

const formConfig: any = computed(() => ({
  fields: [
    {
      type: 'tree-select', label: '所属单元', prop: 'unitId', placeholder: '选择单元',
      rules: [{ required: true, message: '请选择所属单元', trigger: 'change' }],
      treeProps: { data: locationTree.value, props: { label: 'name', value: 'id', children: 'children' } },
    },
    { type: 'input', label: '编码', prop: 'code', placeholder: '请输入编码', rules: [{ required: true, message: '请输入编码', trigger: 'blur' }] },
    { type: 'input', label: '名称', prop: 'name', placeholder: '请输入名称', rules: [{ required: true, message: '请输入名称', trigger: 'blur' }] },
    { type: 'input', label: '介质', prop: 'medium', placeholder: '请输入介质' },
    { type: 'input', label: '压力等级', prop: 'pressureRating', placeholder: '请输入压力等级' },
    { type: 'input', label: '温度等级', prop: 'temperatureRating', placeholder: '请输入温度等级' },
    { type: 'select', label: '危害等级', prop: 'hazardLevel', options: hazardLevelOptions.filter(o => o.value) },
    { type: 'input', label: '点位类型', prop: 'pointType', placeholder: '请输入点位类型' },
    { type: 'input', label: '适配盲板规格', prop: 'blindSpec', placeholder: '请输入盲板规格' },
    { type: 'input', label: '关联设备位号', prop: 'equipmentTag', placeholder: '请输入设备位号' },
    { type: 'input', label: '关联管线号', prop: 'pipelineNo', placeholder: '请输入管线号' },
    { type: 'textarea', label: '备注', prop: 'remark' },
  ],
  createApi: createIsolationPoint,
  updateApi: (id: number | string, data: any) => updateIsolationPoint(Number(id), data),
  deleteApi: (id: number | string) => deleteIsolationPoint(Number(id)),
  getApi: (id: number | string) => getIsolationPointById(Number(id)).then(r => r.data),
  dialogTitle: { create: '新增隔离点', edit: '编辑隔离点' },
}))

const actionButtons: ActionButton[] = [
  {
    label: '设为盲板', size: 'small', type: 'text', confirm: '确定设为盲板吗？',
    onClick: async (row: IsolationPointVO) => {
      await updateIsolationPointStatus(row.id, 'BLIND')
      searchTableRef.value?.fetchList()
    },
  },
  {
    label: '设为通板', size: 'small', type: 'text', confirm: '确定设为通板吗？',
    onClick: async (row: IsolationPointVO) => {
      await updateIsolationPointStatus(row.id, 'OPEN')
      searchTableRef.value?.fetchList()
    },
  },
  {
    label: '占用', size: 'small', type: 'text', confirm: '确定占用该隔离点吗？',
    onClick: async (row: IsolationPointVO) => {
      await updateIsolationPointOccupy(row.id, 'OCCUPIED')
      searchTableRef.value?.fetchList()
    },
  },
  {
    label: '释放', size: 'small', type: 'text', confirm: '确定释放该隔离点吗？',
    onClick: async (row: IsolationPointVO) => {
      await updateIsolationPointOccupy(row.id, 'FREE')
      searchTableRef.value?.fetchList()
    },
  },
]

async function fetchApi(params: any) {
  const res = await getIsolationPointList(params)
  return { rows: res.data.rows, total: res.data.total }
}
</script>

<template>
  <SearchTable
    ref="searchTableRef"
    :search-fields="searchFields"
    :columns="columns"
    :action-buttons="actionButtons"
    :fetch-api="fetchApi"
    :form-config="formConfig"
  >
    <template #status="{ row }">
      <el-tag :type="row.status === 'OPEN' ? 'success' : 'danger'" size="small">
        {{ row.status === 'OPEN' ? '通板' : '盲板' }}
      </el-tag>
    </template>
    <template #occupyStatus="{ row }">
      <el-tag :type="row.occupyStatus === 'OCCUPIED' ? 'warning' : 'info'" size="small">
        {{ row.occupyStatus === 'OCCUPIED' ? '已占用' : '空闲' }}
      </el-tag>
    </template>
  </SearchTable>
</template>