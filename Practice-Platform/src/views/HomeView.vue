<template>
  <div class="home-container">
    <!-- 英雄区域 -->
    <section class="hero-section">
      <div class="hero-content">
        <h1 class="hero-title">传承书法艺术，弘扬中华文化</h1>
        <p class="hero-subtitle">通过在线平台，随时随地练习书法，提升艺术修养</p>
        <div class="hero-actions">
          <el-button type="primary" size="large" @click="handleGoToPractice">
            开始练习
          </el-button>
          <el-button type="success" size="large" @click="handleGoToCopybook">
            生成字帖
          </el-button>
        </div>
      </div>
    </section>

    <!-- 平台数据 -->
    <section class="stats-section">
      <h2 class="section-title">平台数据</h2>
      <div class="stats-area" v-loading="statsLoading">
        <el-alert
          v-if="statsError"
          type="error"
          :title="statsError"
          show-icon
          :closable="false"
        >
          <template #default>
            <el-button type="primary" size="small" @click="loadOverview">重新加载</el-button>
          </template>
        </el-alert>
        <div v-else class="stats-grid">
          <div class="stat-card">
            <div class="stat-number">{{ overview.totalWorks }}</div>
            <div class="stat-label">收录作品</div>
          </div>
          <div class="stat-card">
            <div class="stat-number">{{ overview.totalUsers }}</div>
            <div class="stat-label">注册用户</div>
          </div>
          <div class="stat-card">
            <div class="stat-number">{{ overview.totalPracticeRecords }}</div>
            <div class="stat-label">练习记录</div>
          </div>
        </div>
      </div>
    </section>

    <!-- 功能特点 -->
    <section class="features-section">
      <h2 class="section-title">平台特色</h2>
      <div class="features-grid">
        <div class="feature-card">
          <div class="feature-icon">📝</div>
          <h3 class="feature-title">智能练习</h3>
          <p class="feature-desc">使用Fabric.js提供真实的毛笔书写体验，支持多种笔刷效果</p>
          <el-button type="primary" @click="handleGoToPractice">开始练习</el-button>
        </div>
        <div class="feature-card">
          <div class="feature-icon">📚</div>
          <h3 class="feature-title">字帖生成</h3>
          <p class="feature-desc">根据用户需求生成个性化字帖，支持多种字体风格选择</p>
          <el-button type="primary" @click="handleGoToCopybook">生成字帖</el-button>
        </div>
        <div class="feature-card">
          <div class="feature-icon">🖼️</div>
          <h3 class="feature-title">作品展示</h3>
          <p class="feature-desc">展示个人书法作品，获得其他用户的欣赏和评价</p>
          <el-button type="primary" @click="router.push('/gallery')">浏览作品</el-button>
        </div>
        <div class="feature-card">
          <div class="feature-icon">👥</div>
          <h3 class="feature-title">社区交流</h3>
          <p class="feature-desc">与其他书法爱好者交流学习心得，共同进步</p>
          <el-button type="primary" @click="router.push('/community')">进入社区</el-button>
        </div>
      </div>
    </section>

    <!-- 最新作品预览 -->
    <section class="preview-section">
      <div class="section-header">
        <h2 class="section-title">精选作品</h2>
        <router-link to="/gallery" class="view-more">查看更多 →</router-link>
      </div>
      <div class="works-area" v-loading="worksLoading">
        <el-alert
          v-if="worksError"
          type="error"
          :title="worksError"
          show-icon
          :closable="false"
        >
          <template #default>
            <el-button type="primary" size="small" @click="loadFeaturedWorks">重新加载</el-button>
          </template>
        </el-alert>
        <el-empty v-else-if="!worksLoading && featuredWorks.length === 0" description="暂无公开作品" />
        <div v-else class="works-grid">
          <div class="work-card" v-for="work in featuredWorks" :key="work.id">
            <div class="work-thumbnail">
              <img v-if="work.imageUrl" :src="work.imageUrl" :alt="work.title" class="work-image" />
              <div v-else class="work-image work-image-placeholder">暂无图片</div>
            </div>
            <div class="work-info">
              <h3 class="work-title">{{ work.title }}</h3>
              <p class="work-author">作者：{{ work.userNickname || work.username || '匿名书友' }}</p>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- 热门字帖 -->
    <section class="copybooks-section">
      <div class="section-header">
        <h2 class="section-title">热门字帖</h2>
        <router-link to="/copybook" class="view-more">去练习 →</router-link>
      </div>
      <div class="copybooks-area" v-loading="statsLoading">
        <el-empty
          v-if="!statsLoading && !statsError && popularCopybooks.length === 0"
          description="暂无热门字帖"
        />
        <div v-else class="copybooks-grid">
          <div class="copybook-card" v-for="book in popularCopybooks" :key="book.id">
            <div class="copybook-thumbnail">
              <img v-if="book.imageUrl" :src="book.imageUrl" :alt="book.title" class="copybook-image" />
              <div v-else class="copybook-image copybook-image-placeholder">暂无图片</div>
            </div>
            <div class="copybook-info">
              <h3 class="copybook-title">{{ book.title }}</h3>
              <p class="copybook-desc">{{ truncateText(book.description, 40) }}</p>
              <div class="copybook-meta">
                <span class="meta-item">{{ book.difficulty || '难度未知' }}</span>
                <span class="meta-item">{{ book.characterCount || 0 }} 字</span>
                <span class="meta-item">练习 {{ book.usageCount || 0 }} 次</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { statsAPI, worksAPI, getToken } from '../utils/api';
