<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { createUser, deleteUser, listUsers, updateUser } from '../api/user'
import { ROLE_OPTIONS, roleLabel } from '../utils/options'

const loading = ref(false)
const saving = ref(false)
const dialogVisible = ref(false)
const editingUser = ref(null)
const users = ref([])
const total = ref(0)
const query = reactive({
  page: 1,
  size: 20,
})

const formRef = ref()
const form = reactive({
  username: '',
  password: '',
  nickname: '',
  role: 1,
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  nickname: [{ required: true, message: '请输入昵称', trigger: 'blur' }],
  role: [{ required: true, message: '请选择角色', trigger: 'change' }],
}

onMounted(loadUsers)

async function loadUsers() {
  loading.value = true
  try {
    const res = await listUsers(query)
    users.value = res.records || res
    total.value = res.total || res.length
  } catch (error) {
    ElMessage.error(error.message || '用户列表加载失败')
  } finally {
    loading.value = false
  }
}

function openCreateDialog() {
  editingUser.value = null
  Object.assign(form, {
    username: '',
    password: '',
    nickname: '',
    role: 1,
  })
  dialogVisible.value = true
}

function openEditDialog(row) {
  editingUser.value = row
  Object.assign(form, {
    username: row.username,
    password: '',
    nickname: row.nickname,
    role: row.role,
  })
  dialogVisible.value = true
}

async function saveUser() {
  await formRef.value.validate()
  if (!editingUser.value && !form.password) {
    ElMessage.warning('新增用户需要设置密码')
    return
  }
  saving.value = true
  try {
    if (editingUser.value) {
      await updateUser(editingUser.value.id, {
        password: form.password || null,
        nickname: form.nickname,
        role: form.role,
      })
      ElMessage.success('用户已更新')
    } else {
      await createUser({
        username: form.username,
        password: form.password,
        nickname: form.nickname,
        role: form.role,
      })
      ElMessage.success('用户已创建')
    }
    dialogVisible.value = false
    await loadUsers()
  } catch (error) {
    ElMessage.error(error.message || '保存失败')
  } finally {
    saving.value = false
  }
}

async function removeUser(row) {
  await ElMessageBox.confirm(`确认删除用户「${row.nickname}」吗？`, '删除用户', {
    type: 'warning',
    confirmButtonText: '删除',
    cancelButtonText: '取消',
  })
  try {
    await deleteUser(row.id)
    ElMessage.success('用户已删除')
    await loadUsers()
  } catch (error) {
    ElMessage.error(error.message || '删除失败')
  }
}
</script>

<template>
  <section class="page-stack">
    <section class="surface page-toolbar">
      <div>
        <p class="eyebrow">用户与权限</p>
        <h2>管理员用户管理</h2>
      </div>
      <div class="toolbar-actions">
        <el-button @click="loadUsers">刷新</el-button>
        <el-button type="primary" @click="openCreateDialog">新增用户</el-button>
      </div>
    </section>

    <section class="surface table-surface">
      <el-table v-loading="loading" :data="users" stripe>
        <el-table-column label="ID" prop="id" width="90" />
        <el-table-column label="用户名" prop="username" min-width="140" />
        <el-table-column label="昵称" prop="nickname" min-width="140" />
        <el-table-column label="角色" min-width="120">
          <template #default="{ row }">
            <el-tag effect="plain">{{ roleLabel(row.role) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" prop="createdAt" min-width="180" />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openEditDialog(row)">编辑</el-button>
            <el-button link type="danger" @click="removeUser(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </section>

    <el-dialog v-model="dialogVisible" :title="editingUser ? '编辑用户' : '新增用户'" width="480px">
      <el-form ref="formRef" label-width="82px" :model="form" :rules="rules">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" :disabled="Boolean(editingUser)" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="form.password" show-password type="password" placeholder="编辑时留空表示不修改" />
        </el-form-item>
        <el-form-item label="昵称" prop="nickname">
          <el-input v-model="form.nickname" />
        </el-form-item>
        <el-form-item label="角色" prop="role">
          <el-select v-model="form.role" class="full-input">
            <el-option v-for="item in ROLE_OPTIONS" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="saveUser">保存</el-button>
      </template>
    </el-dialog>
  </section>
</template>
