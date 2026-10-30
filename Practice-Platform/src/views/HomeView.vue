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
          <!-- 次要操作用同一主色的描边款：全屏只有一套主色，靠「实心 / 描边」分主次 -->
          <el-button type="primary" plain size="large" @click="handleGoToCopybook">
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
          <svg class="feature-icon" viewBox="0 0 64 64" aria-hidden="true">
            <path d="M13 49C21 36 30 27 47 15" stroke="currentColor" stroke-width="7" stroke-linecap="round" fill="none" />
            <path d="M47 15c4-2 6-1 5 2" stroke="currentColor" stroke-width="3" stroke-linecap="round" fill="none" />
          </svg>
          <h3 class="feature-title">智能练习</h3>
          <p class="feature-desc">使用Fabric.js提供真实的毛笔书写体验，支持多种笔刷效果</p>
          <el-button type="primary" @click="handleGoToPractice">开始练习</el-button>
        </div>
        <div class="feature-card">
          <svg class="feature-icon" viewBox="0 0 64 64" aria-hidden="true">
            <rect x="15" y="8" width="34" height="48" rx="3" stroke="currentColor" stroke-width="2.6" fill="none" />
            <path d="M24 21h16M24 31h16M24 41h10" stroke="currentColor" stroke-width="2.6" stroke-linecap="round" fill="none" />
            <rect x="42" y="40" width="12" height="12" rx="2" fill="var(--el-color-danger)" />
          </svg>
          <h3 class="feature-title">字帖生成</h3>
          <p class="feature-desc">根据用户需求生成个性化字帖，支持多种字体风格选择</p>
          <el-button type="primary" @click="handleGoToCopybook">生成字帖</el-button>
        </div>
        <div class="feature-card">
          <svg class="feature-icon" viewBox="0 0 64 64" aria-hidden="true">
            <rect x="10" y="12" width="44" height="38" rx="3" stroke="currentColor" stroke-width="2.6" fill="none" />
            <circle cx="24" cy="25" r="5" stroke="currentColor" stroke-width="2.6" fill="none" />
            <path d="M14 46l12-13 9 9 7-8 8 12" stroke="currentColor" stroke-width="2.6" stroke-linejoin="round" fill="none" />
          </svg>
          <h3 class="feature-title">作品展示</h3>
          <p class="feature-desc">展示个人书法作品，获得其他用户的欣赏和评价</p>
          <el-button type="primary" @click="router.push('/gallery')">浏览作品</el-button>
        </div>
        <div class="feature-card">
          <svg class="feature-icon" viewBox="0 0 64 64" aria-hidden="true">
            <path d="M32 10c12 0 22 7 22 16s-10 16-22 16c-3 0-6-.5-8-1.4L14 46l2.5-9.5C12.5 33 10 29.7 10 26c0-9 10-16 22-16z"
              stroke="currentColor" stroke-width="2.6" stroke-linejoin="round" fill="none" />
            <path d="M24 25h16M24 32h10" stroke="currentColor" stroke-width="2.4" stroke-linecap="round" fill="none" />
          </svg>
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
                <span class="meta-item">{{ formatDifficulty(book.difficulty, '难度未知') }}</span>
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
import { formatDifficulty } from '../utils/constants';
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
/* ============================================================
   首页版式
   - 页面容器与全站内容列同一条左右边界（.app-main 的内容列）
   - 间距刻度：4 / 8 / 12 / 16 / 24 / 32 / 48
     组内用小值，组间用大值
   ============================================================ */
.home-container {
  --sp-1: 4px;
  --sp-2: 8px;
  --sp-3: 12px;
  --sp-4: 16px;
  --sp-5: 24px;
  --sp-6: 32px;
  --sp-7: 48px;

  width: 100%;
  max-width: 1200px;
  margin-inline: auto;
  padding-block: var(--sp-2);
}

/* 英雄区：左对齐到与全站一致的左边界。
   原来这里是「居中大标题 + 副标题 + 居中按钮」的模板写法，
   面板铺满内容列、内容却居中，看上去比下面的内容列还宽。 */
.hero-section {
  padding: var(--sp-7) var(--sp-6);
  margin-bottom: var(--sp-6);
  background-color: rgba(245, 241, 228, 0.82);
  border-left: 3px solid var(--primary-color);
  border-radius: 0 8px 8px 0;
}

