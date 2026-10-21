<template>
  <div class="community-container">
    <div class="community-header">
      <h2 class="section-title">社区交流</h2>
      <p class="section-subtitle">与书法爱好者交流心得，共同进步</p>
    </div>

    <!-- 社区导航 -->
    <div class="community-tabs">
      <el-tabs v-model="activeTab" class="community-tabs-bar">
        <el-tab-pane label="讨论区" name="discussions">
          <div class="discussions-content">
            <!-- 讨论区头部 -->
            <div class="discussion-header">
              <h3>热门讨论</h3>
              <el-button type="primary" @click="handleCreateDiscussion"> 发布讨论 </el-button>
            </div>

            <!-- 搜索和筛选 -->
            <div class="search-filter">
              <el-input
                v-model="searchQuery"
                placeholder="搜索讨论内容"
                prefix-icon="el-icon-search"
                clearable
                style="width: 300px; margin-right: 10px"
              />
              <el-select v-model="discussionFilter" placeholder="筛选类别">
                <el-option label="全部" value="all" />
                <el-option label="学习交流" value="study" />
                <el-option label="作品点评" value="review" />
                <el-option label="问题求助" value="help" />
                <el-option label="资源分享" value="share" />
              </el-select>
            </div>

            <!-- 讨论列表 -->
              <div class="discussions-list" v-loading="loading">
                <div
                  v-for="discussion in discussions"
                  :key="discussion.id"
                  class="discussion-item"
                >
                  <div class="discussion-content">
                    <h4 class="discussion-title">{{ discussion.title }}</h4>
                    <p class="discussion-excerpt">{{ truncateText(discussion.content, 100) }}</p>

                    <div class="discussion-meta">
                      <div class="author-info">
                        <img
                          :src="discussion.authorAvatar"
                          :alt="discussion.authorName"
                          class="author-avatar"
                        />
                        <span class="author-name">{{ discussion.authorName }}</span>
                        <span class="post-time">{{ formatDate(discussion.createdAt) }}</span>
                      </div>

                      <div class="discussion-stats">
                        <span class="stat-item">
                          <i class="el-icon-view"></i>
                          {{ discussion.views }}
                        </span>
                        <span class="stat-item">
                          <i class="el-icon-chat-dot-round" @click="handleReply(discussion)"></i>
                          {{ discussion.comments }}
                        </span>
                        <span
                          class="stat-item"
                          @click="handleLike(discussion)"
                          :class="{ active: discussion.isLiked }"
                        >
                          <i class="el-icon-thumb-up"></i>
                          {{ discussion.likes }}
                        </span>
                      </div>
                    </div>
                  </div>
                </div>
                <div v-if="discussions.length === 0 && !loading" class="empty-state">
                  <el-empty description="暂无讨论" />
                </div>
              </div>

            <!-- 分页 -->
            <div class="pagination">
              <el-pagination
                v-model:current-page="discussionPage"
                v-model:page-size="pageSize"
                :page-sizes="[10, 20, 50]"
                layout="total, sizes, prev, pager, next, jumper"
                :total="totalDiscussions"
              />
            </div>
          </div>
        </el-tab-pane>

        <el-tab-pane label="活动" name="activities">
          <div class="activities-content">
            <!-- 活动头部 -->
            <div class="activity-header">
              <h3>近期活动</h3>
              <el-button type="primary" @click="createActivity">创建活动</el-button>
            </div>

            <!-- 活动列表 -->
            <div class="activities-list">
              <div v-for="activity in activities" :key="activity.id" class="activity-item">
                <div class="activity-image">
                  <img :src="activity.imageUrl" :alt="activity.title" class="activity-img" />
                </div>

                <div class="activity-info">
                  <h4 class="activity-title">{{ activity.title }}</h4>
                  <p class="activity-description">{{ truncateText(activity.description, 100) }}</p>

                  <div class="activity-meta">
                    <div class="activity-time">
                      <i class="el-icon-date"></i>
                      {{ activity.time }}
                    </div>
                    <div class="activity-location">
                      <i class="el-icon-position"></i>
                      {{ activity.location }}
                    </div>
                    <div class="activity-participants">
                      <i class="el-icon-user"></i>
                      {{ activity.participants }} 人已报名
                    </div>
                  </div>

                  <div class="activity-actions">
                    <el-button type="primary" size="small" @click="joinActivity(activity.id)">
                      {{ isUserJoined(activity.id) ? '已报名' : '立即报名' }}
                    </el-button>
                    <el-button size="small" @click="viewActivityDetail(activity.id)"
                      >查看详情</el-button
                    >
                  </div>
                </div>
              </div>
            </div>
          </div>
        </el-tab-pane>

        <el-tab-pane label="学习小组" name="groups">
          <div class="groups-content">
            <!-- 小组头部 -->
            <div class="group-header">
              <h3>学习小组</h3>
              <el-button type="primary" @click="createGroup">创建小组</el-button>
            </div>

            <!-- 小组列表 -->
            <div class="groups-list">
              <div v-for="group in groups" :key="group.id" class="group-item">
                <div class="group-icon">
                  <img :src="group.iconUrl" :alt="group.name" class="group-img" />
                </div>

                <div class="group-info">
                  <h4 class="group-name">{{ group.name }}</h4>
                  <p class="group-description">{{ truncateText(group.description, 80) }}</p>

                  <div class="group-stats">
                    <span class="stat-item">{{ group.members }} 成员</span>
                    <span class="stat-item">{{ group.topics }} 话题</span>
                    <span class="stat-item">{{ group.activities }} 活动</span>
                  </div>
                </div>

                <div class="group-actions">
                  <el-button type="primary" size="small" @click="joinGroup(group.id)">
                    {{ isUserInGroup(group.id) ? '已加入' : '加入' }}
                  </el-button>
                </div>
              </div>
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>

    <!-- 右侧边栏 -->
    <div class="community-sidebar">
      <div class="sidebar-section">
        <h4 class="section-header">社区公告</h4>
        <div class="announcement-list">
          <div
            v-for="announcement in announcements"
            :key="announcement.id"
            class="announcement-item"
          >
            <p class="announcement-title">{{ announcement.title }}</p>
            <span class="announcement-date">{{ formatDate(announcement.createdAt) }}</span>
          </div>
        </div>
      </div>

      <div class="sidebar-section">
        <h4 class="section-header">活跃用户</h4>
        <div class="active-users">
          <div v-for="user in activeUsers" :key="user.id" class="user-item">
            <img :src="user.avatar" :alt="user.name" class="user-avatar" />
            <div class="user-info">
              <div class="user-name">{{ user.name }}</div>
              <div class="user-rank">{{ user.rank }}</div>
            </div>
          </div>
        </div>
      </div>

      <div class="sidebar-section">
        <h4 class="section-header">热门标签</h4>
        <div class="tags-list">
          <el-tag v-for="tag in popularTags" :key="tag.name" @click="searchByTag(tag.name)">
            {{ tag.name }} ({{ tag.count }})
          </el-tag>
        </div>
      </div>
    </div>

    <!-- 发布讨论对话框 -->
    <el-dialog
      v-model="showCreatePostDialog"
      title="发布讨论"
      width="600px"
      :before-close="closeCreatePostDialog"
    >
      <el-form :model="newPost" label-width="80px">
        <el-form-item label="标题" prop="title">
          <el-input v-model="newPost.title" placeholder="请输入讨论标题" />
        </el-form-item>

        <el-form-item label="类别" prop="category">
          <el-select v-model="newPost.category" placeholder="请选择讨论类别">
            <el-option label="学习交流" value="study" />
            <el-option label="作品点评" value="review" />
            <el-option label="问题求助" value="help" />
            <el-option label="资源分享" value="share" />
          </el-select>
        </el-form-item>

        <el-form-item label="内容" prop="content">
          <el-input
            v-model="newPost.content"
            type="textarea"
            :rows="6"
            placeholder="请输入讨论内容"
          />
        </el-form-item>

        <el-form-item label="标签">
          <el-tag-input v-model="newPostTags" placeholder="请输入标签，回车确认" />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="closeCreatePostDialog">取消</el-button>
        <el-button type="primary" @click="submitPost">发布</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../stores/userStore'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getDiscussions, createDiscussion, likeDiscussion, replyToDiscussion } from '../api/community'

