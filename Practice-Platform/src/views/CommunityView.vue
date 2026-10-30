<template>
  <div class="community-container">
    <div class="community-header">
      <h2 class="section-title">社区交流</h2>
      <p class="section-subtitle">与书法爱好者交流心得，共同进步</p>
    </div>

    <!-- 讨论区：数据来自 /api/discussions 系列端点 -->
    <div class="community-main">
      <div class="discussion-header">
        <h3>讨论区</h3>
        <el-button type="primary" @click="openCreateDialog">发布讨论</el-button>
      </div>

      <div class="search-filter">
        <el-input
          v-model="searchQuery"
          placeholder="搜索讨论内容"
          clearable
          class="search-input"
          @input="onSearchInput"
          @clear="onSearchInput"
        />
        <el-select v-model="scope" class="filter-select" @change="handleFilterChange">
          <el-option label="全部讨论" value="all" />
          <el-option label="热门讨论" value="hot" />
          <el-option label="精华讨论" value="featured" />
          <el-option label="置顶讨论" value="pinned" />
        </el-select>
        <el-select v-model="category" class="filter-select" @change="handleFilterChange">
          <el-option label="全部分类" value="all" />
          <el-option label="学习交流" value="study" />
          <el-option label="作品点评" value="review" />
          <el-option label="问题求助" value="help" />
          <el-option label="资源分享" value="share" />
        </el-select>
        <el-select
          v-model="sort"
          class="filter-select"
          :disabled="scope !== 'all'"
          @change="handleFilterChange"
        >
          <el-option label="最新发布" value="latest" />
          <el-option label="最多点赞" value="mostLiked" />
          <el-option label="最多回复" value="mostReplied" />
          <el-option label="最多浏览" value="mostViewed" />
        </el-select>
      </div>

      <el-alert
        v-if="listError"
        type="error"
        :title="listError"
        show-icon
        :closable="false"
        class="state-alert"
      >
        <el-button size="small" type="primary" @click="fetchDiscussions">重试</el-button>
      </el-alert>

      <div v-loading="loading" class="discussions-list">
        <div v-for="discussion in discussions" :key="discussion.id" class="discussion-item">
          <div class="discussion-content">
            <div class="discussion-title-row">
              <h4 class="discussion-title">{{ discussion.title }}</h4>
              <el-tag v-if="discussion.isPinned" type="danger" size="small">置顶</el-tag>
              <el-tag v-if="discussion.isFeatured" type="warning" size="small">精华</el-tag>
            </div>
            <p class="discussion-excerpt">{{ truncateText(discussion.content, 100) }}</p>

            <div v-if="parseTags(discussion.tags).length" class="discussion-tags">
              <el-tag
                v-for="tag in parseTags(discussion.tags)"
                :key="tag"
                type="info"
                size="small"
                class="tag-item"
                @click="searchByTag(tag)"
              >
                {{ tag }}
              </el-tag>
            </div>

            <div class="discussion-meta">
              <div class="author-info">
                <el-avatar :size="36" :src="discussion.userAvatar">
                  {{ initialOf(discussion.username) }}
                </el-avatar>
                <span class="author-name">{{ discussion.username || '匿名书友' }}</span>
                <span class="post-time">{{ formatDate(discussion.createdAt) }}</span>
              </div>

              <div class="discussion-stats">
                <span class="stat-item">
                  <el-icon><View /></el-icon>
                  {{ discussion.viewCount ?? 0 }}
                </span>
                <span
                  class="stat-item"
                  role="button"
                  tabindex="0"
                  @click="openDetail(discussion)"
                  @keydown.enter="openDetail(discussion)"
                  @keydown.space.prevent="openDetail(discussion)"
                >
                  <el-icon><ChatDotRound /></el-icon>
                  {{ discussion.replyCount ?? 0 }}
                </span>
                <span
                  class="stat-item"
                  :class="{ active: isLiked(discussion) }"
                  role="button"
                  tabindex="0"
                  @click="handleLike(discussion)"
                  @keydown.enter="handleLike(discussion)"
                  @keydown.space.prevent="handleLike(discussion)"
                >
                  <el-icon><StarFilled /></el-icon>
                  {{ discussion.likeCount ?? 0 }}
                </span>
              </div>
            </div>

            <div class="discussion-actions">
              <el-button size="small" @click="openDetail(discussion)">查看回复</el-button>
              <el-button v-if="isOwnDiscussion(discussion)" size="small" @click="openEditDialog(discussion)">
                编辑
              </el-button>
              <el-button
                v-if="isOwnDiscussion(discussion)"
                size="small"
                type="danger"
                @click="handleDeleteDiscussion(discussion)"
              >
                删除
              </el-button>
            </div>
          </div>
        </div>

        <el-empty
          v-if="!loading && !listError && discussions.length === 0"
          description="暂无讨论"
        />
      </div>

      <div v-if="totalDiscussions > pageSize" class="pagination">
        <el-pagination
          v-model:current-page="page"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="totalDiscussions"
          @size-change="handleSizeChange"
          @current-change="handlePageChange"
        />
      </div>
    </div>

    <!-- 右侧边栏 -->
    <aside class="community-sidebar">
      <!-- 置顶讨论：GET /api/discussions/pinned -->
      <div class="sidebar-section">
        <h4 class="section-header">置顶讨论</h4>
        <el-alert
          v-if="pinnedError"
          type="error"
          :title="pinnedError"
          show-icon
          :closable="false"
        />
        <div v-loading="pinnedLoading" class="announcement-list">
          <div
            v-for="item in pinnedDiscussions"
            :key="item.id"
            class="announcement-item"
            role="button"
            tabindex="0"
            @click="openDetail(item)"
            @keydown.enter="openDetail(item)"
            @keydown.space.prevent="openDetail(item)"
          >
            <p class="announcement-title">{{ item.title }}</p>
            <span class="announcement-date">{{ formatDate(item.createdAt) }}</span>
          </div>
          <el-empty
            v-if="!pinnedLoading && !pinnedError && pinnedDiscussions.length === 0"
            description="暂无置顶讨论"
            :image-size="60"
          />
        </div>
      </div>

      <!-- 活跃用户：由当前页的讨论作者聚合而来 -->
      <div class="sidebar-section">
        <h4 class="section-header">本页活跃用户</h4>
        <div class="active-users">
          <div v-for="user in activeUsers" :key="user.id" class="user-item">
            <el-avatar :size="32" :src="user.avatar">{{ initialOf(user.name) }}</el-avatar>
            <div class="user-info">
              <div class="user-name">{{ user.name }}</div>
              <div class="user-rank">本页发帖 {{ user.posts }} 篇</div>
            </div>
          </div>
          <el-empty v-if="activeUsers.length === 0" description="暂无数据" :image-size="60" />
        </div>
      </div>

      <!-- 热门标签：由当前页讨论的 tags 聚合而来 -->
      <div class="sidebar-section">
        <h4 class="section-header">本页热门标签</h4>
        <div class="tags-list">
          <el-tag v-for="tag in popularTags" :key="tag.name" @click="searchByTag(tag.name)">
            {{ tag.name }} ({{ tag.count }})
          </el-tag>
          <el-empty v-if="popularTags.length === 0" description="暂无标签" :image-size="60" />
        </div>
      </div>
    </aside>

    <!-- 讨论详情与回复 -->
    <el-dialog v-model="detailVisible" :title="detail?.title || '讨论详情'" width="720px">
      <div v-loading="detailLoading" class="detail-body">
        <el-alert
          v-if="detailError"
          type="error"
          :title="detailError"
          show-icon
          :closable="false"
        />
        <template v-else-if="detail">
          <p class="detail-content">{{ detail.content }}</p>
          <div class="detail-meta">
            <span class="author-name">{{ detail.username || '匿名书友' }}</span>
            <span class="post-time">{{ formatDate(detail.createdAt) }}</span>
            <span class="stat-item">浏览 {{ detail.viewCount ?? 0 }}</span>
            <span class="stat-item">点赞 {{ detail.likeCount ?? 0 }}</span>
            <span class="stat-item">回复 {{ detail.replyCount ?? 0 }}</span>
          </div>
        </template>
        <el-empty v-else-if="!detailLoading" description="暂无详情" />
      </div>

      <div class="reply-section">
        <h4 class="reply-header">回复（{{ replyTotal }}）</h4>
        <el-alert
          v-if="replyError"
          type="error"
          :title="replyError"
          show-icon
          :closable="false"
        />
        <div v-loading="replyLoading" class="reply-list">
          <div v-for="reply in replies" :key="reply.id" class="reply-item">
            <div class="reply-head">
              <el-avatar :size="28" :src="reply.userAvatar">
                {{ initialOf(reply.username) }}
              </el-avatar>
              <span class="reply-author">{{ reply.username || '匿名书友' }}</span>
              <span class="post-time">{{ formatDate(reply.createdAt) }}</span>
            </div>

            <el-input
              v-if="editingReplyId === reply.id"
              v-model="editingReplyContent"
              type="textarea"
              :rows="2"
              maxlength="1000"
              show-word-limit
            />
            <p v-else class="reply-content">{{ reply.content }}</p>

            <div class="reply-actions">
              <template v-if="editingReplyId === reply.id">
                <el-button size="small" type="primary" @click="submitReplyEdit(reply)">保存</el-button>
                <el-button size="small" @click="cancelReplyEdit">取消</el-button>
              </template>
              <el-button v-else-if="isOwnReply(reply)" size="small" @click="startReplyEdit(reply)">
                编辑
              </el-button>
              <el-button
                v-if="isOwnReply(reply) && editingReplyId !== reply.id"
                size="small"
                type="danger"
                @click="handleDeleteReply(reply)"
              >
                删除
              </el-button>
            </div>
          </div>

          <el-empty
            v-if="!replyLoading && !replyError && replies.length === 0"
            description="暂无回复"
          />
        </div>

        <el-button
          v-if="replies.length < replyTotal"
          size="small"
          class="load-more"
          :loading="replyLoading"
          @click="loadMoreReplies"
        >
          加载更多回复
        </el-button>

        <div class="reply-editor">
          <el-input
            v-model="replyDraft"
            type="textarea"
            :rows="3"
            maxlength="1000"
            show-word-limit
            placeholder="写下你的回复…"
          />
          <el-button
            type="primary"
            :loading="replySubmitting"
            @click="submitReply"
          >
            提交回复
          </el-button>
        </div>
      </div>
    </el-dialog>

    <!-- 发布 / 编辑讨论 -->
    <el-dialog
      v-model="editorVisible"
      :title="editingId ? '编辑讨论' : '发布讨论'"
      width="600px"
      :before-close="closeEditor"
    >
      <el-form :model="postForm" label-width="80px">
        <el-form-item label="标题" prop="title">
          <el-input
            v-model="postForm.title"
            maxlength="100"
            show-word-limit
            placeholder="请输入讨论标题"
          />
        </el-form-item>

        <el-form-item label="类别" prop="category">
          <el-select v-model="postForm.category" placeholder="请选择讨论类别">
            <el-option label="学习交流" value="study" />
            <el-option label="作品点评" value="review" />
            <el-option label="问题求助" value="help" />
            <el-option label="资源分享" value="share" />
          </el-select>
        </el-form-item>

        <el-form-item label="内容" prop="content">
          <el-input
            v-model="postForm.content"
            type="textarea"
            :rows="6"
            maxlength="2000"
            show-word-limit
            placeholder="请输入讨论内容"
          />
        </el-form-item>

        <el-form-item label="标签">
          <!-- el-tag-input 是 Element Plus 1.x 的组件名，2.x 已经没有了
               （之前靠全量注册也解析不到，标签输入框其实一直是空的）。
               2.x 对应的是 el-input-tag。 -->
          <el-input-tag v-model="postFormTags" placeholder="请输入标签，回车确认" />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="closeEditor">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitPost">提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { View, ChatDotRound, StarFilled } from '@element-plus/icons-vue'
