<template>
  <div class="my-works-container">
    <div class="page-header">
      <h2 class="page-title">我的作品</h2>
      <p class="page-subtitle">展示和管理您的书法作品</p>
    </div>

    <div class="filter-section">
      <el-input
        v-model="searchKeyword"
        placeholder="搜索作品标题"
        prefix-icon="el-icon-search"
        clearable
        class="search-input"
        @input="handleSearch"
      />

      <el-select v-model="sortBy" placeholder="排序方式" class="filter-select" @change="handleSort">
        <el-option label="最新上传" value="newest" />
        <el-option label="最多点赞" value="likes" />
        <el-option label="最多评论" value="comments" />
      </el-select>

      <el-button type="primary" :loading="loading" @click="refreshWorks">
        <i class="el-icon-refresh"></i> 刷新列表
      </el-button>
    </div>
    <p class="filter-hint">后端列表接口只支持分页，搜索与排序作用于当前页已加载的作品。</p>

    <!-- 错误状态 -->
    <el-alert
      v-if="errorMessage"
      class="state-alert"
      type="error"
      :title="errorMessage"
      show-icon
      :closable="false"
    >
      <template #default>
        <el-button type="primary" size="small" @click="loadMyWorks">重新加载</el-button>
      </template>
    </el-alert>

    <div class="works-area" v-loading="loading">
      <div class="works-grid" v-if="filteredWorks.length > 0">
        <div v-for="work in filteredWorks" :key="work.id" class="work-item">
          <div class="work-image-wrapper">
            <img v-if="work.imageUrl" :src="work.imageUrl" :alt="work.title" class="work-image" />
            <div v-else class="work-image work-image-placeholder">暂无图片</div>
            <div class="work-overlay">
              <div class="work-actions">
                <el-button type="primary" size="small" @click="viewWorkDetail(work)">
                  <i class="el-icon-view"></i> 查看
                </el-button>
                <el-button type="success" size="small" @click="openEditDialog(work)">
                  <i class="el-icon-edit"></i> 编辑
                </el-button>
                <el-button type="danger" size="small" @click="deleteWork(work)">
                  <i class="el-icon-delete"></i> 删除
                </el-button>
              </div>
            </div>
          </div>

          <div class="work-info">
            <h3 class="work-title">{{ work.title }}</h3>
            <el-tag class="status-tag" :type="statusTagType(work.status)" size="small">
              {{ statusText(work.status) }}
            </el-tag>
            <el-tag v-if="work.reviewStatus" class="status-tag" :type="reviewTagType(work.reviewStatus)" size="small">
              审核：{{ reviewText(work.reviewStatus) }}
            </el-tag>

            <div class="work-stats">
              <div class="stat-item">
                <i class="el-icon-star-on"></i>
                <span>{{ work.likeCount || 0 }}</span>
              </div>
              <div class="stat-item">
                <i class="el-icon-chat-dot-round"></i>
                <span>{{ work.commentCount || 0 }}</span>
              </div>
              <div class="stat-item">
                <i class="el-icon-eye"></i>
                <span>{{ work.viewCount || 0 }}</span>
              </div>
            </div>

            <div class="work-meta">
              <span class="upload-time">{{ formatDate(work.createdAt) }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 空状态 -->
    <div v-if="!loading && !errorMessage && filteredWorks.length === 0" class="empty-state">
      <el-empty :description="myWorks.length === 0 ? '还没有上传过作品' : '暂无符合条件的作品'">
        <el-button type="primary" @click="router.push('/gallery')">去作品展示</el-button>
      </el-empty>
    </div>

    <!-- 分页 -->
    <div class="pagination-container" v-if="totalItems > 0">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :page-sizes="[8, 16, 24, 32]"
        layout="total, sizes, prev, pager, next, jumper"
        :total="totalItems"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>

    <!-- 作品详情对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="selectedWork?.title || '作品详情'"
      width="80%"
      :before-close="closeDialog"
    >
      <div v-if="selectedWork" class="work-detail">
        <div class="detail-image-wrapper">
          <img
            v-if="selectedWork.imageUrl"
            :src="selectedWork.imageUrl"
            :alt="selectedWork.title"
            class="detail-image"
          />
          <div v-else class="detail-image detail-image-placeholder">暂无图片</div>
        </div>
        <div class="detail-info">
          <h3 class="detail-title">{{ selectedWork.title }}</h3>
          <div class="detail-meta">
            <span class="detail-style">{{ statusText(selectedWork.status) }}</span>
            <span class="detail-date">{{ formatDate(selectedWork.createdAt) }}</span>
          </div>
          <div class="detail-description">{{ selectedWork.description || '暂无描述' }}</div>
          <div class="detail-stats">
            <div class="detail-stat-item">
              <i class="el-icon-star-on"></i>
              <span>{{ selectedWork.likeCount || 0 }} 点赞</span>
            </div>
            <div class="detail-stat-item">
              <i class="el-icon-chat-dot-round"></i>
              <span>{{ selectedWork.commentCount || 0 }} 评论</span>
            </div>
            <div class="detail-stat-item">
              <i class="el-icon-eye"></i>
              <span>{{ selectedWork.viewCount || 0 }} 浏览</span>
            </div>
          </div>
        </div>
      </div>

      <template #footer>
        <el-button @click="closeDialog">关闭</el-button>
        <el-button type="primary" @click="openEditDialog(selectedWork)">编辑作品</el-button>
      </template>
    </el-dialog>

    <!-- 编辑作品对话框 -->
    <el-dialog v-model="editDialogVisible" title="编辑作品" width="520px">
      <el-form :model="editForm" label-width="80px">
        <el-form-item label="标题" required>
          <el-input v-model="editForm.title" maxlength="200" show-word-limit placeholder="请输入作品标题" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input
            v-model="editForm.description"
            type="textarea"
            :rows="4"
            maxlength="1000"
            show-word-limit
            placeholder="介绍一下你的作品"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submitEdit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import { worksAPI } from '../utils/api';
import { useUserStore } from '../stores/userStore';

const router = useRouter();
const userStore = useUserStore();

// 搜索和排序
const searchKeyword = ref('');
const sortBy = ref('newest');

// 分页（el-pagination 从 1 起，后端分页从 0 起）
const currentPage = ref(1);
const pageSize = ref(8);
const totalItems = ref(0);

// 列表状态
const myWorks = ref([]);
const loading = ref(false);
const errorMessage = ref('');

// 对话框
const dialogVisible = ref(false);
const selectedWork = ref(null);
const editDialogVisible = ref(false);
const editForm = ref({ id: null, title: '', description: '' });
const saving = ref(false);

// 加载我的作品：GET /api/works/user/{userId}，返回分页信封
const loadMyWorks = async () => {
  loading.value = true;
  errorMessage.value = '';
  try {
    if (!userStore.userInfo?.id) {
      // store 的 fetchMyWorks 依赖 userInfo.id，这里先把资料拉回来
      await userStore.fetchUserProfile();
    }
    const response = await userStore.fetchMyWorks({
      page: currentPage.value - 1,
      size: pageSize.value,
    });
    myWorks.value = Array.isArray(response?.content) ? response.content : [];
    totalItems.value = typeof response?.total === 'number' ? response.total : myWorks.value.length;
    if (typeof response?.page === 'number' && response.page + 1 !== currentPage.value) {
      currentPage.value = response.page + 1;
    }
  } catch (error) {
    myWorks.value = [];
    totalItems.value = 0;
    errorMessage.value = error?.message || '作品列表加载失败，请稍后重试';
    ElMessage.error(errorMessage.value);
  } finally {
    loading.value = false;
  }
};

// 过滤后的作品（后端列表接口只支持分页，筛选与排序在当前页内进行）
const filteredWorks = computed(() => {
  let filtered = [...myWorks.value];

  if (searchKeyword.value) {
    const keyword = searchKeyword.value.trim().toLowerCase();
    if (keyword) {
      filtered = filtered.filter(
        (work) =>
          (work.title || '').toLowerCase().includes(keyword) ||
          (work.description || '').toLowerCase().includes(keyword)
      );
    }
  }

  switch (sortBy.value) {
    case 'likes':
      filtered.sort((a, b) => (b.likeCount || 0) - (a.likeCount || 0));
      break;
    case 'comments':
      filtered.sort((a, b) => (b.commentCount || 0) - (a.commentCount || 0));
      break;
    default:
      filtered.sort((a, b) => new Date(b.createdAt || 0) - new Date(a.createdAt || 0));
      break;
  }

  return filtered;
});

// 搜索与排序
const handleSearch = () => {
  currentPage.value = 1;
};

const handleSort = () => {
  currentPage.value = 1;
};

// 刷新作品列表
const refreshWorks = async () => {
  currentPage.value = 1;
  await loadMyWorks();
  if (!errorMessage.value) {
    ElMessage.success('列表已刷新');
  }
};

// 查看作品详情
const viewWorkDetail = (work) => {
  selectedWork.value = work;
  dialogVisible.value = true;
};

// 关闭对话框
const closeDialog = () => {
  dialogVisible.value = false;
  selectedWork.value = null;
};

// 打开编辑对话框
const openEditDialog = (work) => {
  if (!work) {
    return;
  }
  editForm.value = {
    id: work.id,
    title: work.title || '',
    description: work.description || '',
  };
  dialogVisible.value = false;
  editDialogVisible.value = true;
};

// 保存修改：PUT /api/works/{workId}
const submitEdit = async () => {
  const title = editForm.value.title.trim();
  if (!title) {
    ElMessage.warning('请填写作品标题');
    return;
  }

  saving.value = true;
  try {
    await worksAPI.updateWork(editForm.value.id, {
      title,
      description: editForm.value.description || '',
    });
    ElMessage.success('保存成功');
    editDialogVisible.value = false;
    await loadMyWorks();
  } catch (error) {
    ElMessage.error(error?.message || '保存失败，请稍后重试');
  } finally {
    saving.value = false;
  }
};

// 删除作品：DELETE /api/works/{workId}
const deleteWork = async (work) => {
  try {
    await ElMessageBox.confirm(
      '确定要删除这个作品吗？此操作不可撤销',
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    );
  } catch {
    // 用户取消确认
    return;
  }

  try {
    await worksAPI.deleteWork(work.id);
    ElMessage.success('作品已删除');
    await loadMyWorks();
  } catch (error) {
    // 后端删除成功返回 204（无响应体），前端统一请求层解析空响应体会报错，
    // 所以这里重新拉一次列表，以服务端返回的最终状态为准。
    await loadMyWorks();
    const stillThere = myWorks.value.some((item) => item.id === work.id);
    if (stillThere) {
      ElMessage.error(error?.message || '删除作品失败，请稍后重试');
    } else {
      ElMessage.success('作品已删除');
    }
  }
};

// 分页处理
const handleSizeChange = (newSize) => {
  pageSize.value = newSize;
  currentPage.value = 1;
  loadMyWorks();
};

const handleCurrentChange = (newCurrent) => {
  currentPage.value = newCurrent;
  loadMyWorks();
};

// 状态文案
const statusText = (status) => {
  const map = {
    DRAFT: '草稿',
    PUBLISHED: '已发布',
    HIDDEN: '已隐藏',
    UNDER_REVIEW: '审核中'
  };
  return map[status] || status || '未知状态';
};

const statusTagType = (status) => {
  const map = {
    DRAFT: 'info',
    PUBLISHED: 'success',
    HIDDEN: 'warning',
    UNDER_REVIEW: 'primary'
  };
  return map[status] || 'info';
};

const reviewText = (reviewStatus) => {
  const map = {
    PENDING: '待审核',
    APPROVED: '已通过',
    REJECTED: '已驳回',
    NEEDS_REVISION: '需修改'
  };
  return map[reviewStatus] || reviewStatus;
};

const reviewTagType = (reviewStatus) => {
  const map = {
    PENDING: 'info',
    APPROVED: 'success',
    REJECTED: 'danger',
    NEEDS_REVISION: 'warning'
  };
  return map[reviewStatus] || 'info';
};

const formatDate = (dateString) => {
  if (!dateString) return '';
  const date = new Date(dateString);
  if (Number.isNaN(date.getTime())) return '';
  return new Intl.DateTimeFormat('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  }).format(date);
};

// 生命周期钩子
onMounted(() => {
  loadMyWorks();
});
</script>

<style scoped>
.my-works-container {
  padding: 2rem;
  max-width: 1200px;
  margin: 0 auto;
}

.page-header {
  text-align: center;
  margin-bottom: 2rem;
}

.page-title {
  font-size: 2rem;
  color: #333;
  margin-bottom: 0.5rem;
  font-family: 'SimSun', serif;
}

.page-subtitle {
  color: #666;
  font-size: 1rem;
}

.filter-section {
  display: flex;
  gap: 1rem;
  margin-bottom: 0.5rem;
  flex-wrap: wrap;
  align-items: center;
}

.filter-hint {
  color: #999;
  font-size: 0.85rem;
  margin-bottom: 1.5rem;
}

.state-alert {
  margin-bottom: 1.5rem;
}

.search-input {
  flex: 1;
  min-width: 200px;
}

.filter-select {
  min-width: 150px;
}

.works-area {
  min-height: 12rem;
}

.works-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 2rem;
}

.work-item {
  background-color: rgba(255, 255, 255, 0.95);
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
  transition: transform 0.3s, box-shadow 0.3s;
}

.work-item:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
}

