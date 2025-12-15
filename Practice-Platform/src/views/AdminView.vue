<template>
  <div class="admin-container">
    <!-- 侧边栏导航 -->
    <div class="admin-sidebar">
      <div class="sidebar-header">
        <h2>管理中心</h2>
      </div>
      
      <nav class="sidebar-nav">
        <div 
          class="nav-item"
          :class="{ active: activeTab === 'users' }"
          @click="setActiveTab('users')"
        >
          <i class="el-icon-user"></i>
          <span>用户管理</span>
        </div>
        
        <div 
          class="nav-item"
          :class="{ active: activeTab === 'works' }"
          @click="setActiveTab('works')"
        >
          <i class="el-icon-picture"></i>
          <span>作品管理</span>
        </div>
        
        <div 
          class="nav-item"
          :class="{ active: activeTab === 'comments' }"
          @click="setActiveTab('comments')"
        >
          <i class="el-icon-chat-dot-round"></i>
          <span>评论管理</span>
        </div>
        
        <div 
          class="nav-item"
          :class="{ active: activeTab === 'category' }"
          @click="setActiveTab('category')"
        >
          <i class="el-icon-folder"></i>
          <span>分类管理</span>
        </div>
        
        <div 
          class="nav-item"
          :class="{ active: activeTab === 'system' }"
          @click="setActiveTab('system')"
        >
          <i class="el-icon-setting"></i>
          <span>系统设置</span>
        </div>
        
        <div 
          class="nav-item"
          :class="{ active: activeTab === 'analytics' }"
          @click="setActiveTab('analytics')"
        >
          <i class="el-icon-data-line"></i>
          <span>数据分析</span>
        </div>
      </nav>
      
      <div class="sidebar-footer">
          <div class="admin-info">
            <img :src="getUserAvatar" alt="管理员头像" class="admin-avatar-small" />
            <span>{{ getUsername }}</span>
          </div>
          <el-button type="danger" plain @click="logout" style="width: 100%; margin-top: 10px;">退出登录</el-button>
        </div>
    </div>
    
    <!-- 主内容区 -->
    <div class="admin-main">
      <!-- 顶部导航栏 -->
      <div class="main-header">
        <div class="header-title">
          <h3>{{ getTabTitle(activeTab) }}</h3>
        </div>
        
        <div class="header-info">
        <div class="user-info">
          <img :src="getUserAvatar" alt="管理员头像" class="admin-avatar" />
          <span>{{ getUsername }}</span>
        </div>
        <div class="current-time">
          {{ formattedDateTime }}
        </div>
      </div>
      </div>
      
      <!-- 内容区域 -->
      <div class="content-area">
        <!-- 用户管理 -->
        <div v-if="activeTab === 'users'" class="tab-content">
          <div class="content-header">
            <h4>用户列表</h4>
            <div class="header-actions">
              <el-input
                v-model="userSearchQuery"
                placeholder="搜索用户"
                prefix-icon="el-icon-search"
                clearable
                style="width: 200px; margin-right: 10px;"
              />
              <el-select v-model="userStatusFilter" placeholder="状态" style="width: 120px; margin-right: 10px;">
                <el-option label="全部" value="all" />
                <el-option label="正常" value="active" />
                <el-option label="禁用" value="disabled" />
              </el-select>
              <el-button type="primary">添加用户</el-button>
            </div>
          </div>
          
          <div class="data-table">
            <el-table :data="filteredUsers" style="width: 100%">
              <el-table-column type="selection" width="55" />
              <el-table-column prop="id" label="用户ID" width="80" />
              <el-table-column prop="username" label="用户名" />
              <el-table-column prop="email" label="邮箱" />
              <el-table-column prop="role" label="角色">
                <template #default="scope">
                  <el-tag :type="getRoleType(scope.row.role)">{{ scope.row.role }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="status" label="状态">
                <template #default="scope">
                  <el-switch
                    v-model="scope.row.status"
                    active-value="active"
                    inactive-value="disabled"
                    @change="handleUserStatusChange(scope.row)"
                  />
                </template>
              </el-table-column>
              <el-table-column prop="createdAt" label="注册时间">
                <template #default="scope">
                  {{ formatDate(scope.row.createdAt) }}
                </template>
              </el-table-column>
              <el-table-column prop="lastLogin" label="最后登录" />
              <el-table-column label="操作" width="150" fixed="right">
                <template #default="scope">
                  <el-button size="small" @click="editUser(scope.row)">编辑</el-button>
                  <el-button size="small" type="danger" @click="handleDeleteUser(scope.row.id)">删除</el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
          
          <div class="pagination">
            <el-pagination
              v-model:current-page="userCurrentPage"
              v-model:page-size="userPageSize"
              :page-sizes="[10, 20, 50]"
              layout="total, sizes, prev, pager, next, jumper"
              :total="filteredUsers.length"
            />
          </div>
        </div>
        
        <!-- 作品管理 -->
        <div v-if="activeTab === 'works'" class="tab-content">
          <div class="content-header">
            <h4>作品列表</h4>
            <div class="header-actions">
              <el-input
                v-model="workSearchQuery"
                placeholder="搜索作品标题或作者"
                prefix-icon="el-icon-search"
                clearable
                style="width: 300px; margin-right: 10px;"
              />
              <el-select v-model="workCategoryFilter" placeholder="分类" style="width: 120px; margin-right: 10px;">
                <el-option label="全部" value="all" />
                <el-option label="楷书" value="kai" />
                <el-option label="行书" value="xing" />
                <el-option label="草书" value="cao" />
                <el-option label="隶书" value="li" />
                <el-option label="篆书" value="zhuan" />
              </el-select>
              <el-select v-model="workStatusFilter" placeholder="审核状态" style="width: 120px; margin-right: 10px;">
                <el-option label="全部" value="all" />
                <el-option label="待审核" value="pending" />
                <el-option label="已通过" value="approved" />
                <el-option label="已拒绝" value="rejected" />
              </el-select>
              <el-button type="primary">批量审核</el-button>
            </div>
          </div>
          
          <div class="data-table">
            <el-table :data="filteredWorks" style="width: 100%">
              <el-table-column type="selection" width="55" />
              <el-table-column prop="id" label="作品ID" width="80" />
              <el-table-column label="作品图片" width="100">
                <template #default="scope">
                  <img :src="scope.row.imageUrl" alt="作品缩略图" class="work-thumbnail-small" />
                </template>
              </el-table-column>
              <el-table-column prop="title" label="作品标题" />
              <el-table-column prop="authorName" label="作者" />
              <el-table-column prop="category" label="分类" />
              <el-table-column prop="status" label="审核状态">
                <template #default="scope">
                  <el-tag :type="getStatusType(scope.row.status)">{{ getStatusText(scope.row.status) }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="createdAt" label="上传时间">
                <template #default="scope">
                  {{ formatDate(scope.row.createdAt) }}
                </template>
              </el-table-column>
              <el-table-column prop="views" label="浏览量" />
              <el-table-column label="操作" width="200" fixed="right">
                <template #default="scope">
                  <el-button size="small" @click="viewWork(scope.row)">查看</el-button>
                  <el-button v-if="scope.row.status === 'pending'" size="small" type="success" @click="approveWork(scope.row.id)">通过</el-button>
                  <el-button v-if="scope.row.status === 'pending'" size="small" type="danger" @click="rejectWork(scope.row.id)">拒绝</el-button>
                  <el-button size="small" type="danger" @click="handleDeleteWork(scope.row.id)">删除</el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
          
          <div class="pagination">
            <el-pagination
              v-model:current-page="workCurrentPage"
              v-model:page-size="workPageSize"
              :page-sizes="[10, 20, 50]"
              layout="total, sizes, prev, pager, next, jumper"
              :total="filteredWorks.length"
            />
          </div>
        </div>
        
        <!-- 评论管理 -->
        <div v-if="activeTab === 'comments'" class="tab-content">
          <div class="content-header">
            <h4>评论列表</h4>
            <div class="header-actions">
              <el-input
                v-model="commentSearchQuery"
                placeholder="搜索评论内容"
                prefix-icon="el-icon-search"
                clearable
                style="width: 300px; margin-right: 10px;"
              />
              <el-select v-model="commentStatusFilter" placeholder="状态" style="width: 120px; margin-right: 10px;">
                <el-option label="全部" value="all" />
                <el-option label="已显示" value="active" />
                <el-option label="已隐藏" value="hidden" />
              </el-select>
              <el-button type="danger">批量删除</el-button>
            </div>
          </div>
          
          <div class="data-table">
            <el-table :data="filteredComments" style="width: 100%">
              <el-table-column type="selection" width="55" />
              <el-table-column prop="id" label="评论ID" width="80" />
              <el-table-column prop="content" label="评论内容">
                <template #default="scope">
                  <div class="comment-content">{{ truncateText(scope.row.content, 50) }}</div>
                </template>
              </el-table-column>
              <el-table-column prop="username" label="评论者" />
              <el-table-column prop="workTitle" label="所属作品" />
              <el-table-column prop="status" label="状态">
                <template #default="scope">
                  <el-switch
                    v-model="scope.row.status"
                    active-value="active"
                    inactive-value="hidden"
                    @change="handleCommentStatusChange(scope.row)"
                  />
                </template>
              </el-table-column>
              <el-table-column prop="createdAt" label="评论时间">
                <template #default="scope">
                  {{ formatDate(scope.row.createdAt) }}
                </template>
              </el-table-column>
              <el-table-column label="操作" width="120" fixed="right">
                <template #default="scope">
                  <el-button size="small" type="danger" @click="handleDeleteComment(scope.row.id)">删除</el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </div>
        
        <!-- 分类管理 -->
        <div v-if="activeTab === 'category'" class="tab-content">
          <div class="content-header">
            <h4>分类管理</h4>
            <div class="header-actions">
              <el-button type="primary" @click="showAddCategoryDialog = true">添加分类</el-button>
            </div>
          </div>
          
          <div class="category-list">
            <el-table :data="categories" style="width: 100%">
              <el-table-column prop="id" label="ID" width="80" />
              <el-table-column prop="name" label="分类名称" />
              <el-table-column prop="code" label="分类代码" />
              <el-table-column prop="description" label="描述" />
              <el-table-column prop="sortOrder" label="排序" />
              <el-table-column prop="status" label="状态">
                <template #default="scope">
                  <el-switch
                    v-model="scope.row.status"
                    active-value="active"
                    inactive-value="disabled"
                    @change="handleCategoryStatusChange(scope.row)"
                  />
                </template>
              </el-table-column>
              <el-table-column label="操作" width="150" fixed="right">
                <template #default="scope">
                  <el-button size="small" @click="editCategory(scope.row)">编辑</el-button>
                  <el-button size="small" type="danger" @click="deleteCategory(scope.row.id)">删除</el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </div>
        
        <!-- 系统设置 -->
        <div v-if="activeTab === 'system'" class="tab-content">
          <el-form :model="systemSettings" label-width="120px">
            <el-form-item label="网站标题">
              <el-input v-model="systemSettings.siteTitle" />
            </el-form-item>
            
            <el-form-item label="网站描述">
              <el-input v-model="systemSettings.siteDescription" type="textarea" :rows="3" />
            </el-form-item>
            
            <el-form-item label="网站关键词">
              <el-input v-model="systemSettings.siteKeywords" />
            </el-form-item>
            
            <el-form-item label="首页轮播图">
              <el-upload
                action="#"
                :auto-upload="false"
                list-type="picture-card"
                :file-list="systemSettings.banners"
                @change="handleBannerChange"
              >
                <i class="el-icon-plus"></i>
              </el-upload>
            </el-form-item>
            
            <el-form-item label="用户注册">
              <el-switch v-model="systemSettings.allowRegister" />
            </el-form-item>
            
            <el-form-item label="作品审核">
              <el-switch v-model="systemSettings.requireWorkApproval" />
            </el-form-item>
            
            <el-form-item label="评论审核">
                <el-switch v-model="systemSettings.requireCommentApproval" />
              </el-form-item>
              
              <el-form-item label="是否开放评论">
                <el-switch v-model="systemSettings.allowComments" />
              </el-form-item>
              
              <el-form-item label="默认字体大小">
                <el-slider v-model="systemSettings.defaultFontSize" :min="12" :max="24" />
              </el-form-item>
              
              <el-form-item>
                <el-button type="primary" @click="handleSaveSettings">保存设置</el-button>
                <el-button @click="resetSystemSettings">重置</el-button>
              </el-form-item>
          </el-form>
        </div>
        
        <!-- 数据分析 -->
        <div v-if="activeTab === 'analytics'" class="tab-content">
          <div class="analytics-cards">
            <div class="stat-card">
              <div class="stat-value">{{ analyticsStats.totalUsers }}</div>
              <div class="stat-label">总用户数</div>
              <div class="stat-change" :class="analyticsStats.userChange > 0 ? 'positive' : 'negative'">
                {{ analyticsStats.userChange > 0 ? '+' : '' }}{{ analyticsStats.userChange }}% 较上月
              </div>
            </div>
            
            <div class="stat-card">
              <div class="stat-value">{{ analyticsStats.totalWorks }}</div>
              <div class="stat-label">总作品数</div>
              <div class="stat-change" :class="analyticsStats.workChange > 0 ? 'positive' : 'negative'">
                {{ analyticsStats.workChange > 0 ? '+' : '' }}{{ analyticsStats.workChange }}% 较上月
              </div>
            </div>
            
            <div class="stat-card">
              <div class="stat-value">{{ analyticsStats.totalComments }}</div>
              <div class="stat-label">总评论数</div>
              <div class="stat-change" :class="analyticsStats.commentChange > 0 ? 'positive' : 'negative'">
                {{ analyticsStats.commentChange > 0 ? '+' : '' }}{{ analyticsStats.commentChange }}% 较上月
              </div>
            </div>
            
            <div class="stat-card">
              <div class="stat-value">{{ analyticsStats.totalViews }}</div>
              <div class="stat-label">总浏览量</div>
              <div class="stat-change" :class="analyticsStats.viewChange > 0 ? 'positive' : 'negative'">
                {{ analyticsStats.viewChange > 0 ? '+' : '' }}{{ analyticsStats.viewChange }}% 较上月
              </div>
            </div>
          </div>
          
          <div class="charts-container">
            <div class="chart-box">
              <h4>用户增长趋势</h4>
              <div class="chart-placeholder">
                <!-- 图表占位，实际项目中可使用ECharts等图表库 -->
                <p>用户增长趋势图表</p>
              </div>
            </div>
            
            <div class="chart-box">
              <h4>作品分类分布</h4>
              <div class="chart-placeholder">
                <!-- 图表占位，实际项目中可使用ECharts等图表库 -->
                <p>作品分类分布图表</p>
              </div>
            </div>
          </div>
          
          <div class="recent-activities">
            <h4>最近活动</h4>
            <el-table :data="recentActivities" style="width: 100%">
              <el-table-column prop="time" label="时间" width="150" />
              <el-table-column prop="user" label="用户" width="120" />
              <el-table-column prop="action" label="操作" />
              <el-table-column prop="target" label="目标" />
            </el-table>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 添加分类对话框 -->
    <el-dialog
      v-model="showAddCategoryDialog"
      title="添加分类"
      width="500px"
      :before-close="closeAddCategoryDialog"
    >
      <el-form :model="newCategory" label-width="80px">
        <el-form-item label="分类名称" prop="name">
          <el-input v-model="newCategory.name" />
        </el-form-item>
        
        <el-form-item label="分类代码" prop="code">
          <el-input v-model="newCategory.code" />
        </el-form-item>
        
        <el-form-item label="分类描述">
          <el-input v-model="newCategory.description" type="textarea" :rows="2" />
        </el-form-item>
        
        <el-form-item label="排序号">
          <el-input v-model.number="newCategory.sortOrder" type="number" />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="closeAddCategoryDialog">取消</el-button>
        <el-button type="primary" @click="addCategory">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { useUserStore } from '@/stores/userStore';
import { ElMessage, ElMessageBox } from 'element-plus';
import api, { fileReviewsAPI } from '../utils/api.js';
import { formatDate } from '../utils';

const router = useRouter();

// 引入状态管理
const userStore = useUserStore();

// 计算属性：检查用户是否为管理员
const isAdmin = computed(() => {
  return userStore.isAdmin;
});

// 计算属性：检查用户是否登录
const isLoggedIn = computed(() => {
  return userStore.isLoggedIn;
});

// 当前激活的标签
const activeTab = ref('users');

// 格式化日期时间
const formattedDateTime = computed(() => {
  const date = new Date();
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  });
});

