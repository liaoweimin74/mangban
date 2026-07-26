<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getDictTypeList, createDictType, updateDictType, deleteDictType, getDictDataList, createDictData, updateDictData, deleteDictData } from '@/api/dict'
import type { DictTypeVO, DictTypeQueryParams, DictTypeCreateForm, DictDataVO, DictDataCreateForm } from '@/types/dict'

// 字典类型
const typeLoading = ref(false)
const typeList = ref<DictTypeVO[]>([])
const typeTotal = ref(0)
const selectedType = ref<DictTypeVO | null>(null)
const typeQuery = reactive<DictTypeQueryParams>({ page: 1, size: 10, dictName: '', dictCode: '' })
const typeDialogVisible = ref(false)
const typeDialogTitle = ref('新增字典类型')
const typeIsEdit = ref(false)
const typeEditId = ref<number>(0)
const typeForm = reactive<DictTypeCreateForm>({ dictName: '', dictCode: '', remark: '' })

// 字典数据
const dataLoading = ref(false)
const dataList = ref<DictDataVO[]>([])
const dataTotal = ref(0)
const dataQuery = reactive({ page: 1, size: 10, label: '', value: '' })
const dataDialogVisible = ref(false)
const dataDialogTitle = ref('新增字典项')
const dataIsEdit = ref(false)
const dataEditId = ref<number>(0)
const dataForm = reactive<DictDataCreateForm>({ dictCode: '', label: '', value: '', sortOrder: 0 })

// --- 字典类型 ---
async function fetchTypeList() {
  typeLoading.value = true
  try { const res = await getDictTypeList(typeQuery); typeList.value = res.data.rows; typeTotal.value = res.data.total }
  finally { typeLoading.value = false }
}

function handleTypeSearch() { typeQuery.page = 1; fetchTypeList() }
function handleTypeReset() { typeQuery.dictName = ''; typeQuery.dictCode = ''; typeQuery.page = 1; fetchTypeList() }

function handleTypeAdd() {
  typeDialogTitle.value = '新增字典类型'; typeIsEdit.value = false; typeEditId.value = 0
  Object.assign(typeForm, { dictName: '', dictCode: '', remark: '' })
  typeDialogVisible.value = true
}

function handleTypeEdit(row: DictTypeVO) {
  typeDialogTitle.value = '编辑字典类型'; typeIsEdit.value = true; typeEditId.value = row.id
  Object.assign(typeForm, { dictName: row.dictName, dictCode: row.dictCode, remark: row.remark })
  typeDialogVisible.value = true
}

async function handleTypeSubmit() {
  try {
    if (typeIsEdit.value) {
      await updateDictType(typeEditId.value, typeForm)
      ElMessage.success('修改成功')
    } else {
      await createDictType(typeForm)
      ElMessage.success('创建成功')
    }
    typeDialogVisible.value = false; fetchTypeList()
  } catch (e: any) { ElMessage.error(e?.msg || '操作失败') }
}

async function handleTypeDelete(row: DictTypeVO) {
  await ElMessageBox.confirm(`确定删除字典类型「${row.dictName}」吗？`, '确认删除', { type: 'warning' })
  await deleteDictType(row.id)
  ElMessage.success('删除成功')
  if (selectedType.value?.id === row.id) { selectedType.value = null; dataList.value = [] }
  fetchTypeList()
}

async function handleTypeToggleStatus(row: DictTypeVO) {
  try {
    await updateDictType(row.id, { status: row.status === 1 ? 0 : 1 })
    ElMessage.success(row.status === 1 ? '已停用' : '已启用')
    fetchTypeList()
  } catch (e: any) { ElMessage.error(e?.msg || '操作失败') }
}

function handleTypeSelect(row: DictTypeVO) {
  selectedType.value = row
  dataQuery.label = ''
  dataQuery.value = ''
  fetchDataList()
}

// --- 字典数据 ---
async function fetchDataList() {
  if (!selectedType.value) return
  dataLoading.value = true
  try {
    const res = await getDictDataList(selectedType.value.dictCode)
    // 过滤
    let list = res.data as any[]
    if (dataQuery.label) list = list.filter((d: any) => d.label?.includes(dataQuery.label))
    if (dataQuery.value) list = list.filter((d: any) => d.value?.includes(dataQuery.value))
    dataTotal.value = list.length
    // 前端分页
    const start = (dataQuery.page - 1) * dataQuery.size
    dataList.value = list.slice(start, start + dataQuery.size)
  } finally { dataLoading.value = false }
}