// 状态管理
const activeTab = ref('discussions')
const searchQuery = ref('')
const discussionFilter = ref('all')
const discussionPage = ref(1)
const pageSize = ref(10)
const showCreatePostDialog = ref(false)
const loading = ref(false)
const totalDiscussions = ref(0)

// 引入状态管理
const userStore = useUserStore()
const router = useRouter()

// 计算属性：检查用户是否登录
const isLoggedIn = computed(() => {
  return userStore.isLoggedIn
})

// 计算属性：获取当前用户名
const currentUsername = computed(() => {
  return userStore.userInfo?.username || ''
})

// 获取讨论列表
const fetchDiscussions = async () => {
  loading.value = true
  try {
    const response = await getDiscussions({
      page: discussionPage.value - 1,
      size: pageSize.value,
      category: discussionFilter.value !== 'all' ? discussionFilter.value : undefined,
      keyword: searchQuery.value || undefined
    })
    
    if (response.success) {
      discussions.value = response.data.content.map(discussion => ({
        id: discussion.id,
        title: discussion.title,
        content: discussion.content,
        authorName: discussion.username,
        authorAvatar: discussion.userAvatar || 'https://picsum.photos/seed/default/100/100',
        category: discussion.category,
        views: discussion.viewCount || 0,
        comments: discussion.replyCount || 0,
        likes: discussion.likeCount || 0,
        createdAt: discussion.createdAt,
        isLiked: false // 需要实现点赞状态检查
      }))
      totalDiscussions.value = response.data.totalElements
    }
  } catch (error) {
    ElMessage.error('获取讨论列表失败')
    console.error('获取讨论列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 创建讨论
const handleCreateDiscussion = () => {
  if (!isLoggedIn.value) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }

  showCreatePostDialog.value = true
}

// 处理回复
const handleReply = (discussion) => {
  if (!isLoggedIn.value) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }

  ElMessageBox.prompt('请输入回复内容', '回复讨论', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    inputPattern: /^.+$/, // 非空验证
    inputErrorMessage: '回复内容不能为空',
    inputType: 'textarea',
    inputPlaceholder: '写下你的回复...',
  })
    .then(() => {
      discussion.comments++
      ElMessage.success('回复成功')
    })
    .catch(() => {
      // 取消回复
    })
}

