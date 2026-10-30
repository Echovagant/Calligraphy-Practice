import { describe, it, expect } from 'vitest';

import { measureStorageQuota } from '../storageQuota.js';

/** 按字节封顶的假 localStorage（setItem 超额抛 QuotaExceededError） */
function createCappedStorage(capBytes) {
  const data = new Map();
  let used = 0;
  return {
    data,
    setItem(key, value) {
      const v = String(value);
      if (data.has(key)) {
        used -= data.get(key).length;
      }
      if (used + v.length > capBytes) {
        const err = new Error('The quota has been exceeded.');
        err.name = 'QuotaExceededError';
        throw err;
      }
      data.set(key, v);
      used += v.length;
    },
    getItem(key) {
      return data.has(key) ? data.get(key) : null;
    },
    removeItem(key) {
      if (data.has(key)) {
        used -= data.get(key).length;
        data.delete(key);
      }
    }
  };
}

describe('measureStorageQuota', () => {
  it('容量 25B、每块 10B → 存下 2 块后报额满', () => {
    const r = measureStorageQuota(createCappedStorage(25), 10, 1000);
    expect(r.ok).toBe(true);
    expect(r.bytes).toBe(20);
    expect(r.chunks).toBe(2);
    expect(r.hitBudget).toBe(false);
    expect(r.errorName).toBe('QuotaExceededError');
  });

  it('写到探测上限仍未报错 → hitBudget=true，bytes 是下界', () => {
    const r = measureStorageQuota(createCappedStorage(1_000_000), 10, 25);
    expect(r.ok).toBe(true);
    expect(r.bytes).toBe(30);
    expect(r.hitBudget).toBe(true);
    expect(r.errorName).toBeNull();
  });

  it('第一块就写不进 → ok=false 且带错误名', () => {
    const r = measureStorageQuota(createCappedStorage(5), 10, 1000);
    expect(r.ok).toBe(false);
    expect(r.bytes).toBe(0);
    expect(r.errorName).toBe('QuotaExceededError');
  });

  it('探测后清理干净，不留 key', () => {
    const storage = createCappedStorage(100);
    measureStorageQuota(storage, 10, 100);
    expect(storage.data.size).toBe(0);
  });

  it('不动用户已有的数据', () => {
    const storage = createCappedStorage(1000);
    storage.setItem('user_draft', 'hello');
    measureStorageQuota(storage, 10, 100);
    expect(storage.getItem('user_draft')).toBe('hello');
  });
});
