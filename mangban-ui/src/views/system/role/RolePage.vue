<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getRoleList, createRole, updateRole, deleteRole, getRoleMenus, assignRoleMenus } from '@/api/role'
import { getMenuTree } from '@/api/menu'
import type { RoleVO, RoleQueryParams, RoleCreateForm, RoleUpdateForm } from '@/types/role'
import type { MenuTree } from '@/types/menu'

const loading = ref(false)
const list = ref<RoleVO[]>([])
const total = ref(0)
const menuTree = ref<MenuTree[]>([])
const dialogVisible = ref(false)
const menuDialogVisible = ref(false)
const dialogTitle = ref('新增角色')
const isEdit = ref(false)
const editId = ref<number>(0)
const currentRoleId = ref<number>(0)
const checkedMenuKeys = ref<number[]>([])
const menuTreeRef = ref<any>(null)

const query = reactive<RoleQueryParams>({ page: 1, size: 10, roleName: '', status: undefined })
const form = reactive<RoleCreateForm>({ roleName: '', roleCode: '', description: '' })

async function fetchList() {
  loading.value = true
  try {
    const res = await getRoleList(query)
    list.value = res.data.rows
    total.value = res.data.total
  } finally { loading.value = false }
}

function handleSearch() { query.page = 1; fetchList() }
function handleReset() { query.roleName = ''; query.status = undefined; query.page = 1; fetchList() }

function handleAdd() {
  dialogTitle.value = '新增角色'; isEdit.value = false; editId.value = 0
  Object.assign(form, { roleName: '', roleCode: '', description: '' })
  dialogVisible.value = true
}

function handleEdit(row: RoleVO) {
  dialogTitle.value = '编辑角色'; isEdit.value = true; editId.value = row.id
  Object.assign(form, { roleName: row.roleName, roleCode: row.roleCode, description: row.description })
  dialogVisible.value = true
}

async function handleSubmit() {
  try {
    if (isEdit.value) {
      await updateRole(editId.value, { roleName: form.roleName, description: form.description })
      ElMessage.success('修改成功')
    } else {
      await createRole(form)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false; fetchList()
  } catch (e: any) { ElMessage.error(e?.msg || '操作失败') }
}

async function handleDelete(row: RoleVO) {
  await ElMessageBox.confirm(`确定删除角色「${row.name}」吗？`, '确认删除', { type: 'warning' })
  await deleteRole(row.id)
  ElMessage.success('删除成功'); fetchList()
}

async function handleAssignMenu(row: RoleVO) {
  currentRoleId.value = row.id
  const res = await getRoleMenus(row.id)
  checkedMenuKeys.value = res.data || []
  menuDialogVisible.value = true
}

async function handleMenuSubmit() {
  const keys = menuTreeRef.value?.getCheckedKeys(true) || []
  await assignRoleMenus(currentRoleId.value, keys)
  ElMessage.success('分配菜单成功')
  menuDialogVisible.value = false
}

onMounted(async () => {
  const menuRes = await getMenuTree()
  menuTree.value = menuRes.data
  fetchList()
})
</script>

<template>
  <div>
    <el-card style="margin-bottom: 16px">
      <el-form :inline="true" :model="query">
        <el-form-item label="角色名称"><el-input v-model="query.roleName" placeholder="输入名称" clearable /></el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.status" placeholder="选择" clearable style="width: 120px">
            <el-option label="启用" :value="1" /><el-option label="停用" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item><el-button type="primary" @click="handleSearch">搜索</el-button><el-button @click="handleReset">重置</el-button></el-form-item>
      </el-form>
    </el-card>

    <el-card>
      <div style="margin-bottom: 12px"><el-button type="primary" @click="handleAdd">新增角色</el-button></div>
      <el-table :data="list" v-loading="loading" border>
        <el-table-column prop="roleName" label="角色名称" width="140" />
        <el-table-column prop="roleCode" label="角色编码" width="160" />
        <el-table-column prop="description" label="描述" min-width="200" />
        <el-table-column prop="status" label="状态" width="80" align="center">
          <template #default="{ row }">{{ row.status === 1 ? '启用' : '停用' }}</template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="170" />
        <el-table-column label="操作" width="250" fixed="right">
          <template #default="{ row }">
            <div class="flex items-center gap-1 whitespace-nowrap">
              <el-button size="small" text type="primary" @click="handleEdit(row)">编辑</el-button>
              <el-popconfirm title="确定删除吗？" @confirm="handleDelete(row)">
                <template #reference><el-button size="small" text type="danger">删除</el-button></template>
              </el-popconfirm>
              <el-button size="small" text type="success" @click="handleAssignMenu(row)">分配菜单</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
      <div style="margin-top: 16px; display: flex; justify-content: flex-end">
        <el-pagination v-model:current-page="query.page" v-model:page-size="query.size" :total="total" :page-sizes="[10, 20, 50]" layout="total, sizes, prev, pager, next" @size-change="fetchList()" @current-change="fetchList()" />
      </div>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="角色名称" required><el-input v-model="form.roleName" /></el-form-item>
        <el-form-item label="角色编码" required><el-input v-model="form.roleCode" :disabled="isEdit" /></el-form-item>
        <el-form-item label="描述"><el-input v-model="form.description" type="textarea" :rows="3" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="dialogVisible = false">取消</el-button><el-button type="primary" @click="handleSubmit">确定</el-button></template>
    </el-dialog>

    <el-dialog v-model="menuDialogVisible" title="分配菜单权限" width="420px">
      <el-tree ref="menuTreeRef" :data="menuTree" show-checkbox node-key="id" :default-checked-keys="checkedMenuKeys" :props="{ label: 'menuName', children: 'children' }" default-expand-all />
      <template #footer><el-button @click="menuDialogVisible = false">取消</el-button><el-button type="primary" @click="handleMenuSubmit">保存</el-button></template>
    </el-dialog>
  </div>
</template>