// 模拟讨论数据
const discussions = ref([
  {
    id: 1,
    title: '如何提高楷书的笔力？',
    content:
      '最近练习楷书有一段时间了，总感觉笔力不够，线条没有力度，请问各位老师有什么好的方法可以提高笔力吗？',
    authorName: '书法爱好者',
    authorAvatar: 'https://picsum.photos/seed/user1/100/100',
    category: 'study',
    views: 523,
    comments: 34,
    likes: 28,
    createdAt: '2024-01-15T10:30:00Z',
  },
  {
    id: 2,
    title: '请各位老师点评我的作品',
    content: '这是我最近练习的行书作品，希望各位老师能给予指导和点评，指出不足之处，谢谢！',
    authorName: '初学者小王',
    authorAvatar: 'https://picsum.photos/seed/user2/100/100',
    category: 'review',
    views: 345,
    comments: 45,
    likes: 32,
    createdAt: '2024-01-14T14:20:00Z',
  },
  {
    id: 3,
    title: '求推荐适合初学者的字帖',
    content: '刚刚开始学习书法，想找一些适合初学者的字帖，请问各位有什么好的推荐吗？',
    authorName: '书法新手',
    authorAvatar: 'https://picsum.photos/seed/user3/100/100',
    category: 'help',
    views: 789,
    comments: 67,
    likes: 45,
    createdAt: '2024-01-13T09:15:00Z',
  },
  {
    id: 4,
    title: '分享一些书法学习资源',
    content: '整理了一些书法学习的优质资源，包括教程视频、电子书、练习方法等，希望对大家有帮助。',
    authorName: '资深书家',
    authorAvatar: 'https://picsum.photos/seed/user4/100/100',
    category: 'share',
    views: 1234,
    comments: 89,
    likes: 123,
    createdAt: '2024-01-12T16:45:00Z',
  },
])

// 过滤后的讨论列表
const filteredDiscussions = computed(() => {
  let result = [...discussions.value]

  // 按搜索词过滤
  if (searchQuery.value) {
    const query = searchQuery.value.toLowerCase()
    result = result.filter(
      (discussion) =>
        discussion.title.toLowerCase().includes(query) ||
        discussion.content.toLowerCase().includes(query),
    )
  }

  // 按分类过滤
  if (discussionFilter.value !== 'all') {
    result = result.filter((discussion) => discussion.category === discussionFilter.value)
  }

  return result
})

