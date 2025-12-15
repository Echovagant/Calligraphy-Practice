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
        <el-select v-model="selectedCategory" placeholder="分类" @change="handleFilter">
          <el-option label="全部" value="all" />
          <el-option label="楷书" value="kai" />
          <el-option label="行书" value="xing" />
          <el-option label="草书" value="cao" />
          <el-option label="隶书" value="li" />
          <el-option label="篆书" value="zhuan" />
        </el-select>
        
        <el-select v-model="sortBy" placeholder="排序" @change="handleSort">
          <el-option label="最新上传" value="latest" />
          <el-option label="最多点赞" value="popular" />
          <el-option label="最多评论" value="commented" />
        </el-select>
      </div>
    </div>
    
    <!-- 作品网格 -->
    <div class="works-grid">
      <div 
      v-for="work in filteredWorks" 
      :key="work.id" 
      class="work-card"
      @click="viewWorkDetail(work.id)"
    >
      <div class="work-thumbnail">
        <img :src="work.imageUrl" :alt="work.title" class="work-image" />
        <div class="work-category" :class="work.category">
          {{ getCategoryName(work.category) }}
        </div>
        <div class="work-overlay">
          <button class="overlay-btn" @click.stop="handleLike(work)" :class="{ active: work.isLiked }">
            <i class="el-icon-thumb-up"></i>
            {{ work.likes }}
          </button>
          <button class="overlay-btn" @click.stop="handleComment(work)">
            <i class="el-icon-chat-dot-round"></i>
            {{ work.comments }}
          </button>
          <button class="overlay-btn" @click.stop="handleFavorite(work)" :class="{ active: work.isFavorite }">
            <i class="el-icon-star-off"></i>
          </button>
        </div>
      </div>
      
      <div class="work-info">
        <h3 class="work-title">{{ work.title }}</h3>
        
        <div class="work-meta">
          <div class="author-info">
            <img :src="work.authorAvatar" :alt="work.authorName" class="author-avatar" />
            <span class="author-name">{{ work.authorName }}</span>
          </div>
          
          <div class="work-stats">
            <span class="stat-item">
              <i class="el-icon-thumb-up"></i>
              {{ work.likes }}
            </span>
            <span class="stat-item">
              <i class="el-icon-chat-dot-round"></i>
              {{ work.comments }}
            </span>
            <span class="stat-item">
              <i class="el-icon-view"></i>
              {{ work.views }}
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
    
    <!-- 空状态 -->
    <div v-if="filteredWorks.length === 0" class="empty-state">
      <div class="empty-icon">🖼️</div>
      <p>暂无符合条件的作品</p>
      <el-button type="primary" @click="resetFilters">重置筛选条件</el-button>
    </div>
    
    <!-- 分页 -->
    <div class="pagination" v-if="filteredWorks.length > 0">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :page-sizes="[12, 24, 36]"
        layout="total, sizes, prev, pager, next, jumper"
        :total="filteredWorks.length"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>
    
    <!-- 上传按钮 -->
    <div class="upload-button-container">
      <el-button
        type="primary"
        icon="el-icon-plus"
        circle
        class="floating-upload-btn"
        @click="goToUpload"
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
          <img :src="selectedWork.imageUrl" :alt="selectedWork.title" class="full-image" />
        </div>
        
        <div class="detail-info">
          <div class="detail-header">
            <h3>{{ selectedWork.title }}</h3>
            <div class="detail-category" :class="selectedWork.category">
              {{ getCategoryName(selectedWork.category) }}
            </div>
          </div>
          
          <div class="detail-meta">
            <div class="author-section">
              <img :src="selectedWork.authorAvatar" :alt="selectedWork.authorName" class="author-avatar-large" />
              <div class="author-details">
                <div class="author-name-large">{{ selectedWork.authorName }}</div>
                <div class="publish-date">{{ formatDate(selectedWork.createdAt) }}</div>
              </div>
              <el-button type="primary" size="small" plain>关注作者</el-button>
            </div>
            
            <div class="detail-stats">
              <div class="stat-box">
                <div class="stat-number">{{ selectedWork.likes }}</div>
                <div class="stat-label">点赞</div>
              </div>
              <div class="stat-box">
                <div class="stat-number">{{ selectedWork.comments }}</div>
                <div class="stat-label">评论</div>
              </div>
              <div class="stat-box">
                <div class="stat-number">{{ selectedWork.views }}</div>
                <div class="stat-label">浏览</div>
              </div>
            </div>
          </div>
          
          <div class="detail-description">
            <h4>作品描述</h4>
            <p>{{ selectedWork.description }}</p>
          </div>
          
          <div class="detail-actions">
            <el-button 
              type="primary" 
              icon="el-icon-thumb-up" 
              :class="{ active: selectedWork.isLiked }"
              @click="handleLike(selectedWork)"
            >
              {{ selectedWork.isLiked ? '已点赞' : '点赞' }}
            </el-button>
            <el-button 
              icon="el-icon-chat-dot-round" 
              @click="handleComment(selectedWork)"
            >
              评论
            </el-button>
            <el-button 
              icon="el-icon-star-off" 
              :class="{ active: selectedWork.isFavorite }"
              @click="handleFavorite(selectedWork)"
            >
              {{ selectedWork.isFavorite ? '已收藏' : '收藏' }}
            </el-button>
            <el-button 
              icon="el-icon-share" 
              @click="handleShare(selectedWork)"
            >
              分享
            </el-button>
            <el-button 
              icon="el-icon-warning" 
              type="danger" 
              @click="handleReport(selectedWork)"
            >
              举报
            </el-button>
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { ElMessage } from 'element-plus';

