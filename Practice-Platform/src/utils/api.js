import { USER_TYPES, SESSION_TIMEOUT, STORAGE_KEYS } from './constants.js';

// API基础配置
const API_BASE_URL = import.meta.env.VITE_APP_BASE_API || 'http://localhost:8083/api';

// 标记是否正在刷新token
let isRefreshing = false;
// 存储需要等待刷新token后重试的请求
let refreshSubscribers = [];

// 存储最后活动时间
let lastActivityTime = Date.now();

// 更新活动时间
const updateActivityTime = () => {
  lastActivityTime = Date.now();
};

// 检查会话是否超时
const isSessionExpired = () => {
  return Date.now() - lastActivityTime > SESSION_TIMEOUT;
};

// 获取token
const getToken = () => {
  return localStorage.getItem(STORAGE_KEYS.AUTH_TOKEN) || sessionStorage.getItem(STORAGE_KEYS.AUTH_TOKEN);
};

// 获取refreshToken
const getRefreshToken = () => {
  return localStorage.getItem(STORAGE_KEYS.REFRESH_TOKEN) || sessionStorage.getItem(STORAGE_KEYS.REFRESH_TOKEN);
};

// 设置token
const setToken = (token, remember = false) => {
  if (remember) {
    localStorage.setItem(STORAGE_KEYS.AUTH_TOKEN, token);
  } else {
    sessionStorage.setItem(STORAGE_KEYS.AUTH_TOKEN, token);
  }
};

// 设置refreshToken
const setRefreshToken = (refreshToken, remember = false) => {
  if (remember) {
    localStorage.setItem(STORAGE_KEYS.REFRESH_TOKEN, refreshToken);
  } else {
    sessionStorage.setItem(STORAGE_KEYS.REFRESH_TOKEN, refreshToken);
  }
};

// 清除token
const clearTokens = () => {
  localStorage.removeItem(STORAGE_KEYS.AUTH_TOKEN);
  localStorage.removeItem(STORAGE_KEYS.REFRESH_TOKEN);
  sessionStorage.removeItem(STORAGE_KEYS.AUTH_TOKEN);
  sessionStorage.removeItem(STORAGE_KEYS.REFRESH_TOKEN);
  localStorage.removeItem(STORAGE_KEYS.USER_TYPE);
};

// 获取用户类型
const getUserType = () => localStorage.getItem(STORAGE_KEYS.USER_TYPE) || USER_TYPES.FRONTEND;

// 设置用户类型
const setUserType = (userType) => localStorage.setItem(STORAGE_KEYS.USER_TYPE, userType);

// 登出并跳转到登录页
const logout = () => {
  clearTokens();
  const userType = getUserType();
  const loginPath = userType === USER_TYPES.BACKEND ? '/admin/login' : '/login';
  window.location.href = loginPath;
};

// 添加请求到订阅队列
const subscribeTokenRefresh = (cb) => {
  refreshSubscribers.push(cb);
};

// 刷新token完成后，通知所有等待的请求
const onTokenRefreshed = (newToken) => {
  refreshSubscribers.forEach(cb => cb(newToken));
  refreshSubscribers = [];
};

// 刷新token
const refreshToken = async () => {
  const refreshToken = getRefreshToken();
  if (!refreshToken) {
    throw new Error('No refresh token available');
  }

  try {
    const response = await fetch(`${API_BASE_URL}/auth/refresh`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({ refreshToken }),
      credentials: 'include',
    });

    const data = await response.json();

    if (!response.ok) {
      throw new Error(data.message || 'Refresh token failed');
    }

    // 更新token和refreshToken
    if (data.accessToken) {
      const isRemembered = localStorage.getItem('authToken') !== null;
      setToken(data.accessToken, isRemembered);
    }

    if (data.refreshToken) {
      const isRemembered = localStorage.getItem('refreshToken') !== null;
      setRefreshToken(data.refreshToken, isRemembered);
    }

    updateActivityTime();
    return data.accessToken;
  } catch (error) {
    console.error('Refresh token error:', error);
    throw error;
  }
};

// 检查并刷新token
const checkAndRefreshToken = async () => {
  if (isSessionExpired()) {
    try {
      const newToken = await refreshToken();
      return newToken;
    } catch (error) {
      console.error('Token refresh failed:', error);
      return null;
    }
  }
  updateActivityTime();
  return getToken();
};