// 设置激活标签
const setActiveTab = (tab) => {
  activeTab.value = tab;
};

// 获取标签标题
const getTabTitle = (tab) => {
  const titles = {
    users: '用户管理',
    works: '作品管理',
    comments: '评论管理',
    category: '分类管理',
    system: '系统设置',
    analytics: '数据分析'
  };
  return titles[tab] || '管理中心';
};

// 权限检查
onMounted(() => {
  // 检查用户是否登录且为管理员
  if (!isLoggedIn.value || !isAdmin.value) {
    ElMessage.error('您没有管理员权限');
    router.push('/');
    return;
  }
});

// 退出登录（普通用户）- 已废弃，使用后端管理的logout函数
// const userLogout = () => {
//   ElMessage.success('已退出登录');
//   router.push('/login');
// };

// 用户管理相关数据
const userSearchQuery = ref('');
const userStatusFilter = ref('all');
const userCurrentPage = ref(1);
const userPageSize = ref(10);

// 模拟用户数据
const users = ref([
  { id: 1, username: 'user1', email: 'user1@example.com', role: 'admin', status: 'active', createdAt: '2024-01-01', lastLogin: '2024-01-15 10:30' },
  { id: 2, username: 'user2', email: 'user2@example.com', role: 'user', status: 'active', createdAt: '2024-01-02', lastLogin: '2024-01-14 14:20' },
  { id: 3, username: 'user3', email: 'user3@example.com', role: 'user', status: 'disabled', createdAt: '2024-01-03', lastLogin: '2024-01-10 09:15' },
  { id: 4, username: 'user4', email: 'user4@example.com', role: 'user', status: 'active', createdAt: '2024-01-04', lastLogin: '2024-01-15 16:45' },
  { id: 5, username: 'user5', email: 'user5@example.com', role: 'user', status: 'active', createdAt: '2024-01-05', lastLogin: '2024-01-13 11:20' }
]);

