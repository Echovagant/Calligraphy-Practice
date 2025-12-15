import request from '../utils/request'

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
  return request({
    url: '/api/discussions',
    method: 'get',
    params
  })
}

/**
 * 获取讨论详情
 * @param {number} id - 讨论ID
 * @returns {Promise}
 */
export const getDiscussion = (id) => {
  return request({
    url: `/api/discussions/${id}`,
    method: 'get'
  })
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
  return request({
    url: '/api/discussions',
    method: 'post',
    data
  })
}

/**
 * 更新讨论
 * @param {number} id - 讨论ID
 * @param {Object} data - 更新数据
 * @returns {Promise}
 */
export const updateDiscussion = (id, data) => {
  return request({
    url: `/api/discussions/${id}`,
    method: 'put',
    data
  })
}

/**
 * 删除讨论
 * @param {number} id - 讨论ID
 * @returns {Promise}
 */
export const deleteDiscussion = (id) => {
  return request({
    url: `/api/discussions/${id}`,
    method: 'delete'
  })
}

/**
 * 点赞/取消点赞讨论
 * @param {number} id - 讨论ID
 * @returns {Promise}
 */
export const likeDiscussion = (id) => {
  return request({
    url: `/api/discussions/${id}/like`,
    method: 'post'
  })
}

/**
 * 获取讨论回复列表
 * @param {number} discussionId - 讨论ID
 * @param {Object} params - 查询参数
 * @returns {Promise}
 */
export const getDiscussionReplies = (discussionId, params) => {
  return request({
    url: `/api/discussions/${discussionId}/replies`,
    method: 'get',
    params
  })
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
  return request({
    url: `/api/discussions/${discussionId}/replies`,
    method: 'post',
    data
  })
}

/**
 * 更新讨论回复
 * @param {number} replyId - 回复ID
 * @param {Object} data - 更新数据
 * @returns {Promise}
 */
export const updateDiscussionReply = (replyId, data) => {
  return request({
    url: `/api/discussions/replies/${replyId}`,
    method: 'put',
    data
  })
}

/**
 * 删除讨论回复
 * @param {number} replyId - 回复ID
 * @returns {Promise}
 */
export const deleteDiscussionReply = (replyId) => {
  return request({
    url: `/api/discussions/replies/${replyId}`,
    method: 'delete'
  })
}

/**
 * 获取热门讨论
 * @param {Object} params - 查询参数
 * @returns {Promise}
 */
export const getHotDiscussions = (params) => {
  return request({
    url: '/api/discussions/hot',
    method: 'get',
    params
  })
}

/**
 * 获取精华讨论
 * @param {Object} params - 查询参数
 * @returns {Promise}
 */
export const getFeaturedDiscussions = (params) => {
  return request({
    url: '/api/discussions/featured',
    method: 'get',
    params
  })
}

/**
 * 获取置顶讨论
 * @param {Object} params - 查询参数
 * @returns {Promise}
 */
export const getPinnedDiscussions = (params) => {
  return request({
    url: '/api/discussions/pinned',
    method: 'get',
    params
  })
}

/**
 * 搜索讨论
 * @param {string} keyword - 搜索关键词
 * @param {Object} params - 其他参数
 * @returns {Promise}
 */
export const searchDiscussions = (keyword, params = {}) => {
  return request({
    url: '/api/discussions/search',
    method: 'get',
    params: {
      keyword,
      ...params
    }
  })
}