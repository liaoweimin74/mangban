<script setup lang="ts">
import { ref, reactive, onMounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getMenuTree, createMenu, updateMenu, deleteMenu } from '@/api/menu'
import type { MenuTree, MenuCreateForm, MenuUpdateForm } from '@/types/menu'

const loading = ref(false)
const list = ref<MenuTree[]>([])
const dialogVisible = ref(false)
const dialogTitle = ref('新增菜单')
const isEdit = ref(false)
const editId = ref<number>(0)

const menuTypeMap: Record<number, string> = { 0: '目录', 1: '菜单', 2: '按钮' }
const menuTypeOptions = [
  { label: '目录', value: 0 }, { label: '菜单', value: 1 }, { label: '按钮', value: 2 }
]

const form = reactive<MenuCreateForm>({
  parentId: undefined, menuName: '', menuType: 1, path: '', component: '',
  permission: '', icon: '', sortOrder: 0, visible: 1
})

async function fetchList() {
  loading.value = true
  try {
    const res = await getMenuTree()
    list.value = res.data
  } finally { loading.value = false }
}

function handleAdd(parentId?: number) {
  dialogTitle.value = '新增菜单'; isEdit.value = false; editId.value = 0
  Object.assign(form, { parentId: parentId || undefined, menuName: '', menuType: 1, path: '', component: '', permission: '', icon: '', sortOrder: 0, visible: 1 })
  dialogVisible.value = true
}

function handleEdit(row: MenuTree) {
  dialogTitle.value = '编辑菜单'; isEdit.value = true; editId.value = row.id
  Object.assign(form, {
    parentId: row.parentId, menuName: row.menuName, menuType: row.menuType,
    path: row.path, component: row.component, permission: row.permission,
    icon: row.icon, sortOrder: row.sortOrder, visible: row.visible
  })
  dialogVisible.value = true
}

async function handleSubmit() {
  try {
    const data: any = { ...form }
    if (isEdit.value) {
      await updateMenu(editId.value, data)
      ElMessage.success('修改成功')
    } else {
      await createMenu(data)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false; fetchList()
  } catch (e: any) { ElMessage.error(e?.msg || '操作失败') }
}

async function handleDelete(row: MenuTree) {
  await ElMessageBox.confirm(`确定删除菜单「${row.menuName}」吗？`, '确认删除', { type: 'warning' })
  await deleteMenu(row.id)
  ElMessage.success('删除成功'); fetchList()
}

onMounted(fetchList)
</script>

<template>
  <div>
    <el-card>
      <div style="margin-bottom: 12px"><el-button type="primary" @click="handleAdd()">新增根菜单</el-button></div>
      <el-table :data="list" v-loading="loading" row-key="id" :tree-props="{ children: 'children' }" border default-expand-all>
        <el-table-column prop="menuName" label="菜单名称" min-width="180" />
        <el-table-column label="图标" width="80"><template #default="{ row }"><el-icon><component :is="row.icon || 'Menu'" /></el-icon></template></el-table-column>
        <el-table-column label="类型" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.menuType === 0 ? '' : row.menuType === 1 ? 'success' : 'warning'" size="small">{{ menuTypeMap[row.menuType] }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="path" label="路由" width="160" />
        <el-table-column prop="component" label="组件路径" width="200" />
        <el-table-column prop="permission" label="权限标识" width="180" />
        <el-table-column prop="sortOrder" label="排序" width="70" align="center" />
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <div class="flex items-center gap-1 whitespace-nowrap">
              <el-button size="small" text type="primary" @click="handleEdit(row)">编辑</el-button>
              <el-button v-if="row.menuType !== 2" size="small" text type="success" @click="handleAdd(row.id)">新增子菜单</el-button>
              <el-popconfirm title="确定删除吗？" @confirm="handleDelete(row)">
                <template #reference><el-button size="small" text type="danger">删除</el-button></template>
              </el-popconfirm>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="560px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="上级菜单">
          <el-tree-select v-model="form.parentId" :data="list" check-strictly :props="{ label: 'menuName', value: 'id', children: 'children' }" placeholder="选择上级菜单" clearable style="width: 100%" />
        </el-form-item>
        <el-form-item label="菜单类型" required><el-select v-model="form.menuType" style="width: 100%"><el-option v-for="o in menuTypeOptions" :key="o.value" :label="o.label" :value="o.value" /></el-select></el-form-item>
        <el-form-item label="菜单名称" required><el-input v-model="form.menuName" /></el-form-item>
        <el-form-item label="图标"><el-input v-model="form.icon" placeholder="Element Plus 图标名" /></el-form-item>
        <el-form-item v-if="form.menuType !== 2" label="路由路径"><el-input v-model="form.path" /></el-form-item>
        <el-form-item v-if="form.menuType === 1" label="组件路径"><el-input v-model="form.component" placeholder="views/system/user/UserPage" /></el-form-item>
        <el-form-item v-if="form.menuType === 2" label="权限标识"><el-input v-model="form.permission" placeholder="system:user:add" /></el-form-item>
        <el-form-item label="排序"><el-input-number v-model="form.sortOrder" :min="0" /></el-form-item>
        <el-form-item label="可见"><el-radio-group v-model="form.visible"><el-radio :value="1">显示</el-radio><el-radio :value="0">隐藏</el-radio></el-radio-group></el-form-item>
      </el-form>
      <template #footer><el-button @click="dialogVisible = false">取消</el-button><el-button type="primary" @click="handleSubmit">确定</el-button></template>
    </el-dialog>
  </div>
</template>