// 过滤后的用户列表
const filteredUsers = computed(() => {
  let result = [...users.value];
  
  // 按搜索词过滤
  if (userSearchQuery.value) {
    const query = userSearchQuery.value.toLowerCase();
    result = result.filter(user => 
      user.username.toLowerCase().includes(query) ||
      user.email.toLowerCase().includes(query)
    );
  }
  
  // 按状态过滤
  if (userStatusFilter.value !== 'all') {
    result = result.filter(user => user.status === userStatusFilter.value);
  }
  
  return result;
});

// 获取角色对应的标签类型
const getRoleType = (role) => {
  switch (role) {
    case 'admin': return 'danger';
    case 'user': return 'success';
    default: return 'info';
  }
};

// 处理用户状态变更
const handleUserStatusChange = (user) => {
  ElMessage.success(`用户 ${user.username} 的状态已更新为 ${user.status === 'active' ? '正常' : '禁用'}`);
};

// 编辑用户
const editUser = (user) => {
  ElMessage.info(`编辑用户 ${user.username}`);
};

// 删除用户 - 已废弃，使用后端管理的delete函数
// const deleteUser = (userId) => {
//   ElMessageBox.confirm('确定要删除这个用户吗？', '警告', {
//     confirmButtonText: '确定',
//     cancelButtonText: '取消',
//     type: 'warning'
//   }).then(() => {
//     users.value = users.value.filter(user => user.id !== userId);
//     ElMessage.success('用户删除成功');
//   }).catch(() => {
//     // 取消删除
//   });
// };