.hero-title {
  font-size: clamp(1.75rem, 1.15rem + 2.2vw, 2.75rem);
  margin-bottom: var(--sp-4);
  color: #333;
  font-family: 'SimSun', serif;
  letter-spacing: 0.1em;
  line-height: 1.25;
}

.hero-subtitle {
  font-size: clamp(0.95rem, 0.9rem + 0.3vw, 1.1rem);
  margin-bottom: var(--sp-5);
  color: var(--el-text-color-secondary);
}

.hero-actions {
  display: flex;
  gap: var(--sp-3);
  flex-wrap: wrap;
}

/* 平台数据：一条横向信息带，不再是三张等大的卡片 */
.stats-section {
  margin-bottom: var(--sp-7);
}

.stats-area {
  min-height: 5rem;
}

.stats-grid {
  display: flex;
  flex-wrap: wrap;
  align-items: baseline;
  gap: var(--sp-5) var(--sp-7);
  padding: var(--sp-4) var(--sp-5);
  background-color: rgba(255, 255, 255, 0.92);
  border: 1px solid rgba(139, 69, 19, 0.12);
  border-radius: 8px;
  box-shadow: 0 2px 10px rgba(90, 60, 30, 0.06);
}

/* 每个统计项：数字与标签同排、基线对齐，上方一道细墨线 */
.stat-card {
  display: flex;
  align-items: baseline;
  gap: var(--sp-3);
  padding-top: var(--sp-3);
  border-top: 2px solid rgba(139, 69, 19, 0.22);
}

.stat-number {
  font-family: 'SimSun', 'Songti SC', serif;
  font-size: clamp(1.75rem, 1.35rem + 1.4vw, 2.5rem);
  font-weight: 400;
  letter-spacing: 0.04em;
  line-height: 1.1;
  color: var(--primary-color);
}

.stat-label {
  color: var(--el-text-color-secondary);
  font-size: 0.9rem;
  letter-spacing: 0.08em;
  white-space: nowrap;
}

/* 平台特色：主次分明的网格 —— 一张主角卡竖跨两行，
   两张次要卡并排，最后一张做通栏收尾。 */
.features-section {
  margin-bottom: var(--sp-7);
}

.section-title {
  font-size: clamp(1.25rem, 1.05rem + 0.7vw, 1.6rem);
  margin-bottom: var(--sp-4);
  color: #333;
  font-family: 'SimSun', serif;
  letter-spacing: 0.08em;
  font-weight: 500;
  padding-left: var(--sp-3);
  border-left: 3px solid var(--primary-color);
}

.features-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: var(--sp-5);
}

/* 卡片内部统一左对齐：图标 → 标题 → 说明 → 操作 */
.feature-card {
  position: relative;
  display: flex;
  flex-direction: column;
  background-color: rgba(255, 255, 255, 0.92);
  padding: var(--sp-5);
  border-radius: 8px;
  text-align: left;
  transition: transform 0.3s, box-shadow 0.3s, background-color 0.3s, border-color 0.3s;
  box-shadow: 0 2px 10px rgba(90, 60, 30, 0.06);
}

.feature-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(90, 60, 30, 0.12);
}

/* 主角卡：竖跨两行，墨色底 + 更大的标题 */
.feature-card:first-child {
  grid-column: span 2;
  grid-row: span 2;
  background-color: rgba(243, 236, 231, 0.96);
  box-shadow: 0 6px 22px rgba(90, 60, 30, 0.1);
}

.feature-card:nth-child(2),
.feature-card:nth-child(3) {
  grid-column: span 1;
}

.feature-card:nth-child(4) {
  grid-column: span 2;
}

.feature-icon {
  width: 40px;
  height: 40px;
  display: block;
  margin-bottom: var(--sp-4);
  color: var(--primary-color);
}

.feature-card:first-child .feature-icon {
  width: 56px;
  height: 56px;
}

.feature-title {
  font-size: 1.15rem;
  font-weight: 500;
  letter-spacing: 0.06em;
  margin-bottom: var(--sp-2);
  color: #3a342d;
  font-family: 'SimSun', 'Songti SC', serif;
}

.feature-card:first-child .feature-title {
  font-size: 1.6rem;
}

.feature-desc {
  color: var(--el-text-color-secondary);
  font-size: 0.92rem;
  line-height: 1.7;
  margin-bottom: var(--sp-4);
}

/* 操作按钮统一贴在卡片底部，四张卡的内边距与对齐方式一致 */
.feature-card .el-button {
  align-self: flex-start;
  margin-top: auto;
}

