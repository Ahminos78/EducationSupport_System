<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listCourses } from '../api/course'
import {
  createReply,
  createTopic,
  deleteDiscussion,
  getTopic,
  listReplies,
  listTopics,
  updateDiscussion,
  updateDiscussionStatus,
} from '../api/interaction'
import { discussionStatusLabel } from '../utils/options'
import { useAuthStore } from '../stores/auth'

const authStore = useAuthStore()
const courses = ref([])
const topics = ref([])
const replies = ref([])
const selectedCourseId = ref(null)
const selectedTopic = ref(null)
const loadingCourses = ref(false)
const loadingTopics = ref(false)
const loadingReplies = ref(false)
const topicDialogVisible = ref(false)
const editDialogVisible = ref(false)
const detailVisible = ref(false)
const editingDiscussion = ref(null)

const topicFormRef = ref()
const editFormRef = ref()

const topicForm = reactive({
  title: '',
  content: '',
})

const replyForm = reactive({
  content: '',
})

const editForm = reactive({
  title: '',
  content: '',
})

const topicRules = {
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  content: [{ required: true, message: '请输入内容', trigger: 'blur' }],
}

const editRules = {
  content: [{ required: true, message: '请输入内容', trigger: 'blur' }],
}

const selectedCourse = computed(() => courses.value.find((course) => course.id === selectedCourseId.value))
const canManageSelectedCourse = computed(() => {
  if (authStore.user?.role === 3) {
    return true
  }
  return authStore.user?.role === 2 && selectedCourse.value?.teacherId === authStore.user?.id
})

onMounted(async () => {
  await loadCourses()
})

async function loadCourses() {
  loadingCourses.value = true
  try {
    const result = await listCourses({ page: 1, size: 100 })
    courses.value = result.records || result || []
    if (courses.value.length && !selectedCourseId.value) {
      selectedCourseId.value = courses.value[0].id
      await loadTopics()
    }
  } catch (error) {
    ElMessage.error(error.message || '课程加载失败')
  } finally {
    loadingCourses.value = false
  }
}

async function loadTopics() {
  if (!selectedCourseId.value) {
    topics.value = []
    return
  }
  loadingTopics.value = true
  try {
    topics.value = await listTopics(selectedCourseId.value, { page: 1, size: 50 })
  } catch (error) {
    ElMessage.error(error.message || '主题帖加载失败')
  } finally {
    loadingTopics.value = false
  }
}

function openTopicDialog() {
  Object.assign(topicForm, {
    title: '',
    content: '',
  })
  topicDialogVisible.value = true
}

async function submitTopic() {
  await topicFormRef.value.validate()
  try {
    await createTopic({
      courseId: selectedCourseId.value,
      title: topicForm.title,
      content: topicForm.content,
    })
    ElMessage.success('主题已发布')
    topicDialogVisible.value = false
    await loadTopics()
  } catch (error) {
    ElMessage.error(error.message || '发布失败')
  }
}

async function openTopicDetail(topic) {
  detailVisible.value = true
  replyForm.content = ''
  try {
    selectedTopic.value = await getTopic(topic.id)
    await loadTopicReplies()
  } catch (error) {
    ElMessage.error(error.message || '主题详情加载失败')
  }
}

async function loadTopicReplies() {
  if (!selectedTopic.value) {
    replies.value = []
    return
  }
  loadingReplies.value = true
  try {
    replies.value = await listReplies(selectedTopic.value.id, { page: 1, size: 100 })
  } catch (error) {
    ElMessage.error(error.message || '回复加载失败')
  } finally {
    loadingReplies.value = false
  }
}

async function submitReply() {
  if (!replyForm.content.trim()) {
    ElMessage.warning('请输入回复内容')
    return
  }
  try {
    await createReply(selectedTopic.value.id, {
      content: replyForm.content,
    })
    ElMessage.success('回复已发布')
    replyForm.content = ''
    await loadTopicReplies()
  } catch (error) {
    ElMessage.error(error.message || '回复失败')
  }
}