// 处理作品管理操作
const handleDeleteWork = (workId) => {
  ElMessageBox.confirm('确定要删除这个作品吗？', '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    works.value = works.value.filter(work => work.id !== workId);
    ElMessage.success('作品删除成功');
  }).catch(() => {
    // 取消删除
  });
};

// 处理评论管理操作
const handleDeleteComment = (commentId) => {
  ElMessageBox.confirm('确定要删除这条评论吗？', '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    comments.value = comments.value.filter(comment => comment.id !== commentId);
    ElMessage.success('评论删除成功');
  }).catch(() => {
    // 取消删除
  });
};

// 处理设置保存
const handleSaveSettings = () => {
  ElMessage.success('设置保存成功');
};

// 作品管理相关数据
const workSearchQuery = ref('');
const workCategoryFilter = ref('all');
const workStatusFilter = ref('all');
const workCurrentPage = ref(1);
const workPageSize = ref(10);

// 模拟作品数据
const works = ref([
  { id: 1, title: '宁静致远', authorName: 'user1', category: '楷书', status: 'approved', imageUrl: 'https://picsum.photos/seed/calligraphy1/100/100', createdAt: '2024-01-15', views: 1240 },
  { id: 2, title: '厚德载物', authorName: 'user2', category: '行书', status: 'pending', imageUrl: 'https://picsum.photos/seed/calligraphy2/100/100', createdAt: '2024-01-14', views: 956 },
  { id: 3, title: '紫气东来', authorName: 'user3', category: '草书', status: 'rejected', imageUrl: 'https://picsum.photos/seed/calligraphy3/100/100', createdAt: '2024-01-13', views: 1567 },
  { id: 4, title: '天道酬勤', authorName: 'user4', category: '隶书', status: 'approved', imageUrl: 'https://picsum.photos/seed/calligraphy4/100/100', createdAt: '2024-01-12', views: 1023 },
  { id: 5, title: '上善若水', authorName: 'user5', category: '篆书', status: 'approved', imageUrl: 'https://picsum.photos/seed/calligraphy5/100/100', createdAt: '2024-01-11', views: 876 }
]);

