<template>
  <div class="gallery-container">
    <div class="gallery-header">
      <h2 class="section-title">作品展示</h2>
      <p class="section-subtitle">欣赏书法爱好者的优秀作品，获得灵感</p>
    </div>

    <!-- 筛选和搜索 -->
    <div class="filter-section">
      <div class="search-bar">
        <el-input
          v-model="searchQuery"
          placeholder="搜索作品标题或作者"
          prefix-icon="el-icon-search"
          clearable
          @input="handleSearch"
        />
      </div>

      <div class="filter-options">
        <el-select v-model="sortBy" placeholder="排序" @change="handleSort">
          <el-option label="最新上传" value="latest" />
          <el-option label="最多点赞" value="popular" />
          <el-option label="最多评论" value="commented" />
        </el-select>
      </div>
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
        <el-button type="primary" size="small" @click="loadWorks">重新加载</el-button>
      </template>
    </el-alert>

    <!-- 作品网格 -->
    <div class="works-area" v-loading="loading">
      <div class="works-grid">
        <div
          v-for="work in filteredWorks"
          :key="work.id"
          class="work-card"
          @click="viewWorkDetail(work)"
        >
          <div class="work-thumbnail">
            <img v-if="work.imageUrl" :src="work.imageUrl" :alt="work.title" class="work-image" />
            <div v-else class="work-image work-image-placeholder">暂无图片</div>
            <div class="work-overlay">
              <span class="overlay-stat">
                <i class="el-icon-thumb-up"></i>
                {{ work.likeCount || 0 }}
              </span>
              <span class="overlay-stat">
                <i class="el-icon-chat-dot-round"></i>
                {{ work.commentCount || 0 }}
              </span>
            </div>
          </div>

          <div class="work-info">
            <h3 class="work-title">{{ work.title }}</h3>

            <div class="work-meta">
              <div class="author-info">
                <img
                  v-if="work.userAvatar"
                  :src="work.userAvatar"
                  :alt="authorName(work)"
                  class="author-avatar"
                />
                <span v-else class="author-avatar author-avatar-fallback">{{ authorInitial(work) }}</span>
                <span class="author-name">{{ authorName(work) }}</span>
              </div>

              <div class="work-stats">
                <span class="stat-item">
                  <i class="el-icon-thumb-up"></i>
                  {{ work.likeCount || 0 }}
                </span>
                <span class="stat-item">
                  <i class="el-icon-chat-dot-round"></i>
                  {{ work.commentCount || 0 }}
                </span>
                <span class="stat-item">
                  <i class="el-icon-view"></i>
                  {{ work.viewCount || 0 }}
                </span>
              </div>
            </div>

            <div class="work-desc">
              {{ truncateText(work.description, 50) }}
            </div>

            <div class="work-date">
              {{ formatDate(work.createdAt) }}
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 空状态 -->
    <div v-if="!loading && !errorMessage && filteredWorks.length === 0" class="empty-state">
      <el-empty :description="works.length === 0 ? '暂无公开作品' : '暂无符合条件的作品'">
        <el-button v-if="works.length > 0" type="primary" @click="resetFilters">重置筛选条件</el-button>
      </el-empty>
    </div>

    <!-- 分页 -->
    <div class="pagination" v-if="totalItems > 0">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :page-sizes="[12, 24, 36]"
        layout="total, sizes, prev, pager, next, jumper"
        :total="totalItems"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>

    <!-- 上传按钮：右下角拇指区，做成全圆角胶囊 -->
    <div class="upload-button-container">
      <el-button
        type="primary"
        icon="el-icon-plus"
        class="floating-upload-btn"
        @click="openUploadDialog"
      >
        上传作品
      </el-button>
    </div>

    <!-- 作品详情对话框 -->
    <el-dialog
      v-model="showDetailDialog"
      :title="selectedWork?.title || '作品详情'"
      width="80%"
      :before-close="closeDetailDialog"
    >
      <div v-if="selectedWork" class="work-detail">
        <div class="detail-image">
          <img
            v-if="selectedWork.imageUrl"
            :src="selectedWork.imageUrl"
            :alt="selectedWork.title"
            class="full-image"
          />
          <div v-else class="full-image full-image-placeholder">暂无图片</div>
        </div>

        <div class="detail-info">
          <div class="detail-header">
            <h3>{{ selectedWork.title }}</h3>
          </div>

          <div class="detail-meta">
            <div class="author-section">
              <img
                v-if="selectedWork.userAvatar"
                :src="selectedWork.userAvatar"
                :alt="authorName(selectedWork)"
                class="author-avatar-large"
              />
              <span v-else class="author-avatar-large author-avatar-fallback">
                {{ authorInitial(selectedWork) }}
              </span>
              <div class="author-details">
                <div class="author-name-large">{{ authorName(selectedWork) }}</div>
                <div class="publish-date">{{ formatDate(selectedWork.createdAt) }}</div>
              </div>
            </div>

            <div class="detail-stats">
              <div class="stat-box">
                <div class="stat-number">{{ selectedWork.likeCount || 0 }}</div>
                <div class="stat-label">点赞</div>
              </div>
              <div class="stat-box">
                <div class="stat-number">{{ selectedWork.commentCount || 0 }}</div>
                <div class="stat-label">评论</div>
              </div>
              <div class="stat-box">
                <div class="stat-number">{{ selectedWork.viewCount || 0 }}</div>
                <div class="stat-label">浏览</div>
              </div>
            </div>
          </div>

          <div class="detail-description">
            <h4>作品描述</h4>
            <p>{{ selectedWork.description || '这位书法家没有留下描述' }}</p>
          </div>

          <div class="detail-actions">
            <el-button icon="el-icon-chat-dot-round" @click="openComments(selectedWork)">
              评论（{{ selectedWork.commentCount || 0 }}）
            </el-button>
          </div>
        </div>
      </div>
    </el-dialog>

    <!-- 评论对话框 -->
    <el-dialog v-model="commentDialogVisible" title="作品评论" width="600px">
      <div class="comment-area" v-loading="commentLoading">
        <el-alert
          v-if="commentError"
          type="error"
          :title="commentError"
          show-icon
          :closable="false"
        />
        <el-empty v-else-if="!commentLoading && comments.length === 0" description="暂无评论" :image-size="80" />
        <ul v-else class="comment-list">
          <li v-for="comment in comments" :key="comment.id" class="comment-item">
            <div class="comment-author">{{ commentAuthor(comment) }}</div>
            <div class="comment-content">{{ comment.content }}</div>
            <div class="comment-date">{{ formatDate(comment.createdAt) }}</div>
          </li>
        </ul>
      </div>

      <template #footer>
        <el-input
          v-model="commentText"
          type="textarea"
          :rows="2"
          maxlength="500"
          show-word-limit
          placeholder="说点什么吧…"
        />
        <el-button type="primary" :loading="commenting" @click="submitComment">发表</el-button>
      </template>
    </el-dialog>

    <!-- 上传作品对话框 -->
    <el-dialog v-model="uploadDialogVisible" title="上传作品" width="520px">
      <el-form :model="uploadForm" label-width="80px">
        <el-form-item label="标题" required>
          <el-input v-model="uploadForm.title" maxlength="200" show-word-limit placeholder="请输入作品标题" />
        </el-form-item>

        <el-form-item label="描述">
          <el-input
            v-model="uploadForm.description"
            type="textarea"
            :rows="3"
            maxlength="1000"
            show-word-limit
            placeholder="介绍一下你的作品"
          />
        </el-form-item>

        <el-form-item label="图片">
          <el-upload
            :auto-upload="false"
            :show-file-list="false"
            accept="image/jpeg,image/png,image/webp"
            :on-change="handleImageSelected"
          >
            <el-button>选择图片</el-button>
          </el-upload>
          <img v-if="imagePreview" :src="imagePreview" class="upload-preview" alt="待上传图片预览" />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="uploadDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="uploading" @click="submitWork">提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { worksAPI, communityAPI, getToken } from '../utils/api';