// 模拟活动数据
const activities = ref([
  {
    id: 1,
    title: '春季书法展',
    description: '展示优秀书法作品，交流学习心得，欢迎各位书法爱好者参加。',
    imageUrl: 'https://picsum.photos/seed/activity1/400/300',
    time: '2024-03-15 10:00-16:00',
    location: '北京市朝阳区美术馆',
    participants: 156,
  },
  {
    id: 2,
    title: '书法大师讲座',
    description: '邀请著名书法家进行现场讲座，分享书法技巧和心得。',
    imageUrl: 'https://picsum.photos/seed/activity2/400/300',
    time: '2024-03-20 14:00-16:00',
    location: '线上直播',
    participants: 234,
  },
  {
    id: 3,
    title: '书法公益课堂',
    description: '面向初学者的公益书法课堂，免费教授基础书法知识和技巧。',
    imageUrl: 'https://picsum.photos/seed/activity3/400/300',
    time: '2024-03-25 09:00-11:00',
    location: '上海市静安区文化中心',
    participants: 89,
  },
])

// 模拟学习小组数据
const groups = ref([
  {
    id: 1,
    name: '楷书研习社',
    description: '专注于楷书学习和交流的小组，定期组织线上线下活动。',
    iconUrl: 'https://picsum.photos/seed/group1/100/100',
    members: 234,
    topics: 156,
    activities: 23,
  },
  {
    id: 2,
    name: '行书爱好者',
    description: '行书学习交流小组，欢迎喜爱行书的朋友加入。',
    iconUrl: 'https://picsum.photos/seed/group2/100/100',
    members: 189,
    topics: 123,
    activities: 18,
  },
  {
    id: 3,
    name: '书法初学者联盟',
    description: '为书法初学者提供帮助和指导的学习小组。',
    iconUrl: 'https://picsum.photos/seed/group3/100/100',
    members: 345,
    topics: 234,
    activities: 34,
  },
])

// 模拟公告数据
const announcements = ref([
  {
    id: 1,
    title: '社区新功能上线公告',
    createdAt: '2024-01-15T08:00:00Z',
  },
  {
    id: 2,
    title: '春节期间活动安排',
    createdAt: '2024-01-10T10:00:00Z',
  },
  {
    id: 3,
    title: '社区管理规范更新',
    createdAt: '2024-01-05T14:00:00Z',
  },
])

// 模拟活跃用户数据
const activeUsers = ref([
  {
    id: 1,
    name: '书法大师',
    avatar: 'https://picsum.photos/seed/active1/100/100',
    rank: 'Lv.5',
  },
  {
    id: 2,
    name: '练字达人',
    avatar: 'https://picsum.photos/seed/active2/100/100',
    rank: 'Lv.4',
  },
  {
    id: 3,
    name: '墨水飘香',
    avatar: 'https://picsum.photos/seed/active3/100/100',
    rank: 'Lv.4',
  },
])

// 模拟热门标签数据
const popularTags = ref([
  { name: '楷书', count: 234 },
  { name: '行书', count: 189 },
  { name: '入门教程', count: 156 },
  { name: '笔力练习', count: 123 },
  { name: '字帖推荐', count: 102 },
  { name: '作品点评', count: 98 },
])

// 新讨论表单数据
const newPost = reactive({
  title: '',
  category: '',
  content: '',
})
const newPostTags = ref([])

// 发布讨论 - 已废弃，使用submitPost函数
// const createPost = () => {
//   showCreatePostDialog.value = true
// }

// 关闭创建对话框
const closeCreatePostDialog = () => {
  showCreatePostDialog.value = false
  newPost.title = ''
  newPost.content = ''
  newPost.category = ''
  newPostTags.value = []
}

// 处理点赞
const handleLike = async (discussion) => {
  if (!isLoggedIn.value) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }

  try {
    const response = await likeDiscussion(discussion.id)
    if (response.success) {
      discussion.isLiked = !discussion.isLiked
      discussion.likes += discussion.isLiked ? 1 : -1
      ElMessage.success(discussion.isLiked ? '点赞成功' : '取消点赞成功')
    }
  } catch (error) {
    ElMessage.error('操作失败')
    console.error('点赞失败:', error)
  }
}