.work-image-wrapper {
  position: relative;
  overflow: hidden;
  height: 300px;
}

.work-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s;
}

.work-image-placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #f0eee9;
  color: #999;
  font-size: 0.9rem;
}

.work-item:hover .work-image {
  transform: scale(1.05);
}

.work-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.6);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.3s;
}

.work-item:hover .work-overlay {
  opacity: 1;
}

.work-actions {
  display: flex;
  gap: 0.5rem;
}

.work-info {
  padding: 1rem;
}

.work-title {
  font-size: 1.2rem;
  margin: 0 0 0.5rem 0;
  color: #333;
}

.status-tag {
  margin-right: 0.5rem;
}

.work-stats {
  display: flex;
  gap: 1rem;
  margin: 0.6rem 0 0.5rem 0;
  color: #666;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 0.3rem;
  font-size: 0.9rem;
}

.work-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  color: #999;
  font-size: 0.8rem;
}

.empty-state {
  text-align: center;
  padding: 4rem 2rem;
}

.pagination-container {
  margin-top: 2rem;
  display: flex;
  justify-content: center;
}

/* 详情对话框样式 */
.work-detail {
  display: flex;
  gap: 2rem;
  flex-wrap: wrap;
}

.detail-image-wrapper {
  flex: 1;
  min-width: 300px;
}

.detail-image {
  width: 100%;
  max-height: 500px;
  object-fit: contain;
  border-radius: 8px;
}

.detail-image-placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 240px;
  background-color: #f0eee9;
  color: #999;
}

.detail-info {
  flex: 1;
  min-width: 300px;
}

.detail-title {
  font-size: 1.8rem;
  margin-bottom: 1rem;
  color: #333;
  font-family: 'SimSun', serif;
}

.detail-meta {
  display: flex;
  gap: 1rem;
  margin-bottom: 1rem;
  color: #8B4513;
}

.detail-description {
  margin-bottom: 1.5rem;
  line-height: 1.6;
  color: #666;
  white-space: pre-wrap;
  word-break: break-word;
}

.detail-stats {
  display: flex;
  gap: 1.5rem;
  color: #666;
}

.detail-stat-item {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 1rem;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .my-works-container {
    padding: 1rem;
  }

  .filter-section {
    flex-direction: column;
    align-items: stretch;
  }

  .search-input,
  .filter-select {
    width: 100%;
  }

  .works-grid {
    grid-template-columns: 1fr;
  }

  .work-detail {
    flex-direction: column;
  }
}
</style>