import { useUserStore } from '../stores/userStore';

const router = useRouter();
const userStore = useUserStore();

// 概览统计：GET /api/stats/overview
const overview = ref({
  totalWorks: 0,
  totalUsers: 0,
  totalPracticeRecords: 0
});
const popularCopybooks = ref([]);
const statsLoading = ref(false);
const statsError = ref('');

// 精选作品：GET /api/works
const featuredWorks = ref([]);
const worksLoading = ref(false);
const worksError = ref('');

// 是否已登录：store 状态 + 本地是否留有令牌
const isLoggedIn = computed(() => userStore.isLoggedIn || Boolean(getToken()));

// 加载首页概览与热门字帖
const loadOverview = async () => {
  statsLoading.value = true;
  statsError.value = '';
  try {
    const response = await statsAPI.getOverview();
    overview.value = {
      totalWorks: response?.totalWorks ?? 0,
      totalUsers: response?.totalUsers ?? 0,
      totalPracticeRecords: response?.totalPracticeRecords ?? 0
    };
    popularCopybooks.value = Array.isArray(response?.popularCopybooks) ? response.popularCopybooks : [];
  } catch (error) {
    overview.value = { totalWorks: 0, totalUsers: 0, totalPracticeRecords: 0 };
    popularCopybooks.value = [];
    statsError.value = error?.message || '平台数据加载失败，请稍后重试';
    ElMessage.error(statsError.value);
  } finally {
    statsLoading.value = false;
  }
};

// 加载精选作品
const loadFeaturedWorks = async () => {
  worksLoading.value = true;
  worksError.value = '';
  try {
    const response = await worksAPI.getWorks({ page: 0, size: 4 });
    featuredWorks.value = Array.isArray(response?.content) ? response.content : [];
  } catch (error) {
    featuredWorks.value = [];
    worksError.value = error?.message || '精选作品加载失败，请稍后重试';
    ElMessage.error(worksError.value);
  } finally {
    worksLoading.value = false;
  }
};

const handleGoToPractice = () => {
  if (!isLoggedIn.value) {
    ElMessage.warning('请先登录');
    router.push('/login');
    return;
  }
  router.push('/practice');
};

const handleGoToCopybook = () => {
  router.push('/copybook');
};

const truncateText = (text, maxLength) => {
  if (!text) return '';
  return text.length > maxLength ? text.substring(0, maxLength) + '...' : text;
};

onMounted(() => {
  loadOverview();
  loadFeaturedWorks();
});
</script>

