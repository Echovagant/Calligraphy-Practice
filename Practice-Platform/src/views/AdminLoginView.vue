<template>
  <div class="admin-login-container">
    <div class="admin-login-box">
      <div class="login-header">
        <h1>管理员登录</h1>
        <p class="subtitle">书法练习平台 - 后台管理系统</p>
      </div>
      
      <el-form
        ref="loginFormRef"
        :model="loginForm"
        :rules="loginRules"
        class="login-form"
        @submit.prevent="handleLogin"
      >
        <el-form-item prop="username">
          <el-input
            v-model="loginForm.username"
            placeholder="请输入管理员用户名"
            prefix-icon="User"
            size="large"
          />
        </el-form-item>
        
        <el-form-item prop="password">
          <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="请输入管理员密码"
            prefix-icon="Lock"
            size="large"
            show-password
            @keyup.enter="handleLogin"
          />
        </el-form-item>
        
        <el-form-item>
          <el-checkbox v-model="loginForm.rememberMe">记住我</el-checkbox>
        </el-form-item>
        
        <el-form-item>
          <el-button
            type="primary"
            size="large"
            :loading="loading"
            class="login-button"
            @click="handleLogin"
          >
            登录
          </el-button>
        </el-form-item>
      </el-form>
      
      <div class="login-footer">
        <el-link type="primary" @click="goToFrontend">返回前端首页</el-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { useUserStore } from '@/stores/userStore';

const router = useRouter();
const userStore = useUserStore();
const loginFormRef = ref();

const loginForm = reactive({
  username: '',
  password: '',
  rememberMe: false
});

const loading = ref(false);

const loginRules = {
  username: [
    { required: true, message: '请输入管理员用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度应在3-20个字符之间', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入管理员密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度应在6-20个字符之间', trigger: 'blur' }
  ]
};

const handleLogin = async () => {
  if (!loginFormRef.value) return;
  
  try {
    await loginFormRef.value.validate();
    loading.value = true;
    
    const success = await userStore.adminLogin(
      loginForm.username,
      loginForm.password,
      loginForm.rememberMe
    );
    
    if (success) {
      ElMessage.success('管理员登录成功');
      // 跳转到管理员仪表板
      router.push('/admin');
    } else {
      ElMessage.error(userStore.error || '管理员登录失败');
    }
  } catch (error) {
    console.error('管理员登录验证失败:', error);
    if (error.message) {
      ElMessage.error(error.message);
    }
  } finally {
    loading.value = false;
  }
};

const goToFrontend = () => {
  router.push('/');
};
</script>

<style scoped>
.admin-login-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px;
}

.admin-login-box {
  width: 100%;
  max-width: 400px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.1);
  padding: 40px;
}

.login-header {
  text-align: center;
  margin-bottom: 30px;
}

.login-header h1 {
  color: #333;
  font-size: 24px;
  font-weight: 600;
  margin-bottom: 8px;
}

.subtitle {
  color: #666;
  font-size: 14px;
  margin: 0;
}

.login-form {
  margin-bottom: 20px;
}

.login-button {
  width: 100%;
  height: 44px;
  font-size: 16px;
  font-weight: 500;
}

.login-footer {
  text-align: center;
  padding-top: 20px;
  border-top: 1px solid #eee;
}

/* 响应式设计 */
@media (max-width: 480px) {
  .admin-login-box {
    padding: 30px 20px;
    margin: 0 10px;
  }
  
  .login-header h1 {
    font-size: 20px;
  }
}
</style>