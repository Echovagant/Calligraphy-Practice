<template>
  <div class="app-container">
    <!-- WebGL水墨背景 -->
    <InkBackground />

    <!-- 导航栏 -->
    <header class="app-header">
      <div class="logo">
        <router-link to="/" class="logo-link">
          <span class="logo-text">书法练习平台</span>
        </router-link>
      </div>
      <nav class="nav-menu">
        <router-link to="/" class="top-nav-item" exact>首页</router-link>
        <router-link to="/practice" class="top-nav-item">书法练习</router-link>
        <router-link to="/copybook" class="top-nav-item">字帖生成</router-link>
        <router-link to="/gallery" class="top-nav-item">作品展示</router-link>
        <router-link to="/community" class="top-nav-item">社区交流</router-link>
        <!-- 管理员链接只对管理员显示 -->
        <router-link v-if="isAdmin" to="/admin" class="top-nav-item admin-link">管理后台</router-link>
      </nav>
      <div class="user-actions">
        <!-- 未登录状态 -->
        <template v-if="!isLoggedIn">
          <router-link to="/login" class="top-nav-item login-btn">登录</router-link>
          <router-link to="/register" class="top-nav-item register-btn">注册</router-link>
        </template>
        <!-- 已登录状态 -->
        <template v-else>
          <div class="user-dropdown">
            <div class="dropdown-trigger" @click.stop="showDropdown = !showDropdown">
              <span class="user-name">{{ userDisplayName }}</span>
              <span class="dropdown-arrow" :class="{ 'rotate': showDropdown }"></span>
            </div>
            <div v-if="showDropdown" class="dropdown-menu" @click.stop>
              <router-link to="/profile" class="dropdown-item">个人资料</router-link>
              <router-link to="/my-works" class="dropdown-item">我的作品</router-link>
              <router-link to="/my-practices" class="dropdown-item">练习记录</router-link>
              <div class="dropdown-divider"></div>
              <div class="dropdown-item logout-btn" @click="handleLogout">退出登录</div>
            </div>
          </div>
        </template>
      </div>
    </header>

    <!-- 主要内容区域 -->
    <main class="app-main">
      <router-view v-slot="{ Component }">
        <transition name="fade" mode="out-in">
          <component :is="Component" />
        </transition>
      </router-view>
    </main>
    
    <!-- 页脚 -->
    <footer class="app-footer">
      <div class="footer-content">
        <p>© 2025 在线书法练习平台 - 传承中华文化，弘扬书法艺术</p>
      </div>
    </footer>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { useUserStore } from './stores/userStore';
import InkBackground from './components/InkBackground.vue';

const router = useRouter();
const userStore = useUserStore();
const showDropdown = ref(false);

// 计算属性
const isLoggedIn = computed(() => userStore.isLoggedIn);
const isAdmin = computed(() => userStore.isAdmin);
const userDisplayName = computed(() => {
  // 未登录时 userInfo 是 null —— 直接取 .username 会抛错
  return userStore.userInfo?.username || '用户';
});

// 启动时从本地存储恢复登录态。
// 不做这一步的话，刷新页面后 userInfo 为空、isLoggedIn 为 false，
// 明明令牌还在却被当成未登录。
onMounted(() => {
  userStore.initUserFromStorage();
});

// 处理退出登录
const handleLogout = async () => {
  try {
    await userStore.logout();
    showDropdown.value = false;
    router.push('/');
  } catch (error) {
    console.error('退出登录失败:', error);
  }
};

// 点击页面其他地方关闭下拉菜单
document.addEventListener('click', () => {
  showDropdown.value = false;
});
</script>

<style>
/* 全局样式重置 */
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

html, body {
  font-family: 'Microsoft YaHei', Arial, sans-serif;
  line-height: 1.6;
  color: #333;
  background-color: transparent;
}

/* 应用容器 */
.app-container {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  position: relative;
}

/* 导航栏样式 */
.app-header {
  background-color: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(5px);
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  padding: 0 2rem;
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  position: sticky;
  top: 0;
  z-index: 100;
}

