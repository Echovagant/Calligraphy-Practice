// 用户类型常量
export const USER_TYPES = {
  FRONTEND: 'USER',
  BACKEND: 'ADMIN'
};

// API基础配置
export const API_BASE_URL = import.meta.env.VITE_APP_BASE_API || 'http://localhost:8083/api';

// 会话超时时间（30分钟）
export const SESSION_TIMEOUT = 30 * 60 * 1000;

// 存储键名常量
export const STORAGE_KEYS = {
  AUTH_TOKEN: 'authToken',
  REFRESH_TOKEN: 'refreshToken',
  USER_INFO: 'userInfo',
  USER_TYPE: 'userType'
};

// 用户角色常量
export const USER_ROLES = {
  USER: 'USER',
  ADMIN: 'ADMIN',
  SUPER_ADMIN: 'SUPER_ADMIN'
};

// 作品状态常量
export const WORK_STATUS = {
  DRAFT: 'draft',
  PUBLISHED: 'published',
  HIDDEN: 'hidden'
};

// 评论状态常量
export const COMMENT_STATUS = {
  PENDING: 'pending',
  APPROVED: 'approved',
  REJECTED: 'rejected'
};

// 审核状态常量
export const REVIEW_STATUS = {
  PENDING: 'pending',
  APPROVED: 'approved',
  REJECTED: 'rejected'
};