<style scoped>
.home-container {
  padding: 2rem 0;
}

/* 英雄区域样式 */
.hero-section {
  text-align: center;
  padding: 4rem 2rem;
  margin-bottom: 2rem;
  background-color: rgba(245, 245, 240, 0.8);
  border-radius: 8px;
}

.hero-title {
  font-size: 2.5rem;
  margin-bottom: 1rem;
  color: #333;
  font-family: 'SimSun', serif;
  letter-spacing: 3px;
}

.hero-subtitle {
  font-size: 1.2rem;
  margin-bottom: 2rem;
  color: #666;
}

.hero-actions {
  display: flex;
  gap: 1rem;
  justify-content: center;
  flex-wrap: wrap;
}

/* 平台数据 */
.stats-section {
  margin-bottom: 3rem;
}

.stats-area {
  min-height: 6rem;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 1.5rem;
}

.stat-card {
  background-color: rgba(255, 255, 255, 0.9);
  border-radius: 8px;
  padding: 1.5rem;
  text-align: center;
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.05);
}

.stat-number {
  font-size: 2rem;
  font-weight: bold;
  color: #8B4513;
}

.stat-label {
  color: #666;
  font-size: 0.95rem;
  margin-top: 0.3rem;
}

/* 功能特点样式 */
.features-section {
  margin-bottom: 3rem;
}

.section-title {
  text-align: center;
  font-size: 2rem;
  margin-bottom: 2rem;
  color: #333;
  font-family: 'SimSun', serif;
}

.features-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 2rem;
}

.feature-card {
  background-color: rgba(255, 255, 255, 0.9);
  padding: 2rem;
  border-radius: 8px;
  text-align: center;
  transition: all 0.3s;
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.05);
}

.feature-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.1);
}

.feature-icon {
  font-size: 3rem;
  margin-bottom: 1rem;
}

.feature-title {
  font-size: 1.3rem;
  margin-bottom: 0.5rem;
  color: #333;
}

.feature-desc {
  color: #666;
}

/* 作品预览样式 */
.preview-section {
  margin-bottom: 3rem;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 2rem;
}

.view-more {
  color: #8B4513;
  text-decoration: none;
  transition: color 0.3s;
}

.view-more:hover {
  color: #6B340E;
}

.works-area {
  min-height: 8rem;
}

.works-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
  gap: 1.5rem;
}

.work-card {
  background-color: rgba(255, 255, 255, 0.9);
  border-radius: 8px;
  overflow: hidden;
  transition: all 0.3s;
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.05);
}

.work-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.1);
}

.work-thumbnail {
  height: 180px;
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

.work-info {
  padding: 1rem;
}

.work-title {
  font-size: 1.1rem;
  margin-bottom: 0.5rem;
  color: #333;
}

.work-author {
  font-size: 0.9rem;
  color: #666;
}

/* 热门字帖 */
.copybooks-section {
  margin-bottom: 3rem;
}

.copybooks-area {
  min-height: 8rem;
}

.copybooks-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  gap: 1.5rem;
}

.copybook-card {
  background-color: rgba(255, 255, 255, 0.9);
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.05);
  transition: all 0.3s;
}

.copybook-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.1);
}

.copybook-thumbnail {
  height: 160px;
  overflow: hidden;
}

.copybook-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.copybook-image-placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #f0eee9;
  color: #999;
  font-size: 0.9rem;
}

.copybook-info {
  padding: 1rem;
}

.copybook-title {
  font-size: 1.05rem;
  margin: 0 0 0.4rem 0;
  color: #333;
}

.copybook-desc {
  font-size: 0.85rem;
  color: #666;
  margin: 0 0 0.6rem 0;
  line-height: 1.5;
}

.copybook-meta {
  display: flex;
  gap: 0.8rem;
  font-size: 0.8rem;
  color: #999;
  flex-wrap: wrap;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .hero-title {
    font-size: 2rem;
  }

  .hero-actions {
    flex-direction: column;
  }

  .section-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 1rem;
  }
}
</style>
