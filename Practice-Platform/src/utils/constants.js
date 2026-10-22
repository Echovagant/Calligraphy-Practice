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

// 字帖难度：库里存的是英文枚举，展示时统一翻成中文，
// 免得中文界面里夹着 beginner / intermediate / advanced。
export const DIFFICULTY_LABELS = {
  beginner: '入门',
  intermediate: '进阶',
  advanced: '高级'
};

/**
 * 难度展示文案。
 * @param {string|null} difficulty 原始难度值
 * @param {string} fallback 空值时的兜底文案
 * @returns {string} 未知难度原样返回 —— 练习记录的难度是用户自己填的，不一定是这三个。
 */
export const formatDifficulty = (difficulty, fallback = '未标难度') =>
  DIFFICULTY_LABELS[difficulty] || difficulty || fallback;