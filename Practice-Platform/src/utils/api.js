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

// 取存储里的令牌。
// 注意：utils/storage.js 的 setStorage 会 JSON.stringify，存进去的是带引号的 "eyJ..."，
// 而这里读的是裸值。早期 store 用 setStorage 存过令牌，所以这里要能兼容两种格式，
// 否则请求头会变成 `Bearer "eyJ..."` → 401。
const readRawToken = (raw) => {
  if (!raw) return null;
  const value = raw.trim();
  if (value.startsWith('"') && value.endsWith('"')) {
    try {
      return JSON.parse(value);
    } catch {
      return value;
    }
  }
  return value;
};

// 获取token
const getToken = () => {
  return readRawToken(localStorage.getItem(STORAGE_KEYS.AUTH_TOKEN))
    || readRawToken(sessionStorage.getItem(STORAGE_KEYS.AUTH_TOKEN));
};

// 获取refreshToken
const getRefreshToken = () => {
  return readRawToken(localStorage.getItem(STORAGE_KEYS.REFRESH_TOKEN))
    || readRawToken(sessionStorage.getItem(STORAGE_KEYS.REFRESH_TOKEN));
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
// 兼容 utils/storage.js 的 setStorage 写过的值：它会 JSON.stringify，
// localStorage 里存的是带引号的 "ADMIN"，原样比较 === 'ADMIN' 会不成立，
// 导致管理员的 401 自动续期打到 /frontend/auth/refresh。
const getUserType = () => {
  const raw = localStorage.getItem(STORAGE_KEYS.USER_TYPE);
  if (!raw) return USER_TYPES.FRONTEND;
  const value = raw.trim();
  if (value.startsWith('"') && value.endsWith('"')) {
    try {
      return JSON.parse(value);
    } catch {
      return value;
    }
  }
  return value;
};

// 设置用户类型
const setUserType = (userType) => localStorage.setItem(STORAGE_KEYS.USER_TYPE, userType);

// 登出并跳转到登录页
// 不要用 window.location.href —— 那是整页刷新，会白屏重来，
// 而且在 utils 里 import router 会绕成循环依赖（router → stores → api）。
// 改成派发事件，由 App.vue 监听后做路由内跳转。
const logout = () => {
  // 注意顺序：clearTokens() 会把 USER_TYPE 一并清掉，
  // 先取用户类型再清，否则管理员会被送去 /login 而不是 /admin/login。
  const userType = getUserType();
  clearTokens();
  window.dispatchEvent(new CustomEvent('app:auth-expired', { detail: { userType } }));
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
    // 401 自动续期：这里刻意用裸 fetch 而不是 post()——post() 走 request()，
    // 而 request() 在 401 时又会调本函数，直接用会无限递归。
    // 路径按用户类型分流，与 authAPI.refreshToken 保持一致
    // （契约里有 /frontend/auth/refresh 与 /backend/auth/refresh，
    //   没有通用的 /auth/refresh）。
    const userType = localStorage.getItem(STORAGE_KEYS.USER_TYPE) || USER_TYPES.FRONTEND;
    const refreshPath = userType === USER_TYPES.BACKEND
      ? '/backend/auth/refresh'
      : '/frontend/auth/refresh';
    const response = await fetch(`${API_BASE_URL}${refreshPath}`, {
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

// 契约里标了 security: [] 的**公开端点**。
// 这些端点不带令牌也能访问 —— 不能因为「没令牌」就把用户踢去登录页。
// 路径是相对 /api 的（API_BASE_URL 已经带了 /api 前缀）。
const PUBLIC_ENDPOINT_PATTERNS = [
  /^\/frontend\/auth\/register$/,
  /^\/frontend\/auth\/login$/,
  /^\/backend\/auth\/login$/,
  /^\/auth\/verification-code$/,
  /^\/works$/,                                  // 公开画廊
  /^\/stats\/overview$/,                        // 首页概览
  /^\/copybooks$/,                              // 字帖列表
  /^\/copybooks\/popular$/,                     // 热门字帖
  /^\/copybooks\/[^/]+$/,                       // 字帖详情（单段，不会命中 statistics/user 与 practice/history）
  /^\/discussions$/,                            // 讨论列表
  /^\/discussions\/(hot|featured|pinned|search)$/,
  /^\/discussions\/[^/]+$/,                      // 讨论详情
  /^\/discussions\/[^/]+\/replies$/,             // 某个讨论的回复列表
  /^\/community\/comments\/work\/[^/]+$/,        // 作品评论列表（按用户查评论历史仍需令牌）
];

// 请求拦截器
const request = async (url, options = {}) => {
  // 跳过某些公开接口的token验证（如登录、刷新token、公开画廊）
  const pathOnly = url.split('?')[0];
  const isPublicPath = PUBLIC_ENDPOINT_PATTERNS.some(re => re.test(pathOnly));

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

  // 注意：这里**不要**再给 GET 拼时间戳。
  // 后端本来就发 Cache-Control: no-cache, no-store，额外的时间戳只是徒增负担，
  // 还会把 URL 弄脏。查询参数由 get() 统一负责拼接。

  try {
    console.log(`[API请求] ${options.method || 'GET'}: ${API_BASE_URL}${url}`);

    // 网络层错误单独处理：fetch 抛出的 TypeError 消息是浏览器的原文
    // （Chrome 下是 "Failed to fetch"），直接透给用户就是一句英文。
    // 这里换成可读的中文，并保留原始错误供排查。
    let response;
    try {
      response = await fetch(`${API_BASE_URL}${url}`, config);
    } catch (networkError) {
      console.error('[网络错误]', networkError);
      const error = new Error('无法连接服务器，请确认后端服务已启动后重试');
      error.cause = networkError;
      error.isNetworkError = true;
      throw error;
    }

    // 解析响应数据
    let data;
    // 204 No Content：成功但没有响应体。
    // 契约里 PUT /api/users/password、DELETE /api/works/{id}、两个 logout 都返回 204，
    // 无条件 await response.json() 会把**成功**当成「服务器响应格式错误」。
    if (response.status === 204) {
      return null;
    }
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
      // 统一抛一个带 status 的 Error —— 视图要能程序化区分
      // 401（未登录）/ 403（越权）/ 404（不存在）/ 422（业务校验），
      // 不能只能靠中文文案去匹配。
      const apiError = (message) => {
        const err = new Error(message);
        err.status = response.status;
        err.code = data?.code;
        return err;
      };
      // 429: 请求过于频繁
      if (response.status === 429) {
        throw apiError(data.message || '请求过于频繁，请稍后再试');
      }
      // 403: 权限不足
      else if (response.status === 403) {
        throw apiError(data.message || '权限不足，无法访问');
      }
      // 500系列: 服务器错误
      else if (response.status >= 500) {
        throw apiError(data.message || '服务器错误，请稍后再试');
      }
      // 其他错误
      else {
        throw apiError(data.message || data.title || '请求失败');
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
export { getToken, setToken, getRefreshToken, setRefreshToken, clearTokens, logout, updateActivityTime, setUserType, getUserType };

// GET请求
export const get = (url, params) => {
  // URLSearchParams 会把 undefined / null 变成字面量 "undefined" / "null"，
  // 后端会收到一个值不是预期的查询参数。所以先滤掉空值。
  const query = params
    ? Object.entries(params).filter(([, v]) => v !== undefined && v !== null && v !== '')
    : [];
  // url 上已经有 query 时用 & 接着拼，别拼出两个 ?
  const separator = url.includes('?') ? '&' : '?';
  const queryString = query.length ? separator + new URLSearchParams(query).toString() : '';
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
  register: (userData) => post('/frontend/auth/register', userData),

  // 前端用户登录
  login: (credentials) => post('/frontend/auth/login', credentials),

  // 后端管理员登录
  adminLogin: (credentials) => post('/backend/auth/login', credentials),

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

  // 发送验证码
  sendVerificationCode: (data) => post('/auth/verification-code', data),
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
  // 创建作品（multipart）
  createWork: (formData) => upload('/works', formData),

  // 获取作品列表
  // 支持 keyword/sort/category 查询参数，由后端在库内筛选与排序
  getWorks: (params) => get('/works', params),

  // 获取用户作品列表
  // 支持 keyword/sort 查询参数
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

// 管理端作品查询（管理员）
export const adminAPI = {
  // 待审核队列：review_status 为 PENDING 或 NEEDS_REVISION
  // 支持 keyword 查询参数（标题或作者名）
  getPendingWorks: (params) => get('/admin/works/pending', params),
};

// 字帖相关API
export const copybookAPI = {
  // 获取字帖列表（返回分页信封 {content,page,size,total,totalPages}）
  // 支持 keyword/sort/category 等查询参数，由后端在库内筛选
  getCopybooks: (params) => get('/copybooks', params),

  // 保存一份自建字帖（跨设备可见）
  createCopybook: (data) => post('/copybooks', data),

  // 我保存的自建字帖（分页信封）
  getMyCopybooks: (params) => get('/copybooks/mine', params),

  // 获取热门字帖
  getPopularCopybooks: (params) => get('/copybooks/popular', params),

  // 获取字帖详情
  getCopybookDetail: (copybookId) => get(`/copybooks/${copybookId}`),

  // 记录练习历史
  recordPractice: (copybookId, practiceData) => post(`/copybooks/${copybookId}/practice`, practiceData),

  // 获取当前用户的练习历史（分页信封）
  // 支持 from/to/category/keyword 查询参数，由后端在库内筛选
  getPracticeHistory: (params) => get('/copybooks/practice/history', params),

  // 获取用户练习统计
  getUserStatistics: () => get('/copybooks/statistics/user'),
};

// 统计相关API
export const statsAPI = {
  // 首页概览统计
  getOverview: () => get('/stats/overview'),
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
  stats: statsAPI,
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
