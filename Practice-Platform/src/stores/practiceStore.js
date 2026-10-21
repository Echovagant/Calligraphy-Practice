import { defineStore } from 'pinia'
import { copybookAPI, worksAPI } from '../utils/api'
import { getStorage, getSessionStorage } from '../utils/storage'

export const usePracticeStore = defineStore('practice', {
  state: () => ({
    // 练习设置
    practiceSettings: {
      brushSize: 5,
      brushColor: '#000000',
      brushOpacity: 1,
      paperColor: '#F5F5DC',
      gridType: 'cross', // 'cross', 'square', 'none'
      gridColor: '#CCCCCC',
    },

    // 当前参考字帖
    currentCopybook: null,

    // 用户练习历史
    practiceHistory: [],

    // 保存的作品列表
    savedWorks: [],

    // 推荐练习内容
    recommendedContent: [],

    // 练习统计数据
    practiceStats: {
      totalPracticeTime: 0, // 总练习时间（分钟）
      totalWorks: 0, // 总作品数
      totalStrokes: 0, // 总笔画数
      lastPracticeDate: null,
    },
  }),

  getters: {
    // 获取当前练习设置
    getPracticeSettings: (state) => state.practiceSettings,

    // 获取当前参考字帖
    getCurrentCopybook: (state) => state.currentCopybook,

    // 获取最近的练习记录
    getRecentHistory: (state) => {
      return [...state.practiceHistory]
        .sort((a, b) => new Date(b.date) - new Date(a.date))
        .slice(0, 10)
    },

    // 获取保存的作品（按时间倒序）
    getSortedWorks: (state) => {
      return [...state.savedWorks].sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt))
    },

    // 获取练习统计
    getPracticeStats: (state) => state.practiceStats,

    // 获取推荐练习内容
    getRecommendedContent: (state) => state.recommendedContent,
  },

  actions: {
    // 更新练习设置
    updatePracticeSettings(settings) {
      this.practiceSettings = {
        ...this.practiceSettings,
        ...settings,
      }

      // 保存设置到本地存储
      localStorage.setItem('practiceSettings', JSON.stringify(this.practiceSettings))
    },

    // 设置当前参考字帖
    setCurrentCopybook(copybook) {
      this.currentCopybook = copybook
    },

    // 添加练习记录
    async addPracticeRecord(record) {
      try {
        // 调用API保存练习记录
        const response = await copybookAPI.addPracticeRecord({
          copybookId: record.copybookId,
          practiceTime: record.duration,
          charactersPracticed: record.characters || 0,
          notes: record.notes || '',
        })

        // 添加到本地状态
        const newRecord = {
          id: response.id,
          date: response.createdAt,
          ...record,
        }

        this.practiceHistory.push(newRecord)

        // 更新统计数据
        this.updatePracticeStats(record.duration, record.strokes)

        return true
      } catch (error) {
        console.error('保存练习记录失败:', error)
        throw error
      }
    },

    // 保存作品
    async saveWork(work) {
      try {
        // 准备表单数据（支持图片上传）
        const formData = new FormData()
        formData.append('title', work.title || '未命名作品')
        formData.append('description', work.description || '')

        // 添加图片文件（如果有）
        if (work.imageUrl instanceof File) {
          formData.append('image', work.imageUrl)
        } else if (work.imageDataUrl) {
          // 如果是Data URL格式，需要转换为Blob
          const response = await fetch(work.imageDataUrl)
          const blob = await response.blob()
          formData.append('image', blob, 'work.png')
        }

        // 调用API保存作品
        const response = await worksAPI.createWork(formData)

        // 添加到本地状态
        const newWork = {
          id: response.id,
          createdAt: response.createdAt,
          title: response.title,
          description: response.description,
          imageUrl: response.imageUrl,
          status: response.status,
        }

        this.savedWorks.push(newWork)

        // 更新统计数据
        this.practiceStats.totalWorks += 1

        return newWork
      } catch (error) {
        console.error('保存作品失败:', error)
        throw error
      }
    },

    // 删除作品
    async deleteWork(workId) {
      try {
        // 调用API删除作品
        await worksAPI.deleteWork(workId)

        // 从本地状态中移除
        const index = this.savedWorks.findIndex((work) => work.id === workId)
        if (index !== -1) {
          this.savedWorks.splice(index, 1)

          // 更新统计数据
          this.practiceStats.totalWorks = Math.max(0, this.practiceStats.totalWorks - 1)
        }

        return true
      } catch (error) {
        console.error('删除作品失败:', error)
        throw error
      }
    },

    // 更新练习统计数据
    updatePracticeStats(duration, strokes) {
      this.practiceStats.totalPracticeTime += duration
      this.practiceStats.totalStrokes += strokes
      this.practiceStats.lastPracticeDate = new Date().toISOString()

      // 保存统计数据到本地存储
      localStorage.setItem('practiceStats', JSON.stringify(this.practiceStats))
    },

    // 获取推荐练习内容
    async getRecommendations() {
      try {
        const params = {
          sort: 'popular',
          limit: 5,
        }
        const response = await copybookAPI.getCopybooks(params)

        // 处理返回的数据格式
        this.recommendedContent = response.map((copybook) => ({
          id: copybook.id,
          title: copybook.title,
          description: copybook.description,
          level: copybook.difficulty || 'beginner',
          content: copybook.content || copybook.title,
        }))

        return this.recommendedContent
      } catch (err) {
        console.error('获取推荐内容失败:', err);
        // 使用模拟数据作为备选
        this.recommendedContent = [
          { id: 1, title: '楷书基础练习', description: '从基本笔画开始，掌握楷书书写技巧', difficulty: '初级', duration: '30分钟' },
          { id: 2, title: '行书进阶训练', description: '提升行书书写速度和美感', difficulty: '中级', duration: '45分钟' },
          { id: 3, title: '草书艺术欣赏', description: '了解草书的历史和艺术价值', difficulty: '高级', duration: '60分钟' }
        ];
        return this.recommendedContent;
      }
    },

    // 初始化数据
    async initializeData() {
      // 加载本地设置
      this.loadLocalSettings()

      try {
        // 从服务器加载数据
        await Promise.all([
          this.fetchPracticeHistory(),
          this.fetchUserWorks(),
          this.fetchPracticeStats(),
        ])
      } catch (error) {
        console.error('初始化数据失败:', error)
        // 失败时尝试加载本地数据作为备用
        this.loadLocalDataAsBackup()
      }
    },

    // 加载本地设置
    loadLocalSettings() {
      const savedSettings = localStorage.getItem('practiceSettings')
      if (savedSettings) {
        try {
          this.practiceSettings = JSON.parse(savedSettings)
        } catch (e) {
          console.error('加载练习设置失败', e)
        }
      }
    },

    // 从服务器获取练习历史
    async fetchPracticeHistory() {
      try {
        const response = await copybookAPI.getPracticeHistory()
        this.practiceHistory = response.map((record) => ({
          id: record.id,
          date: record.createdAt,
          copybookId: record.copybookId,
          copybookTitle: record.copybook?.title || '未知字帖',
          duration: record.practiceTime,
          characters: record.charactersPracticed,
          notes: record.notes,
        }))
      } catch (error) {
        console.error('获取练习历史失败:', error)
      }
    },

    // 从服务器获取用户作品
    async fetchUserWorks() {
      try {
        // 用户 id 从本地存储里取（userStore 登录时写入的 userInfo）。
        // 之前这里没传 id，实际请求的是 /works/user/undefined。
        const storedUser = getStorage('userInfo') || getSessionStorage('userInfo');
        const userId = storedUser?.id;
        if (!userId) {
          console.warn('未登录，跳过获取用户作品');
          return;
        }
        const response = await worksAPI.getUserWorks(userId);
        // 契约规定列表返回分页信封 {content,page,size,total,totalPages}
        this.savedWorks = (response?.content || []).map((work) => ({
          id: work.id,
          createdAt: work.createdAt,
          title: work.title,
          description: work.description,
          imageUrl: work.imageUrl,
          status: work.status,
          likeCount: work.likeCount || 0,
          commentCount: work.commentCount || 0,
        }))
      } catch (error) {
        console.error('获取用户作品失败:', error)
      }
    },

    // 从服务器获取练习统计
    async fetchPracticeStats() {
      try {
        const response = await copybookAPI.getPracticeStats()
        this.practiceStats = {
          totalPracticeTime: response.totalPracticeTime || 0,
          totalWorks: response.totalWorks || 0,
          totalStrokes: response.totalCharactersPracticed || 0,
          lastPracticeDate: response.lastPracticeDate,
        }
      } catch (error) {
        console.error('获取练习统计失败:', error)
      }
    },

    // 加载本地数据作为备用
    loadLocalDataAsBackup() {
      // 加载统计数据
      const savedStats = localStorage.getItem('practiceStats')
      if (savedStats) {
        try {
          this.practiceStats = JSON.parse(savedStats)
        } catch (e) {
          console.error('加载练习统计失败', e)
        }
      }

      // 加载历史记录
      const savedHistory = localStorage.getItem('practiceHistory')
      if (savedHistory) {
        try {
          this.practiceHistory = JSON.parse(savedHistory)
        } catch (e) {
          console.error('加载练习历史失败', e)
        }
      }

      // 加载保存的作品
      const savedWorks = localStorage.getItem('savedWorks')
      if (savedWorks) {
        try {
          this.savedWorks = JSON.parse(savedWorks)
        } catch (e) {
          console.error('加载保存的作品失败', e)
        }
      }
    },

    // 清空所有数据
    clearAllData() {
      this.practiceHistory = []
      this.savedWorks = []
      this.practiceStats = {
        totalPracticeTime: 0,
        totalWorks: 0,
        totalStrokes: 0,
        lastPracticeDate: null,
      }

      // 清除本地存储
      localStorage.removeItem('practiceHistory')
      localStorage.removeItem('savedWorks')
      localStorage.removeItem('practiceStats')
    },

    // 获取单个作品详情
    async getWorkDetails(workId) {
      try {
        const work = await worksAPI.getWorkDetail(workId)
        return work
      } catch (error) {
        console.error('获取作品详情失败:', error)
        throw error
      }
    },
  },
})
