/**
 * localStorage 可用容量实测：逐块写入直到抛 QuotaExceededError。
 *
 * storage 参数可注入（浏览器传 localStorage，单测传假对象）。
 * 探测以多个 key 进行（每块默认 256KB），总量封顶默认 20MB；
 * 无论成败都会在 finally 里清掉探测 key，不动用户真实数据。
 */

export const QUOTA_CHUNK_BYTES = 256 * 1024;
export const QUOTA_MAX_BYTES = 20 * 1024 * 1024;
const KEY_PREFIX = '__device_lab_quota_';

function describeError(e) {
  return {
    errorName: e && e.name ? String(e.name) : String(e),
    error: e && e.message ? String(e.message).slice(0, 200) : null
  };
}

/**
 * @param {Storage} storage
 * @param {number} chunkBytes 每块字节数
 * @param {number} maxBytes 探测总量上限
 * @returns {{ok: boolean, bytes: number, chunks: number, hitBudget: boolean,
 *            errorName: string|null, error: string|null}}
 *   ok=false 表示连第一块都写不进（如隐私模式的 SecurityError）；
 *   hitBudget=true 表示写到探测上限仍未报错，真实容量 >= bytes。
 */
export function measureStorageQuota(
  storage,
  chunkBytes = QUOTA_CHUNK_BYTES,
  maxBytes = QUOTA_MAX_BYTES
) {
  const chunk = 'x'.repeat(chunkBytes);
  const keys = [];
  let failure = null;
  try {
    while (keys.length * chunkBytes < maxBytes) {
      const key = `${KEY_PREFIX}${keys.length}`;
      storage.setItem(key, chunk);
      keys.push(key);
    }
  } catch (e) {
    failure = e;
  } finally {
    for (const key of keys) {
      try {
        storage.removeItem(key);
      } catch {
        // 清理失败不影响测量结果
      }
    }
  }
  const bytes = keys.length * chunkBytes;
  const hitBudget = failure === null;
  if (bytes === 0) {
    return { ok: false, bytes: 0, chunks: 0, hitBudget: false, ...describeError(failure) };
  }
  return {
    ok: true,
    bytes,
    chunks: keys.length,
    hitBudget,
    ...(failure ? describeError(failure) : { errorName: null, error: null })
  };
}
