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
            :prefix-icon="User"
            clearable
          />
        </el-form-item>

        <el-form-item label="邮箱" prop="email">
          <el-input
            v-model="registerForm.email"
            placeholder="请输入邮箱地址"
            :prefix-icon="Message"
            clearable
          />
        </el-form-item>

        <el-form-item label="验证码" prop="verificationCode">
          <el-row :gutter="10">
            <el-col :span="14">
              <el-input
                v-model="registerForm.verificationCode"
                placeholder="请输入验证码（可选）"
                :prefix-icon="Key"
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
            placeholder="请设置密码（至少6位，含大小写字母和数字）"
            :prefix-icon="Lock"
            show-password
          />
        </el-form-item>

        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input
            v-model="registerForm.confirmPassword"
            type="password"
            placeholder="请再次输入密码"
            :prefix-icon="Lock"
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
import { ref, reactive, onUnmounted } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { User, Message, Key, Lock } from '@element-plus/icons-vue';
import { useUserStore } from '../stores/userStore';
import { authAPI, setToken, setRefreshToken, setUserType } from '../utils/api';
import { USER_TYPES, STORAGE_KEYS } from '../utils/constants';

// 引入状态管理
const userStore = useUserStore();
const router = useRouter();
const registerFormRef = ref(null);
const isLoading = ref(false);
const sendingCode = ref(false);
const countdown = ref(0);
const agreeTerms = ref(false);
let countdownTimer = null;

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
    { max: 6, message: '验证码长度不能超过6位', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请设置密码', trigger: 'blur' },
    { min: 6, message: '密码长度至少为 6 个字符', trigger: 'blur' },
    {
      pattern: /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d).{6,}$/,
      message: '密码必须同时包含大写字母、小写字母和数字',
      trigger: 'blur'
    }
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

// 读取存储里的令牌：store 写入时做过 JSON.stringify，
// 而统一请求层读的是裸字符串，这里把引号去掉再写回去。
const readStoredToken = (raw) => {
  if (!raw) {
    return '';
  }
  try {
    const parsed = JSON.parse(raw);
    return typeof parsed === 'string' ? parsed : '';
  } catch {
    return raw;
  }
};

// 注册成功后把令牌写进统一请求层读的那份存储（注册走会话存储）
const persistSession = () => {
  const token = readStoredToken(sessionStorage.getItem(STORAGE_KEYS.AUTH_TOKEN))
    || readStoredToken(localStorage.getItem(STORAGE_KEYS.AUTH_TOKEN));
  const refreshToken = readStoredToken(sessionStorage.getItem(STORAGE_KEYS.REFRESH_TOKEN))
    || readStoredToken(localStorage.getItem(STORAGE_KEYS.REFRESH_TOKEN));

  if (token) {
    setToken(token, false);
  }
  if (refreshToken) {
    setRefreshToken(refreshToken, false);
  }
  setUserType(USER_TYPES.FRONTEND);
};

// 发送验证码：POST /api/auth/verification-code
const sendVerificationCode = async () => {
  const email = registerForm.email.trim();
  if (!email || !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) {
    ElMessage.warning('请输入有效的邮箱地址');
    return;
  }

  sendingCode.value = true;
  try {
    // 用服务端返回的 message，不要自己编一句「已发送至您的邮箱」——
    // 邮件服务不可用时服务端会如实说明验证码写在了日志里。
    const response = await authAPI.sendVerificationCode({ email });
    ElMessage.success(response?.message || '验证码已生成');
    startCountdown();
  } catch (error) {
    ElMessage.error(error?.message || '发送验证码失败，请稍后重试');
  } finally {
    sendingCode.value = false;
  }
};

// 倒计时功能
const startCountdown = () => {
  countdown.value = 60;
  if (countdownTimer) {
    clearInterval(countdownTimer);
  }
  countdownTimer = setInterval(() => {
    if (countdown.value > 0) {
      countdown.value--;
    } else {
      clearInterval(countdownTimer);
      countdownTimer = null;
    }
  }, 1000);
};

// 处理注册
const handleRegister = async () => {
  if (!registerFormRef.value) {
    ElMessage.error('表单初始化失败，请刷新页面重试');
    return;
  }

  try {
    await registerFormRef.value.validate();
  } catch {
    return;
  }

  if (!agreeTerms.value) {
    ElMessage.warning('请阅读并同意用户协议和隐私政策');
    return;
  }

  isLoading.value = true;
  try {
    // 契约的 RegisterRequest：username / email / password 必填，verificationCode 可选
    const success = await userStore.register({
      username: registerForm.username.trim(),
      email: registerForm.email.trim(),
      password: registerForm.password,
      verificationCode: registerForm.verificationCode.trim()
    });

    if (!success) {
      ElMessage.error(userStore.error || '注册失败，请稍后重试');
      return;
    }

    persistSession();
    ElMessage.success('注册成功！欢迎加入书法练习平台，' + (userStore.userInfo?.username || registerForm.username));
    router.push('/');
  } catch (error) {
    ElMessage.error(error?.message || '注册过程中发生错误，请稍后重试');
  } finally {
    isLoading.value = false;
  }
};

onUnmounted(() => {
  if (countdownTimer) {
    clearInterval(countdownTimer);
    countdownTimer = null;
  }
});
</script>

<style scoped>
.register-container {
  min-height: 80vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 2rem;
}

/* 协议复选框的文字默认不换行 —— 窄屏下会把整页撑出 20px 的横向滚动。
   放开换行，勾选框本身保持对齐。 */
:deep(.el-checkbox) {
  height: auto;
  white-space: normal;
}

:deep(.el-checkbox__label) {
  white-space: normal;
  line-height: 1.5;
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
  /* 窄屏顶栏要占两行，80vh + 顶栏 + 页脚会超出一屏，
     页面底部多出一段没有意义的空白滚动。 */
  .register-container {
    min-height: auto;
    padding: 1.5rem 1rem;
  }

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
