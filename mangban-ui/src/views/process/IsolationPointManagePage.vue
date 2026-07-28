<script setup lang="ts">
import { ref, computed } from 'vue'
import { SearchTable } from '@/components/business'
import type { SearchField, TableColumn, ActionButton } from '@/components/business/types'
import { getLocationTree, createLocation, updateLocation, deleteLocation } from '@/api/location'
import { getIsolationPointList, getIsolationPointById, createIsolationPoint, updateIsolationPoint, deleteIsolationPoint, updateIsolationPointStatus, updateIsolationPointOccupy } from '@/api/isolation-point'
import type { LocationTreeNode } from '@/types/location'

// ========== 左侧：装置层级树 ==========
const locationTableRef = ref()
const locationList = ref<LocationTreeNode[]>([])

const typeOptions = [
  { label: '工厂', value: 'FACTORY' },
  { label: '装置', value: 'PLANT' },
  { label: '单元', value: 'UNIT' },
]

const locationColumns: TableColumn[] = [
  { prop: 'name', label: '名称', minWidth: 200 },
  { prop: 'code', label: '编码', width: 160 },
  {
    label: '类型', width: 100, align: 'center',
    formatter: (_r: any, _c: any, v: string) =>
      ({ FACTORY: '工厂', PLANT: '装置', UNIT: '单元' }[v] || v),
  },
  { prop: 'sortOrder', label: '排序', width: 80, align: 'center' },
]

async function locationFetchApi(_params: any) {
  const res = await getLocationTree()
  locationList.value = res.data
  return { rows: res.data, total: res.data.length }
}

function handleAddChild(parentId: number) {
  locationTableRef.value?.openFormDialog({ parentId, sortOrder: 0 })
}

const locationActionButtons: ActionButton[] = [
  { label: '新增子节点', size: 'small', type: 'text', onClick: (row: LocationTreeNode) => handleAddChild(row.id) },
]

function findNode(tree: LocationTreeNode[], id: number): LocationTreeNode | null {
  for (const node of tree) {
    if (node.id === id) return node
    if (node.children) {
      const found = findNode(node.children, id)
      if (found) return found
    }
  }
  return null
}

const locationFormConfig: any = computed(() => ({
  fields: [
    {
      type: 'tree-select', label: '上级节点', prop: 'parentId',
      placeholder: '选择上级（空=根工厂）',
      treeProps: { data: locationList.value, props: { label: 'name', value: 'id', children: 'children' } },
    },
    { type: 'input', label: '名称', prop: 'name', rules: [{ required: true, message: '请输入名称', trigger: 'blur' }] },
    { type: 'input', label: '编码', prop: 'code', rules: [{ required: true, message: '请输入编码', trigger: 'blur' }] },
    { type: 'select', label: '类型', prop: 'type', options: typeOptions, rules: [{ required: true, message: '请选择类型', trigger: 'change' }] },
    { type: 'input', label: '排序', prop: 'sortOrder' },
    { type: 'textarea', label: '备注', prop: 'remark' },
  ],
  createApi: createLocation,
  updateApi: (id: number, data: any) => updateLocation(id, data),
  deleteApi: deleteLocation,
  getApi: async (id: number | string) => {
    const res = await getLocationTree()
    return findNode(res.data, Number(id))
  },
  dialogTitle: { create: '新增节点', edit: '编辑节点' },
}))

// ========== 右侧：隔离点列表 ==========
const pointTableRef = ref()
const selectedUnitId = ref<number | undefined>()
const selectedUnitName = ref('')

function handleLocationRowClick(row: LocationTreeNode) {
  if (row.type !== 'UNIT') return
  selectedUnitId.value = row.id
  selectedUnitName.value = row.name
  pointTableRef.value?.fetchList()
}

const hazardLevelOptions = [
  { label: 'A级', value: 'A' },
  { label: 'B级', value: 'B' },
  { label: 'C级', value: 'C' },
]

const statusOptions = [
  { label: '开通', value: 'OPEN' },
  { label: '盲板', value: 'BLIND' },
]

const pointSearchFields: SearchField[] = [
  { type: 'input', label: '编号', prop: 'code', placeholder: '输入编号' },
  { type: 'input', label: '名称', prop: 'name', placeholder: '输入名称' },
  { type: 'select', label: '介质', prop: 'medium', placeholder: '选择介质' },
  { type: 'select', label: '危险等级', prop: 'hazardLevel', placeholder: '选择等级', options: hazardLevelOptions },
  { type: 'select', label: '状态', prop: 'status', placeholder: '选择状态', options: statusOptions },
]