// 过滤后的作品列表
const filteredWorks = computed(() => {
  let result = [...works.value];
  
  // 按搜索词过滤
  if (workSearchQuery.value) {
    const query = workSearchQuery.value.toLowerCase();
    result = result.filter(work => 
      work.title.toLowerCase().includes(query) ||
      work.authorName.toLowerCase().includes(query)
    );
  }
  
  // 按分类过滤
  if (workCategoryFilter.value !== 'all') {
    result = result.filter(work => work.category === workCategoryFilter.value);
  }
  
  // 按状态过滤
  if (workStatusFilter.value !== 'all') {
    result = result.filter(work => work.status === workStatusFilter.value);
  }
  
  return result;
});

// 获取状态对应的标签类型
const getStatusType = (status) => {
  switch (status) {
    case 'pending': return 'warning';
    case 'approved': return 'success';
    case 'rejected': return 'danger';
    default: return 'info';
  }
};

// 获取状态文本
const getStatusText = (status) => {
  switch (status) {
    case 'pending': return '待审核';
    case 'approved': return '已通过';
    case 'rejected': return '已拒绝';
    default: return '未知';
  }
};

// 查看作品
const viewWork = (work) => {
  ElMessage.info(`查看作品 ${work.title}`);
};

// 审核通过作品
const approveWork = async (workId) => {
  try {
    await ElMessageBox.confirm('确定要通过此作品吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'success'
    });
    await fileReviewsAPI.createReview({ workId, reviewStatus: 'APPROVED', reviewComments: '' });
    const work = works.value.find(w => w.id === workId);
    if (work) {
      work.status = 'approved';
    }
    ElMessage.success('作品审核已通过');
  } catch (e) {
    ElMessage.info('已取消操作');
  }
};