import { useUserStore } from '../stores/userStore';

const router = useRouter();
const userStore = useUserStore();

// 是否已登录：store 状态 + 本地是否留有令牌
const isLoggedIn = computed(() => userStore.isLoggedIn || Boolean(getToken()));

// 列表状态
const works = ref([]);
const totalItems = ref(0);
const currentPage = ref(1); // el-pagination 是 1 起，后端分页是 0 起
const pageSize = ref(12);
const loading = ref(false);
const errorMessage = ref('');

// 搜索与排序
const searchQuery = ref('');
const sortBy = ref('latest');

// 详情对话框
const showDetailDialog = ref(false);
const selectedWork = ref(null);

// 评论对话框
const commentDialogVisible = ref(false);
const comments = ref([]);
const commentText = ref('');
const commentLoading = ref(false);
const commenting = ref(false);
const commentError = ref('');

// 上传对话框
const uploadDialogVisible = ref(false);
const uploadForm = ref({ title: '', description: '' });
const selectedImage = ref(null);
const imagePreview = ref('');
const uploading = ref(false);

// 加载公开作品：GET /api/works —— 公开端点，返回分页信封
const loadWorks = async () => {
  loading.value = true;
  errorMessage.value = '';
  try {
    const response = await worksAPI.getWorks({
      page: currentPage.value - 1,
      size: pageSize.value,
    });
    works.value = Array.isArray(response?.content) ? response.content : [];
    totalItems.value = typeof response?.total === 'number' ? response.total : works.value.length;
    if (typeof response?.page === 'number' && response.page + 1 !== currentPage.value) {
      currentPage.value = response.page + 1;
    }
  } catch (error) {
    works.value = [];
    totalItems.value = 0;
    errorMessage.value = error?.message || '作品列表加载失败，请稍后重试';
    ElMessage.error(errorMessage.value);
  } finally {
    loading.value = false;
  }
};

