<template>
  <div class="profile-container">
    <div class="profile-header">
      <h2 class="profile-title">个人资料</h2>
      <p class="profile-subtitle">管理您的账号信息</p>
    </div>

    <div class="profile-content">
      <!-- 左侧菜单 -->
      <div class="profile-sidebar">
        <div
          class="sidebar-item active"
          role="button"
          tabindex="0"
          @click="activeTab = 'basic'"
          @keydown.enter="activeTab = 'basic'"
          @keydown.space.prevent="activeTab = 'basic'"
        >基本信息</div>
        <div
          class="sidebar-item"
          role="button"
          tabindex="0"
          @click="activeTab = 'security'"
          @keydown.enter="activeTab = 'security'"
          @keydown.space.prevent="activeTab = 'security'"
        >账号安全</div>
        <div
          class="sidebar-item"
          role="button"
          tabindex="0"
          @click="activeTab = 'preferences'"
          @keydown.enter="activeTab = 'preferences'"
          @keydown.space.prevent="activeTab = 'preferences'"
        >偏好设置</div>
      </div>

      <!-- 右侧内容区 -->
      <div class="profile-main" v-loading="profileLoading">
        <!-- 错误状态 -->
        <el-alert
          v-if="profileError"
          class="state-alert"
          type="error"
          :title="profileError"
          show-icon
          :closable="false"
        >
          <template #default>
            <el-button type="primary" size="small" @click="loadProfile">重新加载</el-button>
          </template>
        </el-alert>

        <!-- 空状态 -->
        <el-empty
          v-else-if="!profileLoading && !hasProfile"
          description="还没有加载到个人资料"
        >
          <el-button type="primary" @click="loadProfile">重新加载</el-button>
          <el-button @click="router.push('/login')">去登录</el-button>
        </el-empty>

        <template v-else>
          <!-- 基本信息 -->
          <div v-if="activeTab === 'basic'" class="profile-section">
            <div class="profile-stats">
              <div class="stat-box">
                <div class="stat-number">{{ userInfo.workCount }}</div>
                <div class="stat-label">作品</div>
              </div>
              <div class="stat-box">
                <div class="stat-number">{{ userInfo.practiceCount }}</div>
                <div class="stat-label">练习</div>
              </div>
              <div class="stat-box">
                <div class="stat-number">{{ userInfo.commentCount }}</div>
                <div class="stat-label">评论</div>
              </div>
            </div>

            <el-form :model="userInfo" :rules="basicRules" ref="basicFormRef" label-width="120px">
              <el-form-item label="用户名">
                <el-input v-model="userInfo.username" disabled />
              </el-form-item>

              <el-form-item label="邮箱">
                <el-input v-model="userInfo.email" disabled />
              </el-form-item>

              <el-form-item label="昵称" prop="nickname">
                <el-input v-model="userInfo.nickname" placeholder="请输入昵称" maxlength="50" show-word-limit />
              </el-form-item>

              <el-form-item label="手机号" prop="phone">
                <el-input v-model="userInfo.phone" placeholder="请输入手机号" maxlength="20" />
              </el-form-item>

              <el-form-item label="头像">
                <el-upload
                  class="avatar-uploader"
                  :show-file-list="false"
                  :before-upload="beforeAvatarUpload"
                  :http-request="uploadAvatarRequest"
                  accept="image/jpeg,image/png"
                >
                  <img v-if="userInfo.avatar" :src="userInfo.avatar" class="avatar" alt="用户头像" />
                  <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
                </el-upload>
                <el-button type="primary" size="small" :loading="avatarLoading" @click="triggerAvatarPicker">
                  上传头像
                </el-button>
              </el-form-item>

              <el-form-item label="个人简介" prop="bio">
                <el-input
                  v-model="userInfo.bio"
                  type="textarea"
                  :rows="3"
                  maxlength="1000"
                  show-word-limit
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
                <el-input v-model="securityForm.oldPassword" type="password" placeholder="请输入旧密码" show-password />
              </el-form-item>

              <el-form-item label="新密码" prop="newPassword">
                <el-input v-model="securityForm.newPassword" type="password" placeholder="请输入新密码" show-password />
              </el-form-item>

              <el-form-item label="确认新密码" prop="confirmPassword">
                <el-input v-model="securityForm.confirmPassword" type="password" placeholder="请确认新密码" show-password />
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
                <el-button type="primary" :loading="preferencesSaving" @click="handleSavePreferences">
                  保存设置
                </el-button>
                <span class="preferences-hint">
                  偏好存在账号里，换设备登录也会同步。<template v-if="preferencesSource === 'local'">当前显示的是本机兜底值，保存后会写进账号。</template>
                </span>
              </el-form-item>
            </el-form>
          </div>
        </template>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Plus } from '@element-plus/icons-vue';