function handleDataSearch() { dataQuery.page = 1; fetchDataList() }
function handleDataReset() { dataQuery.label = ''; dataQuery.value = ''; dataQuery.page = 1; fetchDataList() }

function handleDataAdd() {
  if (!selectedType.value) return
  dataDialogTitle.value = '新增字典项'; dataIsEdit.value = false; dataEditId.value = 0
  Object.assign(dataForm, { dictCode: selectedType.value.dictCode, label: '', value: '', sortOrder: 0 })
  dataDialogVisible.value = true
}

function handleDataEdit(row: DictDataVO) {
  dataDialogTitle.value = '编辑字典项'; dataIsEdit.value = true; dataEditId.value = row.id
  Object.assign(dataForm, { dictCode: row.dictCode, label: row.label, value: row.value, sortOrder: row.sortOrder })
  dataDialogVisible.value = true
}

async function handleDataSubmit() {
  try {
    if (dataIsEdit.value) {
      await updateDictData(dataEditId.value, dataForm)
      ElMessage.success('修改成功')
    } else {
      await createDictData(dataForm)
      ElMessage.success('创建成功')
    }
    dataDialogVisible.value = false; fetchDataList()
  } catch (e: any) { ElMessage.error(e?.msg || '操作失败') }
}

async function handleDataDelete(row: DictDataVO) {
  await ElMessageBox.confirm(`确定删除字典项「${row.label}」吗？`, '确认删除', { type: 'warning' })
  await deleteDictData(row.id)
  ElMessage.success('删除成功'); fetchDataList()
}

async function handleDataToggleStatus(row: DictDataVO) {
  try {
    await updateDictData(row.id, { status: row.status === 1 ? 0 : 1 })
    ElMessage.success(row.status === 1 ? '已停用' : '已启用')
    fetchDataList()
  } catch (e: any) { ElMessage.error(e?.msg || '操作失败') }
}

onMounted(fetchTypeList)
</script>

