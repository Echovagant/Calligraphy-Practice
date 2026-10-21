import { defineStore } from 'pinia';
import { ElMessage } from 'element-plus';
import { authAPI, userAPI, worksAPI, copybookAPI, setUserType, getUserType, getToken, setToken, setRefreshToken, getRefreshToken } from '../utils/api';
import { USER_TYPES, STORAGE_KEYS } from '../utils/constants';
import { getStorage, setStorage, removeStorage } from '../utils/storage';

// SessionStorage工具函数
const getSessionStorage = (key) => {
  try {
    const item = sessionStorage.getItem(key);
    return item ? JSON.parse(item) : null;
  } catch (error) {
    console.error('获取会话存储失败:', error);
    return null;
  }
};

const setSessionStorage = (key, value) => {
  try {
    sessionStorage.setItem(key, JSON.stringify(value));
    return true;
  } catch (error) {
    console.error('设置会话存储失败:', error);
    return false;
  }
};

const removeSessionStorage = (key) => {
  try {
    sessionStorage.removeItem(key);
    return true;
  } catch (error) {
    console.error('移除会话存储失败:', error);
    return false;
  }
};

export const useUserStore = defineStore('user', {
  state: () => ({
    // 用户信息
    userInfo: null,
    // 登录状态
    isLoggedIn: false,
    // 加载状态
    loading: false,
    // 错误信息
    error: null,
    // 最后活动时间，用于检测会话超时
    lastActivityTime: Date.now(),
    // 用户信息加载状态
    userInfoLoaded: false,
    // 用户类型（USER/ADMIN）
    userType: USER_TYPES.FRONTEND
  }),

  getters: {
    // 获取用户名
    getUsername: (state) => state.userInfo?.username || '',

    // 获取用户头像
    // 没有头像就返回空字符串 —— 不要用占位图服务造假头像
    // （契约的 AuthUser.avatar 是用户真实上传的头像地址）
    getUserAvatar: (state) => state.userInfo?.avatar || '',

    // 获取用户角色
    getUserRole: (state) => state.userInfo?.role || 'user',

    // 判断是否是管理员（后端返回ADMIN/SUPER_ADMIN，需要转换为小写比较）
    isAdmin: (state) => state.userInfo?.role?.toLowerCase() === 'admin' || state.userInfo?.role?.toLowerCase() === 'super_admin',
    
    // 判断是否是后端管理员
    isBackendUser: (state) => state.userType === USER_TYPES.BACKEND,
    
    // 检查会话是否超时（30分钟无活动）
    isSessionExpired: (state) => {
      const SESSION_TIMEOUT = 30 * 60 * 1000
      return Date.now() - state.lastActivityTime > SESSION_TIMEOUT
    }
  },

  actions: {
    // 更新活动时间
    updateActivityTime() {
      this.lastActivityTime = Date.now();
    },

    // 前端用户登录
    async login(username, password, rememberMe = false) {
      console.log('执行前端用户登录:', { username, rememberMe });
      this.loading = true;
      this.error = null;

      try {
        if (!username || !password) {
          throw new Error('用户名和密码不能为空');
        }

        // 设置用户类型为前端用户
        this.userType = USER_TYPES.FRONTEND;
        setUserType(USER_TYPES.FRONTEND);

        // 调用真实的后端API进行登录
        const response = await authAPI.login({ username, password });
        console.log('登录成功，获取响应:', response);

        // 存储用户数据和token
        this.userInfo = response.user;
        this.isLoggedIn = true;
        this.updateActivityTime();
        
        // 登录成功后标记用户信息已加载
        this.userInfoLoaded = true;

        // 存储token和用户信息，使用accessToken（后端返回的字段名）
        // 令牌必须用 api.js 的 setToken / setRefreshToken 写 —— 那是请求层读的那一格，
        // 存的是**裸串**。用 utils/storage.js 的 setStorage 会 JSON.stringify，
        // 存成 "eyJ..."（带引号），请求头就成了 Bearer "eyJ..." → 401。
        setToken(response.accessToken, rememberMe);
        setRefreshToken(response.refreshToken, rememberMe);
        if (rememberMe) {
          // 使用localStorage（记住登录）
          setStorage('userInfo', response.user);
        } else {
          // 使用sessionStorage（会话登录）
          setSessionStorage('userInfo', response.user);
        }

        console.log('登录成功，用户信息已更新');
        return true;
      } catch (err) {
        console.error('登录失败:', err);
        this.error = err.response?.data?.message || err.message || '登录失败';
        return false;
      } finally {
        this.loading = false;
        console.log('登录操作完成');
      }
    },

    // 后端管理员登录
    async adminLogin(username, password, rememberMe = false) {
      console.log('执行后端管理员登录:', { username, rememberMe });
      this.loading = true;
      this.error = null;

      try {
        if (!username || !password) {
          throw new Error('用户名和密码不能为空');
        }

        // 调用真实的后端API进行登录
        const response = await authAPI.adminLogin({ username, password });
        console.log('管理员登录成功，获取响应:', response);

        // 登录**成功之后**才切用户类型。
        // 提前切的话，登录失败也不会回滚 —— 前端用户的令牌续期会去打
        // /backend/auth/refresh，续期必然失败。
        this.userType = USER_TYPES.BACKEND;
        setUserType(USER_TYPES.BACKEND);

        // 存储管理员数据和token
        this.userInfo = response.user;
        this.isLoggedIn = true;
        this.updateActivityTime();

        // 登录成功后标记用户信息已加载
        this.userInfoLoaded = true;

        // 存储token和用户信息，使用accessToken（后端返回的字段名）
        // 令牌必须用 api.js 的 setToken / setRefreshToken 写 —— 那是请求层读的那一格，
        // 存的是**裸串**。用 utils/storage.js 的 setStorage 会 JSON.stringify，
        // 存成 "eyJ..."（带引号），请求头就成了 Bearer "eyJ..." → 401。
        setToken(response.accessToken, rememberMe);
        setRefreshToken(response.refreshToken, rememberMe);
        if (rememberMe) {
          // 使用localStorage（记住登录）
          setStorage('userInfo', response.user);
        } else {
          // 使用sessionStorage（会话登录）
          setSessionStorage('userInfo', response.user);
        }

        console.log('管理员登录成功，用户信息已更新');
        return true;
      } catch (err) {
        console.error('管理员登录失败:', err);
        this.error = err.response?.data?.message || err.message || '管理员登录失败';
        return false;
      } finally {
        this.loading = false;
        console.log('管理员登录操作完成');
      }
    },

    // 注册
    async register(userData) {
      console.log('执行用户注册:', { username: userData.username, email: userData.email });
      this.loading = true;
      this.error = null;

      try {
        if (!userData.username || !userData.password || !userData.email) {
          throw new Error('用户名、密码和邮箱不能为空');
        }

        // 调用真实的后端API进行注册
        const response = await authAPI.register(userData);
        console.log('注册成功，获取响应:', response);
        
        // 存储用户数据和token（注册后自动登录）
        // 契约：注册响应的用户对象在 response.user，不是整个响应
        this.userInfo = response.user;
        this.isLoggedIn = true;

        // 设置用户类型为前端用户
        this.userType = USER_TYPES.FRONTEND;
        setUserType(USER_TYPES.FRONTEND);

        // 存储token和用户信息（注册后自动登录）
        // 令牌用 api.js 的 setToken/setRefreshToken 写（裸串），
        // userInfo 存 response.user —— 存整个响应的话 userInfo.id 取不到。
        setToken(response.accessToken, false);
        setSessionStorage('userInfo', response.user);

        if (response.refreshToken) {
          setRefreshToken(response.refreshToken, false);
        }

        console.log('注册成功，用户信息已更新');
        return true;
      } catch (err) {
        console.error('注册失败:', err);
        this.error = err.message || '注册失败';
        return false;
      } finally {
        this.loading = false;
        console.log('注册操作完成');
      }
    },

    // 登出
  async logout() {
    try {
      // 尝试调用登出接口
      await authAPI.logout();
    } catch (error) {
      console.error('登出接口调用失败:', error);
    } finally {
      this.userInfo = null;
      this.isLoggedIn = false;
      this.lastActivityTime = 0;
      this.userInfoLoaded = false;
      this.userType = USER_TYPES.FRONTEND;

      // 清除存储的token和用户信息
      removeStorage('authToken');
      removeStorage('userInfo');
      removeStorage('refreshToken');
      removeStorage('userType');
      removeSessionStorage('authToken');
      removeSessionStorage('userInfo');
      removeSessionStorage('refreshToken');
      removeSessionStorage('userType');
      
      // 清除用户类型设置
      setUserType(USER_TYPES.FRONTEND);
    }
  },

    // 更新用户信息（PUT /api/users/profile）
    // 动作名是 updateProfile —— 视图调的就是这个名字
    async updateProfile(updatedInfo) {
      this.loading = true;
      this.error = null;

      try {
        // 检查会话是否过期，如果过期则尝试刷新令牌
        if (!await this.checkAndRefreshToken()) {
          throw new Error('会话已过期，请重新登录');
        }

        const response = await userAPI.updateProfile(updatedInfo);

        // 契约规定 updateProfile 返回裸用户对象（不是 {user:...} 包装）
        this.userInfo = response;
        this.updateActivityTime();

        // 更新存储的用户信息
        if (getStorage('authToken')) {
          setStorage('userInfo', this.userInfo);
        } else if (getSessionStorage('authToken')) {
          setSessionStorage('userInfo', this.userInfo);
        }

        return true;
      } catch (err) {
        this.error = err.response?.data?.message || err.message || '更新用户信息失败';
        return false;
      } finally {
        this.loading = false;
      }
    },

    // 上传头像
    async uploadAvatar(avatarFile) {
      this.loading = true;
      this.error = null;

      try {
        // 检查会话是否过期，如果过期则尝试刷新令牌
        if (!await this.checkAndRefreshToken()) {
          throw new Error('会话已过期，请重新登录');
        }
        
        const formData = new FormData();
        formData.append('avatar', avatarFile);

        const response = await userAPI.uploadAvatar(formData);

        // 更新头像信息
        // 后端的 FileUploadResponseDto 里是 fileUrl（没有 avatar 字段）
        this.userInfo = {
          ...this.userInfo,
          avatar: response?.fileUrl
        };
        
        this.updateActivityTime();

        // 更新存储的用户信息
        if (getStorage('authToken')) {
          setStorage('userInfo', this.userInfo);
        } else if (getSessionStorage('authToken')) {
          setSessionStorage('userInfo', this.userInfo);
        }

        return true;
      } catch (err) {
        this.error = err.response?.data?.message || err.message || '上传头像失败';
        return false;
      } finally {
        this.loading = false;
      }
    },

    // 修改密码
    async changePassword(passwordData) {
      this.loading = true;
      this.error = null;

      try {
        await userAPI.changePassword(passwordData);
        return true;
      } catch (err) {
        this.error = err.message || '修改密码失败';
        return false;
      } finally {
        this.loading = false;
      }
    },

    // 刷新令牌
    async refreshTokenHandler() {
      try {
        const refreshToken = getRefreshToken();
        if (!refreshToken) {
          throw new Error('没有刷新令牌');
        }

        // 契约规定 refresh 返回裸资源（无 data 信封）
        const data = await authAPI.refreshToken({ refreshToken });

        // 更新token和刷新token
        const { accessToken, refreshToken: newRefreshToken } = data;

        // 根据原token的存储位置，选择相同位置存储新token。
        // 同样必须用 api.js 的 setToken/setRefreshToken —— 存裸串，
        // 下一轮请求的 Authorization 头才是对的。
        const isRemembered = !!localStorage.getItem(STORAGE_KEYS.AUTH_TOKEN);
        setToken(accessToken, isRemembered);
        if (newRefreshToken) {
          setRefreshToken(newRefreshToken, isRemembered);
        }

        this.updateActivityTime();
        return true;
      } catch (error) {
        console.error('刷新令牌失败:', error);
        // 刷新失败，退出登录
        this.logout();
        return false;
      }
    },

    // 检查并自动刷新token
    async checkAndRefreshToken() {
      if (this.isSessionExpired) {
        const refreshToken = getStorage('refreshToken') || getSessionStorage('refreshToken');
        if (refreshToken) {
          return await this.refreshTokenHandler();
        }
        return false;
      }
      this.updateActivityTime();
      return true;
    },

    // 获取当前用户的作品列表
    // GET /api/works/user/{userId} —— 公开画廊是 /api/works（不带 userId）
    // 返回**分页信封** {content,page,size,total,totalPages}，数据在 response.content
    async fetchMyWorks(params = {}) {
      const userId = this.userInfo?.id;
      if (!userId) {
        throw new Error('未登录，无法获取作品列表');
      }
      return await worksAPI.getUserWorks(userId, params);
    },

    // 获取当前用户的练习历史
    // GET /api/copybooks/practice/history —— 返回**分页信封**
    async fetchPractices(params = {}) {
      return await copybookAPI.getPracticeHistory(params);
    },

    // 获取当前用户信息
    async fetchUserProfile() {
      this.loading = true;

      try {
        // 检查会话是否过期，如果过期则尝试刷新令牌
        if (!await this.checkAndRefreshToken()) {
          throw new Error('会话已过期');
        }
        
        const response = await authAPI.getProfile();
        // 契约规定 profile 返回裸用户对象（不是 {user:...} 包装）
        this.userInfo = response;
        this.isLoggedIn = true;
        this.userInfoLoaded = true;
        this.updateActivityTime();

        // 更新存储的用户信息
        if (getStorage('authToken')) {
          setStorage('userInfo', this.userInfo);
        } else if (getSessionStorage('authToken')) {
          setSessionStorage('userInfo', this.userInfo);
        }

        return true;
      } catch (error) {
        console.error('获取用户信息失败:', error);
        // 获取失败，可能是token过期，清除登录状态
        this.logout();
        return false;
      } finally {
        this.loading = false;
      }
    },

    // 从本地存储恢复用户信息
    async initUserFromStorage() {
      // 检查是否有token（用 api.js 的读法 —— 令牌存的是裸串）
      const token = getToken();

      if (token) {
        // 从存储中恢复用户类型。
        // 注意：这里读的值可能来自旧的 setStorage 写法（带引号的 JSON 字符串），
        // 写回去时要用 api.js 的 setUserType（裸值），否则 getUserType 比较不成立。
        const storedUserType = getUserType();
        this.userType = storedUserType;
        setUserType(storedUserType);
        
        // 有token，尝试获取用户信息
        await this.fetchUserProfile();
      } else {
        // 没有token，清除可能残留的用户信息
        this.logout();
      }
    },

    // 发送验证码
    async sendVerificationCode(email) {
      this.loading = true;
      this.error = null;

      try {
        // 实现发送验证码API调用
        await authAPI.sendVerificationCode({ email });
        ElMessage.success(`验证码已发送到 ${email}`);
        return true;
      } catch (err) {
        this.error = err.response?.data?.message || err.message || '发送验证码失败';
        return false;
      } finally {
        this.loading = false;
      }
    }
  }
});
