import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '../stores/userStore'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'home',
      component: () => import('../views/HomeView.vue'),
      meta: { title: '首页' }
    },
    {
      path: '/practice',
      name: 'practice',
      component: () => import('../views/PracticeView.vue'),
      meta: { title: '书法练习' }
    },
    {
      path: '/copybook',
      name: 'copybook',
      component: () => import('../views/CopybookView.vue'),
      meta: { title: '字帖生成' }
    },
    {
      path: '/gallery',
      name: 'gallery',
      component: () => import('../views/GalleryView.vue'),
      meta: { title: '作品展示' }
    },
    {
      path: '/community',
      name: 'community',
      component: () => import('../views/CommunityView.vue'),
      meta: { title: '社区交流' }
    },
    {
      path: '/login',
      name: 'login',
      component: () => import('../views/LoginView.vue'),
      meta: { title: '用户登录' }
    },
    {
      path: '/register',
      name: 'register',
      component: () => import('../views/RegisterView.vue'),
      meta: { title: '用户注册' }
    },
    {
      path: '/profile',
      name: 'profile',
      component: () => import('../views/ProfileView.vue'),
      meta: {
        title: '个人资料',
        requiresAuth: true
      }
    },
    {
      path: '/my-works',
      name: 'my-works',
      component: () => import('../views/MyWorksView.vue'),
      meta: {
        title: '我的作品',
        requiresAuth: true
      }
    },
    {
      path: '/my-practices',
      name: 'my-practices',
      component: () => import('../views/MyPracticesView.vue'),
      meta: {
        title: '练习记录',
        requiresAuth: true
      }
    },
    {
      path: '/admin/login',
      name: 'admin-login',
      component: () => import('../views/AdminLoginView.vue'),
      meta: { title: '管理员登录' }
    },
    {
      path: '/admin',
      name: 'admin',
      component: () => import('../views/AdminView.vue'),
      meta: { 
        title: '管理后台',
        requiresAuth: true,
        requiresAdmin: true
      }
    },
    {
      path: '/:pathMatch(.*)*',
      name: 'not-found',
      component: () => import('../views/NotFoundView.vue'),
      meta: { title: '页面不存在' }
    }
  ],
  scrollBehavior() {
    return { top: 0 }
  }
})

// 路由前置守卫，设置页面标题和权限控制
router.beforeEach((to, from, next) => {
  // 设置页面标题
  if (to.meta.title) {
    document.title = to.meta.title
  }
  
  // 权限控制
  const userStore = useUserStore()
  
  // 检查是否需要登录
  if (to.meta.requiresAuth) {
    if (!userStore.isLoggedIn) {
      // 未登录，跳转到对应的登录页
      const loginRoute = to.path.startsWith('/admin') ? 'admin-login' : 'login';
      return next({
        name: loginRoute,
        query: { redirect: to.fullPath }
      })
    }
    
    // 检查是否需要管理员权限
    if (to.meta.requiresAdmin) {
      if (!userStore.isAdmin) {
        // 不是管理员，跳转到首页
        return next({
          name: 'home'
        })
      }
    }
  }
  
  // 登录用户不允许访问登录和注册页面
  if ((to.name === 'login' || to.name === 'register' || to.name === 'admin-login') && userStore.isLoggedIn) {
    // 如果是管理员登录页面，检查是否是管理员用户
    if (to.name === 'admin-login' && userStore.isBackendUser) {
      return next({
        name: 'admin'
      });
    }
    // 如果是普通登录页面，检查是否是普通用户
    if (to.name !== 'admin-login' && !userStore.isBackendUser) {
      return next({
        name: 'home'
      });
    }
  }
  
  next()
})

export default router
