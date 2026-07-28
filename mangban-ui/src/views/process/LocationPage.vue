<script setup lang="ts">
import { ref, computed } from 'vue'
import { SearchTable } from '@/components/business'
import type { SearchField, TableColumn, ActionButton } from '@/components/business/types'
import { getLocationTree, createLocation, updateLocation, deleteLocation } from '@/api/location'
import type { LocationTreeNode } from '@/types/location'

const searchTableRef = ref()
const list = ref<LocationTreeNode[]>([])

const typeOptions = [
  { label: '工厂', value: 'FACTORY' },
  { label: '装置', value: 'PLANT' },
  { label: '单元', value: 'UNIT' },
]

const searchFields: SearchField[] = []

const columns: TableColumn[] = [
  { prop: 'name', label: '名称', minWidth: 200 },
  { prop: 'code', label: '编码', width: 160 },
  {
    label: '类型', width: 100, align: 'center',
    formatter: (_r: any, _c: any, v: string) =>
      ({ FACTORY: '工厂', PLANT: '装置', UNIT: '单元' }[v] || v),
  },
  { prop: 'sortOrder', label: '排序', width: 80, align: 'center' },
]

async function fetchApi(_params: any) {
  const res = await getLocationTree()
  list.value = res.data
  return { rows: res.data, total: res.data.length }
}

function handleAddChild(parentId: number) {
  searchTableRef.value?.openFormDialog({ parentId, sortOrder: 0 })
}

const actionButtons: ActionButton[] = [
  { label: '新增子节点', size: 'small', type: 'text', onClick: (row: LocationTreeNode) => handleAddChild(row.id) },
]

const formConfig: any = computed(() => ({
  fields: [
    {
      type: 'tree-select', label: '上级节点', prop: 'parentId',
      placeholder: '选择上级（空=根工厂）',
      treeProps: { data: list.value, props: { label: 'name', value: 'id', children: 'children' } },
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
</script>

<template>
  <SearchTable
    ref="searchTableRef"
    :search-fields="searchFields"
    :columns="columns"
    :action-buttons="actionButtons"
    :fetch-api="fetchApi"
    :form-config="formConfig"
    :tree-props="{ rowKey: 'id', children: 'children', defaultExpandAll: true }"
    :show-search="false"
  />
</template>