// 过滤后的作品（后端列表接口只支持分页，筛选与排序在当前页内进行）
const filteredWorks = computed(() => {
  let result = [...works.value];

  if (searchQuery.value) {
    const query = searchQuery.value.trim().toLowerCase();
    if (query) {
      result = result.filter((work) =>
        (work.title || '').toLowerCase().includes(query) ||
        (work.username || '').toLowerCase().includes(query) ||
        (work.userNickname || '').toLowerCase().includes(query) ||
        (work.description || '').toLowerCase().includes(query)
      );
    }
  }

  switch (sortBy.value) {
    case 'popular':
      result.sort((a, b) => (b.likeCount || 0) - (a.likeCount || 0));
      break;
    case 'commented':
      result.sort((a, b) => (b.commentCount || 0) - (a.commentCount || 0));
      break;
    default:
      result.sort((a, b) => new Date(b.createdAt || 0) - new Date(a.createdAt || 0));
      break;
  }

  return result;
});

// 处理搜索
const handleSearch = () => {
  currentPage.value = 1;
};

// 处理排序
const handleSort = () => {
  currentPage.value = 1;
};

// 重置筛选条件
const resetFilters = () => {
  searchQuery.value = '';
  sortBy.value = 'latest';
  currentPage.value = 1;
};

// 分页处理
const handleSizeChange = (size) => {
  pageSize.value = size;
  currentPage.value = 1;
  loadWorks();
};

const handleCurrentChange = (current) => {
  currentPage.value = current;
  loadWorks();
};

// 查看作品详情
const viewWorkDetail = (work) => {
  selectedWork.value = work;
  showDetailDialog.value = true;
};

// 关闭详情对话框
const closeDetailDialog = () => {
  showDetailDialog.value = false;
  selectedWork.value = null;
};

// 打开评论：GET /api/community/comments/work/{workId}
const openComments = async (work) => {
  selectedWork.value = work;
  commentText.value = '';
  commentError.value = '';
  comments.value = [];
  commentDialogVisible.value = true;
  commentLoading.value = true;
  try {
    const response = await communityAPI.getWorkComments(work.id, { page: 0, size: 50 });
    comments.value = Array.isArray(response?.content) ? response.content : [];
  } catch (error) {
    commentError.value = error?.message || '评论加载失败，请稍后重试';
  } finally {
    commentLoading.value = false;
  }
};

// 发表评论：POST /api/community/comments
const submitComment = async () => {
  const content = commentText.value.trim();
  if (!content) {
    ElMessage.warning('请输入评论内容');
    return;
  }
  if (!isLoggedIn.value) {
    ElMessage.warning('请先登录后再发表评论');
    router.push('/login');
    return;
  }

  commenting.value = true;
  try {
    await communityAPI.createComment({ workId: selectedWork.value.id, content });
    commentText.value = '';
    ElMessage.success('评论已发表');
    await openComments(selectedWork.value);
    await loadWorks();
    // 列表刷新了，详情弹窗里的评论数还是打开时那份快照 —— 用新列表里的条目替换掉
    const fresh = works.value.find((w) => w.id === selectedWork.value?.id);
    if (fresh) {
      selectedWork.value = fresh;
    }
  } catch (error) {
    ElMessage.error(error?.message || '评论发表失败，请稍后重试');
  } finally {
    commenting.value = false;
  }
};