// 提交讨论
const submitPost = async () => {
  if (!newPost.title || !newPost.category || !newPost.content) {
    ElMessage.warning('请填写完整信息')
    return
  }

  loading.value = true
  try {
    const response = await createDiscussion({
      title: newPost.title,
      content: newPost.content,
      category: newPost.category,
      tags: newPostTags.value.join(',')
    })
    
    if (response.success) {
      ElMessage.success('讨论发布成功')
      closeCreatePostDialog()
      await fetchDiscussions()
    }
  } catch (error) {
    ElMessage.error('讨论发布失败')
    console.error('讨论发布失败:', error)
  } finally {
    loading.value = false
  }
}

// 报名活动 - 已废弃，使用后端API
// const joinActivity = (activityId) => {
//   ElMessage.success('报名成功')
// }

// 查看活动详情 - 已废弃，使用路由跳转
// const viewActivityDetail = (activityId) => {
//   ElMessage.info('查看活动详情功能开发中')
// }

// 创建活动 - 已废弃，使用后端API
// const createActivity = () => {
//   ElMessage.info('创建活动功能开发中')
// }

// 加入小组 - 已废弃，使用后端API
// const joinGroup = (groupId) => {
//   ElMessage.success('加入小组成功')
// }

// 创建小组 - 已废弃，使用后端API
// const createGroup = () => {
//   ElMessage.info('创建小组功能开发中')
// }

// 按标签搜索 - 已废弃，直接设置搜索值
// const searchByTag = (tagName) => {
//   searchQuery.value = tagName
//   activeTab.value = 'discussions'
// }

// 工具函数
const truncateText = (text, maxLength) => {
  if (!text) return ''
  return text.length > maxLength ? text.substring(0, maxLength) + '...' : text
}

// 工具函数 - 已废弃，使用Element Plus的日期格式化
// const formatDate = (dateString) => {
//   const date = new Date(dateString)
//   return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
// }

// 工具函数 - 已废弃，使用计算属性
// const isUserJoined = (activityId) => {
//   return joinedActivities.value.includes(activityId);
// };

// 工具函数 - 已废弃，使用计算属性
// const isUserInGroup = (groupId) => {
//   return joinedGroups.value.includes(groupId);
// };

// 组件挂载时初始化
onMounted(() => {
  fetchDiscussions()
})

// 监听筛选条件变化
watch([searchQuery, discussionFilter, discussionPage], () => {
  fetchDiscussions()
})

// 搜索输入防抖
let searchTimer = null
const onSearchInput = () => {
  clearTimeout(searchTimer)
  searchTimer = setTimeout(() => {
    fetchDiscussions()
  }, 500)
}
</script>

<style scoped>
.community-container {
  display: grid;
  grid-template-columns: 1fr 300px;
  gap: 2rem;
  padding: 2rem 0;
}

.community-header {
  grid-column: 1 / -1;
  text-align: center;
  margin-bottom: 2rem;
}

.section-title {
  font-size: 2rem;
  margin-bottom: 0.5rem;
  color: #333;
  font-family: 'SimSun', serif;
}

.section-subtitle {
  color: #666;
}

/* 社区内容区域 */
.community-tabs {
  background-color: white;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.community-tabs-bar {
  margin: 0;
}

.community-tabs-bar .el-tabs__header {
  border-bottom: 1px solid #e6e6e6;
}

/* 讨论区样式 */
.discussions-content {
  padding: 1.5rem;
}

.discussion-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1.5rem;
}

.discussion-header h3 {
  margin: 0;
  color: #333;
}

.search-filter {
  display: flex;
  align-items: center;
  margin-bottom: 1.5rem;
}

.discussions-list {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.discussion-item {
  padding: 1.5rem;
  background-color: #f8f9fa;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
}

.discussion-item:hover {
  background-color: #f0f2f5;
  transform: translateY(-2px);
}

.discussion-title {
  font-size: 1.2rem;
  margin-bottom: 0.5rem;
  color: #333;
}

.discussion-excerpt {
  color: #666;
  margin-bottom: 1rem;
  line-height: 1.6;
}

.discussion-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.author-info {
  display: flex;
  align-items: center;
  gap: 0.8rem;
}

.author-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  object-fit: cover;
}

.author-name {
  font-weight: 500;
  color: #333;
}

.post-time {
  color: #999;
  font-size: 0.9rem;
}

.discussion-stats {
  display: flex;
  gap: 1.5rem;
  color: #999;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 0.3rem;
}

/* 活动区域样式 */
.activities-content {
  padding: 1.5rem;
}

.activity-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1.5rem;
}

.activity-header h3 {
  margin: 0;
  color: #333;
}

