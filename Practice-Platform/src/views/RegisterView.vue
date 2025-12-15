<template>
  <div class="register-container">
    <div class="register-form-wrapper">
      <div class="register-header">
        <h2 class="register-title">用户注册</h2>
        <p class="register-subtitle">加入书法练习平台，开启艺术之旅</p>
      </div>

      <el-form :model="registerForm" :rules="registerRules" ref="registerFormRef" label-width="80px">
        <el-form-item label="用户名" prop="username">
          <el-input
            v-model="registerForm.username"
            placeholder="请输入用户名（3-20个字符）"
            prefix-icon="el-icon-user"
            clearable
          />
        </el-form-item>

        <el-form-item label="邮箱" prop="email">
          <el-input
            v-model="registerForm.email"
            placeholder="请输入邮箱地址"
            prefix-icon="el-icon-message"
            clearable
          />
        </el-form-item>

        <el-form-item label="验证码" prop="verificationCode">
          <el-row :gutter="10">
            <el-col :span="14">
              <el-input
                v-model="registerForm.verificationCode"
                placeholder="请输入验证码"
                prefix-icon="el-icon-key"
              />
            </el-col>
            <el-col :span="10">
              <el-button
                type="primary"
                :disabled="countdown > 0"
                @click="sendVerificationCode"
                :loading="sendingCode"
                class="verification-btn"
              >
                {{ countdown > 0 ? `${countdown}秒后重新发送` : '获取验证码' }}
              </el-button>
            </el-col>
          </el-row>
        </el-form-item>

        <el-form-item label="密码" prop="password">
          <el-input
            v-model="registerForm.password"
            type="password"
            placeholder="请设置密码（至少6位）"
            prefix-icon="el-icon-lock"
            show-password
          />
        </el-form-item>

        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input
            v-model="registerForm.confirmPassword"
            type="password"
            placeholder="请再次输入密码"
            prefix-icon="el-icon-lock"
            show-password
          />
        </el-form-item>

        <el-form-item>
          <el-checkbox v-model="agreeTerms" prop="agreeTerms">
            我已阅读并同意 <el-link type="primary" :underline="false">用户协议</el-link> 和
            <el-link type="primary" :underline="false">隐私政策</el-link>
          </el-checkbox>
        </el-form-item>

        <el-form-item>
          <el-button
            type="primary"
            :loading="isLoading"
            @click="handleRegister"
            class="register-button"
            :disabled="isLoading || !agreeTerms"
          >
            {{ isLoading ? '注册中...' : '立即注册' }}
          </el-button>
        </el-form-item>

        <el-form-item>
          <div class="login-link">
            <span>已有账号？</span>
            <router-link to="/login" class="link-text">立即登录</router-link>
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
const registerFormRef = ref(null);
const isLoading = ref(false);
const sendingCode = ref(false);
const countdown = ref(0);
const agreeTerms = ref(false);

// 注册表单数据
const registerForm = reactive({
  username: '',
  email: '',
  verificationCode: '',
  password: '',
  confirmPassword: ''
});

// 表单验证规则
const registerRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度在 3 到 20 个字符', trigger: 'blur' },
    { pattern: /^[a-zA-Z0-9_\u4e00-\u9fa5]+$/, message: '用户名只能包含字母、数字、下划线和中文', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱地址', trigger: 'blur' },
    { type: 'email', message: '请输入有效的邮箱地址', trigger: 'blur' }
  ],
  verificationCode: [
    { required: true, message: '请输入验证码', trigger: 'blur' },
    { min: 4, max: 6, message: '验证码长度为4-6位', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请设置密码', trigger: 'blur' },
    { min: 6, message: '密码长度至少为 6 个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== registerForm.password) {
          callback(new Error('两次输入的密码不一致'));
        } else {
          callback();
        }
      },
      trigger: 'blur'
    }
  ],
  agreeTerms: [
    { validator: (rule, value, callback) => {
      if (!value) {
        callback(new Error('请阅读并同意用户协议和隐私政策'));
      } else {
        callback();
      }
    }, trigger: 'change' }
  ]
};