// 打开上传对话框
const openUploadDialog = () => {
  if (!isLoggedIn.value) {
    ElMessage.warning('请先登录后再上传作品');
    router.push('/login');
    return;
  }
  uploadForm.value = { title: '', description: '' };
  selectedImage.value = null;
  imagePreview.value = '';
  uploadDialogVisible.value = true;
};

// 选择图片（只做本地校验，真正提交在 submitWork）
const handleImageSelected = (file) => {
  const raw = file?.raw;
  if (!raw) {
    return;
  }
  if (!['image/jpeg', 'image/png', 'image/webp'].includes(raw.type)) {
    ElMessage.error('图片仅支持 JPG / PNG / WebP 格式');
    return;
  }
  if (raw.size / 1024 / 1024 > 5) {
    ElMessage.error('图片大小不能超过 5MB');
    return;
  }
  selectedImage.value = raw;
  imagePreview.value = URL.createObjectURL(raw);
};

// 提交作品：POST /api/works（multipart）
const submitWork = async () => {
  const title = uploadForm.value.title.trim();
  if (!title) {
    ElMessage.warning('请填写作品标题');
    return;
  }

  const formData = new FormData();
  formData.append('title', title);
  formData.append('description', uploadForm.value.description || '');
  formData.append('content', uploadForm.value.description || title);
  if (selectedImage.value) {
    formData.append('image', selectedImage.value);
  }

  uploading.value = true;
  try {
    await worksAPI.createWork(formData);
    ElMessage.success('作品已提交，审核通过后会出现在公开画廊');
    uploadDialogVisible.value = false;
    await loadWorks();
  } catch (error) {
    ElMessage.error(error?.message || '作品上传失败，请稍后重试');
  } finally {
    uploading.value = false;
  }
};

// 工具函数
const authorName = (work) => work?.userNickname || work?.username || '匿名书友';

const authorInitial = (work) => {
  const name = authorName(work);
  return name ? name.charAt(0) : '书';
};

const commentAuthor = (comment) => comment?.userNickname || comment?.username || '匿名书友';

const truncateText = (text, maxLength) => {
  if (!text) return '';
  return text.length > maxLength ? text.substring(0, maxLength) + '...' : text;
};

