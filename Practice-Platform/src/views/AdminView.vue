<template>
  <div class="admin-container">
    <!-- 侧边栏导航 -->
    <div class="admin-sidebar">
      <div class="sidebar-header">
        <h2>管理中心</h2>
      </div>

      <nav class="sidebar-nav">
        <div
          class="nav-item"
          :class="{ active: activeTab === 'works' }"
          @click="setActiveTab('works')"
        >
          <i class="el-icon-picture"></i>
          <span>作品审核</span>
        </div>

        <div
          class="nav-item"
          :class="{ active: activeTab === 'reviews' }"
          @click="setActiveTab('reviews')"
        >
          <i class="el-icon-document-checked"></i>
          <span>审核记录</span>
        </div>

        <div
          class="nav-item"
          :class="{ active: activeTab === 'comments' }"
          @click="setActiveTab('comments')"
        >
          <i class="el-icon-chat-dot-round"></i>
          <span>评论管理</span>
        </div>

        <div
          class="nav-item"
          :class="{ active: activeTab === 'users' }"
          @click="setActiveTab('users')"
        >
          <i class="el-icon-user"></i>
          <span>用户查询</span>
        </div>

        <div
          class="nav-item"
          :class="{ active: activeTab === 'analytics' }"
          @click="setActiveTab('analytics')"
        >
          <i class="el-icon-data-line"></i>
          <span>数据分析</span>
        </div>
      </nav>

      <div class="sidebar-footer">
        <div class="admin-info">
          <el-avatar :size="32" :src="getUserAvatar">{{ initialOf(getUsername) }}</el-avatar>
          <span>{{ getUsername }}</span>
        </div>
        <el-button type="danger" plain @click="logout" style="width: 100%; margin-top: 10px;">
          退出登录
        </el-button>
      </div>
    </div>

    <!-- 主内容区 -->
    <div class="admin-main">
      <!-- 顶部导航栏 -->
      <div class="main-header">
        <div class="header-title">
          <h3>{{ getTabTitle(activeTab) }}</h3>
        </div>

        <div class="header-info">
          <div class="user-info">
            <el-avatar :size="40" :src="getUserAvatar">{{ initialOf(getUsername) }}</el-avatar>
            <span>{{ getUsername }}</span>
          </div>
          <div class="current-time">
            {{ formattedDateTime }}
          </div>
        </div>
      </div>

      <!-- 内容区域 -->
      <div class="content-area">
        <!-- 作品审核：GET /api/works + POST /api/admin/file-reviews -->
        <div v-if="activeTab === 'works'" class="tab-content">
          <div class="content-header">
            <h4>作品列表</h4>
            <div class="header-actions">
              <el-input
                v-model="workSearchQuery"
                placeholder="在当前页筛选作品"
                prefix-icon="el-icon-search"
                clearable
                style="width: 220px; margin-right: 10px;"
              />
              <el-button type="primary" :loading="worksLoading" @click="fetchWorks">刷新</el-button>
            </div>
          </div>
          <p class="data-hint">
            作品来自公开画廊 GET /api/works，只包含已发布且已通过审核的作品。
          </p>

          <el-alert
            v-if="worksError"
            type="error"
            :title="worksError"
            show-icon
            :closable="false"
            class="state-alert"
          >
            <el-button size="small" type="primary" @click="fetchWorks">重试</el-button>
          </el-alert>

          <div v-loading="worksLoading" class="data-table">
            <el-table :data="filteredWorks" style="width: 100%">
              <el-table-column prop="id" label="作品ID" width="100" />
              <el-table-column label="作品图片" width="100">
                <template #default="scope">
                  <el-image
                    v-if="scope.row.imageUrl"
                    :src="scope.row.imageUrl"
                    fit="cover"
                    class="work-thumbnail-small"
                  >
                    <template #error>
                      <div class="image-fallback">加载失败</div>
                    </template>
                  </el-image>
                  <div v-else class="image-fallback">暂无图片</div>
                </template>
              </el-table-column>
              <el-table-column prop="title" label="作品标题" />
              <el-table-column prop="username" label="作者" />
              <el-table-column prop="category" label="分类" />
              <el-table-column label="审核状态" width="110">
                <template #default="scope">
                  <el-tag :type="getReviewStatusType(scope.row.reviewStatus)">
                    {{ getReviewStatusText(scope.row.reviewStatus) }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column label="作品状态" width="100">
                <template #default="scope">
                  <el-tag :type="getWorkStatusType(scope.row.status)">
                    {{ getWorkStatusText(scope.row.status) }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column label="上传时间" width="160">
                <template #default="scope">
                  {{ formatDate(scope.row.createdAt) }}
                </template>
              </el-table-column>
              <el-table-column label="浏览量" width="90" prop="viewCount" />
              <el-table-column label="操作" width="210" fixed="right">
                <template #default="scope">
                  <el-button size="small" type="success" @click="reviewWork(scope.row, 'APPROVED')">
                    通过
                  </el-button>
                  <el-button size="small" type="danger" @click="reviewWork(scope.row, 'REJECTED')">
                    拒绝
                  </el-button>
                  <el-button
                    size="small"
                    type="warning"
                    @click="reviewWork(scope.row, 'NEEDS_REVISION')"
                  >
                    需修改
                  </el-button>
                </template>
              </el-table-column>
            </el-table>

            <el-empty
              v-if="!worksLoading && !worksError && filteredWorks.length === 0"
              description="暂无作品"
            />
          </div>

          <div v-if="worksTotal > worksPageSize" class="pagination">
            <el-pagination
              v-model:current-page="worksPage"
              v-model:page-size="worksPageSize"
              :page-sizes="[10, 20, 50]"
              layout="total, sizes, prev, pager, next, jumper"
              :total="worksTotal"
              @size-change="handleWorksSizeChange"
              @current-change="handleWorksPageChange"
            />
          </div>
        </div>

        <!-- 审核记录：/api/admin/file-reviews 全套 -->
        <div v-if="activeTab === 'reviews'" class="tab-content">
          <div class="content-header">
            <h4>我提交的审核记录</h4>
            <div class="header-actions">
              <el-input
                v-model="lookupWorkId"
                placeholder="按作品 ID 查询"
                clearable
                style="width: 180px; margin-right: 10px;"
              />
              <el-button type="primary" plain @click="lookupReviewByWork">查询</el-button>
              <el-button type="warning" @click="openBatchDialog">批量审核</el-button>
              <el-button :loading="reviewsLoading" @click="fetchReviews">刷新</el-button>
            </div>
          </div>

          <el-alert
            v-if="reviewsError"
            type="error"
            :title="reviewsError"
            show-icon
            :closable="false"
            class="state-alert"
          >
            <el-button size="small" type="primary" @click="fetchReviews">重试</el-button>
          </el-alert>

          <div v-loading="reviewsLoading" class="data-table">
            <el-table :data="reviews" style="width: 100%">
              <el-table-column prop="id" label="记录ID" width="180" />
              <el-table-column prop="relatedWorkId" label="作品ID" width="100" />
              <el-table-column prop="relatedWorkTitle" label="作品标题" />
              <el-table-column label="审核状态" width="110">
                <template #default="scope">
                  <el-tag :type="getReviewStatusType(scope.row.status)">
                    {{ getReviewStatusText(scope.row.status) }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column label="审核意见" min-width="160">
                <template #default="scope">
                  {{ truncateText(scope.row.reviewComment || scope.row.feedback || '', 40) }}
                </template>
              </el-table-column>
              <el-table-column prop="reviewerUsername" label="审核人" width="120" />
              <el-table-column label="审核时间" width="160">
                <template #default="scope">
                  {{ formatDate(scope.row.reviewedAt) }}
                </template>
              </el-table-column>
              <el-table-column label="操作" width="180" fixed="right">
                <template #default="scope">
                  <el-button size="small" @click="openReviewDetail(scope.row)">详情</el-button>
                  <el-button size="small" type="primary" @click="openReviewEdit(scope.row)">
                    改结论
                  </el-button>
                  <el-button size="small" type="danger" @click="removeReview(scope.row)">
                    删除
                  </el-button>
                </template>
              </el-table-column>
            </el-table>

            <el-empty
              v-if="!reviewsLoading && !reviewsError && reviews.length === 0"
              description="暂无审核记录"
            />
          </div>

          <div v-if="reviewsTotal > reviewsPageSize" class="pagination">
            <el-pagination
              v-model:current-page="reviewsPage"
              v-model:page-size="reviewsPageSize"
              :page-sizes="[10, 20, 50]"
              layout="total, sizes, prev, pager, next, jumper"
              :total="reviewsTotal"
              @size-change="handleReviewsSizeChange"
              @current-change="handleReviewsPageChange"
            />
          </div>
        </div>

        <!-- 评论管理：GET /api/community/comments/work/{workId} -->
        <div v-if="activeTab === 'comments'" class="tab-content">
          <div class="content-header">
            <h4>作品评论</h4>
            <div class="header-actions">
              <el-select
                v-model="commentWorkId"
                filterable
                allow-create
                default-first-option
                placeholder="选择或输入作品 ID"
                style="width: 240px; margin-right: 10px;"
              >
                <el-option
                  v-for="work in works"
                  :key="work.id"
                  :label="`${work.id} · ${work.title || '未命名作品'}`"
                  :value="work.id"
                />
              </el-select>
              <el-button type="primary" :loading="commentsLoading" @click="fetchComments">
                查询评论
              </el-button>
            </div>
          </div>
          <p class="data-hint">评论按作品查询：GET /api/community/comments/work/{workId}。</p>

          <el-alert
            v-if="commentsError"
            type="error"
            :title="commentsError"
            show-icon
            :closable="false"
            class="state-alert"
          >
            <el-button size="small" type="primary" @click="fetchComments">重试</el-button>
          </el-alert>

          <div v-loading="commentsLoading" class="data-table">
            <el-table :data="comments" style="width: 100%">
              <el-table-column prop="id" label="评论ID" width="180" />
              <el-table-column label="评论内容">
                <template #default="scope">
                  <div class="comment-content">{{ truncateText(scope.row.content, 50) }}</div>
                </template>
              </el-table-column>
              <el-table-column prop="username" label="评论者" width="140" />
              <el-table-column prop="workTitle" label="所属作品" width="160" />
              <el-table-column label="状态" width="100">
                <template #default="scope">
                  <el-tag :type="getCommentStatusType(scope.row.status)">
                    {{ getCommentStatusText(scope.row.status) }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column label="评论时间" width="160">
                <template #default="scope">
                  {{ formatDate(scope.row.createdAt) }}
                </template>
              </el-table-column>
              <el-table-column label="操作" width="100" fixed="right">
                <template #default="scope">
                  <el-button size="small" type="danger" @click="removeComment(scope.row)">
                    删除
                  </el-button>
                </template>
              </el-table-column>
            </el-table>

            <el-empty
              v-if="!commentsLoading && !commentsError && comments.length === 0"
              description="暂无评论"
            />
          </div>

          <div v-if="commentsTotal > commentsPageSize" class="pagination">
            <el-pagination
              v-model:current-page="commentsPage"
              v-model:page-size="commentsPageSize"
              :page-sizes="[10, 20, 50]"
              layout="total, sizes, prev, pager, next, jumper"
              :total="commentsTotal"
              @size-change="handleCommentsSizeChange"
              @current-change="handleCommentsPageChange"
            />
          </div>
        </div>

        <!-- 用户查询：GET /api/users/{userId} -->
        <div v-if="activeTab === 'users'" class="tab-content">
          <div class="content-header">
            <h4>用户资料</h4>
            <div class="header-actions">
              <el-input
                v-model="userIdInput"
                placeholder="输入用户 ID"
                clearable
                style="width: 220px; margin-right: 10px;"
                @keyup.enter="fetchUser"
              />
              <el-button type="primary" :loading="userLoading" @click="fetchUser">查询</el-button>
            </div>
          </div>
          <p class="data-hint">后端只提供按 ID 查询单个用户：GET /api/users/{userId}。</p>

          <el-alert
            v-if="userError"
            type="error"
            :title="userError"
            show-icon
            :closable="false"
            class="state-alert"
          />

          <div v-loading="userLoading" class="data-table">
            <el-descriptions v-if="user" :column="2" border>
              <el-descriptions-item label="用户ID">{{ user.id }}</el-descriptions-item>
              <el-descriptions-item label="用户名">{{ user.username }}</el-descriptions-item>
              <el-descriptions-item label="昵称">{{ user.nickname || '-' }}</el-descriptions-item>
              <el-descriptions-item label="邮箱">{{ user.email || '-' }}</el-descriptions-item>
              <el-descriptions-item label="角色">
                {{ (user.roles || []).join('、') || '-' }}
              </el-descriptions-item>
              <el-descriptions-item label="状态">{{ user.status || '-' }}</el-descriptions-item>
              <el-descriptions-item label="注册时间">
                {{ formatDate(user.createdAt) }}
              </el-descriptions-item>
              <el-descriptions-item label="最后登录">
                {{ formatDate(user.lastLoginAt) }}
              </el-descriptions-item>
              <el-descriptions-item label="作品数">{{ user.workCount ?? 0 }}</el-descriptions-item>
              <el-descriptions-item label="评论数">{{ user.commentCount ?? 0 }}</el-descriptions-item>
              <el-descriptions-item label="练习数">{{ user.practiceCount ?? 0 }}</el-descriptions-item>
              <el-descriptions-item label="收到点赞">
                {{ user.totalLikesReceived ?? 0 }}
              </el-descriptions-item>
            </el-descriptions>

            <el-empty
              v-else-if="!userLoading && !userError"
              description="请输入用户 ID 后查询"
            />
          </div>
        </div>

        <!-- 数据分析：GET /api/stats/overview -->
        <div v-if="activeTab === 'analytics'" class="tab-content">
          <el-alert
            v-if="analyticsError"
            type="error"
            :title="analyticsError"
            show-icon
            :closable="false"
            class="state-alert"
          >
            <el-button size="small" type="primary" @click="fetchAnalytics">重试</el-button>
          </el-alert>

          <div v-loading="analyticsLoading" class="analytics-cards">
            <div class="stat-card">
              <div class="stat-value">{{ analytics.totalWorks ?? 0 }}</div>
              <div class="stat-label">总作品数</div>
            </div>
            <div class="stat-card">
              <div class="stat-value">{{ analytics.totalUsers ?? 0 }}</div>
              <div class="stat-label">总用户数</div>
            </div>
            <div class="stat-card">
              <div class="stat-value">{{ analytics.totalPracticeRecords ?? 0 }}</div>
              <div class="stat-label">练习记录数</div>
            </div>
          </div>

          <div class="recent-activities">
            <h4>热门字帖</h4>
            <el-table :data="analytics.popularCopybooks || []" style="width: 100%">
              <el-table-column prop="id" label="字帖ID" width="120" />
              <el-table-column prop="title" label="标题" />
              <el-table-column prop="category" label="分类" width="120" />
              <el-table-column prop="usageCount" label="使用次数" width="110" />
            </el-table>
            <el-empty
              v-if="!analyticsLoading && !analyticsError && (analytics.popularCopybooks || []).length === 0"
              description="暂无热门字帖"
            />
          </div>
        </div>
      </div>
    </div>

    <!-- 审核记录详情 -->
    <el-dialog v-model="reviewDetailVisible" title="审核记录详情" width="640px">
      <div v-loading="reviewDetailLoading" class="detail-body">
        <el-alert
          v-if="reviewDetailError"
          type="error"
          :title="reviewDetailError"
          show-icon
          :closable="false"
        />
        <el-descriptions v-else-if="reviewDetail" :column="2" border>
          <el-descriptions-item label="记录ID">{{ reviewDetail.id }}</el-descriptions-item>
          <el-descriptions-item label="作品ID">{{ reviewDetail.relatedWorkId || '-' }}</el-descriptions-item>
          <el-descriptions-item label="作品标题">
            {{ reviewDetail.relatedWorkTitle || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="作者">{{ reviewDetail.username || '-' }}</el-descriptions-item>
          <el-descriptions-item label="审核状态">
            <el-tag :type="getReviewStatusType(reviewDetail.status)">
              {{ getReviewStatusText(reviewDetail.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="审核人">
            {{ reviewDetail.reviewerUsername || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="审核意见">
            {{ reviewDetail.reviewComment || reviewDetail.feedback || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="文件地址">
            {{ reviewDetail.fileUrl || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="提交时间">
            {{ formatDate(reviewDetail.submittedAt) }}
          </el-descriptions-item>
          <el-descriptions-item label="审核时间">
            {{ formatDate(reviewDetail.reviewedAt) }}
          </el-descriptions-item>
          <el-descriptions-item label="该作品审核总数">
            {{ reviewDetail.totalReviews ?? 0 }}
          </el-descriptions-item>
          <el-descriptions-item label="该作品通过率">
            {{ reviewDetail.approvalRate ?? 0 }}
          </el-descriptions-item>
        </el-descriptions>
        <el-empty v-else-if="!reviewDetailLoading" description="暂无记录" />
      </div>
    </el-dialog>

    <!-- 更新审核结论 -->
    <el-dialog v-model="reviewEditVisible" title="更新审核结论" width="520px">
      <el-form :model="reviewEditForm" label-width="100px">
        <el-form-item label="审核状态">
          <el-select v-model="reviewEditForm.reviewStatus" placeholder="请选择审核状态">
            <el-option label="待审核" value="PENDING" />
            <el-option label="已通过" value="APPROVED" />
            <el-option label="已拒绝" value="REJECTED" />
            <el-option label="需修改" value="NEEDS_REVISION" />
          </el-select>
        </el-form-item>
        <el-form-item label="审核意见">
          <el-input
            v-model="reviewEditForm.reviewComments"
            type="textarea"
            :rows="4"
            maxlength="1000"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="reviewEditVisible = false">取消</el-button>
        <el-button type="primary" :loading="reviewEditSubmitting" @click="submitReviewEdit">
          保存
        </el-button>
      </template>
    </el-dialog>

    <!-- 批量审核 -->
    <el-dialog v-model="batchVisible" title="批量审核作品" width="560px">
      <el-form :model="batchForm" label-width="100px">
        <el-form-item label="作品 ID">
          <el-input
            v-model="batchForm.workIds"
            type="textarea"
            :rows="3"
            placeholder="多个作品 ID 用逗号分隔，例如 1,2,3"
          />
        </el-form-item>
        <el-form-item label="审核状态">
          <el-select v-model="batchForm.reviewStatus" placeholder="请选择审核状态">
            <el-option label="待审核" value="PENDING" />
            <el-option label="已通过" value="APPROVED" />
            <el-option label="已拒绝" value="REJECTED" />
            <el-option label="需修改" value="NEEDS_REVISION" />
          </el-select>
        </el-form-item>
        <el-form-item label="审核意见">
          <el-input
            v-model="batchForm.reviewComments"
            type="textarea"
            :rows="3"
            maxlength="1000"
            show-word-limit
          />
        </el-form-item>
      </el-form>

      <el-alert
        v-if="batchResult"
        :type="batchResult.failed > 0 ? 'warning' : 'success'"
        :title="`共 ${batchResult.total ?? 0} 件，成功 ${batchResult.succeeded ?? 0} 件，失败 ${batchResult.failed ?? 0} 件`"
        show-icon
        :closable="false"
      />

      <template #footer>
        <el-button @click="batchVisible = false">关闭</el-button>
        <el-button type="primary" :loading="batchSubmitting" @click="submitBatchReview">
          提交
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/stores/userStore'
import { fileReviewsAPI, worksAPI, communityAPI, userAPI, statsAPI } from '../utils/api.js'
import { formatDate as formatDateTime, truncateText } from '../utils'

const router = useRouter()
const userStore = useUserStore()

// 当前激活的标签
const activeTab = ref('works')

// 格式化日期时间
const formattedDateTime = computed(() => {
  const date = new Date()
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  })
})

// 设置激活标签
const setActiveTab = (tab) => {
  activeTab.value = tab
}

// 获取标签标题
const getTabTitle = (tab) => {
  const titles = {
    works: '作品审核',
    reviews: '审核记录',
    comments: '评论管理',
    users: '用户查询',
    analytics: '数据分析'
  }
  return titles[tab] || '管理中心'
}

// ------------------------------------------------------------ 作品审核

const works = ref([])
const worksTotal = ref(0)
const worksLoading = ref(false)
const worksError = ref('')
const worksPage = ref(1)
const worksPageSize = ref(10)
const workSearchQuery = ref('')

// 当前页内的筛选（服务端只提供分页，没有按关键字查询作品的管理端点）
const filteredWorks = computed(() => {
  const keyword = workSearchQuery.value.trim().toLowerCase()
  if (!keyword) {
    return works.value
  }
  return works.value.filter(
    (work) =>
      String(work.title || '').toLowerCase().includes(keyword) ||
      String(work.username || '').toLowerCase().includes(keyword) ||
      String(work.id || '').toLowerCase().includes(keyword)
  )
})

const fetchWorks = async () => {
  worksLoading.value = true
  worksError.value = ''
  try {
    const payload = await worksAPI.getWorks({
      page: worksPage.value - 1,
      size: worksPageSize.value
    })
    works.value = Array.isArray(payload?.content) ? payload.content : []
    worksTotal.value = payload?.total ?? works.value.length
  } catch (error) {
    works.value = []
    worksTotal.value = 0
    worksError.value = describeError(error, '获取作品列表失败')
    ElMessage.error(worksError.value)
    console.error('获取作品列表失败:', error)
  } finally {
    worksLoading.value = false
  }
}

const handleWorksPageChange = () => {
  fetchWorks()
}

const handleWorksSizeChange = () => {
  worksPage.value = 1
  fetchWorks()
}

// 审核作品：POST /api/admin/file-reviews
const reviewWork = async (work, reviewStatus) => {
  let reviewComments = ''
  if (reviewStatus !== 'APPROVED') {
    try {
      const { value } = await ElMessageBox.prompt('请填写审核意见', '审核作品', {
        confirmButtonText: '提交',
        cancelButtonText: '取消',
        inputType: 'textarea',
        inputPlaceholder: '写下给作者的反馈…',
        inputValidator: (input) => (input && input.trim() ? true : '审核意见不能为空')
      })
      reviewComments = value.trim()
    } catch {
      return
    }
  } else {
    try {
      await ElMessageBox.confirm('确定要通过这个作品的审核吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'success'
      })
    } catch {
      return
    }
  }

  try {
    await fileReviewsAPI.createReview({
      workId: work.id,
      reviewStatus,
      reviewComments
    })
    ElMessage.success('审核结论已提交')
    await fetchWorks()
  } catch (error) {
    ElMessage.error(describeError(error, '提交审核结论失败'))
    console.error('提交审核结论失败:', error)
  }
}

// ------------------------------------------------------------ 审核记录

const reviews = ref([])
const reviewsTotal = ref(0)
const reviewsLoading = ref(false)
const reviewsError = ref('')
const reviewsPage = ref(1)
const reviewsPageSize = ref(10)
const lookupWorkId = ref('')

const fetchReviews = async () => {
  reviewsLoading.value = true
  reviewsError.value = ''
  try {
    const payload = await fileReviewsAPI.getMyReviews({
      page: reviewsPage.value - 1,
      size: reviewsPageSize.value
    })
    reviews.value = Array.isArray(payload?.content) ? payload.content : []
    reviewsTotal.value = payload?.total ?? reviews.value.length
  } catch (error) {
    reviews.value = []
    reviewsTotal.value = 0
    reviewsError.value = describeError(error, '获取审核记录失败')
    ElMessage.error(reviewsError.value)
    console.error('获取审核记录失败:', error)
  } finally {
    reviewsLoading.value = false
  }
}

const handleReviewsPageChange = () => {
  fetchReviews()
}

const handleReviewsSizeChange = () => {
  reviewsPage.value = 1
  fetchReviews()
}

// 审核记录详情：GET /api/admin/file-reviews/{reviewId}
const reviewDetailVisible = ref(false)
const reviewDetailLoading = ref(false)
const reviewDetailError = ref('')
const reviewDetail = ref(null)

const openReviewDetail = async (review) => {
  reviewDetailVisible.value = true
  reviewDetail.value = null
  reviewDetailError.value = ''
  reviewDetailLoading.value = true
  try {
    reviewDetail.value = await fileReviewsAPI.getReviewById(review.id)
  } catch (error) {
    reviewDetailError.value = describeError(error, '获取审核记录详情失败')
    ElMessage.error(reviewDetailError.value)
    console.error('获取审核记录详情失败:', error)
  } finally {
    reviewDetailLoading.value = false
  }
}

// 按作品查询审核记录：GET /api/admin/file-reviews/work/{workId}
const lookupReviewByWork = async () => {
  const workId = lookupWorkId.value.trim()
  if (!workId) {
    ElMessage.warning('请输入作品 ID')
    return
  }

  reviewDetailVisible.value = true
  reviewDetail.value = null
  reviewDetailError.value = ''
  reviewDetailLoading.value = true
  try {
    reviewDetail.value = await fileReviewsAPI.getReviewByWorkId(workId)
  } catch (error) {
    // 该作品还没有审核记录时后端返回 404，这里说人话
    reviewDetailError.value = /不存在/.test(error?.message || '')
      ? `作品 ${workId} 还没有审核记录`
      : describeError(error, '获取作品的审核记录失败')
    ElMessage.error(reviewDetailError.value)
    console.error('获取作品的审核记录失败:', error)
  } finally {
    reviewDetailLoading.value = false
  }
}

// 更新审核结论：PUT /api/admin/file-reviews/{reviewId}
const reviewEditVisible = ref(false)
const reviewEditSubmitting = ref(false)
const reviewEditForm = reactive({
  id: '',
  reviewStatus: 'PENDING',
  reviewComments: ''
})

const openReviewEdit = (review) => {
  reviewEditForm.id = review.id
  reviewEditForm.reviewStatus = review.status || 'PENDING'
  reviewEditForm.reviewComments = review.reviewComment || review.feedback || ''
  reviewEditVisible.value = true
}

const submitReviewEdit = async () => {
  if (!reviewEditForm.id) {
    ElMessage.warning('审核记录不存在')
    return
  }

  reviewEditSubmitting.value = true
  try {
    await fileReviewsAPI.updateReview(reviewEditForm.id, {
      reviewStatus: reviewEditForm.reviewStatus,
      reviewComments: reviewEditForm.reviewComments
    })
    ElMessage.success('审核结论已更新')
    reviewEditVisible.value = false
    await fetchReviews()
  } catch (error) {
    ElMessage.error(describeError(error, '更新审核结论失败'))
    console.error('更新审核结论失败:', error)
  } finally {
    reviewEditSubmitting.value = false
  }
}

// 删除审核记录：DELETE /api/admin/file-reviews/{reviewId}
const removeReview = async (review) => {
  try {
    await ElMessageBox.confirm('确定要删除这条审核记录吗？', '警告', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
  } catch {
    return
  }

  try {
    await fileReviewsAPI.deleteReview(review.id)
    ElMessage.success('审核记录已删除')
    if (reviews.value.length === 1 && reviewsPage.value > 1) {
      reviewsPage.value -= 1
    }
    await fetchReviews()
  } catch (error) {
    ElMessage.error(describeError(error, '删除审核记录失败'))
    console.error('删除审核记录失败:', error)
  }
}

// 批量审核：POST /api/admin/file-reviews/batch
const batchVisible = ref(false)
const batchSubmitting = ref(false)
const batchResult = ref(null)
const batchForm = reactive({
  workIds: '',
  reviewStatus: 'APPROVED',
  reviewComments: ''
})

const openBatchDialog = () => {
  batchResult.value = null
  batchVisible.value = true
}

const submitBatchReview = async () => {
  const workIds = batchForm.workIds
    .split(',')
    .map((item) => item.trim())
    .filter(Boolean)
  if (workIds.length === 0) {
    ElMessage.warning('请至少填写一个作品 ID')
    return
  }

  batchSubmitting.value = true
  try {
    const result = await fileReviewsAPI.batchReviewWorks(
      workIds,
      batchForm.reviewStatus,
      batchForm.reviewComments
    )
    batchResult.value = {
      total: result?.total ?? workIds.length,
      succeeded: result?.succeeded ?? 0,
      failed: result?.failed ?? 0
    }
    ElMessage.success(`批量审核完成，成功 ${batchResult.value.succeeded} 件`)
    await fetchReviews()
  } catch (error) {
    ElMessage.error(describeError(error, '批量审核失败'))
    console.error('批量审核失败:', error)
  } finally {
    batchSubmitting.value = false
  }
}

// ------------------------------------------------------------ 评论管理

const comments = ref([])
const commentsTotal = ref(0)
const commentsLoading = ref(false)
const commentsError = ref('')
const commentsPage = ref(1)
const commentsPageSize = ref(10)
const commentWorkId = ref('')

const fetchComments = async () => {
  const workId = String(commentWorkId.value || '').trim()
  if (!workId) {
    ElMessage.warning('请先选择或输入作品 ID')
    return
  }

  commentsLoading.value = true
  commentsError.value = ''
  try {
    const payload = await communityAPI.getWorkComments(workId, {
      page: commentsPage.value - 1,
      size: commentsPageSize.value
    })
    comments.value = Array.isArray(payload?.content) ? payload.content : []
    commentsTotal.value = payload?.total ?? comments.value.length
  } catch (error) {
    comments.value = []
    commentsTotal.value = 0
    commentsError.value = describeError(error, '获取作品评论失败')
    ElMessage.error(commentsError.value)
    console.error('获取作品评论失败:', error)
  } finally {
    commentsLoading.value = false
  }
}

const handleCommentsPageChange = () => {
  fetchComments()
}

const handleCommentsSizeChange = () => {
  commentsPage.value = 1
  fetchComments()
}

// 删除评论：DELETE /api/community/comments/{commentId}
const removeComment = async (comment) => {
  try {
    await ElMessageBox.confirm('确定要删除这条评论吗？', '警告', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
  } catch {
    return
  }

  try {
    await communityAPI.deleteComment(comment.id)
    ElMessage.success('评论已删除')
    if (comments.value.length === 1 && commentsPage.value > 1) {
      commentsPage.value -= 1
    }
    await fetchComments()
  } catch (error) {
    ElMessage.error(describeError(error, '删除评论失败'))
    console.error('删除评论失败:', error)
  }
}

// ------------------------------------------------------------ 用户查询

const user = ref(null)
const userLoading = ref(false)
const userError = ref('')
const userIdInput = ref('')

const fetchUser = async () => {
  const userId = userIdInput.value.trim()
  if (!userId) {
    ElMessage.warning('请输入用户 ID')
    return
  }

  userLoading.value = true
  userError.value = ''
  try {
    user.value = await userAPI.getUserInfo(userId)
  } catch (error) {
    user.value = null
    userError.value = describeError(error, '获取用户资料失败')
    ElMessage.error(userError.value)
    console.error('获取用户资料失败:', error)
  } finally {
    userLoading.value = false
  }
}

// ------------------------------------------------------------ 数据分析

const analytics = ref({})
const analyticsLoading = ref(false)
const analyticsError = ref('')

const fetchAnalytics = async () => {
  analyticsLoading.value = true
  analyticsError.value = ''
  try {
    const payload = await statsAPI.getOverview()
    analytics.value = payload || {}
  } catch (error) {
    analytics.value = {}
    analyticsError.value = describeError(error, '获取统计概览失败')
    ElMessage.error(analyticsError.value)
    console.error('获取统计概览失败:', error)
  } finally {
    analyticsLoading.value = false
  }
}

// ------------------------------------------------------------ 通用

// 状态文案
const REVIEW_STATUS_TEXT = {
  PENDING: '待审核',
  APPROVED: '已通过',
  REJECTED: '已拒绝',
  NEEDS_REVISION: '需修改'
}

const REVIEW_STATUS_TYPE = {
  PENDING: 'warning',
  APPROVED: 'success',
  REJECTED: 'danger',
  NEEDS_REVISION: 'info'
}

const WORK_STATUS_TEXT = {
  DRAFT: '草稿',
  PUBLISHED: '已发布',
  HIDDEN: '已隐藏',
  UNDER_REVIEW: '审核中'
}

const WORK_STATUS_TYPE = {
  DRAFT: 'info',
  PUBLISHED: 'success',
  HIDDEN: 'info',
  UNDER_REVIEW: 'warning'
}

const COMMENT_STATUS_TEXT = {
  PENDING: '待审核',
  APPROVED: '已显示',
  REJECTED: '已拒绝',
  DELETED: '已删除'
}

const COMMENT_STATUS_TYPE = {
  PENDING: 'warning',
  APPROVED: 'success',
  REJECTED: 'danger',
  DELETED: 'info'
}

const getReviewStatusText = (status) => REVIEW_STATUS_TEXT[status] || '未知'
const getReviewStatusType = (status) => REVIEW_STATUS_TYPE[status] || 'info'
const getWorkStatusText = (status) => WORK_STATUS_TEXT[status] || '未知'
const getWorkStatusType = (status) => WORK_STATUS_TYPE[status] || 'info'
const getCommentStatusText = (status) => COMMENT_STATUS_TEXT[status] || '未知'
const getCommentStatusType = (status) => COMMENT_STATUS_TYPE[status] || 'info'

// 把后端的错误信息翻译成能区分 401 / 403 的提示。
// api.js 抛出的 Error 不带 HTTP 状态码，这里按后端固定文案区分：
//   403 → CustomAccessDeniedHandler：「访问被拒绝，权限不足」
//   401 → JwtAuthenticationEntryPoint：「未授权访问，请先登录」
const describeError = (error, fallback) => {
  const message = error?.message || fallback
  if (/权限不足|访问被拒绝|forbidden/i.test(message)) {
    return `权限不足：当前账号不是管理员，接口返回 403（${message}）`
  }
  if (/未授权|认证失败|session expired|please login/i.test(message)) {
    return `登录状态已失效，接口返回 401（${message}）`
  }
  return message
}

const initialOf = (name) => (name ? String(name).charAt(0).toUpperCase() : '?')

// 后端没给的时间字段显示为 '-'，不让 formatDate 算出 NaN-NaN-NaN
const formatDate = (value) => (value ? formatDateTime(value) : '-')

// 获取用户信息
const getUsername = computed(() => userStore.getUsername || '管理员')
const getUserAvatar = computed(() => userStore.getUserAvatar || '')

// 退出登录
const logout = async () => {
  try {
    await userStore.logout()
    ElMessage.success('退出登录成功')
    router.push('/admin/login')
  } catch (error) {
    console.error('退出登录失败:', error)
    ElMessage.error('退出登录失败')
  }
}

onMounted(async () => {
  // 刷新页面后 store 可能还没恢复用户信息，先尝试恢复一次
  if (!userStore.isLoggedIn) {
    await userStore.initUserFromStorage()
  }

  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录管理员账号')
    router.push('/admin/login')
    return
  }

  if (!userStore.isAdmin) {
    ElMessage.error('当前账号不是管理员，访问审核接口会返回 403（权限不足）')
    router.push('/')
    return
  }

  await Promise.all([fetchWorks(), fetchReviews(), fetchAnalytics()])
})
</script>

<style scoped>
.admin-container {
  display: flex;
  min-height: 100vh;
  background-color: #f5f7fa;
}

/* 侧边栏样式 */
.admin-sidebar {
  width: 240px;
  background-color: #304156;
  color: white;
  display: flex;
  flex-direction: column;
}

.sidebar-header {
  padding: 1.5rem;
  border-bottom: 1px solid #475669;
}

.sidebar-header h2 {
  margin: 0;
  font-size: 1.4rem;
  font-weight: 500;
}

.sidebar-nav {
  flex: 1;
  padding: 1rem 0;
}

.nav-item {
  display: flex;
  align-items: center;
  padding: 1rem 1.5rem;
  cursor: pointer;
  transition: all 0.3s;
  gap: 1rem;
}

.nav-item:hover {
  background-color: #475669;
}

.nav-item.active {
  background-color: #1e9fff;
}

.sidebar-footer {
  padding: 1.5rem;
  border-top: 1px solid #475669;
}

.admin-info {
  display: flex;
  align-items: center;
  margin-bottom: 10px;
  padding: 10px;
  background: #f5f7fa;
  border-radius: 8px;
  gap: 10px;
}

/* 主内容区 */
.admin-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.main-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1rem 2rem;
  background-color: white;
  border-bottom: 1px solid #e6e6e6;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
}

.header-title h3 {
  margin: 0;
  font-size: 1.3rem;
  color: #333;
}

.header-info {
  display: flex;
  align-items: center;
  gap: 2rem;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 0.8rem;
}

.current-time {
  color: #666;
  font-size: 0.9rem;
}

.content-area {
  flex: 1;
  padding: 1.5rem 2rem;
  overflow-x: auto;
}

.tab-content {
  background-color: white;
  border-radius: 8px;
  padding: 1.5rem;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.08);
}

.content-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1rem;
}

.content-header h4 {
  margin: 0;
  color: #333;
}

.header-actions {
  display: flex;
  align-items: center;
}

.data-hint {
  color: #909399;
  font-size: 0.85rem;
  margin: 0 0 1rem;
}

.state-alert {
  margin-bottom: 1rem;
}

.data-table {
  min-height: 160px;
}

.work-thumbnail-small {
  width: 60px;
  height: 60px;
  border-radius: 4px;
}

.image-fallback {
  width: 60px;
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #f0f2f5;
  color: #909399;
  font-size: 0.75rem;
  border-radius: 4px;
  text-align: center;
}

.comment-content {
  color: #555;
}

.pagination {
  margin-top: 1.5rem;
  display: flex;
  justify-content: flex-end;
}

/* 数据分析 */
.analytics-cards {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 1.5rem;
  margin-bottom: 1.5rem;
}

.stat-card {
  background-color: #f8f9fa;
  border-radius: 8px;
  padding: 1.5rem;
  text-align: center;
}

.stat-value {
  font-size: 2rem;
  font-weight: 600;
  color: #303133;
}

.stat-label {
  color: #909399;
  font-size: 0.9rem;
  margin-top: 0.5rem;
}

.recent-activities h4 {
  margin: 0 0 1rem;
  color: #333;
}

.detail-body {
  min-height: 100px;
}
</style>
