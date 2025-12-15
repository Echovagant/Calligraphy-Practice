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
      />
      
      <el-select v-model="filterStyle" placeholder="筛选书法风格" class="filter-select">
        <el-option label="全部" value="" />
        <el-option label="楷书" value="regular" />
        <el-option label="行书" value="running" />
        <el-option label="草书" value="cursive" />
        <el-option label="隶书" value="official" />
        <el-option label="篆书" value="seal" />
      </el-select>
      
      <el-select v-model="sortBy" placeholder="排序方式" class="filter-select">
        <el-option label="最新上传" value="newest" />
        <el-option label="最多点赞" value="likes" />
        <el-option label="最多评论" value="comments" />
      </el-select>
      
      <el-button type="primary" @click="refreshWorks">
        <i class="el-icon-refresh"></i> 刷新列表
      </el-button>
    </div>
    
    <div class="works-grid" v-if="filteredWorks.length > 0">
      <div v-for="work in filteredWorks" :key="work.id" class="work-item">
        <div class="work-image-wrapper">
          <img :src="work.imageUrl" :alt="work.title" class="work-image" />
          <div class="work-overlay">
            <div class="work-actions">
              <el-button type="primary" size="small" @click="viewWorkDetail(work)">
                <i class="el-icon-view"></i> 查看
              </el-button>
              <el-button type="success" size="small" @click="editWork(work)">
                <i class="el-icon-edit"></i> 编辑
              </el-button>
              <el-button type="danger" size="small" @click="deleteWork(work.id)">
                <i class="el-icon-delete"></i> 删除
              </el-button>
            </div>
          </div>
        </div>
        
        <div class="work-info">
          <h3 class="work-title">{{ work.title }}</h3>
          <p class="work-style">{{ getStyleName(work.style) }}</p>
          
          <div class="work-stats">
            <div class="stat-item">
              <i class="el-icon-star-on"></i>
              <span>{{ work.likesCount || 0 }}</span>
            </div>
            <div class="stat-item">
              <i class="el-icon-chat-dot-round"></i>
              <span>{{ work.commentsCount || 0 }}</span>
            </div>
            <div class="stat-item">
              <i class="el-icon-eye"></i>
              <span>{{ work.viewsCount || 0 }}</span>
            </div>
          </div>
          
          <div class="work-meta">
            <span class="upload-time">{{ formatDate(work.createdAt) }}</span>
            <el-switch
              v-model="work.isPublic"
              active-color="#8B4513"
              inactive-color="#dcdfe6"
              @change="toggleWorkVisibility(work.id, work.isPublic)"
              size="small"
              class="visibility-switch"
            />
          </div>
        </div>
      </div>
    </div>
    
    <div v-else class="empty-state">
      <el-empty description="暂无作品" :image-size="120">
        <template #image>
          <img src="https://picsum.photos/seed/empty-works/300/300" alt="暂无作品" />
        </template>
        <el-button type="primary" @click="router.push('/gallery')">去作品展示</el-button>
      </el-empty>
    </div>
    
    <!-- 分页 -->
    <div class="pagination-container" v-if="totalPages > 1">
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
      :title="selectedWork.title"
      width="80%"
      :before-close="closeDialog"
    >
      <div class="work-detail">
        <div class="detail-image-wrapper">
          <img :src="selectedWork.imageUrl" :alt="selectedWork.title" class="detail-image" />
        </div>
        <div class="detail-info">
          <h3 class="detail-title">{{ selectedWork.title }}</h3>
          <div class="detail-meta">
            <span class="detail-style">{{ getStyleName(selectedWork.style) }}</span>
            <span class="detail-date">{{ formatDate(selectedWork.createdAt) }}</span>
          </div>
          <div class="detail-description">{{ selectedWork.description || '暂无描述' }}</div>
          <div class="detail-stats">
            <div class="detail-stat-item">
              <i class="el-icon-star-on"></i>
              <span>{{ selectedWork.likesCount || 0 }} 点赞</span>
            </div>
            <div class="detail-stat-item">
              <i class="el-icon-chat-dot-round"></i>
              <span>{{ selectedWork.commentsCount || 0 }} 评论</span>
            </div>
            <div class="detail-stat-item">
              <i class="el-icon-eye"></i>
              <span>{{ selectedWork.viewsCount || 0 }} 浏览</span>
            </div>
          </div>
        </div>
      </div>
      
      <template #footer>
        <el-button @click="closeDialog">关闭</el-button>
        <el-button type="primary" @click="shareWork(selectedWork.id)">分享作品</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';