// 状态管理
const searchQuery = ref('');
const selectedCategory = ref('all');
const sortBy = ref('latest');
const currentPage = ref(1);
const pageSize = ref(12);
const showDetailDialog = ref(false);
const selectedWork = ref(null);

// 状态管理 - userStore已废弃，使用后端API获取用户信息

// 模拟作品数据
const works = ref([
  {
    id: 1,
    title: '宁静致远',
    description: '这幅作品展现了宁静致远的意境，通过流畅的线条和和谐的布局，表达了书法家对平静生活的向往和追求。',
    imageUrl: 'https://picsum.photos/seed/calligraphy1/800/600',
    category: 'kai',
    authorName: '王羲之',
    authorAvatar: 'https://picsum.photos/seed/author1/100/100',
    likes: 256,
    comments: 32,
    views: 1240,
    createdAt: '2024-01-15T10:30:00Z'
  },
  {
    id: 2,
    title: '厚德载物',
    description: '以楷书书写的经典名句，字体端庄大方，结构严谨，体现了中华传统文化的深厚底蕴。',
    imageUrl: 'https://picsum.photos/seed/calligraphy2/800/600',
    category: 'xing',
    authorName: '颜真卿',
    authorAvatar: 'https://picsum.photos/seed/author2/100/100',
    likes: 189,
    comments: 24,
    views: 956,
    createdAt: '2024-01-12T14:20:00Z'
  },
  {
    id: 3,
    title: '紫气东来',
    description: '行草结合，笔势流畅，气势磅礴，展现了书法家深厚的功力和独特的艺术风格。',
    imageUrl: 'https://picsum.photos/seed/calligraphy3/800/600',
    category: 'cao',
    authorName: '柳公权',
    authorAvatar: 'https://picsum.photos/seed/author3/100/100',
    likes: 324,
    comments: 45,
    views: 1567,
    createdAt: '2024-01-10T09:15:00Z'
  },
  {
    id: 4,
    title: '天道酬勤',
    description: '隶书作品，古朴典雅，结构扁平，蚕头燕尾，体现了汉代隶书的典型特征。',
    imageUrl: 'https://picsum.photos/seed/calligraphy4/800/600',
    category: 'li',
    authorName: '欧阳询',
    authorAvatar: 'https://picsum.photos/seed/author4/100/100',
    likes: 210,
    comments: 28,
    views: 1023,
    createdAt: '2024-01-08T16:45:00Z'
  },
  {
    id: 5,
    title: '上善若水',
    description: '篆书作品，线条圆润，结构严谨，体现了中国古代文字的独特美感和艺术价值。',
    imageUrl: 'https://picsum.photos/seed/calligraphy5/800/600',
    category: 'zhuan',
    authorName: '米芾',
    authorAvatar: 'https://picsum.photos/seed/author5/100/100',
    likes: 178,
    comments: 21,
    views: 876,
    createdAt: '2024-01-05T11:20:00Z'
  },
  {
    id: 6,
    title: '家和万事兴',
    description: '楷书作品，字体端庄秀丽，笔力雄健，表达了对家庭和睦的美好祝愿。',
    imageUrl: 'https://picsum.photos/seed/calligraphy6/800/600',
    category: 'kai',
    authorName: '苏轼',
    authorAvatar: 'https://picsum.photos/seed/author6/100/100',
    likes: 234,
    comments: 35,
    views: 1156,
    createdAt: '2024-01-03T13:50:00Z'
  }
]);