/* 精选作品 / 热门字帖 */
.preview-section {
  margin-bottom: var(--sp-7);
}

.copybooks-section {
  margin-bottom: var(--sp-7);
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: var(--sp-4);
  margin-bottom: var(--sp-4);
}

/* 区块标题与「查看更多」并排时，两者的左边界要对齐 */
.section-header .section-title {
  margin-bottom: 0;
}

.view-more {
  color: var(--primary-color);
  text-decoration: none;
  transition: color 0.3s;
  white-space: nowrap;
}

.view-more:hover {
  color: #6B340E;
}

.works-area {
  min-height: 8rem;
}

.works-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  gap: var(--sp-5);
}

.work-card {
  background-color: rgba(255, 255, 255, 0.9);
  border-radius: 8px;
  overflow: hidden;
  transition: transform 0.3s, box-shadow 0.3s, background-color 0.3s, border-color 0.3s;
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.05);
}

.work-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.1);
}

.work-thumbnail {
  height: 190px;
  overflow: hidden;
  /* 作品图完整展示不裁切（contain），留白用纸色填 */
  background-color: #f0eee9;
}

.work-image {
  width: 100%;
  height: 100%;
  object-fit: contain;
}

.work-image-placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #f0eee9;
  color: #6b6259;
  font-size: 0.9rem;
}

.work-info {
  padding: var(--sp-4);
}

.work-title {
  font-size: 1.05rem;
  margin-bottom: var(--sp-1);
  color: #333;
  font-family: 'SimSun', serif;
}

.work-author {
  font-size: 0.88rem;
  color: var(--el-text-color-secondary);
}

.copybooks-area {
  min-height: 8rem;
}

.copybooks-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  gap: var(--sp-5);
}

.copybook-card {
  background-color: rgba(255, 255, 255, 0.9);
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.05);
  transition: transform 0.3s, box-shadow 0.3s, background-color 0.3s, border-color 0.3s;
}

.copybook-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.1);
}

.copybook-thumbnail {
  height: 170px;
  overflow: hidden;
  background-color: #f0eee9;
}

/* 字帖封面用 cover（填满卡片），跟作品图相反：
   封面是装饰性缩略图，裁一点没关系；
   用户的作品图才必须 contain 完整展示，不能替作者裁掉半张字。 */
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
  color: #6b6259;
  font-size: 0.9rem;
}

.copybook-info {
  padding: var(--sp-4);
}

.copybook-title {
  font-size: 1rem;
  margin: 0 0 var(--sp-1) 0;
  color: #333;
  font-family: 'SimSun', serif;
}

.copybook-desc {
  font-size: 0.85rem;
  color: var(--el-text-color-secondary);
  margin: 0 0 var(--sp-3) 0;
  line-height: 1.6;
}

.copybook-meta {
  display: flex;
  gap: var(--sp-3);
  font-size: 0.8rem;
  color: var(--el-text-color-secondary);
  flex-wrap: wrap;
}

/* 触摸设备：「查看更多」也是可点目标，抬到 44px 才点得中 */
@media (pointer: coarse) {
  .view-more {
    display: inline-flex;
    align-items: center;
    min-height: 44px;
  }
}

/* 响应式设计 */
@media (max-width: 960px) {
  .features-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .feature-card:first-child {
    grid-column: span 2;
    grid-row: auto;
  }

  .feature-card:nth-child(4) {
    grid-column: span 2;
  }
}

@media (max-width: 768px) {
  .hero-section {
    padding: var(--sp-6) var(--sp-5);
  }

  .section-header {
    flex-direction: column;
    align-items: flex-start;
    gap: var(--sp-2);
  }

  .stats-grid {
    gap: var(--sp-4) var(--sp-5);
    padding: var(--sp-4);
  }
}

@media (max-width: 480px) {
  .features-grid {
    grid-template-columns: 1fr;
    gap: var(--sp-4);
  }

  .feature-card:first-child,
  .feature-card:nth-child(4) {
    grid-column: span 1;
  }

  .stats-grid {
    flex-direction: column;
    gap: var(--sp-4);
  }
}

/* 系统开启「减少动态效果」时：保留状态反馈，去掉位移 */
@media (prefers-reduced-motion: reduce) {
  .feature-card:hover,
  .work-card:hover,
  .copybook-card:hover {
    transform: none !important;
  }
}
</style>
