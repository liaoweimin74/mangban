<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getUserList, createUser, updateUser, deleteUser, updateUserStatus, resetUserPassword } from '@/api/user'
import { getOrgTree } from '@/api/org'
import { getRoleList } from '@/api/role'
import type { UserVO, UserQueryParams, UserCreateForm, UserUpdateForm } from '@/types/user'
import type { TreeNode } from '@/types/org'
import type { RoleVO } from '@/types/role'

const loading = ref(false)
const list = ref<UserVO[]>([])
const total = ref(0)
const orgTree = ref<TreeNode[]>([])
const roleList = ref<RoleVO[]>([])
const dialogVisible = ref(false)
const dialogTitle = ref('新增用户')
const isEdit = ref(false)
const editId = ref<number>(0)

const query = reactive<UserQueryParams>({ page: 1, size: 10, username: '', nickname: '', orgId: undefined, status: undefined })
const form = reactive<UserCreateForm>({ username: '', nickname: '', email: '', phone: '', orgId: undefined, roleIds: [] })

const statusOptions = [
  { label: '全部', value: undefined },
  { label: '启用', value: 1 },
  { label: '停用', value: 0 }
]

async function fetchList() {
  loading.value = true
  try {
    const res = await getUserList(query)
    const data = res.data
    list.value = data.rows
    total.value = data.total
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  query.page = 1
  fetchList()
}

function handleReset() {
  query.username = ''
  query.nickname = ''
  query.orgId = undefined
  query.status = undefined
  query.page = 1
  fetchList()
}

function handleAdd() {
  dialogTitle.value = '新增用户'
  isEdit.value = false
  editId.value = 0
  Object.assign(form, { username: '', nickname: '', email: '', phone: '', orgId: undefined, roleIds: [] })
  dialogVisible.value = true
}

function handleEdit(row: UserVO) {
  dialogTitle.value = '编辑用户'
  isEdit.value = true
  editId.value = row.id
  Object.assign(form, {
    username: row.username,
    nickname: row.nickname,
    email: row.email || '',
    phone: row.phone || '',
    orgId: row.orgId,
    roleIds: row.roleIds || []
  })
  dialogVisible.value = true
}

async function handleSubmit() {
  try {
    if (isEdit.value) {
      await updateUser(editId.value, {
        nickname: form.nickname, email: form.email, phone: form.phone,
        orgId: form.orgId, roleIds: form.roleIds
      })
      ElMessage.success('修改成功')
    } else {
      await createUser(form)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    fetchList()
  } catch (e: any) {
    ElMessage.error(e?.msg || e?.message || '操作失败')
  }
}

async function handleDelete(row: UserVO) {
  await ElMessageBox.confirm(`确定删除用户「${row.nickname}」吗？`, '确认删除', { type: 'warning' })
  await deleteUser(row.id)
  ElMessage.success('删除成功')
  fetchList()
}

async function handleStatusChange(row: UserVO) {
  const newStatus = row.status === 1 ? 0 : 1
  const label = newStatus === 0 ? '停用' : '启用'
  await ElMessageBox.confirm(`确定${label}用户「${row.nickname}」吗？`, '确认', { type: 'warning' })
  await updateUserStatus(row.id, newStatus)
  ElMessage.success(`${label}成功`)
  fetchList()
}

async function handleResetPassword(row: UserVO) {
  await ElMessageBox.confirm(`确定重置用户「${row.nickname}」的密码吗？`, '确认重置密码', { type: 'warning' })
  await resetUserPassword(row.id)
  ElMessage.success('密码已重置为 123456')
}

onMounted(async () => {
  const [orgRes, roleRes] = await Promise.all([getOrgTree(), getRoleList({ page: 1, size: 999 })])
  orgTree.value = orgRes.data
  roleList.value = roleRes.data.rows
  fetchList()
})
</script>

<template>
  <div>
    <!-- 搜索栏 -->
    <el-card style="margin-bottom: 16px">
      <el-form :inline="true" :model="query">
        <el-form-item label="用户名"><el-input v-model="query.username" placeholder="输入用户名" clearable /></el-form-item>
        <el-form-item label="昵称"><el-input v-model="query.nickname" placeholder="输入昵称" clearable /></el-form-item>
        <el-form-item label="组织机构"><el-tree-select v-model="query.orgId" :data="orgTree" check-strictly :props="{ label: 'label', value: 'id', children: 'children' }" placeholder="选择组织" clearable style="width: 200px" /></el-form-item>
        <el-form-item label="状态"><el-select v-model="query.status" placeholder="选择状态" clearable style="width: 120px"><el-option v-for="o in statusOptions" :key="String(o.value)" :label="o.label" :value="o.value" /></el-select></el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 表格 -->
    <el-card>
      <div style="margin-bottom: 12px">
        <el-button type="primary" @click="handleAdd" v-permission="'system:user:add'">新增用户</el-button>
      </div>
      <el-table :data="list" v-loading="loading" border>
        <el-table-column prop="username" label="用户名" width="120" />
        <el-table-column prop="nickname" label="昵称" width="120" />
        <el-table-column prop="email" label="邮箱" min-width="160" />
        <el-table-column prop="phone" label="手机号" width="140" />
        <el-table-column prop="orgName" label="组织机构" width="140" />
        <el-table-column label="角色" min-width="140">
          <template #default="{ row }">{{ row.roleIds?.map((id: number) => roleList.find(r => r.id === id)?.roleName || id).join(' / ') }}</template>
        </el-table-column>
        <el-table-column label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-switch :model-value="row.status === 1" @change="handleStatusChange(row)" />
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="170" />
        <el-table-column label="操作" width="240" fixed="right">
          <template #default="{ row }">
            <div class="flex items-center gap-1 whitespace-nowrap">
              <el-button size="small" text type="primary" @click="handleEdit(row)" v-permission="'system:user:edit'">编辑</el-button>
              <el-popconfirm title="确定删除吗？" @confirm="handleDelete(row)">
                <template #reference><el-button size="small" text type="danger">删除</el-button></template>
              </el-popconfirm>
              <el-popconfirm title="确定重置密码吗？" @confirm="handleResetPassword(row)">
                <template #reference><el-button size="small" text type="warning">重置密码</el-button></template>
              </el-popconfirm>
            </div>
          </template>
        </el-table-column>
      </el-table>
      <div style="margin-top: 16px; display: flex; justify-content: flex-end">
        <el-pagination
          v-model:current-page="query.page"
          v-model:page-size="query.size"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          @size-change="fetchList()"
          @current-change="fetchList()"
        />
      </div>
    </el-card>

    <!-- 新增/编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="520px" @closed="Object.assign(form, { username: '', nickname: '', email: '', phone: '', orgId: undefined, roleIds: [] })">
      <el-form :model="form" label-width="80px">
        <el-form-item label="用户名" required v-if="!isEdit"><el-input v-model="form.username" placeholder="请输入用户名" /></el-form-item>
        <el-form-item label="昵称" required><el-input v-model="form.nickname" placeholder="请输入昵称" /></el-form-item>
        <el-form-item label="邮箱"><el-input v-model="form.email" placeholder="请输入邮箱" /></el-form-item>
        <el-form-item label="手机号"><el-input v-model="form.phone" placeholder="请输入手机号" /></el-form-item>
        <el-form-item label="组织机构">
          <el-tree-select v-model="form.orgId" :data="orgTree" check-strictly :props="{ label: 'label', value: 'id', children: 'children' }" placeholder="选择组织" clearable style="width: 100%" />
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="form.roleIds" multiple placeholder="选择角色" style="width: 100%">
            <el-option v-for="r in roleList" :key="r.id" :label="r.roleName" :value="r.id" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer><el-button @click="dialogVisible = false">取消</el-button><el-button type="primary" @click="handleSubmit">确定</el-button></template>
    </el-dialog>
  </div>
</template>