const formatDate = (dateString) => {
  if (!dateString) return '';
  const date = new Date(dateString);
  if (Number.isNaN(date.getTime())) return '';
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`;
};

// 组件挂载时拉取真实数据
onMounted(() => {
  loadWorks();
});
</script>

<style scoped>
.gallery-container {
  padding: 2rem 0;
}

.gallery-header {
  text-align: center;
  margin-bottom: 2rem;
}

.section-title {
  font-size: 2rem;
  margin-bottom: 0.5rem;
  color: #333;
  font-family: 'SimSun', serif;
}

.section-subtitle {
  color: #666;
}

/* 筛选区域 */
.filter-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 0.5rem;
  gap: 1rem;
  flex-wrap: wrap;
}

.search-bar {
  flex: 1;
  min-width: 300px;
}

.filter-options {
  display: flex;
  gap: 1rem;
}

.filter-hint {
  color: #999;
  font-size: 0.85rem;
  margin-bottom: 1.5rem;
}

.state-alert {
  margin-bottom: 1.5rem;
}

/* 作品网格 */
.works-area {
  min-height: 12rem;
}

.works-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 2rem;
  margin-bottom: 2rem;
}

.work-card {
  background-color: rgba(255, 255, 255, 0.95);
  border-radius: 8px;
  overflow: hidden;
  cursor: pointer;
  /* 悬停只动位移与阴影 —— transition: all 会连宽高一起动，掉帧 */
  transition: transform 0.3s, box-shadow 0.3s;
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.05);
}

.work-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.1);
}

.work-thumbnail {
  height: 200px;
  position: relative;
  overflow: hidden;
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

.work-card:hover .work-image {
  transform: scale(1.05);
}

.work-overlay {
  position: absolute;
  right: 10px;
  bottom: 10px;
  display: flex;
  gap: 0.8rem;
  color: #fff;
  font-size: 0.85rem;
  text-shadow: 0 1px 3px rgba(0, 0, 0, 0.6);
}

.overlay-stat {
  display: flex;
  align-items: center;
  gap: 0.3rem;
}

.work-info {
  padding: 1.5rem;
}

.work-title {
  font-size: 1.2rem;
  margin-bottom: 1rem;
  color: #333;
  font-family: 'SimSun', serif;
}

.work-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1rem;
}

.author-info {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.author-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  object-fit: cover;
}

.author-avatar-fallback {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  background-color: #8B4513;
  color: #fff;
  font-size: 0.9rem;
}

.author-name {
  font-size: 0.9rem;
  color: #666;
}

.work-stats {
  display: flex;
  gap: 1rem;
  font-size: 0.9rem;
  color: #999;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 0.3rem;
}

.work-desc {
  color: #666;
  font-size: 0.9rem;
  line-height: 1.6;
  margin-bottom: 1rem;
}

.work-date {
  font-size: 0.8rem;
  color: #999;
  text-align: right;
}

/* 空状态 */
.empty-state {
  text-align: center;
  padding: 4rem 2rem;
  color: #999;
}

/* 分页 */
.pagination {
  display: flex;
  justify-content: center;
  margin-top: 2rem;
}

/* 上传按钮 */
.upload-button-container {
  position: fixed;
  bottom: 2rem;
  right: 2rem;
}

.floating-upload-btn {
  background-color: #8B4513;
  border-color: #8B4513;
  /* 全圆角胶囊：4 个汉字塞不进 50px 的正圆按钮，文字会溢出圆外 */
  height: 48px;
  padding: 0 1.25rem;
  border-radius: 24px;
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.2);
  /* 只过渡位移与颜色 —— transition: all 会连宽高一起动，掉帧 */
  transition: transform 0.3s, background-color 0.3s, border-color 0.3s;
}

.floating-upload-btn:hover {
  background-color: #6B340E;
  border-color: #6B340E;
  transform: scale(1.1);
}

/* 作品详情 */
.work-detail {
  display: flex;
  gap: 2rem;
}

.detail-image {
  flex: 1;
  max-width: 50%;
}

.full-image {
  width: 100%;
  border-radius: 8px;
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.1);
}

.full-image-placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 240px;
  background-color: #f0eee9;
  color: #999;
}

.detail-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.detail-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.detail-meta {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.author-section {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.author-avatar-large {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  object-fit: cover;
}

.author-details {
  flex: 1;
}

.author-name-large {
  font-size: 1.1rem;
  font-weight: bold;
  color: #333;
}

.publish-date {
  color: #999;
  font-size: 0.9rem;
}

.detail-stats {
  display: flex;
  gap: 2rem;
}

.stat-box {
  text-align: center;
}

.stat-number {
  font-size: 1.5rem;
  font-weight: bold;
  color: #8B4513;
}

.stat-label {
  color: #666;
  font-size: 0.9rem;
}

.detail-description h4 {
  margin-bottom: 0.5rem;
  color: #333;
}

.detail-description p {
  line-height: 1.8;
  color: #666;
}

.detail-actions {
  display: flex;
  gap: 1rem;
  margin-top: auto;
}

/* 评论 */
.comment-area {
  min-height: 8rem;
  max-height: 22rem;
  overflow-y: auto;
}

.comment-list {
  list-style: none;
  margin: 0;
  padding: 0;
}

.comment-item {
  padding: 0.8rem 0;
  border-bottom: 1px solid #f0f0f0;
}

.comment-author {
  font-weight: bold;
  color: #333;
  font-size: 0.9rem;
}

.comment-content {
  color: #555;
  line-height: 1.6;
  margin: 0.3rem 0;
  white-space: pre-wrap;
  word-break: break-word;
}

.comment-date {
  color: #999;
  font-size: 0.8rem;
}

/* 上传 */
.upload-preview {
  display: block;
  margin-top: 0.8rem;
  max-width: 220px;
  border-radius: 6px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .filter-section {
    flex-direction: column;
    align-items: stretch;
  }

  .filter-options {
    justify-content: center;
  }

  .works-grid {
    grid-template-columns: 1fr;
  }

  .work-detail {
    flex-direction: column;
  }

  .detail-image {
    max-width: 100%;
  }

  .detail-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 1rem;
  }

  .author-section {
    flex-wrap: wrap;
  }

  .detail-stats {
    justify-content: space-around;
  }
}
</style>