// 状态管理 - userStore已废弃，使用后端API获取用户信息
const router = useRouter();



// 搜索和筛选
const searchKeyword = ref('');
const filterStyle = ref('');
const sortBy = ref('newest');

// 分页
const currentPage = ref(1);
const pageSize = ref(8);
const totalItems = ref(0);

// 作品数据 - 模拟数据
const myWorks = ref([
  {
    id: 1,
    title: '宁静致远',
    description: '行书作品，表达内心的平静与追求',
    imageUrl: 'https://picsum.photos/id/1/600/800',
    style: 'running',
    createdAt: '2024-01-15T10:30:00Z',
    likesCount: 12,
    commentsCount: 3,
    viewsCount: 89,
    isPublic: true
  },
  {
    id: 2,
    title: '道法自然',
    description: '楷书创作，体现中国传统哲学思想',
    imageUrl: 'https://picsum.photos/id/2/600/800',
    style: 'regular',
    createdAt: '2024-01-10T14:20:00Z',
    likesCount: 25,
    commentsCount: 7,
    viewsCount: 143,
    isPublic: true
  },
  {
    id: 3,
    title: '梅兰竹菊',
    description: '隶书作品，展示四君子的高雅品质',
    imageUrl: 'https://picsum.photos/id/3/600/800',
    style: 'official',
    createdAt: '2024-01-05T09:15:00Z',
    likesCount: 18,
    commentsCount: 5,
    viewsCount: 97,
    isPublic: false
  }
]);

// 对话框
const dialogVisible = ref(false);
const selectedWork = reactive({
  id: null,
  title: '',
  description: '',
  imageUrl: '',
  style: '',
  createdAt: '',
  likesCount: 0,
  commentsCount: 0,
  viewsCount: 0
});

// 计算属性
const filteredWorks = computed(() => {
  let filtered = [...myWorks.value];
  
  // 关键词搜索
  if (searchKeyword.value) {
    const keyword = searchKeyword.value.toLowerCase();
    filtered = filtered.filter(work => 
      work.title.toLowerCase().includes(keyword) ||
      (work.description && work.description.toLowerCase().includes(keyword))
    );
  }
  
  // 风格筛选
  if (filterStyle.value) {
    filtered = filtered.filter(work => work.style === filterStyle.value);
  }
  
  // 排序
  filtered.sort((a, b) => {
    switch (sortBy.value) {
      case 'newest':
        return new Date(b.createdAt) - new Date(a.createdAt);
      case 'likes':
        return (b.likesCount || 0) - (a.likesCount || 0);
      case 'comments':
        return (b.commentsCount || 0) - (a.commentsCount || 0);
      default:
        return 0;
    }
  });
  
  // 避免在computed中直接修改ref值
  // const totalFiltered = filtered.length;
  
  // 分页
  const start = (currentPage.value - 1) * pageSize.value;
  const end = start + pageSize.value;
  return filtered.slice(start, end);
});

// 统计数据 - 使用纯函数计算，避免副作用
const calculateTotalWorks = () => myWorks.value.length;

const calculateWorksByStyle = () => {
  const styleCount = {};
  myWorks.value.forEach(work => {
    if (!styleCount[work.style]) {
      styleCount[work.style] = 0;
    }
    styleCount[work.style]++;
  });
  return styleCount;
};

const totalPages = computed(() => {
  let filtered = [...myWorks.value];
  
  // 关键词搜索
  if (searchKeyword.value) {
    const keyword = searchKeyword.value.toLowerCase();
    filtered = filtered.filter(work => 
      work.title.toLowerCase().includes(keyword) ||
      (work.description && work.description.toLowerCase().includes(keyword))
    );
  }
  
  // 风格筛选
  if (filterStyle.value) {
    filtered = filtered.filter(work => work.style === filterStyle.value);
  }
  
  return Math.ceil(filtered.length / pageSize.value);
});

