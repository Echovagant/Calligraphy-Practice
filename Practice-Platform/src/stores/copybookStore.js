import { defineStore } from 'pinia';
import { getStorage, setStorage } from '../utils/storage';
import { copybookAPI } from '../utils/api';
import { useUserStore } from './userStore';

// 字帖生成状态管理
export const useCopybookStore = defineStore('copybook', {
  state: () => ({
    // 字帖设置
    settings: {
      // 内容设置
      content: '',
      sampleText: '床前明月光，疑是地上霜。举头望明月，低头思故乡。',

      // 样式设置
      font: 'kai', // kai, xing, li, zhuan, cao
      layout: 'grid', // grid, vertical, horizontal
      charactersPerPage: 24,
      fontSize: 48,
      lineSpacing: 20,
      characterSpacing: 10,

      // 纸张设置
      paperColor: '#f8f0e3', // 宣纸色
      gridColor: '#a0522d',
      paperSize: 'a4', // a4, a3, letter
      orientation: 'portrait', // portrait, landscape

      // 额外设置
      showGrid: true,
      showStrokeOrder: false,
      showPinyin: false,
      showTranslation: false
    },

    // 历史记录
    history: [],

    // 字体列表
    fonts: [
      { value: 'kai', label: '楷书', description: '端庄秀丽，适合初学者' },
      { value: 'xing', label: '行书', description: '流畅自然，实用性强' },
      { value: 'li', label: '隶书', description: '古朴典雅，蚕头燕尾' },
      { value: 'zhuan', label: '篆书', description: '象形会意，历史悠久' },
      { value: 'cao', label: '草书', description: '自由奔放，艺术表现力强' }
    ],

    // 布局方式
    layouts: [
      { value: 'grid', label: '网格布局', description: '标准方格练习' },
      { value: 'vertical', label: '竖排布局', description: '传统竖式排列' },
      { value: 'horizontal', label: '横排布局', description: '现代横式排列' }
    ],

    // 纸张颜色预设
    paperColors: [
      { name: '宣纸色', value: '#f8f0e3' },
      { name: '米黄色', value: '#f5deb3' },
      { name: '白色', value: '#ffffff' },
      { name: '浅灰色', value: '#f5f5f5' },
      { name: '淡黄色', value: '#ffffcc' }
    ],

    // 生成的字帖数据（用于自定义生成）
    generatedCopybooks: [],
    
    // 后端字帖列表
    copybooks: [],

    // 字帖总数（列表端点返回分页信封，total 在信封里）
    copybookTotal: 0,
    
    // 我保存到账号的自建字帖（GET /api/copybooks/mine）
    myCopybooks: [],

    // 我的字帖总数
    myCopybookTotal: 0,
    
    // 热门字帖
    popularCopybooks: [],
    
    // 当前选中的字帖
    selectedCopybook: null,
    
    // 用户练习统计
    practiceStatistics: null,
    
    // 加载状态
    loading: {
      copybooks: false,
      popular: false,
      statistics: false,
      mine: false,
    },

    // 当前编辑的字帖
    currentCopybook: null
  }),

  getters: {
    // 获取当前设置
    getSettings: (state) => state.settings,

    // 获取字体列表
    getFonts: (state) => state.fonts,

    // 获取布局列表
    getLayouts: (state) => state.layouts,

    // 获取纸张颜色预设
    getPaperColors: (state) => state.paperColors,

    // 获取历史记录
    getHistory: (state) => state.history,

    // 获取生成的字帖列表
    getGeneratedCopybooks: (state) => state.generatedCopybooks,

    // 我保存到账号的字帖
    getMyCopybooks: (state) => state.myCopybooks,

    // 获取当前编辑的字帖
    getCurrentCopybook: (state) => state.currentCopybook,

    // 根据ID获取字帖
    getCopybookById: (state) => (id) => {
      return state.generatedCopybooks.find(copybook => copybook.id === id);
    }
  },

  actions: {
    // 初始化
    initialize() {
      // 从本地存储加载设置
      const savedSettings = getStorage('copybookSettings');
      if (savedSettings) {
        this.settings = { ...this.settings, ...JSON.parse(savedSettings) };
      }

      // 加载历史记录
      const savedHistory = getStorage('copybookHistory');
      if (savedHistory) {
        this.history = JSON.parse(savedHistory);
      }

      // 加载生成的字帖
      const savedCopybooks = getStorage('generatedCopybooks');
      if (savedCopybooks) {
        this.generatedCopybooks = JSON.parse(savedCopybooks);
      }
    },

    // 更新设置
    updateSettings(newSettings) {
      this.settings = { ...this.settings, ...newSettings };
      setStorage('copybookSettings', JSON.stringify(this.settings));
    },

    // 设置内容
    setContent(content) {
      this.settings.content = content;
      this.updateSettings({ content });
    },

    // 设置字体
    setFont(font) {
      this.settings.font = font;
      this.updateSettings({ font });
    },

    // 设置布局
    setLayout(layout) {
      this.settings.layout = layout;
      this.updateSettings({ layout });
    },

    // 设置纸张颜色
    setPaperColor(color) {
      this.settings.paperColor = color;
      this.updateSettings({ paperColor: color });
    },

    // 生成自定义字帖（本地生成 + 存到账号）
    //
    // 返回值说明：
    //   savedToAccount: true  已经存进账号，可在「我的字帖」里看到
    //   reason: 'not-logged-in' 没登录，字帖只落在本机（仍然可以下载）
    //   reason: 'error'        账号保存失败（本地字帖照样能用）
    async generateCopybook() {
      const content = this.settings.content || this.settings.sampleText;

      // 验证内容
      if (!content.trim()) {
        throw new Error('请输入要生成的文字内容');
      }

      // 生成字帖数据
      const copybook = {
        id: Date.now(),
        title: content.length > 20 ? content.substring(0, 20) + '...' : content,
        content,
        settings: { ...this.settings },
        createdAt: new Date(),
        pageCount: Math.ceil(content.length / this.settings.charactersPerPage)
      };

      // 添加到生成列表
      this.generatedCopybooks.unshift(copybook);

      // 限制生成列表数量
      if (this.generatedCopybooks.length > 10) {
        this.generatedCopybooks = this.generatedCopybooks.slice(0, 10);
      }

      // 保存到历史记录
      this.addToHistory(copybook);

      // 保存到本地存储
      setStorage('generatedCopybooks', JSON.stringify(this.generatedCopybooks));

      // 设置为当前编辑的字帖
      this.currentCopybook = copybook;

      // 额外存一份到账号：本地只是缓存，账号才是跨设备能看到的真相源
      const saved = await this.saveCopybookToAccount(copybook);

      return {
        copybook,
        savedToAccount: saved.saved,
        reason: saved.reason,
        error: saved.error
      };
    },

    // 把字帖存到账号：POST /api/copybooks
    //
    // 入参按契约的 CopybookCreate 组装：title / content 是必填，
    // font / layout / charactersPerPage / fontSize / paperColor / gridColor
    // 用生成时的设置。
    async saveCopybookToAccount(copybook) {
      const userStore = useUserStore();

      // 未登录就不调创建接口 —— 请求层拿不到令牌会整页跳登录页。
      if (!userStore.isLoggedIn) {
        return { saved: false, reason: 'not-logged-in' };
      }

      const settings = copybook?.settings || {};
      try {
        const created = await copybookAPI.createCopybook({
          title: copybook.title,
          content: copybook.content,
          description: `自建字帖，共 ${copybook.content.length} 字`,
          font: settings.font,
          layout: settings.layout,
          charactersPerPage: settings.charactersPerPage,
          fontSize: settings.fontSize,
          paperColor: settings.paperColor,
          gridColor: settings.gridColor
        });

        // 新建的字帖放在最前面，列表不用重新拉
        if (created && created.id) {
          this.myCopybooks = [created, ...this.myCopybooks];
          this.myCopybookTotal += 1;
        }

        return { saved: true, copybook: created };
      } catch (error) {
        console.error('保存字帖到账号失败:', error);
        return { saved: false, reason: 'error', error };
      }
    },

    // 我保存的自建字帖：GET /api/copybooks/mine —— 分页信封
    async fetchMyCopybooks(params = {}) {
      try {
        this.loading.mine = true;
        const response = await copybookAPI.getMyCopybooks(params);
        this.myCopybooks = response?.content || [];
        this.myCopybookTotal = response?.total ?? this.myCopybooks.length;
        return this.myCopybooks;
      } catch (error) {
        console.error('获取我的字帖失败:', error);
        throw error;
      } finally {
        this.loading.mine = false;
      }
    },
    
    // 从后端获取字帖列表
    // GET /api/copybooks —— 返回**分页信封** {content,page,size,total,totalPages}
    async fetchCopybooks(params = {}) {
      try {
        this.loading.copybooks = true;
        const response = await copybookAPI.getCopybooks(params);
        this.copybooks = response?.content || [];
        this.copybookTotal = response?.total ?? 0;
        return this.copybooks;
      } catch (error) {
        console.error('获取字帖列表失败:', error);
        throw error;
      } finally {
        this.loading.copybooks = false;
      }
    },

    // 获取热门字帖
    // GET /api/copybooks/popular —— 同样是分页信封
    async fetchPopularCopybooks(params = {}) {
      try {
        this.loading.popular = true;
        const response = await copybookAPI.getPopularCopybooks(params);
        this.popularCopybooks = response?.content || [];
        return this.popularCopybooks;
      } catch (error) {
        console.error('获取热门字帖失败:', error);
        throw error;
      } finally {
        this.loading.popular = false;
      }
    },

    // 获取字帖详情
    // GET /api/copybooks/{id} —— 返回**裸字帖对象**
    async fetchCopybookDetail(copybookId) {
      try {
        const response = await copybookAPI.getCopybookDetail(copybookId);
        this.selectedCopybook = response;
        return this.selectedCopybook;
      } catch (error) {
        console.error('获取字帖详情失败:', error);
        throw error;
      }
    },

    // 记录练习历史
    // POST /api/copybooks/{id}/practice —— 返回**裸练习记录**
    async recordPractice(copybookId, practiceData) {
      try {
        const response = await copybookAPI.recordPractice(copybookId, practiceData);
        return response;
      } catch (error) {
        console.error('记录练习历史失败:', error);
        throw error;
      }
    },

    // 获取用户练习统计
    // GET /api/copybooks/statistics/user —— 返回裸对象
    async fetchUserStatistics() {
      try {
        this.loading.statistics = true;
        const response = await copybookAPI.getUserStatistics();
        this.practiceStatistics = response;
        return this.practiceStatistics;
      } catch (error) {
        console.error('获取用户练习统计失败:', error);
        throw error;
      } finally {
        this.loading.statistics = false;
      }
    },

    // 添加到历史记录
    addToHistory(copybook) {
      const historyItem = {
        id: copybook.id,
        title: copybook.title,
        createdAt: new Date(),
        settings: {
          font: copybook.settings.font,
          layout: copybook.settings.layout,
          charactersPerPage: copybook.settings.charactersPerPage
        }
      };

      // 避免重复
      this.history = this.history.filter(item => item.id !== historyItem.id);

      // 添加到历史记录开头
      this.history.unshift(historyItem);

      // 限制历史记录数量
      if (this.history.length > 20) {
        this.history = this.history.slice(0, 20);
      }

      // 保存到本地存储
      setStorage('copybookHistory', JSON.stringify(this.history));
    },

    // 加载历史记录
    loadFromHistory(historyId) {
      const historyItem = this.history.find(item => item.id === historyId);
      if (historyItem) {
        const copybook = this.getCopybookById(historyId);
        if (copybook) {
          this.settings = { ...this.settings, ...copybook.settings };
          this.currentCopybook = copybook;
          return true;
        }
      }
      return false;
    },

    // 删除生成的字帖
    deleteCopybook(id) {
      this.generatedCopybooks = this.generatedCopybooks.filter(copybook => copybook.id !== id);
      this.history = this.history.filter(item => item.id !== id);

      // 如果当前编辑的是被删除的字帖，清除当前编辑
      if (this.currentCopybook && this.currentCopybook.id === id) {
        this.currentCopybook = null;
      }

      // 保存到本地存储
      setStorage('generatedCopybooks', JSON.stringify(this.generatedCopybooks));
      setStorage('copybookHistory', JSON.stringify(this.history));

      return true;
    },

    // 清空历史记录
    clearHistory() {
      this.history = [];
      setStorage('copybookHistory', JSON.stringify([]));
    },

    // 清空生成的字帖
    clearGeneratedCopybooks() {
      this.generatedCopybooks = [];
      setStorage('generatedCopybooks', JSON.stringify([]));
      this.currentCopybook = null;
    },

    // 获取示例文本
    getSampleText() {
      return this.settings.sampleText;
    },

    // 设置示例文本
    setSampleText(text) {
      this.settings.sampleText = text;
      this.updateSettings({ sampleText: text });
    },

    // 导出字帖设置
    exportSettings() {
      return JSON.stringify(this.settings, null, 2);
    },

    // 导入字帖设置
    importSettings(settingsJson) {
      try {
        const settings = JSON.parse(settingsJson);
        this.updateSettings(settings);
        return true;
      } catch (error) {
        console.error('导入设置失败:', error);
        return false;
      }
    }
  }
});