const pointColumns: TableColumn[] = [
  { prop: 'code', label: '编号', width: 120 },
  { prop: 'name', label: '名称', minWidth: 140 },
  { prop: 'medium', label: '介质', width: 100 },
  { prop: 'hazardLevel', label: '危险等级', width: 100 },
  { prop: 'pointType', label: '隔离方式', width: 100 },
  { prop: 'blindSpec', label: '盲板规格', width: 100 },
  {
    prop: 'status', label: '状态', width: 100, align: 'center',
    formatter: (_r: any, _c: any, v: string) =>
      ({ OPEN: '开通', BLIND: '盲板' }[v] || v),
  },
  {
    prop: 'occupyStatus', label: '占用', width: 90, align: 'center',
    formatter: (_r: any, _c: any, v: string) =>
      ({ OCCUPIED: '占用', FREE: '空闲' }[v] || v),
  },
]

async function pointFetchApi(params: any) {
  if (!selectedUnitId.value) return { rows: [], total: 0 }
  const res = await getIsolationPointList({ ...params, unitId: selectedUnitId.value })
  return { rows: res.data.rows, total: res.data.total }
}

const pointFormConfig: any = computed(() => ({
  fields: [
    { type: 'input', label: '编号', prop: 'code', rules: [{ required: true, message: '请输入编号', trigger: 'blur' }] },
    { type: 'input', label: '名称', prop: 'name', rules: [{ required: true, message: '请输入名称', trigger: 'blur' }] },
    { type: 'input', label: '介质', prop: 'medium' },
    { type: 'select', label: '危险等级', prop: 'hazardLevel', options: hazardLevelOptions },
    { type: 'input', label: '压力等级', prop: 'pressureRating' },
    { type: 'input', label: '温度等级', prop: 'temperatureRating' },
    { type: 'select', label: '隔离方式', prop: 'pointType' },
    { type: 'input', label: '盲板规格', prop: 'blindSpec' },
    { type: 'input', label: '设备位号', prop: 'equipmentTag' },
    { type: 'input', label: '管线号', prop: 'pipelineNo' },
    { type: 'textarea', label: '备注', prop: 'remark' },
  ],
  createApi: async (data: any) => createIsolationPoint({ ...data, unitId: selectedUnitId.value }),
  updateApi: (id: number | string, data: any) => updateIsolationPoint(Number(id), data),
  deleteApi: (id: number | string) => deleteIsolationPoint(Number(id)),
  getApi: (id: number | string) => getIsolationPointById(Number(id)).then(r => r.data),
  dialogTitle: { create: '新增隔离点', edit: '编辑隔离点' },
}))

const pointActionButtons: ActionButton[] = [
  {
    label: '通盲切换', size: 'small', type: 'text',
    onClick: async (row: any) => {
      const newStatus = row.status === 'OPEN' ? 'BLIND' : 'OPEN'
      await updateIsolationPointStatus(row.id, { status: newStatus })
      pointTableRef.value?.fetchList()
    },
  },
  {
    label: '占用/释放', size: 'small', type: 'text',
    onClick: async (row: any) => {
      const newOccupy = row.occupyStatus === 'FREE' ? 'OCCUPIED' : 'FREE'
      await updateIsolationPointOccupy(row.id, { occupyStatus: newOccupy })
      pointTableRef.value?.fetchList()
    },
  },
]
</script>

<template>
  <div style="display: flex; gap: 12px; height: calc(100vh - 140px)">
    <!-- 左侧：装置层级 -->
    <el-card style="width: 480px; flex-shrink: 0">
      <template #header><span style="font-weight: bold; font-size: 14px">装置层级</span></template>
      <SearchTable
        ref="locationTableRef"
        :search-fields="[]"
        :columns="locationColumns"
        :action-buttons="locationActionButtons"
        :fetch-api="locationFetchApi"
        :form-config="locationFormConfig"
        :tree-props="{ rowKey: 'id', children: 'children', defaultExpandAll: true }"
        :show-search="false"
        @row-click="handleLocationRowClick"
      />
    </el-card>

    <!-- 右侧：隔离点列表 -->
    <el-card style="flex: 1" v-if="selectedUnitId">
      <template #header>
        <span style="font-weight: bold; font-size: 14px">隔离点列表 - {{ selectedUnitName }}</span>
      </template>
      <SearchTable
        ref="pointTableRef"
        :search-fields="pointSearchFields"
        :columns="pointColumns"
        :action-buttons="pointActionButtons"
        :fetch-api="pointFetchApi"
        :form-config="pointFormConfig"
        table-size="small"
      />
    </el-card>

    <el-card v-else style="flex: 1; display: flex; align-items: center; justify-content: center; color: #999; font-size: 14px">
      请先在左侧选择单元节点
    </el-card>
  </div>
</template>