function canEdit(row) {
  return authStore.user?.role === 3 || row.authorId === authStore.user?.id || canManageSelectedCourse.value
}

function canModerate(row) {
  return authStore.user?.role === 3 || canManageSelectedCourse.value || row.authorId === authStore.user?.id
}

function openEditDialog(row) {
  editingDiscussion.value = row
  Object.assign(editForm, {
    title: row.title || '',
    content: row.content || '',
  })
  editDialogVisible.value = true
}

async function submitEdit() {
  await editFormRef.value.validate()
  try {
    await updateDiscussion(editingDiscussion.value.id, {
      title: editingDiscussion.value.parentId ? null : editForm.title,
      content: editForm.content,
    })
    ElMessage.success('内容已更新')
    editDialogVisible.value = false
    if (editingDiscussion.value.parentId) {
      await loadTopicReplies()
    } else {
      await loadTopics()
      if (selectedTopic.value?.id === editingDiscussion.value.id) {
        selectedTopic.value = await getTopic(editingDiscussion.value.id)
      }
    }
  } catch (error) {
    ElMessage.error(error.message || '更新失败')
  }
}

async function toggleStatus(row) {
  const nextStatus = row.status === 1 ? 0 : 1
  try {
    await updateDiscussionStatus(row.id, nextStatus)
    ElMessage.success(nextStatus === 1 ? '已恢复显示' : '已隐藏')
    if (row.parentId) {
      await loadTopicReplies()
    } else {
      await loadTopics()
      if (selectedTopic.value?.id === row.id) {
        selectedTopic.value = await getTopic(row.id)
      }
    }
  } catch (error) {
    ElMessage.error(error.message || '状态修改失败')
  }
}

async function removeDiscussion(row) {
  await ElMessageBox.confirm('确认删除这条讨论内容吗？', '删除讨论', {
    type: 'warning',
    confirmButtonText: '删除',
    cancelButtonText: '取消',
  })
  try {
    await deleteDiscussion(row.id)
    ElMessage.success('已删除')
    if (row.parentId) {
      await loadTopicReplies()
    } else {
      detailVisible.value = false
      selectedTopic.value = null
      await loadTopics()
    }
  } catch (error) {
    ElMessage.error(error.message || '删除失败')
  }
}
</script>

