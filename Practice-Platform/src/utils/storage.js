/**
 * 本地存储工具函数
 */

/**
 * 从本地存储获取数据
 * @param {string} key - 存储键名
 * @returns {any} 解析后的数据，如果不存在则返回null
 */
export const getStorage = (key) => {
  try {
    const item = localStorage.getItem(key);
    return item ? JSON.parse(item) : null;
  } catch (error) {
    console.error('获取本地存储失败:', error);
    return null;
  }
};

/**
 * 设置本地存储数据
 * @param {string} key - 存储键名
 * @param {any} value - 要存储的数据
 * @returns {boolean} 操作是否成功
 */
export const setStorage = (key, value) => {
  try {
    localStorage.setItem(key, JSON.stringify(value));
    return true;
  } catch (error) {
    console.error('设置本地存储失败:', error);
    return false;
  }
};

/**
 * 从本地存储移除数据
 * @param {string} key - 存储键名
 * @returns {boolean} 操作是否成功
 */
export const removeStorage = (key) => {
  try {
    localStorage.removeItem(key);
    return true;
  } catch (error) {
    console.error('移除本地存储失败:', error);
    return false;
  }
};

/**
 * 清空所有本地存储
 * @returns {boolean} 操作是否成功
 */
export const clearStorage = () => {
  try {
    localStorage.clear();
    return true;
  } catch (error) {
    console.error('清空本地存储失败:', error);
    return false;
  }
};

/**
 * 从会话存储获取数据
 * @param {string} key - 存储键名
 * @returns {any} 解析后的数据，如果不存在则返回null
 */
export const getSessionStorage = (key) => {
  try {
    const item = sessionStorage.getItem(key);
    return item ? JSON.parse(item) : null;
  } catch (error) {
    console.error('获取会话存储失败:', error);
    return null;
  }
};

/**
 * 设置会话存储数据
 * @param {string} key - 存储键名
 * @param {any} value - 要存储的数据
 * @returns {boolean} 操作是否成功
 */
export const setSessionStorage = (key, value) => {
  try {
    sessionStorage.setItem(key, JSON.stringify(value));
    return true;
  } catch (error) {
    console.error('设置会话存储失败:', error);
    return false;
  }
};

/**
 * 移除会话存储数据
 * @param {string} key - 存储键名
 * @returns {boolean} 操作是否成功
 */
export const removeSessionStorage = (key) => {
  try {
    sessionStorage.removeItem(key);
    return true;
  } catch (error) {
    console.error('移除会话存储失败:', error);
    return false;
  }
};

/**
 * 清空所有会话存储
 * @returns {boolean} 操作是否成功
 */
export const clearSessionStorage = () => {
  try {
    sessionStorage.clear();
    return true;
  } catch (error) {
    console.error('清空会话存储失败:', error);
    return false;
  }
};