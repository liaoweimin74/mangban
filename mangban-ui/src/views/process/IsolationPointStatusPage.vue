<script setup lang="ts">
import { SearchTable } from '@/components/business'
import type { SearchField, TableColumn } from '@/components/business/types'
import { getIsolationPointList } from '@/api/isolation-point'

const hazardLevelOptions = [
  { label: 'A级', value: 'A' },
  { label: 'B级', value: 'B' },
  { label: 'C级', value: 'C' },
]

const statusOptions = [
  { label: '开通', value: 'OPEN' },
  { label: '盲板', value: 'BLIND' },
]

const occupyStatusOptions = [
  { label: '空闲', value: 'FREE' },
  { label: '占用', value: 'OCCUPIED' },
]

const searchFields: SearchField[] = [
  { type: 'input', label: '编号', prop: 'code', placeholder: '输入编号' },
  { type: 'input', label: '名称', prop: 'name', placeholder: '输入名称' },
  { type: 'select', label: '介质', prop: 'medium', placeholder: '选择介质' },
  { type: 'select', label: '危险等级', prop: 'hazardLevel', placeholder: '选择等级', options: hazardLevelOptions },
  { type: 'select', label: '通盲状态', prop: 'status', placeholder: '选择状态', options: statusOptions },
  { type: 'select', label: '占用状态', prop: 'occupyStatus', placeholder: '占用状态', options: occupyStatusOptions },

]

const columns: TableColumn[] = [
  { prop: 'code', label: '编号', width: 120 },
  { prop: 'name', label: '名称', minWidth: 140 },
  { prop: 'factoryName', label: '工厂', width: 120 },
  { prop: 'plantName', label: '装置', width: 120 },
  { prop: 'unitName', label: '单元', width: 120 },
  { prop: 'medium', label: '介质', width: 100 },
  { prop: 'hazardLevel', label: '危险等级', width: 100 },
  {
    prop: 'status', label: '通盲状态', width: 100, align: 'center',
    formatter: (_r: any, _c: any, v: string) =>
      ({ OPEN: '开通', BLIND: '盲板' }[v] || v),
  },
  {
    prop: 'occupyStatus', label: '占用状态', width: 100, align: 'center',
    formatter: (_r: any, _c: any, v: string) =>
      ({ OCCUPIED: '占用', FREE: '空闲' }[v] || v),
  },
  { prop: 'pointType', label: '隔离方式', width: 100 },
  { prop: 'blindSpec', label: '盲板规格', width: 100 },
]

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
    />
</template>