// 过滤后的作品
const filteredWorks = computed(() => {
  let result = [...works.value];
  
  // 按分类过滤
  if (selectedCategory.value !== 'all') {
    result = result.filter(work => work.category === selectedCategory.value);
  }
  
  // 按搜索词过滤
  if (searchQuery.value) {
    const query = searchQuery.value.toLowerCase();
    result = result.filter(work => 
      work.title.toLowerCase().includes(query) ||
      work.authorName.toLowerCase().includes(query) ||
      work.description.toLowerCase().includes(query)
    );
  }
  
  // 排序
  switch (sortBy.value) {
    case 'latest':
      result.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt));
      break;
    case 'popular':
      result.sort((a, b) => b.likes - a.likes);
      break;
    case 'commented':
      result.sort((a, b) => b.comments - a.comments);
      break;
  }
  
  return result;
});

// 处理搜索
const handleSearch = () => {
  currentPage.value = 1;
};

// 处理筛选
const handleFilter = () => {
  currentPage.value = 1;
};

// 处理排序
const handleSort = () => {
  currentPage.value = 1;
};

// 分页处理
const handleSizeChange = (size) => {
  pageSize.value = size;
  currentPage.value = 1;
};

const handleCurrentChange = (current) => {
  currentPage.value = current;
};

// 重置筛选条件
const resetFilters = () => {
  searchQuery.value = '';
  selectedCategory.value = 'all';
  sortBy.value = 'latest';
  currentPage.value = 1;
};

// 查看作品详情
const viewWorkDetail = (workId) => {
  const work = works.value.find(w => w.id === workId);
  if (work) {
    selectedWork.value = work;
    showDetailDialog.value = true;
    // 增加浏览量
    work.views++;
  }
};

// 关闭详情对话框
const closeDetailDialog = () => {
  showDetailDialog.value = false;
  selectedWork.value = null;
};

// 前往上传页面
const goToUpload = () => {
  // 这里应该跳转到上传页面，目前显示提示
  ElMessage.info('上传功能开发中');
};

// 工具函数
const getCategoryName = (category) => {
  const categories = {
    kai: '楷书',
    xing: '行书',
    cao: '草书',
    li: '隶书',
    zhuan: '篆书'
  };
  return categories[category] || '其他';
};

const truncateText = (text, maxLength) => {
  if (!text) return '';
  return text.length > maxLength ? text.substring(0, maxLength) + '...' : text;
};

const formatDate = (dateString) => {
  const date = new Date(dateString);
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`;
};



// 组件挂载时初始化数据
onMounted(() => {
  // 模拟从服务器加载数据
  console.log('加载作品数据');
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
  margin-bottom: 2rem;
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

/* 作品网格 */
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
  transition: all 0.3s;
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

.work-card:hover .work-image {
  transform: scale(1.05);
}

.work-category {
  position: absolute;
  top: 10px;
  right: 10px;
  padding: 0.3rem 0.8rem;
  border-radius: 4px;
  font-size: 0.8rem;
  color: white;
}

.work-category.kai { background-color: #8B4513; }
.work-category.xing { background-color: #A0522D; }
.work-category.cao { background-color: #CD853F; }
.work-category.li { background-color: #DEB887; }
.work-category.zhuan { background-color: #F4A460; }

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

.empty-icon {
  font-size: 4rem;
  margin-bottom: 1rem;
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
  width: 50px;
  height: 50px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.2);
  transition: all 0.3s;
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

.detail-category {
  padding: 0.4rem 1rem;
  border-radius: 4px;
  font-size: 0.9rem;
  color: white;
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