// 统计数据
const totalWorks = ref(0);
const worksByStyle = ref({});

// 生命周期钩子
onMounted(() => {
  loadMyWorks();
  // 初始化统计数据
  totalWorks.value = calculateTotalWorks();
  worksByStyle.value = calculateWorksByStyle();
});

// 加载我的作品
const loadMyWorks = async () => {
  try {
    // 在实际项目中，这里应该调用API获取用户作品
    // const works = await userStore.fetchMyWorks();
    // myWorks.value = works;
    
    // 这里使用模拟数据
    console.log('加载我的作品');
  } catch (error) {
    console.error('加载作品失败:', error);
    ElMessage.error('加载作品失败，请稍后重试');
  }
};

// 刷新作品列表
const refreshWorks = () => {
  currentPage.value = 1;
  loadMyWorks();
  // 更新统计数据
  totalWorks.value = calculateTotalWorks();
  worksByStyle.value = calculateWorksByStyle();
  ElMessage.success('列表已刷新');
};

// 查看作品详情
const viewWorkDetail = (work) => {
  Object.assign(selectedWork, work);
  dialogVisible.value = true;
};

// 编辑作品
const editWork = (work) => {
  // 在实际项目中，这里应该跳转到编辑页面或打开编辑对话框
  ElMessage.info('编辑功能开发中');
  console.log('编辑作品:', work);
};

// 删除作品
const deleteWork = async (workId) => {
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
    
    // 在实际项目中，这里应该调用API删除作品
    const index = myWorks.value.findIndex(work => work.id === workId);
    if (index !== -1) {
      myWorks.value.splice(index, 1);
      ElMessage.success('作品已删除');
    }
  } catch (error) {
    // 如果用户取消确认，则不处理错误
    if (error !== 'cancel') {
      console.error('删除作品失败:', error);
      ElMessage.error('删除作品失败，请稍后重试');
    }
  }
};

// 切换作品可见性
const toggleWorkVisibility = async (workId, isPublic) => {
  try {
    // 在实际项目中，这里应该调用API更新作品可见性
    const work = myWorks.value.find(work => work.id === workId);
    if (work) {
      work.isPublic = isPublic;
      ElMessage.success(isPublic ? '作品已公开' : '作品已设为私有');
    }
  } catch (error) {
    console.error('更新作品可见性失败:', error);
    ElMessage.error('操作失败，请稍后重试');
  }
};

// 分享作品
const shareWork = (workId) => {
  // 在实际项目中，这里应该生成分享链接或打开分享面板
  ElMessage.success('分享链接已复制到剪贴板');
  console.log('分享作品:', workId);
};

// 关闭对话框
const closeDialog = () => {
  dialogVisible.value = false;
  // 重置选中的作品
  Object.keys(selectedWork).forEach(key => {
    selectedWork[key] = key === 'likesCount' || key === 'commentsCount' || key === 'viewsCount' ? 0 : '';
  });
};

// 分页处理
const handleSizeChange = (newSize) => {
  pageSize.value = newSize;
  currentPage.value = 1;
};

const handleCurrentChange = (newCurrent) => {
  currentPage.value = newCurrent;
};

// 工具函数
const getStyleName = (style) => {
  const styleMap = {
    'regular': '楷书',
    'running': '行书',
    'cursive': '草书',
    'official': '隶书',
    'seal': '篆书'
  };
  return styleMap[style] || style;
};

const formatDate = (dateString) => {
  const date = new Date(dateString);
  return new Intl.DateTimeFormat('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  }).format(date);
};
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
  margin-bottom: 2rem;
  flex-wrap: wrap;
  align-items: center;
}

.search-input {
  flex: 1;
  min-width: 200px;
}

.filter-select {
  min-width: 150px;
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

.work-style {
  color: #8B4513;
  font-size: 0.9rem;
  margin-bottom: 0.5rem;
}

.work-stats {
  display: flex;
  gap: 1rem;
  margin-bottom: 0.5rem;
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