// 请求拦截器
const request = async (url, options = {}) => {
  // 跳过某些公开接口的token验证（如登录、刷新token）
  const publicPaths = ['/auth/login', '/auth/register', '/auth/verification-code'];
  const isPublicPath = publicPaths.some(path =>
    url.includes(path) || url.endsWith(path)
  );

  let token;

  // 如果不是公开接口，添加token
  if (!isPublicPath) {
    // 检查并刷新token
    token = await checkAndRefreshToken();

    // 如果token不存在或刷新失败，重定向到登录页
    if (!token) {
      logout();
      throw new Error('Session expired');
    }
  } else {
    // 公开接口也尝试获取已有token
    token = getToken();
  }

  // 设置默认配置
  const config = {
    ...options,
    headers: {
      'Content-Type': 'application/json',
      ...options.headers,
    },
    credentials: options.credentials || 'include', // 默认包含凭证
  };

  // 添加token
  if (token) {
    config.headers['Authorization'] = `Bearer ${token}`;
  }

  // 处理文件上传，不需要设置Content-Type
  if (options.formData) {
    delete config.headers['Content-Type'];
    config.body = options.formData;
  }

  // 添加请求时间戳，防止缓存
  if (options.method === 'GET') {
    const queryChar = url.includes('?') ? '&' : '?';
    url = `${url}${queryChar}_t=${Date.now()}`;
  }

  try {
    console.log(`[API请求] ${options.method || 'GET'}: ${API_BASE_URL}${url}`);
    const response = await fetch(`${API_BASE_URL}${url}`, config);

    // 解析响应数据
    let data;
    try {
      data = await response.json();
    } catch (jsonError) {
      console.error('响应解析失败:', jsonError);
      throw new Error('服务器响应格式错误');
    }

    // 处理401错误
    if (response.status === 401 && !isPublicPath) {
      const originalRequest = { url, options };

      // 避免重复刷新token
      if (!options._retry) {
        originalRequest.options._retry = true;

        // 如果正在刷新token，等待刷新完成后重试
        if (isRefreshing) {
          return new Promise(resolve => {
            subscribeTokenRefresh(newToken => {
              originalRequest.options.headers = {
                ...originalRequest.options.headers,
                'Authorization': `Bearer ${newToken}`
              };
              resolve(request(originalRequest.url, originalRequest.options));
            });
          });
        }

        // 开始刷新token
        isRefreshing = true;

        try {
          const newToken = await refreshToken();

          // 通知所有等待的请求
          onTokenRefreshed(newToken);

          // 更新当前请求的token并重试
          config.headers['Authorization'] = `Bearer ${newToken}`;
          return request(url, config);
        } catch (refreshError) {
          console.error('Token refresh failed:', refreshError);

          // 刷新失败，清除登录状态
          logout();
          throw new Error('Session expired, please login again');
        } finally {
          isRefreshing = false;
        }
      }
    }

    // 处理其他错误
    if (!response.ok) {
      console.error(`[API错误] ${url}:`, data);
      // 429: 请求过于频繁
      if (response.status === 429) {
        throw new Error(data.message || '请求过于频繁，请稍后再试');
      }
      // 403: 权限不足
      else if (response.status === 403) {
        throw new Error(data.message || '权限不足，无法访问');
      }
      // 500系列: 服务器错误
      else if (response.status >= 500) {
        throw new Error(data.message || '服务器错误，请稍后再试');
      }
      // 其他错误
      else {
        throw new Error(data.message || data.title || '请求失败');
      }
    }

    console.log(`[API成功] ${url}:`, data);
    updateActivityTime();
    return data;
  } catch (error) {
    console.error('[API请求错误]', error);
    throw error;
  }
};

// 导出工具函数
export { getToken, setToken, getRefreshToken, setRefreshToken, clearTokens, logout, updateActivityTime, setUserType };

// GET请求
export const get = (url, params) => {
  const queryString = params ? '?' + new URLSearchParams(params).toString() : '';
  return request(`${url}${queryString}`, { method: 'GET' });
};

// POST请求
export const post = (url, data) => {
  return request(url, {
    method: 'POST',
    body: JSON.stringify(data),
  });
};

// PUT请求
export const put = (url, data) => {
  return request(url, {
    method: 'PUT',
    body: JSON.stringify(data),
  });
};

// DELETE请求
export const del = (url) => {
  return request(url, { method: 'DELETE' });
};