<template>
  <section class="page-stack">
    <section class="surface page-toolbar">
      <div>
        <p class="eyebrow">课程讨论</p>
        <h2>论坛讨论</h2>
      </div>
      <div class="toolbar-actions">
        <el-select
          v-model="selectedCourseId"
          :loading="loadingCourses"
          placeholder="选择课程"
          style="width: 240px"
          @change="loadTopics"
        >
          <el-option v-for="course in courses" :key="course.id" :label="course.name" :value="course.id" />
        </el-select>
        <el-button @click="loadTopics">刷新</el-button>
        <el-button type="primary" :disabled="!selectedCourseId" @click="openTopicDialog">发布主题</el-button>
      </div>
    </section>

    <section class="forum-grid">
      <section class="surface forum-course-panel">
        <p class="eyebrow">当前课程</p>
        <h3>{{ selectedCourse?.name || '请选择课程' }}</h3>
        <p class="muted">{{ selectedCourse?.description || '课程主题帖会显示在右侧列表中。' }}</p>
      </section>

      <section class="surface table-surface">
        <el-table v-loading="loadingTopics" :data="topics" stripe>
          <el-table-column label="主题" min-width="260">
            <template #default="{ row }">
              <button class="link-title" type="button" @click="openTopicDetail(row)">{{ row.title }}</button>
              <p class="table-subtitle">{{ row.content }}</p>
            </template>
          </el-table-column>
          <el-table-column label="作者ID" prop="authorId" width="100" />
          <el-table-column label="状态" width="110">
            <template #default="{ row }">
              <el-tag :type="row.status === 1 ? 'success' : 'info'" effect="plain">
                {{ discussionStatusLabel(row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="更新时间" prop="updatedAt" min-width="180" />
          <el-table-column label="操作" width="230" fixed="right">
            <template #default="{ row }">
              <el-button link type="primary" @click="openTopicDetail(row)">查看</el-button>
              <el-button v-if="canEdit(row)" link @click="openEditDialog(row)">编辑</el-button>
              <el-button v-if="canManageSelectedCourse || authStore.user?.role === 3" link @click="toggleStatus(row)">
                {{ row.status === 1 ? '隐藏' : '恢复' }}
              </el-button>
              <el-button v-if="canModerate(row)" link type="danger" @click="removeDiscussion(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </section>
    </section>

    <el-dialog v-model="topicDialogVisible" title="发布主题" width="560px">
      <el-form ref="topicFormRef" label-width="82px" :model="topicForm" :rules="topicRules">
        <el-form-item label="标题" prop="title">
          <el-input v-model="topicForm.title" />
        </el-form-item>
        <el-form-item label="内容" prop="content">
          <el-input v-model="topicForm.content" :rows="5" type="textarea" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="topicDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitTopic">发布</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="editDialogVisible" title="编辑讨论内容" width="560px">
      <el-form ref="editFormRef" label-width="82px" :model="editForm" :rules="editRules">
        <el-form-item v-if="!editingDiscussion?.parentId" label="标题">
          <el-input v-model="editForm.title" />
        </el-form-item>
        <el-form-item label="内容" prop="content">
          <el-input v-model="editForm.content" :rows="5" type="textarea" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitEdit">保存</el-button>
      </template>
    </el-dialog>

    <el-drawer v-model="detailVisible" size="560px" title="主题详情">
      <section v-if="selectedTopic" class="topic-detail">
        <div class="topic-head">
          <div>
            <h3>{{ selectedTopic.title }}</h3>
            <p class="muted">作者ID {{ selectedTopic.authorId }} · {{ discussionStatusLabel(selectedTopic.status) }}</p>
          </div>
          <div class="topic-actions">
            <el-button v-if="canEdit(selectedTopic)" link @click="openEditDialog(selectedTopic)">编辑</el-button>
            <el-button
              v-if="canManageSelectedCourse || authStore.user?.role === 3"
              link
              @click="toggleStatus(selectedTopic)"
            >
              {{ selectedTopic.status === 1 ? '隐藏' : '恢复' }}
            </el-button>
            <el-button v-if="canModerate(selectedTopic)" link type="danger" @click="removeDiscussion(selectedTopic)">
              删除
            </el-button>
          </div>
        </div>

        <p class="topic-content">{{ selectedTopic.content }}</p>

        <el-divider />

        <section class="reply-box">
          <el-input v-model="replyForm.content" :rows="3" placeholder="写下你的回复" type="textarea" />
          <el-button type="primary" @click="submitReply">回复</el-button>
        </section>

        <section v-loading="loadingReplies" class="reply-list">
          <article v-for="reply in replies" :key="reply.id" class="reply-item">
            <div class="reply-main">
              <p>{{ reply.content }}</p>
              <span>作者ID {{ reply.authorId }} · {{ reply.updatedAt }}</span>
            </div>
            <div class="reply-actions">
              <el-button v-if="canEdit(reply)" link @click="openEditDialog(reply)">编辑</el-button>
              <el-button v-if="canManageSelectedCourse || authStore.user?.role === 3" link @click="toggleStatus(reply)">
                {{ reply.status === 1 ? '隐藏' : '恢复' }}
              </el-button>
              <el-button v-if="canModerate(reply)" link type="danger" @click="removeDiscussion(reply)">删除</el-button>
            </div>
          </article>
          <el-empty v-if="!loadingReplies && !replies.length" description="暂无回复" />
        </section>
      </section>
    </el-drawer>
  </section>
</template>