// 拒绝作品
const rejectWork = async (workId) => {
  try {
    await ElMessageBox.confirm('确定要拒绝此作品吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    });
    await fileReviewsAPI.createReview({ workId, reviewStatus: 'REJECTED', reviewComments: '不符合要求' });
    const work = works.value.find(w => w.id === workId);
    if (work) {
      work.status = 'rejected';
    }
    ElMessage.success('作品已拒绝');
  } catch (e) {
    ElMessage.info('已取消操作');
  }
};

// 删除作品 - 已废弃，使用后端管理的delete函数
// const deleteWork = (workId) => {
//   ElMessage.confirm('确定要删除此作品吗？', '提示', {
//     confirmButtonText: '确定',
//     cancelButtonText: '取消',
//     type: 'warning'
//   }).then(() => {
//     works.value = works.value.filter(work => work.id !== workId);
//     ElMessage.success('作品已删除');
//   }).catch(() => {
//     ElMessage.info('已取消删除');
//   });
// };

// 评论管理相关数据
const commentSearchQuery = ref('');
const commentStatusFilter = ref('all');

// 模拟评论数据
const comments = ref([
  { id: 1, content: '这是一个很好的作品，学习了！', username: 'user1', workTitle: '宁静致远', status: 'active', createdAt: '2024-01-15 10:30' },
  { id: 2, content: '书法功底深厚，值得收藏。', username: 'user2', workTitle: '厚德载物', status: 'active', createdAt: '2024-01-15 09:20' },
  { id: 3, content: '非常喜欢这种风格。', username: 'user3', workTitle: '紫气东来', status: 'hidden', createdAt: '2024-01-14 16:45' },
  { id: 4, content: '期待更多佳作！', username: 'user4', workTitle: '天道酬勤', status: 'active', createdAt: '2024-01-14 14:20' },
  { id: 5, content: '写得真不错，向你学习。', username: 'user5', workTitle: '上善若水', status: 'active', createdAt: '2024-01-13 11:10' }
]);

// 过滤后的评论列表
const filteredComments = computed(() => {
  let result = [...comments.value];
  
  // 按搜索词过滤
  if (commentSearchQuery.value) {
    const query = commentSearchQuery.value.toLowerCase();
    result = result.filter(comment => 
      comment.content.toLowerCase().includes(query)
    );
  }
  
  // 按状态过滤
  if (commentStatusFilter.value !== 'all') {
    result = result.filter(comment => comment.status === commentStatusFilter.value);
  }
  
  return result;
});

// 处理评论状态变更
const handleCommentStatusChange = (comment) => {
  ElMessage.success(`评论状态已更新为 ${comment.status === 'active' ? '显示' : '隐藏'}`);
};

// 删除评论 - 已废弃，使用后端管理的delete函数
// const deleteComment = (commentId) => {
//   ElMessage.confirm('确定要删除此评论吗？', '提示', {
//     confirmButtonText: '确定',
//     cancelButtonText: '取消',
//     type: 'warning'
//   }).then(() => {
//     comments.value = comments.value.filter(comment => comment.id !== commentId);
//     ElMessage.success('评论已删除');
//   }).catch(() => {
//     ElMessage.info('已取消删除');
//   });
// };

// 分类管理相关数据
const categories = ref([
  { id: 1, name: '楷书', code: 'kai', description: '楷书，又称正书、真书，是一种字体端正、结构严谨的书体。', sortOrder: 1, status: 'active' },
  { id: 2, name: '行书', code: 'xing', description: '行书，是介于楷书与草书之间的一种书体，笔势流畅，易读易写。', sortOrder: 2, status: 'active' },
  { id: 3, name: '草书', code: 'cao', description: '草书，是一种笔画简略、流畅飞动的书体。', sortOrder: 3, status: 'active' },
  { id: 4, name: '隶书', code: 'li', description: '隶书，是古代书体之一，笔画有波磔，结构扁平。', sortOrder: 4, status: 'active' },
  { id: 5, name: '篆书', code: 'zhuan', description: '篆书，是古代文字，包括大篆和小篆，字形圆转。', sortOrder: 5, status: 'active' }
]);

const showAddCategoryDialog = ref(false);
const newCategory = reactive({
  name: '',
  code: '',
  description: '',
  sortOrder: 0
});

// 处理分类状态变更
const handleCategoryStatusChange = (category) => {
  ElMessage.success(`分类 ${category.name} 的状态已更新为 ${category.status === 'active' ? '启用' : '禁用'}`);
};

// 编辑分类
const editCategory = (category) => {
  ElMessage.info(`编辑分类 ${category.name}`);
};

// 删除分类
const deleteCategory = (categoryId) => {
  ElMessage.confirm('确定要删除此分类吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    categories.value = categories.value.filter(category => category.id !== categoryId);
    ElMessage.success('分类已删除');
  }).catch(() => {
    ElMessage.info('已取消删除');
  });
};

