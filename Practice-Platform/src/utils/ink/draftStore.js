/**
 * 草稿自动保存：骨架落 storage，切后台 / 关页面不丢笔迹。
 *
 * 命名空间：`ink-draft:{userId}:{copybookId}`（未登录时用 `ink-draft:anon`）。
 * 登录态变化时把草稿从匿名命名空间迁移到具名命名空间。
 *
 * storage 与定时器都可注入，单元测试里不需要真实 DOM。
 */

export const DRAFT_NAMESPACE_PREFIX = 'ink-draft';

export function draftKey({ userId = null, copybookId = null } = {}) {
  const scope = userId ? String(userId) : 'anon';
  return `${DRAFT_NAMESPACE_PREFIX}:${scope}:${copybookId || 'default'}`;
}

export function createDraftStore({
  storage = globalThis.localStorage,
  namespace = draftKey({}),
  debounceMs = 500,
  setTimeoutFn = globalThis.setTimeout ? globalThis.setTimeout.bind(globalThis) : null,
  clearTimeoutFn = globalThis.clearTimeout ? globalThis.clearTimeout.bind(globalThis) : null
} = {}) {
  let currentNamespace = namespace;
  let timer = null;
  let pending = null;

  const write = (payload) => {
    if (!storage || payload === null || payload === undefined) return false;
    try {
      storage.setItem(currentNamespace, JSON.stringify(payload));
      return true;
    } catch {
      // 配额满 / 隐私模式禁写：静默失败，不能打断书写
      return false;
    }
  };

  const cancelTimer = () => {
    if (timer !== null && clearTimeoutFn) {
      clearTimeoutFn(timer);
      timer = null;
    }
  };

  const schedule = (payload) => {
    pending = payload;
    cancelTimer();
    if (!setTimeoutFn) {
      // 没有定时器（极简环境）就直接落盘
      write(pending);
      pending = null;
      return;
    }
    timer = setTimeoutFn(() => {
      timer = null;
      const data = pending;
      pending = null;
      write(data);
    }, debounceMs);
  };

  return {
    get namespace() {
      return currentNamespace;
    },

    /** 保存草稿；默认 debounce，立即落盘用 immediate */
    save(payload, { immediate = false } = {}) {
      if (immediate) {
        cancelTimer();
        pending = null;
        return write(payload);
      }
      schedule(payload);
      return true;
    },

    /** 把挂起的写入立刻落盘（切后台 / 离开页面时调用） */
    flush() {
      cancelTimer();
      if (pending === null) return false;
      const data = pending;
      pending = null;
      return write(data);
    },

    /** 读取草稿；没有或损坏时返回 null */
    restore() {
      if (!storage) return null;
      try {
        const raw = storage.getItem(currentNamespace);
        if (!raw) return null;
        return JSON.parse(raw);
      } catch {
        return null;
      }
    },

    /** 迁移命名空间（匿名 → 具名），返回迁移前的草稿 */
    migrate(nextNamespace) {
      const payload = this.restore();
      this.flush();
      currentNamespace = nextNamespace || currentNamespace;
      if (payload) write(payload);
      return payload;
    },

    clear() {
      cancelTimer();
      pending = null;
      if (!storage) return;
      try {
        storage.removeItem(currentNamespace);
      } catch {
        // 忽略
      }
    }
  };
}
