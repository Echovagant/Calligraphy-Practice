<template>
  <div class="login-container">
    <div class="login-form-wrapper">
      <div class="login-header">
        <h2 class="login-title">用户登录</h2>
        <p class="login-subtitle">欢迎回到书法练习平台</p>
      </div>

      <el-form :model="loginForm" :rules="formRules" ref="loginFormRef" label-width="80px">
        <el-form-item label="用户名" prop="username">
          <el-input
            v-model="loginForm.username"
            placeholder="请输入用户名"
            prefix-icon="el-icon-user"
            clearable
          />
        </el-form-item>

        <el-form-item label="密码" prop="password">
          <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="请输入密码"
            prefix-icon="el-icon-lock"
            show-password
          />
        </el-form-item>

        <el-form-item>
          <div class="login-options">
            <el-checkbox v-model="rememberMe">记住我</el-checkbox>
            <el-link type="primary" :underline="false">忘记密码？</el-link>
          </div>
        </el-form-item>

        <el-form-item>
          <el-button
            type="primary"
            :loading="isLoading"
            @click="handleLogin"
            class="login-button"
            :disabled="isLoading"
          >
            {{ isLoading ? '登录中...' : '登录' }}
          </el-button>
        </el-form-item>

        <el-form-item>
          <div class="register-link">
            <span>还没有账号？</span>
            <router-link to="/register" class="link-text">立即注册</router-link>
          </div>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { useUserStore } from '../stores/userStore';


// 引入状态管理
const userStore = useUserStore();
const router = useRouter();
const loginFormRef = ref(null);
const isLoading = ref(false);
const rememberMe = ref(false);

// 登录表单数据
const loginForm = reactive({
  username: '',
  password: ''
});

// 表单验证规则
const formRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度在 3 到 20 个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度至少为 6 个字符', trigger: 'blur' }
  ]
};

// 处理登录
const handleLogin = async () => {
  try {
    // 添加表单引用检查
    if (!loginFormRef.value) {
      console.error('表单引用未找到');
      ElMessage.error('表单初始化失败，请刷新页面重试');
      return;
    }

    // 表单验证
    await loginFormRef.value.validate();
    console.log('登录表单验证通过，准备发送登录请求');

    // 调用store的登录方法
    const success = await userStore.login(loginForm.username, loginForm.password, rememberMe.value);

    if (success) {
      console.log('登录成功，用户信息:', userStore.userInfo);
      ElMessage.success('登录成功！欢迎回来，' + (userStore.userInfo?.username || loginForm.username));

      // 保存用户名到sessionStorage
      sessionStorage.setItem('username', loginForm.username);

      // 如果选择记住我，保存用户名到localStorage
      if (rememberMe.value) {
        localStorage.setItem('rememberedUsername', loginForm.username);
      } else {
        localStorage.removeItem('rememberedUsername');
      }

      // 根据用户角色跳转
      console.log('用户角色:', userStore.getUserRole);
      setTimeout(() => {
        if (userStore.isAdmin) {
          console.log('跳转到管理员页面');
          router.push('/admin');
        } else {
          console.log('跳转到首页');
          router.push('/');
        }
      }, 500);
    } else {
      console.error('登录失败:', userStore.error);
      ElMessage.error(userStore.error || '登录失败，请检查用户名和密码');
    }
  } catch (error) {
    console.error('登录过程中发生错误:', error);
    if (error.message?.includes('表单验证失败')) {
      ElMessage.warning('请检查表单填写是否正确');
    } else {
      ElMessage.error('登录过程中发生错误，请稍后重试');
    }
  }
};

// 检查是否有记住的用户名
const rememberedUsername = localStorage.getItem('rememberedUsername');
if (rememberedUsername) {
  loginForm.username = rememberedUsername;
  rememberMe.value = true;
} else {
  const sessionUsername = sessionStorage.getItem('username');
  if (sessionUsername) {
    loginForm.username = sessionUsername;
  }
}
</script>

<style scoped>
.login-container {
  min-height: 80vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 2rem;
}

.login-form-wrapper {
  background-color: rgba(255, 255, 255, 0.95);
  padding: 2.5rem;
  border-radius: 12px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  width: 100%;
  max-width: 450px;
  backdrop-filter: blur(10px);
}

.login-header {
  text-align: center;
  margin-bottom: 2rem;
}

.login-title {
  font-size: 1.8rem;
  color: #333;
  margin-bottom: 0.5rem;
  font-family: 'SimSun', serif;
}

.login-subtitle {
  color: #666;
  font-size: 1rem;
}

.login-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

/* 复选框勾选后字体变红 */
.el-checkbox.is-checked .el-checkbox__label {
  color: #f56c6c !important;
}

.login-button {
  width: 100%;
  padding: 0.8rem;
  font-size: 1rem;
  background-color: #8B4513;
  border-color: #8B4513;
}

.login-button:hover,
.login-button:focus {
  background-color: #6B340E;
  border-color: #6B340E;
}

.register-link {
  text-align: center;
  margin-top: 1rem;
  color: #666;
}

.link-text {
  color: #8B4513;
  text-decoration: none;
  transition: color 0.3s;
}

.link-text:hover {
  color: #6B340E;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .login-form-wrapper {
    padding: 1.5rem;
    margin: 1rem;
  }

  .login-title {
    font-size: 1.5rem;
  }
}
</style>