import { useUserStore } from '../stores/userStore'
import { formatDate as formatDateTime, truncateText } from '../utils'
import {
  getDiscussions,
  getDiscussion,
  createDiscussion,
  updateDiscussion,
  deleteDiscussion,
  likeDiscussion,
  getDiscussionReplies,
  replyToDiscussion,
  updateDiscussionReply,
  deleteDiscussionReply,
  getHotDiscussions,
  getFeaturedDiscussions,
  getPinnedDiscussions,
  searchDiscussions
} from '../api/community'

const REPLY_PAGE_SIZE = 20

// 状态管理
const userStore = useUserStore()
const router = useRouter()

// 讨论列表
const discussions = ref([])
const totalDiscussions = ref(0)
const loading = ref(false)
const listError = ref('')

// 筛选条件
const scope = ref('all')
const category = ref('all')
const sort = ref('latest')
const searchQuery = ref('')
const page = ref(1)
const pageSize = ref(10)

// 置顶讨论（侧栏）
const pinnedDiscussions = ref([])
const pinnedLoading = ref(false)
const pinnedError = ref('')

// 详情与回复
const detailVisible = ref(false)
const detailLoading = ref(false)
const detailError = ref('')
const detail = ref(null)
const replies = ref([])
const replyTotal = ref(0)
const replyLoading = ref(false)
const replyError = ref('')
const replyDraft = ref('')
const replySubmitting = ref(false)
const replyPage = ref(0)
const editingReplyId = ref('')
const editingReplyContent = ref('')

