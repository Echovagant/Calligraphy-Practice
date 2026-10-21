<template>
  <div class="profile-container">
    <div class="profile-header">
      <h2 class="profile-title">个人资料</h2>
      <p class="profile-subtitle">管理您的账号信息</p>
    </div>
    
    <div class="profile-content">
      <!-- 左侧菜单 -->
      <div class="profile-sidebar">
        <div class="sidebar-item active" @click="activeTab = 'basic'">基本信息</div>
        <div class="sidebar-item" @click="activeTab = 'security'">账号安全</div>
        <div class="sidebar-item" @click="activeTab = 'preferences'">偏好设置</div>
      </div>
      
      <!-- 右侧内容区 -->
      <div class="profile-main">
        <!-- 基本信息 -->
        <div v-if="activeTab === 'basic'" class="profile-section">
          <el-form :model="userInfo" :rules="basicRules" ref="basicFormRef" label-width="120px">
            <el-form-item label="用户名">
              <el-input v-model="userInfo.username" disabled />
            </el-form-item>
            
            <el-form-item label="邮箱">
              <el-input v-model="userInfo.email" disabled />
            </el-form-item>
            
            <el-form-item label="昵称" prop="nickname">
              <el-input v-model="userInfo.nickname" placeholder="请输入昵称" />
            </el-form-item>
            
            <el-form-item label="头像">
              <el-upload
                class="avatar-uploader"
                action=""
                :show-file-list="false"
                :on-success="handleAvatarSuccess"
                :before-upload="beforeAvatarUpload"
                :auto-upload="false"
              >
                <img v-if="userInfo.avatar" :src="userInfo.avatar" class="avatar" />
                <i v-else class="el-icon-plus avatar-uploader-icon"></i>
              </el-upload>
              <el-button type="primary" size="small" @click="uploadAvatar">上传头像</el-button>
            </el-form-item>
            
            <el-form-item label="个人简介" prop="bio">
              <el-input
                v-model="userInfo.bio"
                type="textarea"
                :rows="3"
                placeholder="请输入个人简介"
              />
            </el-form-item>
            
            <el-form-item>
              <el-button type="primary" @click="handleSaveBasic" :loading="basicLoading">保存修改</el-button>
            </el-form-item>
          </el-form>
        </div>
        
        <!-- 账号安全 -->
        <div v-if="activeTab === 'security'" class="profile-section">
          <el-form :model="securityForm" :rules="securityRules" ref="securityFormRef" label-width="120px">
            <el-form-item label="旧密码" prop="oldPassword">
              <el-input v-model="securityForm.oldPassword" type="password" placeholder="请输入旧密码" />
            </el-form-item>
            
            <el-form-item label="新密码" prop="newPassword">
              <el-input v-model="securityForm.newPassword" type="password" placeholder="请输入新密码" />
            </el-form-item>
            
            <el-form-item label="确认新密码" prop="confirmPassword">
              <el-input v-model="securityForm.confirmPassword" type="password" placeholder="请确认新密码" />
            </el-form-item>
            
            <el-form-item>
              <el-button type="primary" @click="handleChangePassword" :loading="securityLoading">修改密码</el-button>
            </el-form-item>
          </el-form>
        </div>
        
        <!-- 偏好设置 -->
        <div v-if="activeTab === 'preferences'" class="profile-section">
          <el-form :model="preferences" ref="preferencesFormRef" label-width="120px">
            <el-form-item label="默认字帖风格">
              <el-select v-model="preferences.defaultCopybookStyle" placeholder="请选择">
                <el-option label="楷书" value="regular" />
                <el-option label="行书" value="running" />
                <el-option label="草书" value="cursive" />
                <el-option label="隶书" value="official" />
                <el-option label="篆书" value="seal" />
              </el-select>
            </el-form-item>
            
            <el-form-item label="练习提醒">
              <el-switch v-model="preferences.enableReminder" />
            </el-form-item>
            
            <el-form-item label="通知设置" v-if="preferences.enableReminder">
              <el-checkbox-group v-model="preferences.notifications">
                <el-checkbox label="每日练习提醒" />
                <el-checkbox label="作品获得点赞通知" />
                <el-checkbox label="社区消息通知" />
              </el-checkbox-group>
            </el-form-item>
            
            <el-form-item>
              <el-button type="primary" @click="handleSavePreferences">保存设置</el-button>
            </el-form-item>
          </el-form>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue';
import { useUserStore } from '../stores/userStore';
import { ElMessage, ElMessageBox } from 'element-plus';

// 引入状态管理
const userStore = useUserStore();
const activeTab = ref('basic');
const basicLoading = ref(false);
const securityLoading = ref(false);

// 表单引用
const basicFormRef = ref(null);
const securityFormRef = ref(null);
const preferencesFormRef = ref(null);

// 用户基本信息
const userInfo = reactive({
  username: '',
  email: '',
  nickname: '',
  avatar: '',
  bio: ''
});

// 密码修改表单
const securityForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
});

// 偏好设置
const preferences = reactive({
  defaultCopybookStyle: 'regular',
  enableReminder: false,
  notifications: []
});

// 验证规则
const basicRules = {
  nickname: [
    { max: 20, message: '昵称长度不能超过20个字符', trigger: 'blur' }
  ],
  bio: [
    { max: 200, message: '个人简介长度不能超过200个字符', trigger: 'blur' }
  ]
};

const securityRules = {
  oldPassword: [
    { required: true, message: '请输入旧密码', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度至少为6个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== securityForm.newPassword) {
          callback(new Error('两次输入的密码不一致'));
        } else {
          callback();
        }
      },
      trigger: 'blur'
    }
  ]
};

// 生命周期钩子
onMounted(() => {
  loadUserInfo();
  loadPreferences();
});

