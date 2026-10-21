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

    // 练习统计数据 —— 形状与契约的 PracticeStatistics 一致
    // （GET /api/copybooks/statistics/user 返回裸对象，没有 data 信封）
    practiceStats: {
      totalPractices: 0, // 练习总次数
      totalDuration: 0, // 总练习时长（秒）
      averageDuration: 0, // 平均单次时长（秒）
      streakDays: 0, // 连续练习天数
      characterCount: 0, // 累计练习字数
      copybookCount: 0, // 练过的字帖数（去重）
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
      // 真实方法叫 copybookAPI.recordPractice(copybookId, data) —— 两个参数
      // （原来调的 copybookAPI.addPracticeRecord 根本不存在，运行时直接 TypeError）
      if (!record?.copybookId) {
        throw new Error('缺少字帖 ID，无法记录练习');
      }
      const response = await copybookAPI.recordPractice(record.copybookId, {
        title: record.title ?? null,
        content: record.content ?? null,
        practiceType: record.practiceType ?? null,
        // 契约的 duration 单位是「秒」
        duration: record.duration ?? 0,
        characterCount: record.characters ?? record.characterCount ?? 0,
        difficulty: record.difficulty ?? null,
        notes: record.notes ?? '',
      })

      // 添加到本地状态
      this.practiceHistory.unshift({
        id: response.id,
        date: response.createdAt,
        ...record,
      })

      // 更新统计数据
      this.updatePracticeStats(record.duration, record.characters ?? record.characterCount ?? 0)

      return true
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

    // 更新练习统计数据（字段与契约的 PracticeStatistics 一致）
    updatePracticeStats(duration, characterCount) {
      this.practiceStats.totalPractices += 1
      this.practiceStats.totalDuration += duration || 0
      this.practiceStats.characterCount += characterCount || 0

      // 保存统计数据到本地存储
      localStorage.setItem('practiceStats', JSON.stringify(this.practiceStats))
    },

    // 获取推荐练习内容
    async getRecommendations() {
      // 用「热门字帖」端点 —— 列表端点只认 page/size/category/difficulty，
      // 传 sort/limit 后端会忽略，等于拿到的是第 0 页默认排序而不是热门榜
      const response = await copybookAPI.getPopularCopybooks({ size: 5 })
      const list = response?.content ?? []

      this.recommendedContent = list.map((copybook) => ({
        id: copybook.id,
        title: copybook.title,
        description: copybook.description,
        level: copybook.difficulty || 'beginner',
        content: copybook.description || copybook.title,
      }))

      return this.recommendedContent
    },

    // 初始化数据
    async initializeData() {
      // 加载本地设置（这是用户自己的笔刷/纸张偏好，不是假数据）
      this.loadLocalSettings()

      // 从服务器加载数据。
      // 失败**不**回退到本地缓存的旧数据 —— 那会让接口没通时页面看起来是好的。
      await Promise.all([
        this.fetchPracticeHistory(),
        this.fetchUserWorks(),
        this.fetchPracticeStats(),
      ])
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
    // GET /api/copybooks/practice/history —— 返回**分页信封**，数据在 content 里
    async fetchPracticeHistory() {
      try {
        const response = await copybookAPI.getPracticeHistory()
        this.practiceHistory = (response?.content || []).map((record) => ({
          id: record.id,
          date: record.createdAt,
          copybookId: record.copybookId,
          // 契约的练习记录里没有嵌套的 copybook 对象，字帖标题用 record.title
          copybookTitle: record.title || '未知字帖',
          // 契约字段是 duration（秒）与 characterCount
          duration: record.duration,
          characters: record.characterCount,
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
    // GET /api/copybooks/statistics/user —— 返回裸对象，字段与契约的 PracticeStatistics 一致
    async fetchPracticeStats() {
      try {
        const response = await copybookAPI.getUserStatistics()
        this.practiceStats = {
          totalPractices: response?.totalPractices || 0,
          totalDuration: response?.totalDuration || 0,
          averageDuration: response?.averageDuration || 0,
          streakDays: response?.streakDays || 0,
          characterCount: response?.characterCount || 0,
          copybookCount: response?.copybookCount || 0,
        }
      } catch (error) {
        console.error('获取练习统计失败:', error)
      }
    },

    // 清空所有数据
    clearAllData() {
      this.practiceHistory = []
      this.savedWorks = []
      this.practiceStats = {
        totalPractices: 0,
        totalDuration: 0,
        averageDuration: 0,
        streakDays: 0,
        characterCount: 0,
        copybookCount: 0,
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