.activities-list {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.activity-item {
  display: flex;
  gap: 1.5rem;
  padding: 1.5rem;
  background-color: #f8f9fa;
  border-radius: 8px;
  transition: all 0.3s;
}

.activity-item:hover {
  background-color: #f0f2f5;
  transform: translateY(-2px);
}

.activity-image {
  flex-shrink: 0;
  width: 200px;
  height: 150px;
  border-radius: 8px;
  overflow: hidden;
}

.activity-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.activity-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.activity-title {
  font-size: 1.2rem;
  margin-bottom: 0.5rem;
  color: #333;
}

.activity-description {
  color: #666;
  margin-bottom: 1rem;
  line-height: 1.6;
}

.activity-meta {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
  margin-bottom: 1rem;
  color: #666;
}

.activity-time,
.activity-location,
.activity-participants {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.activity-actions {
  display: flex;
  gap: 1rem;
}

/* 学习小组样式 */
.groups-content {
  padding: 1.5rem;
}

.group-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1.5rem;
}

.group-header h3 {
  margin: 0;
  color: #333;
}

.groups-list {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.group-item {
  display: flex;
  align-items: center;
  gap: 1.5rem;
  padding: 1.5rem;
  background-color: #f8f9fa;
  border-radius: 8px;
  transition: all 0.3s;
}

.group-item:hover {
  background-color: #f0f2f5;
  transform: translateY(-2px);
}

.group-icon {
  flex-shrink: 0;
  width: 80px;
  height: 80px;
  border-radius: 50%;
  overflow: hidden;
}

.group-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.group-info {
  flex: 1;
}

.group-name {
  font-size: 1.2rem;
  margin-bottom: 0.5rem;
  color: #333;
}

.group-description {
  color: #666;
  margin-bottom: 0.5rem;
  line-height: 1.6;
}

.group-stats {
  display: flex;
  gap: 1.5rem;
  color: #999;
}

/* 侧边栏样式 */
.community-sidebar {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.sidebar-section {
  background-color: white;
  border-radius: 8px;
  padding: 1.5rem;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.section-header {
  font-size: 1.1rem;
  margin-bottom: 1rem;
  color: #333;
  border-bottom: 2px solid #8b4513;
  padding-bottom: 0.5rem;
}

.announcement-list {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.announcement-item {
  cursor: pointer;
  padding: 0.5rem;
  border-radius: 4px;
  transition: all 0.3s;
}

.announcement-item:hover {
  background-color: #f8f9fa;
}

.announcement-title {
  font-weight: 500;
  color: #333;
  margin-bottom: 0.3rem;
}

.announcement-date {
  color: #999;
  font-size: 0.8rem;
}

.active-users {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.user-item {
  display: flex;
  align-items: center;
  gap: 0.8rem;
}

.user-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  object-fit: cover;
}

.user-name {
  font-weight: 500;
  color: #333;
}

.user-rank {
  color: #999;
  font-size: 0.9rem;
}

.tags-list {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
}

.tags-list .el-tag {
  cursor: pointer;
  transition: all 0.3s;
}

.tags-list .el-tag:hover {
  transform: scale(1.05);
}

/* 分页 */
.pagination {
  margin-top: 2rem;
  display: flex;
  justify-content: center;
}

/* 响应式设计 */
@media (max-width: 1024px) {
  .community-container {
    grid-template-columns: 1fr;
  }

  .community-sidebar {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  }
}

@media (max-width: 768px) {
  .discussion-meta {
    flex-direction: column;
    align-items: flex-start;
    gap: 1rem;
  }

  .activity-item {
    flex-direction: column;
  }

  .activity-image {
    width: 100%;
    height: 200px;
  }

  .group-item {
    flex-direction: column;
    align-items: flex-start;
  }

  .group-actions {
    align-self: flex-end;
  }

  .search-filter {
    flex-direction: column;
    align-items: stretch;
    gap: 1rem;
  }

  .search-filter .el-input,
  .search-filter .el-select {
    width: 100% !important;
  }
}

@media (max-width: 480px) {
  .community-tabs-bar .el-tabs__nav {
    overflow-x: auto;
  }

  .discussion-header,
  .activity-header,
  .group-header {
    flex-direction: column;
    align-items: stretch;
    gap: 1rem;
  }

  .activity-meta {
    gap: 0.3rem;
  }

  .activity-actions {
    flex-direction: column;
  }
}
</style>