// 加载用户信息
const loadUserInfo = () => {
  const user = userStore.userInfo;
  if (user) {
    userInfo.username = user.username || '';
    userInfo.email = user.email || '';
    userInfo.nickname = user.nickname || user.username || '';
    userInfo.avatar = user.avatar || '';
    userInfo.bio = user.bio || '';
  }
};

// 加载用户偏好设置
const loadPreferences = () => {
  // 这里可以从store或localStorage加载用户偏好设置
  const saved = localStorage.getItem('userPreferences');
  if (saved) {
    Object.assign(preferences, JSON.parse(saved));
  }
};

// 保存基本信息
const handleSaveBasic = async () => {
  try {
    await basicFormRef.value.validate();
    basicLoading.value = true;
    
    // 调用store保存用户信息
    const success = await userStore.updateProfile(userInfo);
    
    if (success) {
      ElMessage.success('保存成功');
    } else {
      ElMessage.error('保存失败，请稍后重试');
    }
  } catch (error) {
    console.error('Save basic info error:', error);
  } finally {
    basicLoading.value = false;
  }
};

// 修改密码
const handleChangePassword = async () => {
  try {
    await securityFormRef.value.validate();
    
    await ElMessageBox.confirm(
      '确定要修改密码吗？修改后需要重新登录',
      '确认修改',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    );
    
    securityLoading.value = true;
    
    // 调用store修改密码
    // 契约规定请求体是 {oldPassword, newPassword, confirmPassword} 对象；
    // 之前这里传的是两个位置参数，store 只收一个对象，
    // 结果发出去的是裸 JSON 字符串。
    const success = await userStore.changePassword({
      oldPassword: securityForm.oldPassword,
      newPassword: securityForm.newPassword,
      confirmPassword: securityForm.confirmPassword,
    });
    
    if (success) {
      ElMessage.success('密码修改成功，请重新登录');
      // 清空表单
      Object.keys(securityForm).forEach(key => {
        securityForm[key] = '';
      });
      // 退出登录
      await userStore.logout();
      window.location.href = '/login';
    } else {
      ElMessage.error('密码修改失败，请检查旧密码是否正确');
    }
  } catch (error) {
    // 如果用户取消确认，则不处理错误
    if (error !== 'cancel') {
      console.error('Change password error:', error);
    }
  } finally {
    securityLoading.value = false;
  }
};

// 保存偏好设置
const handleSavePreferences = () => {
  // 保存到localStorage
  localStorage.setItem('userPreferences', JSON.stringify(preferences));
  ElMessage.success('偏好设置已保存');
};

// 头像上传相关
const uploadAvatar = () => {
  // 触发文件选择
  const uploadInput = document.querySelector('.el-upload__input');
  if (uploadInput) {
    uploadInput.click();
  }
};

const beforeAvatarUpload = (file) => {
  const isJPG = file.type === 'image/jpeg' || file.type === 'image/png';
  const isLt2M = file.size / 1024 / 1024 < 2;

  if (!isJPG) {
    ElMessage.error('上传头像图片只能是 JPG/PNG 格式!');
  }
  if (!isLt2M) {
    ElMessage.error('上传头像图片大小不能超过 2MB!');
  }
  return isJPG && isLt2M;
};

const handleAvatarSuccess = (response, file) => {
  // 这里模拟上传成功后的处理
  // 实际项目中，应该将文件上传到服务器，然后获取返回的图片URL
  userInfo.avatar = URL.createObjectURL(file.raw);
  ElMessage.success('头像上传成功');
};
</script>

<style scoped>
.profile-container {
  padding: 2rem;
  max-width: 1200px;
  margin: 0 auto;
}

.profile-header {
  text-align: center;
  margin-bottom: 2rem;
}

.profile-title {
  font-size: 2rem;
  color: #333;
  margin-bottom: 0.5rem;
  font-family: 'SimSun', serif;
}

.profile-subtitle {
  color: #666;
  font-size: 1rem;
}

.profile-content {
  display: flex;
  gap: 2rem;
  background-color: rgba(255, 255, 255, 0.95);
  border-radius: 12px;
  padding: 2rem;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
}

.profile-sidebar {
  width: 200px;
  border-right: 1px solid #e0e0e0;
}

.sidebar-item {
  padding: 1rem;
  cursor: pointer;
  border-radius: 4px;
  transition: all 0.3s;
  margin-bottom: 0.5rem;
}

.sidebar-item:hover {
  background-color: #f5f5f5;
}

.sidebar-item.active {
  background-color: #8B4513;
  color: white;
}

.profile-main {
  flex: 1;
}

.profile-section {
  padding: 1rem 0;
}

/* 头像上传样式 */
.avatar-uploader {
  display: flex;
  align-items: center;
  gap: 1rem;
  margin-bottom: 0.5rem;
}

.avatar-uploader .el-upload {
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: all 0.3s;
}

.avatar-uploader .el-upload:hover {
  border-color: #8B4513;
}

.avatar-uploader-icon {
  font-size: 28px;
  color: #c0c4cc;
  width: 120px;
  height: 120px;
  line-height: 120px;
  text-align: center;
}

.avatar {
  width: 120px;
  height: 120px;
  display: block;
  object-fit: cover;
  border-radius: 6px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .profile-container {
    padding: 1rem;
  }
  
  .profile-content {
    flex-direction: column;
    padding: 1rem;
  }
  
  .profile-sidebar {
    width: 100%;
    border-right: none;
    border-bottom: 1px solid #e0e0e0;
    margin-bottom: 1rem;
    display: flex;
    overflow-x: auto;
  }
  
  .sidebar-item {
    white-space: nowrap;
    margin-bottom: 0;
    margin-right: 0.5rem;
  }
}
</style>