import { useUserStore } from '../stores/userStore';
import { userAPI } from '../utils/api';

const router = useRouter();
const userStore = useUserStore();

const activeTab = ref('basic');
const basicLoading = ref(false);
const securityLoading = ref(false);
const avatarLoading = ref(false);
const profileLoading = ref(false);
const profileError = ref('');
const preferencesSaving = ref(false);

// 表单引用
const basicFormRef = ref(null);
const securityFormRef = ref(null);
const preferencesFormRef = ref(null);

// 用户基本信息（来自 GET /api/users/{userId} 的裸用户对象）
const userInfo = reactive({
  id: '',
  username: '',
  email: '',
  nickname: '',
  avatar: '',
  phone: '',
  bio: '',
  status: '',
  // 账号级偏好（JSON 字符串），存在 users.preferences 列里
  preferences: '',
  workCount: 0,
  practiceCount: 0,
  commentCount: 0
});

const hasProfile = computed(() => Boolean(userInfo.id || userInfo.username));

// 密码修改表单
const securityForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
});

// 偏好设置：账号是真相源，本地只留一份兜底默认值
const preferences = reactive({
  defaultCopybookStyle: 'regular',
  enableReminder: false,
  notifications: []
});

// 偏好当前来自哪里：account（账号）/ local（本地兜底）/ default（默认值）
const preferencesSource = ref('default');

// 偏好的默认值
const defaultPreferences = () => ({
  defaultCopybookStyle: 'regular',
  enableReminder: false,
  notifications: []
});