// 关闭添加分类对话框
const closeAddCategoryDialog = () => {
  showAddCategoryDialog.value = false;
  // 重置表单
  newCategory.name = '';
  newCategory.code = '';
  newCategory.description = '';
  newCategory.sortOrder = 0;
};

// 添加分类
const addCategory = () => {
  const newId = Math.max(...categories.value.map(c => c.id)) + 1;
  categories.value.push({
    id: newId,
    ...newCategory,
    status: 'active'
  });
  ElMessage.success('分类添加成功');
  closeAddCategoryDialog();
};

// 系统设置相关数据
const systemSettings = reactive({
  siteTitle: '在线书法练习平台',
  siteDescription: '提供书法学习、练习、展示和交流的综合性服务',
  siteKeywords: '书法,练习,字帖,交流',
  banners: [],
  allowRegister: true,
  requireWorkApproval: true,
  requireCommentApproval: false
});

// 保存系统设置 - 已废弃，使用后端API
// const saveSystemSettings = () => {
//   ElMessage.success('系统设置已保存');
// };

// 重置系统设置
const resetSystemSettings = () => {
  ElMessage.info('已重置设置');
};

// 处理轮播图上传变更
const handleBannerChange = () => {
  ElMessage.info('轮播图变更处理中');
};

// 数据分析相关数据
const analyticsStats = ref({
  totalUsers: 156,
  totalWorks: 423,
  totalComments: 1289,
  totalViews: 15689,
  userChange: 12.5,
  workChange: 8.3,
  commentChange: 15.2,
  viewChange: 23.7
});

// 最近活动数据
const recentActivities = ref([
  { time: '10:30', user: 'user1', action: '上传了作品', target: '宁静致远' },
  { time: '09:20', user: 'user2', action: '评论了作品', target: '厚德载物' },
  { time: '昨天', user: 'user3', action: '点赞了作品', target: '紫气东来' },
  { time: '昨天', user: 'admin', action: '审核通过作品', target: '天道酬勤' },
  { time: '前天', user: 'user4', action: '注册了账号', target: '' }
]);

// 工具函数
const truncateText = (text, maxLength) => {
  if (!text) return '';
  return text.length > maxLength ? text.substring(0, maxLength) + '...' : text;
};

// 获取用户信息
const getUsername = computed(() => userStore.getUsername || '管理员');
const getUserAvatar = computed(() => userStore.getUserAvatar);

// 退出登录
const logout = async () => {
  try {
    await userStore.logout();
    ElMessage.success('退出登录成功');
    router.push('/admin/login');
  } catch (error) {
    console.error('退出登录失败:', error);
    ElMessage.error('退出登录失败');
  }
};

// 组件挂载时初始化数据
onMounted(() => {
  console.log('管理员页面初始化');
  
  // 检查是否是管理员用户
  if (!userStore.isAdmin) {
    ElMessage.error('您没有管理员权限');
    router.push('/');
    return;
  }
  
  // 检查是否是后端用户
  if (!userStore.isBackendUser) {
    ElMessage.warning('请使用管理员账号登录');
    router.push('/admin/login');
    return;
  }
});
</script>

<style scoped>
.admin-container {
  display: flex;
  min-height: 100vh;
  background-color: #f5f7fa;
}

