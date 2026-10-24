<template>
  <el-config-provider :locale="zhCn">
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
              <!-- 触发器用 button 而不是 div：div + click 键盘到不了，
                   违反 WCAG 2.1.1（Keyboard） -->
              <button
                ref="triggerRef"
                type="button"
                class="dropdown-trigger"
                :aria-expanded="showDropdown"
                aria-haspopup="menu"
                @click.stop="toggleDropdown"
                @keydown.down.prevent="openAndFocus('first')"
                @keydown.up.prevent="openAndFocus('last')"
                @keydown.esc.prevent="closeDropdown(true)"
              >
                <span class="user-name">{{ userDisplayName }}</span>
                <span class="dropdown-arrow" :class="{ 'rotate': showDropdown }"></span>
              </button>
              <div
                v-if="showDropdown"
                class="dropdown-menu"
                role="menu"
                aria-label="用户菜单"
                @click.stop
                @keydown.esc.prevent.stop="closeDropdown(true)"
                @keydown.down.prevent="moveMenuFocus(1)"
                @keydown.up.prevent="moveMenuFocus(-1)"
                @keydown.home.prevent="focusMenuEdge('first')"
                @keydown.end.prevent="focusMenuEdge('last')"
              >
                <router-link
                  ref="profileLinkRef"
                  to="/profile"
                  class="dropdown-item"
                  role="menuitem"
                  @click="closeDropdown()"
                  @keydown.space.prevent="activateMenuLink('/profile')"
                >个人资料</router-link>
                <router-link
                  ref="worksLinkRef"
                  to="/my-works"
                  class="dropdown-item"
                  role="menuitem"
                  @click="closeDropdown()"
                  @keydown.space.prevent="activateMenuLink('/my-works')"
                >我的作品</router-link>
                <router-link
                  ref="practicesLinkRef"
                  to="/my-practices"
                  class="dropdown-item"
                  role="menuitem"
                  @click="closeDropdown()"
                  @keydown.space.prevent="activateMenuLink('/my-practices')"
                >练习记录</router-link>
                <div class="dropdown-divider" role="separator"></div>
                <button
                  ref="logoutBtnRef"
                  type="button"
                  class="dropdown-item logout-btn"
                  role="menuitem"
                  @click="handleLogout"
                >
                  退出登录
                </button>
              </div>
            </div>
          </template>
        </div>
      </header>

      <!-- 主要内容区域 -->
      <main class="app-main" :class="{ 'app-main--wide': isWideLayout }">
        <router-view v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </main>

      <!-- 页脚 -->
      <footer class="app-footer">
        <div class="footer-content">
          <p>© {{ currentYear }} 在线书法练习平台 - 传承中华文化，弘扬书法艺术</p>
        </div>
      </footer>
    </div>
  </el-config-provider>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, nextTick } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import zhCn from 'element-plus/es/locale/lang/zh-cn';
import { useUserStore } from './stores/userStore';
import { USER_TYPES } from './utils/constants';
import InkBackground from './components/InkBackground.vue';

const router = useRouter();
const route = useRoute();
const userStore = useUserStore();
const showDropdown = ref(false);

// 页脚年份跟着当前时间走，别写死
const currentYear = new Date().getFullYear();

// 管理后台是宽表 + 侧栏的双栏布局，塞在 1200px 的内容容器里会被挤到
// 表格内部横滚，最右侧的审核操作列看不见。宽屏页面放开限宽。
const isWideLayout = computed(() => route.path.startsWith('/admin'));

// 计算属性
const isLoggedIn = computed(() => userStore.isLoggedIn);
const isAdmin = computed(() => userStore.isAdmin);
const userDisplayName = computed(() => {
  // 未登录时 userInfo 是 null —— 直接取 .username 会抛错
  return userStore.userInfo?.username || '用户';
});

// 下拉菜单的可聚焦节点（方向键在它们之间移动）
const triggerRef = ref(null);
const profileLinkRef = ref(null);
const worksLinkRef = ref(null);
const practicesLinkRef = ref(null);
const logoutBtnRef = ref(null);

// router-link 的 ref 拿到的是组件实例，真正的 DOM 节点在 $el 上
const menuItemElements = computed(() =>
  [profileLinkRef.value, worksLinkRef.value, practicesLinkRef.value, logoutBtnRef.value]
    .map((node) => node?.$el || node)
    .filter(Boolean)
);

// 把焦点落到菜单项上（index 为负数时从末尾算）
const focusMenuItem = (index) => {
  const items = menuItemElements.value;
  if (!items.length) return;
  items[(index + items.length) % items.length]?.focus?.();
};

