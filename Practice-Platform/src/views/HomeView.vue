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
      <div class="works-grid">
        <div class="work-card" v-for="work in featuredWorks" :key="work.id">
          <div class="work-thumbnail">
            <img :src="work.thumbnail" :alt="work.title" class="work-image" />
          </div>
          <div class="work-info">
            <h3 class="work-title">{{ work.title }}</h3>
            <p class="work-author">作者: {{ work.author }}</p>
          </div>
        </div>
      </div>
    </section>
    
    <!-- 用户评价 -->
    <section class="testimonials-section">
      <h2 class="section-title">用户评价</h2>
      <div class="testimonials-slider">
        <div class="testimonial-item" v-for="testimonial in testimonials" :key="testimonial.id">
          <div class="testimonial-content">
            <p class="testimonial-text">{{ testimonial.content }}</p>
            <div class="testimonial-author">
              <span class="author-name">{{ testimonial.author }}</span>
              <span class="author-role">{{ testimonial.role }}</span>
            </div>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';
// import { useUserStore } from '../stores/userStore';
import { ElMessage } from 'element-plus';

// 状态管理 - userStore已废弃，使用后端API获取用户信息
// const userStore = useUserStore();
const router = useRouter();

// 计算属性：获取用户显示名称 - 已废弃，使用后端API获取用户信息
// const userDisplayName = computed(() => {
//   return userStore.userInfo?.nickname || userStore.userInfo?.username || '游客';
// });

// 计算属性：检查用户是否为管理员 - 已废弃，使用后端API获取用户信息
// const isAdmin = computed(() => {
//   return userStore.isAdmin;
// });

// 用户状态 - 已废弃，使用后端API获取用户信息
const isLoggedIn = ref(false); // 临时使用ref替代computed

// 计算属性：检查用户是否为管理员 - 已废弃，使用后端API获取用户信息
// const isAdmin = computed(() => {
//   return userStore.isAdmin;
// });

// 处理用户操作 - 已废弃，使用后端API处理用户认证
// const handleLogin = () => {
//   router.push('/login');
// };

// const handleRegister = () => {
//   router.push('/register');
// };

// const handleLogout = () => {
//   userStore.logout();
//   ElMessage.success('已退出登录');
// };

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

// 精选作品数据
const featuredWorks = ref([
  {
    id: 1,
    title: '宁静致远',
    author: '王羲之',
    thumbnail: 'https://picsum.photos/seed/calligraphy1/300/200'
  },
  {
    id: 2,
    title: '厚德载物',
    author: '颜真卿',
    thumbnail: 'https://picsum.photos/seed/calligraphy2/300/200'
  },
  {
    id: 3,
    title: '紫气东来',
    author: '柳公权',
    thumbnail: 'https://picsum.photos/seed/calligraphy3/300/200'
  },
  {
    id: 4,
    title: '天道酬勤',
    author: '欧阳询',
    thumbnail: 'https://picsum.photos/seed/calligraphy4/300/200'
  }
]);

// 用户评价数据
const testimonials = ref([
  {
    id: 1,
    content: '这个平台让我能够随时随地练习书法，界面优雅，功能强大，是书法爱好者的福音！',
    author: '张三',
    role: '书法爱好者'
  },
  {
    id: 2,
    content: '作为初学者，这里的字帖生成功能对我帮助很大，能够快速提升书写技巧。',
    author: '李四',
    role: '初学者'
  },
  {
    id: 3,
    content: '社区氛围很好，能够欣赏到其他爱好者的作品，也能得到他们的指导和建议。',
    author: '王五',
    role: '资深书友'
  }
]);
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

.primary-btn {
  padding: 0.8rem 2rem;
  background-color: #8B4513;
  color: white;
  text-decoration: none;
  border-radius: 4px;
  font-size: 1.1rem;
  transition: all 0.3s;
}

.primary-btn:hover {
  background-color: #6B340E;
  transform: translateY(-2px);
}

.secondary-btn {
  padding: 0.8rem 2rem;
  background-color: transparent;
  color: #8B4513;
  border: 2px solid #8B4513;
  text-decoration: none;
  border-radius: 4px;
  font-size: 1.1rem;
  transition: all 0.3s;
}

.secondary-btn:hover {
  background-color: #8B4513;
  color: white;
  transform: translateY(-2px);
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

/* 用户评价样式 */
.testimonials-section {
  background-color: rgba(245, 245, 240, 0.8);
  padding: 3rem 2rem;
  border-radius: 8px;
}

.testimonials-slider {
  max-width: 800px;
  margin: 0 auto;
}

.testimonial-item {
  background-color: white;
  padding: 2rem;
  border-radius: 8px;
  margin-bottom: 1rem;
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.05);
}

.testimonial-text {
  font-style: italic;
  margin-bottom: 1rem;
  color: #333;
  line-height: 1.8;
}

.testimonial-author {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.author-name {
  font-weight: bold;
  color: #8B4513;
}

.author-role {
  color: #666;
  font-size: 0.9rem;
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
  
  .testimonials-slider {
    padding: 0 1rem;
  }
}
</style>