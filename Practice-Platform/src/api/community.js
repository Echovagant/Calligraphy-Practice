import { get, post, put, del } from '../utils/api'

/**
 * 社区讨论相关接口。
 *
 * 之前这里用的是 `../utils/request`（一个 axios 风格的独立客户端），
 * 但那个文件从来不存在，导致 `npm run build` 直接失败：
 *   Could not resolve "../utils/request" from "src/api/community.js"
 * 现在统一复用 utils/api.js 的客户端（fetch + 统一鉴权 + 401 自动刷新），
 * 路径也去掉重复的 /api 前缀（base 里已经带 /api）。
 */

/**
 * 获取讨论列表
 * @param {Object} params - 查询参数
 * @param {number} params.page - 页码 (从0开始)
 * @param {number} params.size - 每页数量
 * @param {string} params.category - 分类
 * @param {string} params.keyword - 关键词
 * @param {string} params.sort - 排序方式
 * @returns {Promise}
 */
export const getDiscussions = (params) => {
  return get('/discussions', params)
}

/**
 * 获取讨论详情
 * @param {number} id - 讨论ID
 * @returns {Promise}
 */
export const getDiscussion = (id) => {
  return get(`/discussions/${id}`)
}

/**
 * 创建讨论
 * @param {Object} data - 讨论数据
 * @param {string} data.title - 标题
 * @param {string} data.content - 内容
 * @param {string} data.category - 分类
 * @param {string} data.tags - 标签
 * @returns {Promise}
 */
export const createDiscussion = (data) => {
  return post('/discussions', data)
}

/**
 * 更新讨论
 * @param {number} id - 讨论ID
 * @param {Object} data - 更新数据
 * @returns {Promise}
 */
export const updateDiscussion = (id, data) => {
  return put(`/discussions/${id}`, data)
}

/**
 * 删除讨论
 * @param {number} id - 讨论ID
 * @returns {Promise}
 */
export const deleteDiscussion = (id) => {
  return del(`/discussions/${id}`)
}

/**
 * 点赞/取消点赞讨论
 * @param {number} id - 讨论ID
 * @returns {Promise}
 */
export const likeDiscussion = (id) => {
  return post(`/discussions/${id}/like`)
}

/**
 * 获取讨论回复列表
 * @param {number} discussionId - 讨论ID
 * @param {Object} params - 查询参数
 * @returns {Promise}
 */
export const getDiscussionReplies = (discussionId, params) => {
  return get(`/discussions/${discussionId}/replies`, params)
}

/**
 * 创建讨论回复
 * @param {number} discussionId - 讨论ID
 * @param {Object} data - 回复数据
 * @param {string} data.content - 回复内容
 * @param {number} data.parentId - 父回复ID（可选）
 * @param {number} data.replyToUserId - 被回复用户ID（可选）
 * @param {string} data.replyToUsername - 被回复用户名（可选）
 * @returns {Promise}
 */
export const replyToDiscussion = (discussionId, data) => {
  return post(`/discussions/${discussionId}/replies`, data)
}

/**
 * 更新讨论回复
 * @param {number} replyId - 回复ID
 * @param {Object} data - 更新数据
 * @returns {Promise}
 */
export const updateDiscussionReply = (replyId, data) => {
  return put(`/discussions/replies/${replyId}`, data)
}

/**
 * 删除讨论回复
 * @param {number} replyId - 回复ID
 * @returns {Promise}
 */
export const deleteDiscussionReply = (replyId) => {
  return del(`/discussions/replies/${replyId}`)
}

/**
 * 获取热门讨论
 * @param {Object} params - 查询参数
 * @returns {Promise}
 */
export const getHotDiscussions = (params) => {
  return get('/discussions/hot', params)
}

/**
 * 获取精华讨论
 * @param {Object} params - 查询参数
 * @returns {Promise}
 */
export const getFeaturedDiscussions = (params) => {
  return get('/discussions/featured', params)
}

/**
 * 获取置顶讨论
 * @param {Object} params - 查询参数
 * @returns {Promise}
 */
export const getPinnedDiscussions = (params) => {
  return get('/discussions/pinned', params)
}

/**
 * 搜索讨论
 * @param {string} keyword - 搜索关键词
 * @param {Object} params - 其他参数
 * @returns {Promise}
 */
export const searchDiscussions = (keyword, params = {}) => {
  return get('/discussions/search', { keyword, ...params })
}