// 发送验证码
const sendVerificationCode = async () => {
  // 验证邮箱格式
  const emailRule = registerRules.email[1];
  if (!emailRule.validator || !emailRule.validator({}, registerForm.email, () => {})) {
    if (!emailRule.type || emailRule.type !== 'email' || !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(registerForm.email)) {
      ElMessage.warning('请输入有效的邮箱地址');
      return;
    }
  }

  try {
    sendingCode.value = true;
    console.log('准备发送验证码到邮箱:', registerForm.email);

    // 模拟发送验证码API调用
    await new Promise(resolve => setTimeout(resolve, 1000));

    console.log('验证码发送成功');
    ElMessage.success('验证码已发送至您的邮箱: ' + registerForm.email);

    // 开始倒计时
    startCountdown();

  } catch (error) {
    console.error('发送验证码失败:', error);
    ElMessage.error('发送验证码失败，请稍后重试');
  } finally {
    sendingCode.value = false;
  }
};

// 倒计时功能
const startCountdown = () => {
  countdown.value = 60;
  const timer = setInterval(() => {
    if (countdown.value > 0) {
      countdown.value--;
    } else {
      clearInterval(timer);
    }
  }, 1000);
};

// 处理注册
const handleRegister = async () => {
  try {
    // 添加表单引用检查
    if (!registerFormRef.value) {
      console.error('表单引用未找到');
      ElMessage.error('表单初始化失败，请刷新页面重试');
      return;
    }

    await registerFormRef.value.validate();
    console.log('注册表单验证通过，准备发送注册请求:', { username: registerForm.username, email: registerForm.email });

    // 检查是否同意条款
    if (!agreeTerms.value) {
      ElMessage.warning('请阅读并同意用户协议和隐私政策');
      return;
    }

    // 调用store的注册方法
    console.log('开始注册用户');
    isLoading.value = true;

    const success = await userStore.register({
      username: registerForm.username,
      email: registerForm.email,
      password: registerForm.password,
      confirmPassword: registerForm.confirmPassword
    });

    if (success) {
      console.log('注册成功，用户信息:', userStore.userInfo);
      ElMessage.success('注册成功！欢迎加入书法练习平台，' + (userStore.userInfo?.username || registerForm.username));

      // 注册成功后跳转到首页
      setTimeout(() => {
        console.log('注册成功，跳转到首页');
        router.push('/');
      }, 1500);
    } else {
      console.error('注册失败:', userStore.error);
      ElMessage.error(userStore.error || '注册失败，请稍后重试');
    }
  } catch (error) {
    console.error('注册过程中发生错误:', error);
    if (error !== false) {
      if (error.message?.includes('表单验证失败')) {
        ElMessage.warning('请检查表单填写是否正确');
      } else {
        ElMessage.error('注册过程中发生错误，请稍后重试');
      }
    }
  } finally {
    isLoading.value = false;
  }
};
</script>

<style scoped>
.register-container {
  min-height: 80vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 2rem;
}

.register-form-wrapper {
  background-color: rgba(255, 255, 255, 0.95);
  padding: 2.5rem;
  border-radius: 12px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  width: 100%;
  max-width: 500px;
  backdrop-filter: blur(10px);
}

.register-header {
  text-align: center;
  margin-bottom: 2rem;
}

.register-title {
  font-size: 1.8rem;
  color: #333;
  margin-bottom: 0.5rem;
  font-family: 'SimSun', serif;
}

.register-subtitle {
  color: #666;
  font-size: 1rem;
}

.verification-btn {
  width: 100%;
}

.register-button {
  width: 100%;
  padding: 0.8rem;
  font-size: 1rem;
  background-color: #8B4513;
  border-color: #8B4513;
}

.register-button:hover,
.register-button:focus {
  background-color: #6B340E;
  border-color: #6B340E;
}

.login-link {
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
  .register-form-wrapper {
    padding: 1.5rem;
    margin: 1rem;
  }

  .register-title {
    font-size: 1.5rem;
  }

  .el-row {
    display: flex;
    flex-direction: column;
    gap: 0.5rem;
  }

  .el-col {
    width: 100% !important;
  }
}
</style>