.logo {
  font-size: 1.5rem;
  font-weight: bold;
}

.logo-text {
  color: #333;
  text-decoration: none;
  font-family: 'SimSun', serif;
  letter-spacing: 2px;
}

.nav-menu {
  display: flex;
  gap: 1.5rem;
}

/* 顶栏导航项。
   这个类名是**全局**的（App.vue 的 style 块没有 scoped），
   所以必须够独特 —— 曾经叫 `.nav-item`，结果跟后台侧边栏的 `.nav-item` 撞名，
   全局的 `color: #333` 漏进深色侧边栏，变成「深底深字」，对比度只有 1.21。 */
.top-nav-item {
  color: #333;
  text-decoration: none;
  padding: 0.5rem 0;
  position: relative;
  transition: color 0.3s;
}

.top-nav-item:hover {
  color: #8B4513;
}

.top-nav-item::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  width: 100%;
  height: 2px;
  background-color: #8B4513;
  /* 用 scaleX 画下划线，不要动 width —— 动 width 每帧都要重排 */
  transform: scaleX(0);
  transform-origin: left center;
  transition: transform 0.3s;
}

.top-nav-item:hover::after {
  transform: scaleX(1);
}

.user-actions {
  display: flex;
  gap: 1rem;
}

.logo-link {
  text-decoration: none;
  display: inline-block;
}

/* 用户下拉菜单样式 */
.user-dropdown {
  position: relative;
}

.dropdown-trigger {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.5rem 1rem;
  cursor: pointer;
  border-radius: 4px;
  transition: background-color 0.3s;
}

.dropdown-trigger:hover {
  background-color: #f5f5f5;
}

.user-name {
  font-weight: 500;
  color: #333;
}

.dropdown-arrow {
  width: 0;
  height: 0;
  border-left: 4px solid transparent;
  border-right: 4px solid transparent;
  border-top: 4px solid #666;
  transition: transform 0.3s;
}

.dropdown-arrow.rotate {
  transform: rotate(180deg);
}

.dropdown-menu {
  position: absolute;
  top: 100%;
  right: 0;
  margin-top: 5px;
  background-color: white;
  border: 1px solid #e0e0e0;
  border-radius: 4px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  min-width: 160px;
  z-index: 1000;
}

.dropdown-item {
  padding: 0.75rem 1rem;
  color: #333;
  text-decoration: none;
  display: block;
  transition: background-color 0.3s;
  cursor: pointer;
}

.dropdown-item:hover {
  background-color: #f5f5f5;
}

.dropdown-divider {
  height: 1px;
  background-color: #e0e0e0;
  margin: 0.5rem 0;
}

.logout-btn {
  color: #ff4d4f;
}

.logout-btn:hover {
  background-color: #fff1f0;
}

.admin-link {
  color: #8B4513;
  font-weight: 500;
}

.admin-link:hover {
  color: #6B340E;
}

/* 主要内容区域 */
.app-main {
  flex: 1;
  padding: 2rem;
  max-width: 1200px;
  width: 100%;
  margin: 0 auto;
  background-color: transparent;
  backdrop-filter: none;
  border-radius: 0;
  margin-top: 2rem;
  margin-bottom: 2rem;
  box-shadow: none;
  border: none;
  outline: none;
}

/* 页脚样式 */
.app-footer {
  background-color: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(5px);
  padding: 1.5rem 0;
  text-align: center;
  border-top: 1px solid #eee;
}

.footer-content {
  max-width: 1200px;
  margin: 0 auto;
}

/* 页面过渡动画 */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.5s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .app-header {
    padding: 0 1rem;
    flex-direction: column;
    height: auto;
    padding: 1rem;
  }

  .nav-menu {
    margin: 1rem 0;
    flex-wrap: wrap;
    justify-content: center;
  }

  .app-main {
    padding: 1rem;
    margin-top: 1rem;
    margin-bottom: 1rem;
  }
}
</style>
