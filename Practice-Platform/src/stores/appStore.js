import { defineStore } from 'pinia';
import { getStorage, setStorage } from '../utils';

// 应用状态管理
export const useAppStore = defineStore('app', {
  state: () => ({
    // 系统设置
    systemSettings: {
      siteName: '数字书法练习平台',
      siteDescription: '传承中华文化，提升艺术修养',
      allowRegistration: true,
      allowComments: true,
      defaultFontSize: 16,
      requireCommentApproval: false
    },
    
    // 主题设置
    theme: {
      darkMode: getStorage('darkMode') === 'true',
      primaryColor: getStorage('primaryColor') || '#409EFF',
      sidebarCollapsed: false
    },
    
    // 统计信息
    statistics: {
      totalUsers: 0,
      totalWorks: 0,
      totalComments: 0,
      totalLikes: 0
    },
    
    // 公告信息
    announcements: [],
    
    // 缓存数据
    cache: {
      recentWorks: [],
      hotTags: []
    },
    
    // 加载状态
    loading: {
      global: false,
      page: false
    }
  }),
  
  getters: {
    // 获取系统名称
    getSiteName: (state) => state.systemSettings.siteName,
    
    // 获取是否启用深色模式
    isDarkMode: (state) => state.theme.darkMode,
    
    // 获取主色调
    getPrimaryColor: (state) => state.theme.primaryColor,
    
    // 获取侧边栏折叠状态
    isSidebarCollapsed: (state) => state.theme.sidebarCollapsed,
    
    // 获取系统统计数据
    getStatistics: (state) => state.statistics,
    
    // 获取活跃公告
    getActiveAnnouncements: (state) => {
      return state.announcements.filter(ann => ann.isActive);
    },
    
    // 获取热门标签
    getHotTags: (state) => state.cache.hotTags
  },
  
  actions: {
    // 初始化应用状态
    async initializeApp() {
      try {
        this.loading.global = true;
        
        // 加载系统设置
        const savedSettings = getStorage('systemSettings');
        if (savedSettings) {
          this.systemSettings = { ...this.systemSettings, ...JSON.parse(savedSettings) };
        }
        
        // 加载统计数据
        await this.fetchStatistics();
        
        // 加载公告
        await this.fetchAnnouncements();
        
        // 加载热门标签
        await this.fetchHotTags();
        
      } catch (error) {
        console.error('初始化应用失败:', error);
      } finally {
        this.loading.global = false;
      }
    },
    
    // 保存系统设置
    saveSystemSettings(settings) {
      try {
        this.systemSettings = { ...this.systemSettings, ...settings };
        setStorage('systemSettings', JSON.stringify(this.systemSettings));
        return true;
      } catch (error) {
        console.error('保存系统设置失败:', error);
        return false;
      }
    },
    
    // 切换深色模式
    toggleDarkMode() {
      this.theme.darkMode = !this.theme.darkMode;
      setStorage('darkMode', this.theme.darkMode.toString());
      
      // 应用深色模式样式
      if (this.theme.darkMode) {
        document.documentElement.classList.add('dark-mode');
      } else {
        document.documentElement.classList.remove('dark-mode');
      }
    },
    
    // 设置主色调
    setPrimaryColor(color) {
      this.theme.primaryColor = color;
      setStorage('primaryColor', color);
    },
    
    // 切换侧边栏折叠状态
    toggleSidebar() {
      this.theme.sidebarCollapsed = !this.theme.sidebarCollapsed;
    },
    
    // 获取统计数据（模拟）
    async fetchStatistics() {
      // 模拟API调用
      return new Promise(resolve => {
        setTimeout(() => {
          this.statistics = {
            totalUsers: 1234,
            totalWorks: 5678,
            totalComments: 9012,
            totalLikes: 13579
          };
          resolve(this.statistics);
        }, 500);
      });
    },
    
    // 获取公告（模拟）
    async fetchAnnouncements() {
      // 模拟API调用
      return new Promise(resolve => {
        setTimeout(() => {
          this.announcements = [
            {
              id: 1,
              title: '欢迎使用数字书法练习平台',
              content: '平台正式上线，欢迎各位书法爱好者加入！',
              createdAt: new Date('2024-01-01'),
              isActive: true
            },
            {
              id: 2,
              title: '每周书法比赛',
              content: '参与每周主题创作，赢取精美礼品',
              createdAt: new Date('2024-01-15'),
              isActive: true
            }
          ];
          resolve(this.announcements);
        }, 500);
      });
    },
    
    // 获取热门标签（模拟）
    async fetchHotTags() {
      // 模拟API调用
      return new Promise(resolve => {
        setTimeout(() => {
          this.cache.hotTags = [
            { id: 1, name: '楷书', count: 156 },
            { id: 2, name: '行书', count: 142 },
            { id: 3, name: '隶书', count: 98 },
            { id: 4, name: '篆书', count: 76 },
            { id: 5, name: '草书', count: 64 }
          ];
          resolve(this.cache.hotTags);
        }, 500);
      });
    },
    
    // 添加公告
    addAnnouncement(announcement) {
      const newAnnouncement = {
        id: Date.now(),
        createdAt: new Date(),
        isActive: true,
        ...announcement
      };
      this.announcements.unshift(newAnnouncement);
      return newAnnouncement;
    },
    
    // 更新公告状态
    updateAnnouncementStatus(id, isActive) {
      const announcement = this.announcements.find(ann => ann.id === id);
      if (announcement) {
        announcement.isActive = isActive;
        return true;
      }
      return false;
    },
    
    // 设置全局加载状态
    setGlobalLoading(loading) {
      this.loading.global = loading;
    },
    
    // 设置页面加载状态
    setPageLoading(loading) {
      this.loading.page = loading;
    },
    
    // 清除缓存
    clearCache() {
      this.cache.recentWorks = [];
      this.cache.hotTags = [];
    }
  }
});