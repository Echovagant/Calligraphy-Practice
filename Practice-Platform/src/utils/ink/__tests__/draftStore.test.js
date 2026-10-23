import { describe, it, expect, vi, afterEach } from 'vitest';

import { createDraftStore, draftKey, DRAFT_NAMESPACE_PREFIX } from '../draftStore.js';

function createFakeStorage() {
  const data = new Map();
  return {
    data,
    getItem(key) {
      return data.has(key) ? data.get(key) : null;
    },
    setItem(key, value) {
      data.set(key, String(value));
    },
    removeItem(key) {
      data.delete(key);
    }
  };
}

describe('draftStore', () => {
  afterEach(() => {
    vi.useRealTimers();
  });

  describe('draftKey', () => {
    it('具名用户 + 字帖', () => {
      expect(draftKey({ userId: 42, copybookId: 'abc' })).toBe(`${DRAFT_NAMESPACE_PREFIX}:42:abc`);
    });

    it('未登录用 anon', () => {
      expect(draftKey({})).toBe(`${DRAFT_NAMESPACE_PREFIX}:anon:default`);
    });
  });

  describe('debounce 落盘', () => {
    it('500ms 内的多次保存只落一次盘', () => {
      vi.useFakeTimers();
      const storage = createFakeStorage();
      const store = createDraftStore({ storage, namespace: 'ink-draft:anon:test', debounceMs: 500 });

      store.save({ version: 1, strokes: [] });
      store.save({ version: 1, strokes: [{ id: 1 }] });
      expect(storage.data.size).toBe(0);

      vi.advanceTimersByTime(500);
      expect(storage.data.size).toBe(1);
      expect(JSON.parse(storage.getItem('ink-draft:anon:test')).strokes).toHaveLength(1);
    });

    it('immediate 立刻落盘', () => {
      vi.useFakeTimers();
      const storage = createFakeStorage();
      const store = createDraftStore({ storage, namespace: 'k', debounceMs: 500 });
      store.save({ version: 1, strokes: [{ id: 9 }] }, { immediate: true });
      expect(storage.data.size).toBe(1);
    });

    it('flush 把挂起的写入立刻落盘', () => {
      vi.useFakeTimers();
      const storage = createFakeStorage();
      const store = createDraftStore({ storage, namespace: 'k', debounceMs: 500 });
      store.save({ version: 1, strokes: [{ id: 3 }] });
      expect(storage.data.size).toBe(0);
      store.flush();
      expect(storage.data.size).toBe(1);
    });
  });

  describe('恢复', () => {
    it('能读回自己写的草稿', () => {
      const storage = createFakeStorage();
      const store = createDraftStore({ storage, namespace: 'k' });
      const payload = { version: 1, strokes: [{ id: 1 }] };
      store.save(payload, { immediate: true });
      expect(store.restore()).toEqual(payload);
    });

    it('没有草稿时返回 null', () => {
      const store = createDraftStore({ storage: createFakeStorage(), namespace: 'k' });
      expect(store.restore()).toBeNull();
    });

    it('数据损坏时返回 null 而不是抛错', () => {
      const storage = createFakeStorage();
      storage.setItem('k', '{bad json');
      const store = createDraftStore({ storage, namespace: 'k' });
      expect(store.restore()).toBeNull();
    });
  });

  describe('命名空间迁移（匿名 → 具名）', () => {
    it('草稿跟着账号走', () => {
      const storage = createFakeStorage();
      // 迁移的语义是「把当前命名空间里的草稿搬到新命名空间」，
      // 所以先在匿名命名空间里写，再迁移。
      const store = createDraftStore({ storage, namespace: draftKey({}) });
      store.save({ version: 1, strokes: [{ id: 7 }] }, { immediate: true });

      const namedKey = draftKey({ userId: 42, copybookId: 'x' });
      store.migrate(namedKey);

      expect(store.namespace).toBe(namedKey);
      expect(JSON.parse(storage.getItem(namedKey)).strokes).toHaveLength(1);
    });

    it('迁移后旧命名空间的数据仍可读（不主动删用户数据）', () => {
      const storage = createFakeStorage();
      const store = createDraftStore({ storage, namespace: draftKey({}) });
      store.save({ version: 1, strokes: [{ id: 7 }] }, { immediate: true });
      store.migrate(draftKey({ userId: 42, copybookId: 'x' }));
      expect(storage.getItem(draftKey({}))).not.toBeNull();
    });
  });

  describe('clear', () => {
    it('清掉当前命名空间的草稿', () => {
      const storage = createFakeStorage();
      const store = createDraftStore({ storage, namespace: 'k' });
      store.save({ version: 1, strokes: [{ id: 1 }] }, { immediate: true });
      store.clear();
      expect(storage.getItem('k')).toBeNull();
    });
  });

  describe('storage 不可用时', () => {
    it('写入失败不抛错', () => {
      const broken = {
        getItem() {
          throw new Error('denied');
        },
        setItem() {
          throw new Error('quota');
        },
        removeItem() {
          throw new Error('denied');
        }
      };
      const store = createDraftStore({ storage: broken, namespace: 'k' });
      expect(() => store.save({ version: 1, strokes: [] }, { immediate: true })).not.toThrow();
      expect(store.restore()).toBeNull();
    });
  });
});
