<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getOrgTree, createOrg, updateOrg, deleteOrg } from '@/api/org'
import type { TreeNode, OrgCreateForm } from '@/types/org'

const loading = ref(false)
const list = ref<TreeNode[]>([])
const dialogVisible = ref(false)
const dialogTitle = ref('新增组织')
const isEdit = ref(false)
const editId = ref<number>(0)

const form = reactive<OrgCreateForm>({ name: '', code: '', parentId: undefined, sortOrder: 0 })

async function fetchList() {
  loading.value = true
  try { const res = await getOrgTree(); list.value = res.data }
  finally { loading.value = false }
}

function handleAdd(parentId?: number) {
  dialogTitle.value = '新增组织'; isEdit.value = false; editId.value = 0
  Object.assign(form, { name: '', code: '', parentId: parentId || undefined, sortOrder: 0 })
  dialogVisible.value = true
}

function handleEdit(row: TreeNode) {
  dialogTitle.value = '编辑组织'; isEdit.value = true; editId.value = row.id
  Object.assign(form, { name: row.label, code: row.code, parentId: row.parentId, sortOrder: row.sortOrder })
  dialogVisible.value = true
}

async function handleSubmit() {
  try {
    const data: any = { ...form, orgName: form.name, orgCode: form.code }
    if (isEdit.value) {
      await updateOrg(editId.value, data)
      ElMessage.success('修改成功')
    } else {
      await createOrg(data)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false; fetchList()
  } catch (e: any) { ElMessage.error(e?.msg || '操作失败') }
}

async function handleDelete(row: TreeNode) {
  await ElMessageBox.confirm(`确定删除组织「${row.label}」吗？其子组织也会被删除。`, '确认删除', { type: 'warning' })
  await deleteOrg(row.id)
  ElMessage.success('删除成功'); fetchList()
}

onMounted(fetchList)
</script>

<template>
  <div>
    <el-card>
      <div style="margin-bottom: 12px"><el-button type="primary" @click="handleAdd()">新增根组织</el-button></div>
      <el-table :data="list" v-loading="loading" row-key="id" :tree-props="{ children: 'children' }" border default-expand-all>
        <el-table-column prop="label" label="组织名称" min-width="200" />
        <el-table-column prop="code" label="组织编码" width="180" />
        <el-table-column prop="sortOrder" label="排序" width="80" align="center" />
        <el-table-column prop="status" label="状态" width="80" align="center">
          <template #default="{ row }">{{ row.status === 1 ? '启用' : '停用' }}</template>
        </el-table-column>
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <div class="flex items-center gap-1 whitespace-nowrap">
              <el-button size="small" text type="primary" @click="handleEdit(row)">编辑</el-button>
              <el-button size="small" text type="success" @click="handleAdd(row.id)">新增子组织</el-button>
              <el-popconfirm title="确定删除吗？" @confirm="handleDelete(row)">
                <template #reference><el-button size="small" text type="danger">删除</el-button></template>
              </el-popconfirm>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="480px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="上级组织">
          <el-tree-select v-model="form.parentId" :data="list" check-strictly :props="{ label: 'label', value: 'id', children: 'children' }" placeholder="选择上级（空为根）" clearable style="width: 100%" />
        </el-form-item>
        <el-form-item label="组织名称" required><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="组织编码" required><el-input v-model="form.code" /></el-form-item>
        <el-form-item label="排序"><el-input-number v-model="form.sortOrder" :min="0" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="dialogVisible = false">取消</el-button><el-button type="primary" @click="handleSubmit">确定</el-button></template>
    </el-dialog>
  </div>
</template>