// 方向键在菜单项之间移动
const moveMenuFocus = (delta) => {
  const items = menuItemElements.value;
  if (!items.length) return;
  const current = items.indexOf(document.activeElement);
  // 焦点还不在菜单里时：向下从头开始，向上从尾开始
  if (current === -1) {
    focusMenuItem(delta > 0 ? 0 : items.length - 1);
    return;
  }
  focusMenuItem(current + delta);
};

// Home / End 跳到首尾
const focusMenuEdge = (edge) => {
  const items = menuItemElements.value;
  if (!items.length) return;
  focusMenuItem(edge === 'first' ? 0 : items.length - 1);
};

const openDropdown = () => {
  showDropdown.value = true;
};

const closeDropdown = (restoreFocus = false) => {
  if (!showDropdown.value) return;
  showDropdown.value = false;
  if (restoreFocus) {
    triggerRef.value?.focus?.();
  }
};

const toggleDropdown = () => {
  if (showDropdown.value) {
    closeDropdown();
    return;
  }
  openDropdown();
  nextTick(() => focusMenuItem(0));
};

// 触发器上按上下键：打开菜单并把焦点送进去
const openAndFocus = (edge) => {
  openDropdown();
  nextTick(() => {
    const items = menuItemElements.value;
    if (!items.length) return;
    focusMenuItem(edge === 'first' ? 0 : items.length - 1);
  });
};

// 菜单项用键盘激活（Space）。锚点原生只响应 Enter，
// Space 在 <a> 上是「向下滚动」，所以这里自己接。
const activateMenuLink = (path) => {
  closeDropdown();
  router.push(path);
};

// 启动时从本地存储恢复登录态。
// 不做这一步的话，刷新页面后 userInfo 为空、isLoggedIn 为 false，
// 明明令牌还在却被当成未登录。
onMounted(() => {
  userStore.initUserFromStorage();
  // 点击页面其他地方关闭下拉菜单。
  // 监听器必须在 onMounted 注册、onUnmounted 移除 ——
  // 写在 setup 顶层等于模块被 import 一次就永久挂一个全局监听器。
  document.addEventListener('click', handleDocumentClick);
  window.addEventListener('app:auth-expired', handleAuthExpired);
});

onUnmounted(() => {
  document.removeEventListener('click', handleDocumentClick);
  window.removeEventListener('app:auth-expired', handleAuthExpired);
});

const handleDocumentClick = () => {
  showDropdown.value = false;
};

// 会话失效（令牌过期 / 刷新失败）。
// utils/api.js 不再用 window.location.href 整页跳走 —— 那会白屏重来。
// 它派发这个事件，这里做路由内跳转。
const handleAuthExpired = (event) => {
  const userType = event?.detail?.userType;
  showDropdown.value = false;
  userStore.isLoggedIn = false;
  userStore.userInfo = null;
  router.push(userType === USER_TYPES.BACKEND ? '/admin/login' : '/login');
};

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
  /* 100vh 在 iOS Safari 上是「地址栏藏起来时的大视口」，
     比实际可见区高出一截，页面会多出一屏空白滚动。100dvh 才是可见高度，
     老浏览器不认 dvh，所以两条都写。 */
  min-height: 100vh;
  min-height: 100dvh;
  display: flex;
  flex-direction: column;
  position: relative;
}

/* 导航栏样式 */
.app-header {
  background-color: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(5px);
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  /* 圆角屏 / home 指示条一侧留出安全区（iPad 上一般取 0，iPhone 上才非 0） */
  padding: 0 max(2rem, env(safe-area-inset-left)) 0 max(2rem, env(safe-area-inset-right));
  min-height: 60px;
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

/* 触发器与菜单项都是原生 button —— 键盘、屏幕阅读器都能用。
   这里把 button 的默认外观清掉，视觉跟原来的 div 一致。 */
.dropdown-trigger {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.5rem 1rem;
  cursor: pointer;
  border-radius: 4px;
  transition: background-color 0.3s;
  border: none;
  background: transparent;
  font-size: 1rem;
  color: inherit;
  line-height: 1.6;
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
  width: 100%;
  border: none;
  background: transparent;
  text-align: left;
  font-size: 1rem;
  line-height: 1.6;
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
  /* 底部安全区：home 指示条那一截别被内容压住 */
  padding-left: max(2rem, env(safe-area-inset-left));
  padding-right: max(2rem, env(safe-area-inset-right));
  padding-bottom: max(2rem, env(safe-area-inset-bottom));
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

/* 触摸设备：顶栏链接也是可点目标，手指点不准 */
@media (pointer: coarse) {
  .top-nav-item {
    min-height: 44px;
    display: inline-flex;
    align-items: center;
  }
}

/* 宽屏页面（管理后台）：解除 1200px 限宽 */
.app-main--wide {
  max-width: none;
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