// 发布 / 编辑
const editorVisible = ref(false)
const submitting = ref(false)
const editingId = ref('')
const postForm = reactive({
  title: '',
  category: '',
  content: ''
})
const postFormTags = ref([])

const isLoggedIn = computed(() => userStore.isLoggedIn)
const currentUserId = computed(() => userStore.userInfo?.id || '')

// 本页活跃用户：由当前页讨论的作者聚合
const activeUsers = computed(() => {
  const counter = new Map()
  discussions.value.forEach((discussion) => {
    const name = discussion.username
    if (!name) return
    if (!counter.has(name)) {
      counter.set(name, { id: name, name, avatar: discussion.userAvatar || '', posts: 0 })
    }
    counter.get(name).posts += 1
  })
  return [...counter.values()].sort((a, b) => b.posts - a.posts).slice(0, 5)
})

// 本页热门标签：由当前页讨论的 tags 聚合
const popularTags = computed(() => {
  const counter = new Map()
  discussions.value.forEach((discussion) => {
    parseTags(discussion.tags).forEach((tag) => {
      counter.set(tag, (counter.get(tag) || 0) + 1)
    })
  })
  return [...counter.entries()]
    .map(([name, count]) => ({ name, count }))
    .sort((a, b) => b.count - a.count)
    .slice(0, 10)
})

