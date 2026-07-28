<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { SearchTable } from '@/components/business'
import type { SearchField, TableColumn, FormConfig } from '@/components/business/types'
import { getIsolationPointList } from '@/api/isolation-point'
import { getLocationTree } from '@/api/location'
import type { IsolationPointVO } from '@/types/isolation-point'
import type { LocationTreeNode } from '@/types/location'

const locationTree = ref<LocationTreeNode[]>([])

onMounted(async () => {
  const res = await getLocationTree()
  locationTree.value = res.data
})

const searchFields = computed<SearchField[]>(() => [
  {
    type: 'tree-select', label: '所属装置', prop: 'plantId', placeholder: '选择装置',
    treeProps: { data: locationTree.value, props: { label: 'name', value: 'id', children: 'children' } },
  },
  { type: 'input', label: '介质', prop: 'medium', placeholder: '输入介质' },
  { type: 'select', label: '危害等级', prop: 'hazardLevel', options: [
    { label: '全部', value: undefined },
    { label: 'A级', value: 'A' },
    { label: 'B级', value: 'B' },
    { label: 'C级', value: 'C' },
  ]},
  { type: 'select', label: '通盲状态', prop: 'status', options: [
    { label: '全部', value: undefined },
    { label: '通板', value: 'OPEN' },
    { label: '盲板', value: 'BLIND' },
  ]},
  { type: 'select', label: '占用状态', prop: 'occupyStatus', options: [
    { label: '全部', value: undefined },
    { label: '已占用', value: 'OCCUPIED' },
    { label: '空闲', value: 'FREE' },
  ]},
])

const columns: TableColumn[] = [
  { prop: 'code', label: '编码', width: 140 },
  { prop: 'name', label: '名称', minWidth: 140 },
  { prop: 'factoryName', label: '所属工厂', width: 120 },
  { prop: 'plantName', label: '所属装置', width: 120 },
  { prop: 'unitName', label: '所属单元', width: 120 },
  { prop: 'medium', label: '介质', width: 100 },
  { prop: 'hazardLevel', label: '危害等级', width: 90, align: 'center' },
  { label: '通盲状态', width: 100, align: 'center', slotName: 'status' },
  { label: '占用状态', width: 100, align: 'center', slotName: 'occupyStatus' },
]

const formConfig = computed<FormConfig<IsolationPointVO>>(() => ({
  fields: [],
  createApi: undefined as any,
  updateApi: undefined as any,
  deleteApi: undefined as any,
  getApi: undefined as any,
}))

async function fetchApi(params: any) {
  const res = await getIsolationPointList(params)
  return { rows: res.data.rows, total: res.data.total }
}
</script>

<template>
  <SearchTable
    :search-fields="searchFields"
    :columns="columns"
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