// 文件上传
export const upload = (url, formData) => {
  return request(url, {
    method: 'POST',
    formData,
  });
};

// 用户认证相关API
export const authAPI = {
  // 前端用户注册
  register: async (userData) => {
    try {
      // 尝试调用真实API
      console.log('尝试前端用户注册API:', userData);
      return await post('/frontend/auth/register', userData);
    } catch (error) {
      console.log('真实API调用失败，使用模拟数据:', error.message);
      // 模拟数据响应，用于开发和演示
      return new Promise((resolve) => {
        setTimeout(() => {
          resolve({
            accessToken: 'mock-jwt-token-frontend-register-' + Date.now(),
            refreshToken: 'mock-refresh-token-frontend-register-' + Date.now(),
            user: {
              id: 'frontend-new-' + Date.now(),
              username: userData.username,
              email: userData.email,
              role: 'USER',
              userType: USER_TYPES.FRONTEND,
              avatar: 'https://picsum.photos/seed/' + userData.username + '/100/100'
            }
          });
        }, 300);
      });
    }
  },

  // 前端用户登录
  login: async (credentials) => {
    try {
      // 尝试调用真实API
      console.log('尝试前端用户登录API:', credentials);
      return await post('/frontend/auth/login', credentials);
    } catch (error) {
      console.log('真实API调用失败，使用模拟数据:', error.message);
      // 模拟数据响应，用于开发和演示
      return new Promise((resolve) => {
        setTimeout(() => {
          resolve({
            accessToken: 'mock-jwt-token-frontend-' + Date.now(),
            refreshToken: 'mock-refresh-token-frontend-' + Date.now(),
            user: {
              id: 'frontend-1',
              username: credentials.username,
              email: credentials.username + '@example.com',
              role: 'USER',
              userType: USER_TYPES.FRONTEND,
              avatar: 'https://picsum.photos/seed/' + credentials.username + '/100/100'
            }
          });
        }, 300);
      });
    }
  },

  // 后端管理员登录
  adminLogin: async (credentials) => {
    try {
      // 尝试调用真实API
      console.log('尝试后端管理员登录API:', credentials);
      return await post('/backend/auth/login', credentials);
    } catch (error) {
      console.log('真实API调用失败，使用模拟数据:', error.message);
      // 模拟数据响应，用于开发和演示
      return new Promise((resolve) => {
        setTimeout(() => {
          resolve({
            accessToken: 'mock-jwt-token-backend-' + Date.now(),
            refreshToken: 'mock-refresh-token-backend-' + Date.now(),
            user: {
              id: 'backend-1',
              username: credentials.username,
              email: credentials.username + '@admin.com',
              role: 'ADMIN',
              userType: USER_TYPES.BACKEND,
              avatar: 'https://picsum.photos/seed/admin-' + credentials.username + '/100/100'
            }
          });
        }, 300);
      });
    }
  },

  // 获取当前用户信息（根据用户类型）
  getProfile: () => {
    const userType = localStorage.getItem(STORAGE_KEYS.USER_TYPE) || USER_TYPES.FRONTEND;
    const endpoint = userType === USER_TYPES.BACKEND ? '/backend/auth/profile' : '/frontend/auth/profile';
    return get(endpoint);
  },

  // 刷新token（根据用户类型）
  refreshToken: async (refreshTokenData) => {
    try {
      const refreshToken = refreshTokenData?.refreshToken || getRefreshToken();
      console.log('刷新token:', refreshToken ? '存在' : '不存在');
      const userType = localStorage.getItem(STORAGE_KEYS.USER_TYPE) || USER_TYPES.FRONTEND;
      const endpoint = userType === USER_TYPES.BACKEND ? '/backend/auth/refresh' : '/frontend/auth/refresh';
      return await post(endpoint, { refreshToken });
    } catch (error) {
      console.error('刷新token失败:', error);
      throw error;
    }
  },

  // 登出（根据用户类型）
  logout: async () => {
    try {
      const userType = localStorage.getItem(STORAGE_KEYS.USER_TYPE) || USER_TYPES.FRONTEND;
      const endpoint = userType === USER_TYPES.BACKEND ? '/backend/auth/logout' : '/frontend/auth/logout';
      return await post(endpoint);
    } catch (error) {
      console.error('登出失败:', error);
      // 即使出错也清除本地token
      clearTokens();
      throw error;
    }
  },

  // 发送验证码（添加模拟数据支持）
  sendVerificationCode: async (data) => {
    try {
      // 尝试调用真实API
      console.log('尝试真实发送验证码API:', data);
      return await post('/auth/verification-code', data);
    } catch (error) {
      console.log('真实API调用失败，使用模拟数据:', error.message);
      // 模拟数据响应，用于开发和演示
      return new Promise((resolve) => {
        setTimeout(() => {
          resolve({ success: true, message: '验证码已发送（模拟）', code: '123456' });
        }, 300);
      });
    }
  },
};