<template>
  <div style="display: flex; gap: 12px; height: calc(100vh - 140px)">
    <!-- 左侧：字典类型 -->
    <el-card style="width: 400px; flex-shrink: 0; display: flex; flex-direction: column">
      <template #header><span style="font-weight: bold; font-size: 14px">字典类型</span></template>
      <div class="flex items-center gap-1 mb-2">
        <el-input v-model="typeQuery.dictName" placeholder="名称" clearable size="small" style="width: 80px" />
        <el-input v-model="typeQuery.dictCode" placeholder="编码" clearable size="small" style="width: 70px" />
        <el-button size="small" @click="handleTypeSearch" circle>
          <svg class="w-3.5 h-3.5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"/></svg>
        </el-button>
        <el-button size="small" @click="handleTypeReset" circle>
          <svg class="w-3.5 h-3.5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 4v5h5M20 20v-5h-5M4 9a9 9 0 0115.36-5.36M20 15a9 9 0 01-15.36 5.36"/></svg>
        </el-button>
        <el-button size="small" type="primary" @click="handleTypeAdd" circle class="ml-auto">
          <svg class="w-3.5 h-3.5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4"/></svg>
        </el-button>
      </div>
      <el-table :data="typeList" v-loading="typeLoading" highlight-current-row @row-click="handleTypeSelect" style="flex: 1; overflow-y: auto" size="small">
        <el-table-column prop="dictName" label="名称" min-width="100" />
        <el-table-column prop="dictCode" label="编码" width="70" />
        <el-table-column label="操作" width="160">
          <template #default="{ row }">
            <div class="flex items-center gap-1 whitespace-nowrap">
              <el-button size="small" text type="primary" @click.stop="handleTypeEdit(row)">编辑</el-button>
              <el-button size="small" text :type="row.status === 1 ? 'warning' : 'success'" @click.stop="handleTypeToggleStatus(row)">{{ row.status === 1 ? '停用' : '启用' }}</el-button>
              <el-popconfirm title="确定删除？" @confirm="handleTypeDelete(row)">
                <template #reference><el-button size="small" text type="danger" @click.stop>删除</el-button></template>
              </el-popconfirm>
            </div>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination v-model:current-page="typeQuery.page" v-model:page-size="typeQuery.size" :total="typeTotal" :page-sizes="[10, 20]" layout="total, prev, pager, next" small @size-change="fetchTypeList()" @current-change="fetchTypeList()" />
    </el-card>

    <!-- 右侧：字典数据 -->
    <el-card style="flex: 1; display: flex; flex-direction: column">
      <template #header><span style="font-weight: bold; font-size: 14px">字典项列表{{ selectedType ? ` - ${selectedType.dictName}` : '' }}</span></template>
      <div class="flex items-center gap-1 mb-2">
        <el-input v-model="dataQuery.label" placeholder="标签" clearable size="small" style="width: 100px" />
        <el-input v-model="dataQuery.value" placeholder="值" clearable size="small" style="width: 100px" />
        <el-button size="small" @click="handleDataSearch" circle>
          <svg class="w-3.5 h-3.5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"/></svg>
        </el-button>
        <el-button size="small" @click="handleDataReset" circle>
          <svg class="w-3.5 h-3.5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 4v5h5M20 20v-5h-5M4 9a9 9 0 0115.36-5.36M20 15a9 9 0 01-15.36 5.36"/></svg>
        </el-button>
        <el-button size="small" type="primary" @click="handleDataAdd" :disabled="!selectedType" circle class="ml-auto">
          <svg class="w-3.5 h-3.5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4"/></svg>
        </el-button>
      </div>
      <el-table :data="dataList" v-loading="dataLoading" border style="flex: 1; overflow-y: auto" size="small">
        <el-table-column prop="label" label="标签" min-width="140" />
        <el-table-column prop="value" label="值" min-width="140" />
        <el-table-column prop="sortOrder" label="排序" width="80" align="center" />
        <el-table-column prop="status" label="状态" width="80" align="center">
          <template #default="{ row }">{{ row.status === 1 ? '启用' : '停用' }}</template>
        </el-table-column>
        <el-table-column label="操作" width="170">
          <template #default="{ row }">
            <div class="flex items-center gap-0.5 whitespace-nowrap">
              <el-button size="small" text type="primary" @click="handleDataEdit(row)">编辑</el-button>
              <el-button size="small" text :type="row.status === 1 ? 'warning' : 'success'" @click="handleDataToggleStatus(row)">{{ row.status === 1 ? '停用' : '启用' }}</el-button>
              <el-popconfirm title="确定删除？" @confirm="handleDataDelete(row)">
                <template #reference><el-button size="small" text type="danger">删除</el-button></template>
              </el-popconfirm>
            </div>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination v-model:current-page="dataQuery.page" v-model:page-size="dataQuery.size" :total="dataTotal" :page-sizes="[10, 20, 50]" layout="total, prev, pager, next" small @size-change="fetchDataList()" @current-change="fetchDataList()" />
    </el-card>

    <!-- 字典类型对话框 -->
    <el-dialog v-model="typeDialogVisible" :title="typeDialogTitle" width="460px">
      <el-form :model="typeForm" label-width="80px">
        <el-form-item label="名称" required><el-input v-model="typeForm.dictName" /></el-form-item>
        <el-form-item label="编码" required><el-input v-model="typeForm.dictCode" /></el-form-item>
        <el-form-item label="备注"><el-input v-model="typeForm.remark" type="textarea" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="typeDialogVisible = false">取消</el-button><el-button type="primary" @click="handleTypeSubmit">确定</el-button></template>
    </el-dialog>

    <!-- 字典数据对话框 -->
    <el-dialog v-model="dataDialogVisible" :title="dataDialogTitle" width="460px">
      <el-form :model="dataForm" label-width="80px">
        <el-form-item label="标签" required><el-input v-model="dataForm.label" /></el-form-item>
        <el-form-item label="值" required><el-input v-model="dataForm.value" /></el-form-item>
        <el-form-item label="排序"><el-input-number v-model="dataForm.sortOrder" :min="0" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="dataDialogVisible = false">取消</el-button><el-button type="primary" @click="handleDataSubmit">确定</el-button></template>
    </el-dialog>
  </div>
</template>