// 工具函数
// 后端没给的时间字段显示为 '-'，不让 formatDate 算出 NaN-NaN-NaN
const formatDate = (value) => (value ? formatDateTime(value) : '-')

const parseTags = (tags) => {
  if (!tags) return []
  return String(tags)
    .split(',')
    .map((tag) => tag.trim())
    .filter(Boolean)
}

const initialOf = (name) => (name ? String(name).charAt(0).toUpperCase() : '?')

const isOwnDiscussion = (discussion) =>
  Boolean(currentUserId.value) && discussion.userId === currentUserId.value

const isOwnReply = (reply) => Boolean(currentUserId.value) && reply.userId === currentUserId.value

// 获取讨论列表：GET /api/discussions（含 hot / featured / pinned / search 变体）
const fetchDiscussions = async () => {
  loading.value = true
  listError.value = ''
  try {
    const params = { page: page.value - 1, size: pageSize.value }
    if (category.value !== 'all') {
      params.category = category.value
    }
    const keyword = searchQuery.value.trim()
    let payload
    if (keyword) {
      payload = await searchDiscussions(keyword, params)
    } else if (scope.value === 'hot') {
      payload = await getHotDiscussions(params)
    } else if (scope.value === 'featured') {
      payload = await getFeaturedDiscussions(params)
    } else if (scope.value === 'pinned') {
      payload = await getPinnedDiscussions(params)
    } else {
      params.sort = sort.value
      payload = await getDiscussions(params)
    }

    discussions.value = Array.isArray(payload?.content) ? payload.content : []
    totalDiscussions.value = payload?.total ?? discussions.value.length
  } catch (error) {
    discussions.value = []
    totalDiscussions.value = 0
    listError.value = error?.message || '获取讨论列表失败'
    ElMessage.error(listError.value)
    console.error('获取讨论列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 置顶讨论：GET /api/discussions/pinned
const fetchPinnedDiscussions = async () => {
  pinnedLoading.value = true
  pinnedError.value = ''
  try {
    const payload = await getPinnedDiscussions({ page: 0, size: 5 })
    pinnedDiscussions.value = Array.isArray(payload?.content) ? payload.content : []
  } catch (error) {
    pinnedDiscussions.value = []
    pinnedError.value = error?.message || '获取置顶讨论失败'
    ElMessage.error(pinnedError.value)
    console.error('获取置顶讨论失败:', error)
  } finally {
    pinnedLoading.value = false
  }
}

// 筛选变化
const handleFilterChange = () => {
  page.value = 1
  fetchDiscussions()
}

const handlePageChange = () => {
  fetchDiscussions()
}

const handleSizeChange = () => {
  page.value = 1
  fetchDiscussions()
}

// 搜索输入防抖
let searchTimer = null
const onSearchInput = () => {
  clearTimeout(searchTimer)
  searchTimer = setTimeout(() => {
    page.value = 1
    fetchDiscussions()
  }, 500)
}

// 按标签搜索
const searchByTag = (tagName) => {
  searchQuery.value = tagName
  page.value = 1
  fetchDiscussions()
}

// 打开讨论详情：GET /api/discussions/{id}
const openDetail = async (discussion) => {
  detailVisible.value = true
  detail.value = null
  detailError.value = ''
  detailLoading.value = true
  replies.value = []
  replyTotal.value = 0
  replyError.value = ''
  replyPage.value = 0

  try {
    detail.value = await getDiscussion(discussion.id)
  } catch (error) {
    detailError.value = error?.message || '获取讨论详情失败'
    ElMessage.error(detailError.value)
    console.error('获取讨论详情失败:', error)
  } finally {
    detailLoading.value = false
  }

  await loadReplies(true)
}

// 讨论回复列表：GET /api/discussions/{discussionId}/replies
const loadReplies = async (reset = false) => {
  if (!detail.value) return
  replyLoading.value = true
  replyError.value = ''
  try {
    const payload = await getDiscussionReplies(detail.value.id, {
      page: replyPage.value,
      size: REPLY_PAGE_SIZE
    })
    const content = Array.isArray(payload?.content) ? payload.content : []
    replies.value = reset ? content : [...replies.value, ...content]
    replyTotal.value = payload?.total ?? replies.value.length
  } catch (error) {
    if (reset) {
      replies.value = []
    }
    replyError.value = error?.message || '获取回复列表失败'
    ElMessage.error(replyError.value)
    console.error('获取回复列表失败:', error)
  } finally {
    replyLoading.value = false
  }
}

const loadMoreReplies = () => {
  replyPage.value += 1
  loadReplies(false)
}

// 点赞态来自服务端：契约的 Discussion.liked 是「当前登录用户是否已赞」，
// 未登录时为 false。**不要**在本地存一份 —— 刷新后靠列表接口返回的 liked 恢复。
const isLiked = (discussion) => Boolean(discussion?.liked)

// 点赞 / 取消点赞：POST /api/discussions/{id}/like
const handleLike = async (discussion) => {
  if (!isLoggedIn.value) {
    ElMessage.warning('请先登录后再点赞')
    router.push('/login')
    return
  }

  try {
    const result = await likeDiscussion(discussion.id)
    // 用接口返回的 liked / likeCount 覆盖，本地不做任何持久化
    discussion.liked = Boolean(result?.liked)
    discussion.likeCount = result?.likeCount ?? discussion.likeCount ?? 0
    // 详情弹窗里是打开时的那份快照，同步一下高亮与计数
    if (detail.value && detail.value.id === discussion.id) {
      detail.value.liked = discussion.liked
      detail.value.likeCount = discussion.likeCount
    }
    ElMessage.success(discussion.liked ? '点赞成功' : '已取消点赞')
  } catch (error) {
    ElMessage.error(error?.message || '点赞失败')
    console.error('点赞失败:', error)
  }
}

// 回复讨论：POST /api/discussions/{discussionId}/replies
const submitReply = async () => {
  const content = replyDraft.value.trim()
  if (!content) {
    ElMessage.warning('回复内容不能为空')
    return
  }
  if (!isLoggedIn.value) {
    ElMessage.warning('请先登录后再回复')
    router.push('/login')
    return
  }
  if (!detail.value) {
    ElMessage.warning('讨论详情加载失败，无法回复')
    return
  }

  replySubmitting.value = true
  try {
    await replyToDiscussion(detail.value.id, { content })
    replyDraft.value = ''
    ElMessage.success('回复成功')
    await loadReplies(true)
    await fetchDiscussions()
  } catch (error) {
    ElMessage.error(error?.message || '回复失败')
    console.error('回复失败:', error)
  } finally {
    replySubmitting.value = false
  }
}

// 编辑回复：PUT /api/discussions/replies/{replyId}
const startReplyEdit = (reply) => {
  editingReplyId.value = reply.id
  editingReplyContent.value = reply.content || ''
}

const cancelReplyEdit = () => {
  editingReplyId.value = ''
  editingReplyContent.value = ''
}

const submitReplyEdit = async (reply) => {
  const content = editingReplyContent.value.trim()
  if (!content) {
    ElMessage.warning('回复内容不能为空')
    return
  }

  try {
    await updateDiscussionReply(reply.id, { content })
    ElMessage.success('回复已更新')
    cancelReplyEdit()
    await loadReplies(true)
  } catch (error) {
    ElMessage.error(error?.message || '更新回复失败')
    console.error('更新回复失败:', error)
  }
}

// 删除回复：DELETE /api/discussions/replies/{replyId}
const handleDeleteReply = async (reply) => {
  try {
    await ElMessageBox.confirm('确定要删除这条回复吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
  } catch {
    return
  }

  try {
    await deleteDiscussionReply(reply.id)
    ElMessage.success('回复已删除')
    await loadReplies(true)
    await fetchDiscussions()
  } catch (error) {
    ElMessage.error(error?.message || '删除回复失败')
    console.error('删除回复失败:', error)
  }
}

// 打开发布对话框：POST /api/discussions
const openCreateDialog = () => {
  if (!isLoggedIn.value) {
    ElMessage.warning('请先登录后再发布讨论')
    router.push('/login')
    return
  }

  editingId.value = ''
  postForm.title = ''
  postForm.category = ''
  postForm.content = ''
  postFormTags.value = []
  editorVisible.value = true
}

// 打开编辑对话框：PUT /api/discussions/{id}
const openEditDialog = (discussion) => {
  editingId.value = discussion.id
  postForm.title = discussion.title || ''
  postForm.category = discussion.category || ''
  postForm.content = discussion.content || ''
  postFormTags.value = parseTags(discussion.tags)
  editorVisible.value = true
}

const closeEditor = () => {
  editorVisible.value = false
  editingId.value = ''
  postForm.title = ''
  postForm.content = ''
  postForm.category = ''
  postFormTags.value = []
}

// 提交讨论（新建 / 编辑）
const submitPost = async () => {
  if (!postForm.title.trim() || !postForm.content.trim()) {
    ElMessage.warning('请填写标题和内容')
    return
  }

  submitting.value = true
  try {
    const payload = {
      title: postForm.title.trim(),
      content: postForm.content.trim(),
      category: postForm.category,
      tags: postFormTags.value.join(',')
    }

    if (editingId.value) {
      await updateDiscussion(editingId.value, payload)
      ElMessage.success('讨论已更新')
    } else {
      await createDiscussion(payload)
      ElMessage.success('讨论发布成功')
    }

    closeEditor()
    page.value = 1
    await fetchDiscussions()
    await fetchPinnedDiscussions()
  } catch (error) {
    ElMessage.error(error?.message || (editingId.value ? '更新讨论失败' : '讨论发布失败'))
    console.error('提交讨论失败:', error)
  } finally {
    submitting.value = false
  }
}

// 删除讨论：DELETE /api/discussions/{id}
const handleDeleteDiscussion = async (discussion) => {
  try {
    await ElMessageBox.confirm('确定要删除这条讨论吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
  } catch {
    return
  }

  try {
    await deleteDiscussion(discussion.id)
    ElMessage.success('讨论已删除')
    if (discussions.value.length === 1 && page.value > 1) {
      page.value -= 1
    }
    await fetchDiscussions()
  } catch (error) {
    ElMessage.error(error?.message || '删除讨论失败')
    console.error('删除讨论失败:', error)
  }
}

onMounted(() => {
  fetchDiscussions()
  fetchPinnedDiscussions()
})
</script>

<style scoped>
/* ============================================================
   社区页版式
   - 页面容器与全站内容列共用一条左右边界
   - 间距刻度：4 / 8 / 12 / 16 / 24 / 32 / 48，组内小、组间大
   ============================================================ */
.community-container {
  --sp-1: 4px;
  --sp-2: 8px;
  --sp-3: 12px;
  --sp-4: 16px;
  --sp-5: 24px;
  --sp-6: 32px;
  --sp-7: 48px;

  display: grid;
  grid-template-columns: minmax(0, 1fr) clamp(240px, 22vw, 320px);
  gap: var(--sp-5);
  width: 100%;
  max-width: 1200px;
  margin-inline: auto;
  padding-block: var(--sp-2);
  align-items: start;
}

/* 页头：左对齐 + 墨线 */
.community-header {
  grid-column: 1 / -1;
  padding-left: var(--sp-3);
  border-left: 3px solid var(--primary-color);
  margin-bottom: var(--sp-2);
}

.section-title {
  font-size: clamp(1.5rem, 1.2rem + 1vw, 2rem);
  color: #333;
  font-family: 'SimSun', serif;
  letter-spacing: 0.08em;
  margin-bottom: var(--sp-1);
}

.section-subtitle {
  color: var(--el-text-color-secondary);
}

/* 社区内容区域 */
.community-main {
  background-color: rgba(255, 255, 255, 0.95);
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(90, 60, 30, 0.08);
  padding: var(--sp-5);
  min-width: 0;
}

/* 讨论区样式 */
.discussion-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: var(--sp-3);
  margin-bottom: var(--sp-4);
  padding-bottom: var(--sp-2);
  border-bottom: 1px solid rgba(139, 69, 19, 0.14);
}

.discussion-header h3 {
  margin: 0;
  color: #333;
  font-family: 'SimSun', serif;
  font-size: 1.05rem;
  letter-spacing: 0.06em;
}

.search-filter {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: var(--sp-3);
  margin-bottom: var(--sp-5);
}

.search-input {
  flex: 1 1 220px;
  min-width: 0;
  max-width: 280px;
}

.filter-select {
  flex: 0 0 140px;
  width: 140px;
}

.state-alert {
  margin-bottom: var(--sp-4);
}

.discussions-list {
  display: flex;
  flex-direction: column;
  gap: var(--sp-3);
  min-height: 120px;
}

.discussion-item {
  padding: var(--sp-4);
  background-color: rgba(243, 236, 231, 0.5);
  border: 1px solid rgba(139, 69, 19, 0.1);
  border-radius: 8px;
  transition: background-color 0.3s, border-color 0.3s;
}

.discussion-item:hover {
  background-color: rgba(240, 231, 224, 0.8);
}

.discussion-title-row {
  display: flex;
  align-items: center;
  gap: var(--sp-2);
  margin-bottom: var(--sp-2);
  flex-wrap: wrap;
}

.discussion-title {
  font-size: 1.1rem;
  margin: 0;
  color: #333;
  font-family: 'SimSun', serif;
}

.discussion-excerpt {
  color: var(--el-text-color-secondary);
  margin-bottom: var(--sp-3);
  line-height: 1.7;
}

.discussion-tags {
  display: flex;
  flex-wrap: wrap;
  gap: var(--sp-2);
  margin-bottom: var(--sp-3);
}

.tag-item {
  cursor: pointer;
}

.discussion-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: var(--sp-2) var(--sp-4);
  flex-wrap: wrap;
}

.author-info {
  display: flex;
  align-items: center;
  gap: var(--sp-3);
  min-width: 0;
}

.author-name {
  font-weight: 500;
  color: #333;
}

.post-time {
  color: var(--el-text-color-secondary);
  font-size: 0.9rem;
}

.discussion-stats {
  display: flex;
  gap: var(--sp-4);
  color: var(--el-text-color-secondary);
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 0.3rem;
  cursor: pointer;
}

/* 触摸设备：点赞 / 回复数也是可点的，抬到 44px 才点得中 */
@media (pointer: coarse) {
  .stat-item {
    min-height: 44px;
  }

  .tag-item {
    min-height: 44px;
    display: inline-flex;
    align-items: center;
  }
}

/* 选中态用站点的朱砂色：Element Plus 默认红 #f56c6c 在白底只有 3.0:1 */
.stat-item.active {
  color: var(--el-color-danger);
}

.discussion-actions {
  display: flex;
  gap: var(--sp-2);
  margin-top: var(--sp-3);
}

/* 侧栏 */
.community-sidebar {
  background-color: rgba(255, 255, 255, 0.95);
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(90, 60, 30, 0.08);
  padding: var(--sp-5);
  min-width: 0;
}

.sidebar-section {
  margin-bottom: var(--sp-5);
}

.sidebar-section:last-child {
  margin-bottom: 0;
}

/* 侧栏分区标题：左侧墨线 */
.section-header {
  margin: 0 0 var(--sp-3);
  color: #333;
  font-family: 'SimSun', serif;
  font-size: 1rem;
  letter-spacing: 0.04em;
  border-left: 3px solid var(--primary-color);
  padding-left: var(--sp-2);
}

.announcement-list {
  display: flex;
  flex-direction: column;
  gap: var(--sp-2);
  min-height: 60px;
}

.announcement-item {
  padding: var(--sp-3);
  background-color: rgba(243, 236, 231, 0.5);
  border-radius: 6px;
  cursor: pointer;
  transition: background-color 0.3s;
}

.announcement-item:hover {
  background-color: rgba(240, 231, 224, 0.8);
}

.announcement-title {
  margin: 0 0 var(--sp-1);
  color: #333;
  font-size: 0.92rem;
}

.announcement-date {
  color: var(--el-text-color-secondary);
  font-size: 0.82rem;
}

.active-users {
  display: flex;
  flex-direction: column;
  gap: var(--sp-3);
}

.user-item {
  display: flex;
  align-items: center;
  gap: var(--sp-3);
}

.user-name {
  color: #333;
  font-size: 0.92rem;
}

.user-rank {
  color: var(--el-text-color-secondary);
  font-size: 0.8rem;
}

.tags-list {
  display: flex;
  flex-wrap: wrap;
  gap: var(--sp-2);
}

/* 详情与回复 */
.detail-body {
  min-height: 80px;
}

.detail-content {
  color: #333;
  line-height: 1.7;
  white-space: pre-wrap;
}

.detail-meta {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: var(--sp-4);
  color: var(--el-text-color-secondary);
  margin-top: var(--sp-4);
  font-size: 0.9rem;
}

.reply-section {
  margin-top: var(--sp-5);
  border-top: 1px solid #ebeef5;
  padding-top: var(--sp-4);
}

.reply-header {
  margin: 0 0 var(--sp-3);
  color: #333;
}

.reply-list {
  display: flex;
  flex-direction: column;
  gap: var(--sp-3);
  min-height: 60px;
  max-height: 360px;
  overflow-y: auto;
}

.reply-item {
  padding: var(--sp-3);
  background-color: rgba(243, 236, 231, 0.5);
  border-radius: 6px;
}

.reply-head {
  display: flex;
  align-items: center;
  gap: var(--sp-2);
  margin-bottom: var(--sp-2);
  flex-wrap: wrap;
}

.reply-author {
  color: #333;
  font-weight: 500;
}

.reply-content {
  color: #555;
  margin: 0 0 var(--sp-2);
  line-height: 1.6;
  white-space: pre-wrap;
}

.reply-actions {
  display: flex;
  gap: var(--sp-2);
}

.load-more {
  margin-top: var(--sp-3);
}

.reply-editor {
  margin-top: var(--sp-4);
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: var(--sp-3);
}

/* 分页 */
.pagination {
  margin-top: var(--sp-5);
  display: flex;
  justify-content: flex-end;
  flex-wrap: wrap;
}

@media (max-width: 900px) {
  .community-container {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .community-main,
  .community-sidebar {
    padding: var(--sp-4);
  }

  .filter-select {
    flex: 1 1 130px;
    width: auto;
  }

  .discussion-stats {
    gap: var(--sp-3);
  }

  .pagination {
    justify-content: center;
  }

  .pagination :deep(.el-pagination) {
    flex-wrap: wrap;
    row-gap: var(--sp-2);
    justify-content: center;
  }

  .pagination :deep(.el-pagination__jump),
  .pagination :deep(.el-pagination__sizes) {
    margin-left: 0;
  }
}

/* 系统开启「减少动态效果」时：保留状态反馈，去掉位移 */
@media (prefers-reduced-motion: reduce) {
  .discussion-item:hover,
  .announcement-item:hover {
    transform: none !important;
  }
}
</style>