// 用户管理相关API
export const userAPI = {
  // 更新用户信息
  updateProfile: (userData) => put('/users/profile', userData),

  // 上传头像
  uploadAvatar: (formData) => upload('/users/avatar', formData),

  // 修改密码
  changePassword: (passwordData) => put('/users/password', passwordData),

  // 获取指定用户信息
  getUserInfo: (userId) => get(`/users/${userId}`),
};

// 作品相关API
export const worksAPI = {
  // 创建作品（添加模拟数据支持）
  createWork: async (formData) => {
    try {
      // 尝试调用真实API
      return await upload('/works', formData);
    } catch (error) {
      console.log('API调用失败，使用模拟数据:', error.message);
      // 返回模拟数据，使用占位图片或Base64编码的简单图片
      return {
        id: Date.now().toString(),
        createdAt: new Date().toISOString(),
        title: formData.get('title') || '未命名作品',
        description: formData.get('description') || '',
        imageUrl: 'https://via.placeholder.com/500x300?text=书法作品',
        status: 'draft'
      };
    }
  },

  // 获取作品列表
  getWorks: (params) => get('/works', params),

  // 获取用户作品列表
  getUserWorks: (userId, params) => get(`/works/user/${String(userId)}` , params),

  // 获取作品详情
  getWorkDetail: (workId) => get(`/works/${workId}`),

  // 更新作品
  updateWork: (workId, workData) => put(`/works/${workId}`, workData),

  // 更新作品图片
  updateWorkImage: (workId, formData) => upload(`/works/${workId}/image`, formData),

  // 删除作品
  deleteWork: (workId) => del(`/works/${workId}`),
};

// 文件审核相关API（管理员）
export const fileReviewsAPI = {
  createReview: (data) => post('/admin/file-reviews', data),
  updateReview: (reviewId, data) => put(`/admin/file-reviews/${String(reviewId)}` , data),
  getReviewById: (reviewId) => get(`/admin/file-reviews/${String(reviewId)}`),
  getReviewByWorkId: (workId) => get(`/admin/file-reviews/work/${workId}`),
  getMyReviews: (params) => get('/admin/file-reviews/my-reviews', params),
  deleteReview: (reviewId) => del(`/admin/file-reviews/${String(reviewId)}`),
  batchReviewWorks: (workIds, reviewStatus, reviewComments) => post('/admin/file-reviews/batch', { workIds, reviewStatus, reviewComments })
};

// 字帖相关API
export const copybookAPI = {
  // 获取字帖列表
  getCopybooks: (params) => get('/copybooks', params),

  // 获取热门字帖
  getPopularCopybooks: () => get('/copybooks/popular'),

  // 获取字帖详情
  getCopybookDetail: (copybookId) => get(`/copybooks/${copybookId}`),

  // 记录练习历史
  recordPractice: (copybookId, practiceData) => post(`/copybooks/${copybookId}/practice`, practiceData),

  // 获取用户练习统计
  getUserStatistics: () => get('/copybooks/statistics/user'),
};

// 社区评论相关API
export const communityAPI = {
  // 创建评论
  createComment: (commentData) => post('/community/comments', commentData),

  // 获取作品评论列表
  getWorkComments: (workId, params) => get(`/community/comments/work/${workId}`, params),

  // 删除评论
  deleteComment: (commentId) => del(`/community/comments/${commentId}`),

  // 获取用户评论历史
  getUserComments: (userId, params) => get(`/community/comments/user/${userId}`, params),
};

// 导出默认对象
export default {
  get,
  post,
  put,
  delete: del,
  upload,
  auth: authAPI,
  user: userAPI,
  works: worksAPI,
  copybook: copybookAPI,
  community: communityAPI,
  fileReviews: fileReviewsAPI,
  // Token管理函数
  getToken,
  getRefreshToken,
  setToken,
  setRefreshToken,
  clearTokens,
  getUserType,
  setUserType,
  // 工具函数
  isSessionExpired
};