/* 侧边栏样式 */
.admin-sidebar {
  width: 240px;
  background-color: #304156;
  color: white;
  display: flex;
  flex-direction: column;
}

.sidebar-header {
  padding: 1.5rem;
  border-bottom: 1px solid #475669;
}

.sidebar-header h2 {
  margin: 0;
  font-size: 1.4rem;
  font-weight: 500;
}

.sidebar-nav {
  flex: 1;
  padding: 1rem 0;
}

.nav-item {
  display: flex;
  align-items: center;
  padding: 1rem 1.5rem;
  cursor: pointer;
  transition: all 0.3s;
  gap: 1rem;
}

.nav-item:hover {
  background-color: #475669;
}

.nav-item.active {
  background-color: #1E9FFF;
}

.sidebar-footer {
  padding: 1.5rem;
  border-top: 1px solid #475669;
}

.admin-info {
  display: flex;
  align-items: center;
  margin-bottom: 10px;
  padding: 10px;
  background: #f5f7fa;
  border-radius: 8px;
}

.admin-avatar-small {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  margin-right: 10px;
  border: 2px solid #409eff;
}

/* 主内容区样式 */
.admin-main {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.main-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1rem 2rem;
  background-color: white;
  border-bottom: 1px solid #e6e6e6;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
}

.header-title h3 {
  margin: 0;
  font-size: 1.3rem;
  color: #333;
}

.header-info {
  display: flex;
  align-items: center;
  gap: 2rem;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 0.8rem;
}

.admin-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  object-fit: cover;
}

.current-time {
  color: #666;
  font-size: 0.9rem;
}

/* 内容区域样式 */
.content-area {
  flex: 1;
  padding: 2rem;
  overflow-y: auto;
}

.tab-content {
  background-color: white;
  border-radius: 8px;
  padding: 1.5rem;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.content-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1.5rem;
  padding-bottom: 1rem;
  border-bottom: 1px solid #e6e6e6;
}

.content-header h4 {
  margin: 0;
  font-size: 1.2rem;
  color: #333;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.data-table {
  margin-bottom: 1.5rem;
}

.pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 1rem;
}

/* 作品缩略图 */
.work-thumbnail-small {
  width: 80px;
  height: 80px;
  object-fit: cover;
  border-radius: 4px;
}

/* 评论内容 */
.comment-content {
  max-width: 400px;
  word-break: break-all;
  color: #666;
}

/* 分类管理 */
.category-list {
  margin-top: 1rem;
}

/* 数据分析样式 */
.analytics-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 1.5rem;
  margin-bottom: 2rem;
}

.stat-card {
  background-color: white;
  border-radius: 8px;
  padding: 1.5rem;
  text-align: center;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  transition: transform 0.3s;
}

.stat-card:hover {
  transform: translateY(-5px);
}

.stat-value {
  font-size: 2rem;
  font-weight: bold;
  color: #1E9FFF;
  margin-bottom: 0.5rem;
}

.stat-label {
  color: #666;
  margin-bottom: 0.5rem;
}

.stat-change {
  font-size: 0.9rem;
}

.stat-change.positive {
  color: #67C23A;
}

.stat-change.negative {
  color: #F56C6C;
}

.charts-container {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(400px, 1fr));
  gap: 2rem;
  margin-bottom: 2rem;
}

.chart-box {
  background-color: white;
  border-radius: 8px;
  padding: 1.5rem;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.chart-box h4 {
  margin: 0 0 1rem 0;
  font-size: 1.1rem;
  color: #333;
}

.chart-placeholder {
  height: 300px;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #f5f7fa;
  border-radius: 4px;
  color: #999;
}

.recent-activities {
  background-color: white;
  border-radius: 8px;
  padding: 1.5rem;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.recent-activities h4 {
  margin: 0 0 1rem 0;
  font-size: 1.1rem;
  color: #333;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .admin-container {
    flex-direction: column;
  }
  
  .admin-sidebar {
    width: 100%;
    height: auto;
  }
  
  .sidebar-nav {
    display: flex;
    flex-wrap: wrap;
  }
  
  .nav-item {
    flex: 1;
    min-width: 120px;
    justify-content: center;
  }
  
  .main-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 1rem;
  }
  
  .header-info {
    width: 100%;
    justify-content: space-between;
  }
  
  .content-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 1rem;
  }
  
  .header-actions {
    width: 100%;
    flex-wrap: wrap;
  }
  
  .analytics-cards {
    grid-template-columns: 1fr;
  }
  
  .charts-container {
    grid-template-columns: 1fr;
  }
  
  .chart-box {
    padding: 1rem;
  }
  
  .chart-placeholder {
    height: 250px;
  }
}
</style>