// 验证规则
const basicRules = {
  nickname: [
    { required: true, message: '请输入昵称', trigger: 'blur' },
    { max: 50, message: '昵称长度不能超过50个字符', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入有效的邮箱地址', trigger: 'blur' }
  ],
  phone: [
    { max: 20, message: '手机号长度不能超过20个字符', trigger: 'blur' }
  ],
  bio: [
    { max: 1000, message: '个人简介长度不能超过1000个字符', trigger: 'blur' }
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
  loadProfile();
  loadPreferences();
});

// 解析当前用户 id：先看 store，没有就拉一次资料
const resolveUserId = async () => {
  if (userStore.userInfo?.id) {
    return userStore.userInfo.id;
  }
  const ok = await userStore.fetchUserProfile();
  return ok ? userStore.userInfo?.id : null;
};

// 加载用户资料：GET /api/users/{userId} —— 裸用户对象
const loadProfile = async () => {
  profileLoading.value = true;
  profileError.value = '';
  try {
    const userId = await resolveUserId();
    if (!userId) {
      throw new Error('未获取到当前用户信息，请重新登录');
    }
    const profile = await userAPI.getUserInfo(userId);
    applyProfile(profile);
  } catch (error) {
    profileError.value = error?.message || '个人资料加载失败，请稍后重试';
  } finally {
    profileLoading.value = false;
  }
};

// 把接口返回的用户对象填进表单
const applyProfile = (profile) => {
  if (!profile) {
    return;
  }
  userInfo.id = profile.id || '';
  userInfo.username = profile.username || '';
  userInfo.email = profile.email || '';
  userInfo.nickname = profile.nickname || '';
  userInfo.avatar = profile.avatar || '';
  userInfo.phone = profile.phone || '';
  userInfo.bio = profile.bio || '';
  userInfo.status = profile.status || '';
  userInfo.preferences = profile.preferences || '';
  userInfo.workCount = profile.workCount || 0;
  userInfo.practiceCount = profile.practiceCount || 0;
  userInfo.commentCount = profile.commentCount || 0;
  // 账号里的偏好比本地那份新 —— 资料到手就以账号为准
  loadPreferences();
};

// 把一份偏好（JSON 字符串或对象）写进表单，返回是否解析成功
const applyPreferences = (raw) => {
  let parsed = null;
  if (raw && typeof raw === 'object') {
    parsed = raw;
  } else if (typeof raw === 'string' && raw.trim()) {
    try {
      parsed = JSON.parse(raw);
    } catch (error) {
      console.error('解析账号偏好失败:', error);
      parsed = null;
    }
  }
  if (!parsed || typeof parsed !== 'object') {
    return false;
  }
  // 先回到默认值，避免上一份偏好里多出来的键残留
  Object.assign(preferences, defaultPreferences(), parsed);
  return true;
};

// 加载用户偏好设置
// 账号里的偏好优先（换设备也能用），账号里没有才用本地兜底
const loadPreferences = () => {
  // 先用刚拉到的账号资料，没有再退回 store 里的账号信息
  if (applyPreferences(userInfo.preferences) || applyPreferences(userStore.preferences)) {
    preferencesSource.value = 'account';
    return;
  }

  const saved = localStorage.getItem('userPreferences');
  if (saved) {
    try {
      Object.assign(preferences, defaultPreferences(), JSON.parse(saved));
      preferencesSource.value = 'local';
    } catch (error) {
      console.error('读取本地偏好设置失败:', error);
      localStorage.removeItem('userPreferences');
    }
  }
};

// 保存基本信息：PUT /api/users/profile
const handleSaveBasic = async () => {
  try {
    await basicFormRef.value.validate();
  } catch {
    return;
  }

  basicLoading.value = true;
  try {
    const success = await userStore.updateProfile({
      nickname: userInfo.nickname,
      email: userInfo.email,
      phone: userInfo.phone,
      bio: userInfo.bio,
      avatar: userInfo.avatar
    });

    if (!success) {
      ElMessage.error(userStore.error || '保存失败，请稍后重试');
      return;
    }

    ElMessage.success('保存成功');
    // 以服务端返回的资料为准刷新表单
    await loadProfile();
  } catch (error) {
    ElMessage.error(error?.message || '保存失败，请稍后重试');
  } finally {
    basicLoading.value = false;
  }
};

// 修改密码：PUT /api/users/password
// 后端成功返回 204（无响应体），统一请求层解析空响应体会报错，
// 所以这里把这一种错误当成成功处理，其余错误按后端返回的 message 提示。
const EMPTY_BODY_ERROR = '服务器响应格式错误';

const handleChangePassword = async () => {
  try {
    await securityFormRef.value.validate();
  } catch {
    return;
  }

  try {
    await ElMessageBox.confirm(
      '确定要修改密码吗？修改后需要重新登录',
      '确认修改',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    );
  } catch {
    return;
  }

  securityLoading.value = true;
  try {
    try {
      await userAPI.changePassword({
        oldPassword: securityForm.oldPassword,
        newPassword: securityForm.newPassword,
        confirmPassword: securityForm.confirmPassword
      });
    } catch (error) {
      if (error?.message !== EMPTY_BODY_ERROR) {
        ElMessage.error(error?.message || '密码修改失败，请检查旧密码是否正确');
        return;
      }
    }

    ElMessage.success('密码修改成功，请重新登录');
    Object.keys(securityForm).forEach((key) => {
      securityForm[key] = '';
    });
    await userStore.logout();
    router.push('/login');
  } finally {
    securityLoading.value = false;
  }
};

// 保存偏好设置：PUT /api/users/profile（preferences 是 JSON 字符串）
const handleSavePreferences = async () => {
  preferencesSaving.value = true;
  try {
    const success = await userStore.updatePreferences(
      {
        defaultCopybookStyle: preferences.defaultCopybookStyle,
        enableReminder: preferences.enableReminder,
        notifications: [...preferences.notifications]
      },
      { nickname: userInfo.nickname, email: userInfo.email }
    );

    if (!success) {
      ElMessage.error(userStore.error || '偏好保存失败，请稍后重试');
      return;
    }

    // 本地留一份兜底（账号才是真相源）
    localStorage.setItem('userPreferences', JSON.stringify(preferences));
    preferencesSource.value = 'account';
    ElMessage.success('偏好已保存到账号，换设备登录也会同步');
  } finally {
    preferencesSaving.value = false;
  }
};

// 触发头像文件选择
const triggerAvatarPicker = () => {
  const uploadInput = document.querySelector('.avatar-uploader .el-upload__input');
  if (uploadInput) {
    uploadInput.click();
  }
};

// 头像上传前校验
const beforeAvatarUpload = (file) => {
  const isJPG = file.type === 'image/jpeg' || file.type === 'image/png';
  const isLt2M = file.size / 1024 / 1024 < 2;

  if (!isJPG) {
    ElMessage.error('上传头像图片只能是 JPG/PNG 格式!');
    return false;
  }
  if (!isLt2M) {
    ElMessage.error('上传头像图片大小不能超过 2MB!');
    return false;
  }
  return true;
};

// 上传头像：POST /api/users/avatar（multipart，字段名 avatar）
const uploadAvatarRequest = async ({ file, onSuccess, onError }) => {
  avatarLoading.value = true;
  try {
    const formData = new FormData();
    formData.append('avatar', file);
    const response = await userAPI.uploadAvatar(formData);
    userInfo.avatar = response?.fileUrl || userInfo.avatar;
    ElMessage.success('头像上传成功');
    onSuccess?.(response);
    // 后端已把头像写进用户资料，重新拉一次保证表单与后端一致
    await loadProfile();
  } catch (error) {
    ElMessage.error(error?.message || '头像上传失败，请稍后重试');
    onError?.(error);
  } finally {
    avatarLoading.value = false;
  }
};
</script>

<style scoped>
/* ============================================================
   个人资料页版式
   - 页面容器与全站内容列共用一条左右边界
   - 页头左对齐 + 墨线（与首页/字帖/作品/社区同一套）
   ============================================================ */
.profile-container {
  --sp-1: 4px;
  --sp-2: 8px;
  --sp-3: 12px;
  --sp-4: 16px;
  --sp-5: 24px;
  --sp-6: 32px;
  --sp-7: 48px;

  width: 100%;
  max-width: 1200px;
  margin-inline: auto;
  padding-block: var(--sp-2);
}

.profile-header {
  padding-left: var(--sp-3);
  border-left: 3px solid var(--primary-color);
  margin-bottom: var(--sp-6);
}

.profile-title {
  font-size: clamp(1.5rem, 1.2rem + 1vw, 2rem);
  color: #333;
  margin-bottom: var(--sp-1);
  font-family: 'SimSun', serif;
  letter-spacing: 0.08em;
}

.profile-subtitle {
  color: var(--el-text-color-secondary);
  font-size: 0.95rem;
}

.profile-content {
  display: flex;
  gap: var(--sp-6);
  background-color: rgba(255, 255, 255, 0.95);
  border-radius: 8px;
  padding: var(--sp-6);
  box-shadow: 0 2px 12px rgba(90, 60, 30, 0.08);
}

.profile-sidebar {
  width: 200px;
  border-right: 1px solid #e0e0e0;
}

.sidebar-item {
  padding: 1rem;
  cursor: pointer;
  border-radius: 4px;
  transition: transform 0.3s, box-shadow 0.3s, background-color 0.3s, border-color 0.3s, color 0.3s;
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
  min-height: 20rem;
}

.state-alert {
  margin-bottom: 1rem;
}

.preferences-hint {
  margin-left: 0.8rem;
  color: var(--el-text-color-secondary);
  font-size: 0.85rem;
}

.profile-section {
  padding: 1rem 0;
}

.profile-stats {
  display: flex;
  gap: 2rem;
  margin-bottom: 1.5rem;
  padding-bottom: 1.5rem;
  border-bottom: 1px solid #f0f0f0;
}

.stat-box {
  text-align: center;
}

.stat-number {
  font-size: 1.6rem;
  font-weight: bold;
  color: #8B4513;
}

.stat-label {
  color: #666;
  font-size: 0.9rem;
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
  transition: transform 0.3s, box-shadow 0.3s, background-color 0.3s, border-color 0.3s, color 0.3s;
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
  .profile-content {
    flex-direction: column;
    padding: var(--sp-4);
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
