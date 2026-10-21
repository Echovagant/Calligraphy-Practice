import { defineStore } from 'pinia';
import { getStorage, setStorage } from '../utils';
import { statsAPI } from '../utils/api';

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
    
    // 统计信息 —— 形状与契约的 statsOverview 响应一致
    // （GET /api/stats/overview 返回裸对象，没有 data 信封）
    statistics: {
      totalWorks: 0,
      totalUsers: 0,
      totalPracticeRecords: 0,
      popularCopybooks: []
    },

    // 缓存数据
    cache: {
      recentWorks: []
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
    
    // 获取统计数据 —— 真实调用 GET /api/stats/overview
    // 响应是裸对象（没有 data 信封），字段与契约的 statsOverview 一致
    async fetchStatistics() {
      const overview = await statsAPI.getOverview();
      this.statistics = {
        totalWorks: overview?.totalWorks ?? 0,
        totalUsers: overview?.totalUsers ?? 0,
        totalPracticeRecords: overview?.totalPracticeRecords ?? 0,
        popularCopybooks: overview?.popularCopybooks ?? [],
      };
      return this.statistics;
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
    }
  }
});