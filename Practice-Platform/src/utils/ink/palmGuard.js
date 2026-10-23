/**
 * 手掌误触状态机。
 *
 * 纯函数 + 显式状态，不依赖 DOM。
 * Apple Pencil 书写时手掌搭在屏上不应该画出线条；触摸端在没有笔时
 * 仍然可以用手指写（「pen-first」策略）。
 */

export const PALM_POLICY = {
  /** 默认：iPad。有笔优先，见过笔后短时间内忽略触摸 */
  PEN_FIRST: 'pen-first',
  /** 手机布局：只允许笔，触摸与鼠标一律忽略 */
  TOUCH_ONLY: 'touch-only'
};

/** 见过笔之后，多长时间内忽略触摸 */
export const PALM_EXCLUSIVE_MS = 2000;

export function createPalmState() {
  return { penSeen: false, penExclusiveUntil: 0 };
}

/** pointerType 缺失按 mouse 处理 */
export function normalizePointerType(pointerType) {
  return pointerType || 'mouse';
}

/**
 * 决定一个指针事件要不要落笔。
 * @param {{penSeen: boolean, penExclusiveUntil: number}} state
 * @param {{pointerType?: string}} event
 * @param {number} now 当前时间戳（毫秒）
 * @param {string} policy PALM_POLICY 之一
 * @returns {{action: 'draw' | 'ignore', nextState: object}}
 */
export function decide(state, event, now = 0, policy = PALM_POLICY.PEN_FIRST) {
  const current = state || createPalmState();
  const pointerType = normalizePointerType(event && event.pointerType);

  if (policy === PALM_POLICY.TOUCH_ONLY) {
    if (pointerType !== 'pen') {
      return { action: 'ignore', nextState: { ...current } };
    }
    return {
      action: 'draw',
      nextState: { penSeen: true, penExclusiveUntil: now + PALM_EXCLUSIVE_MS }
    };
  }

  // pen-first（默认，iPad）
  if (pointerType === 'pen') {
    return {
      action: 'draw',
      nextState: { penSeen: true, penExclusiveUntil: now + PALM_EXCLUSIVE_MS }
    };
  }
  if (pointerType === 'touch') {
    // 刚见过笔 → 手掌搭上来的触摸，忽略
    if (current.penSeen && now < current.penExclusiveUntil) {
      return { action: 'ignore', nextState: { ...current } };
    }
    // 从没见过笔 → 没带笔时用手指写
    return { action: 'draw', nextState: { ...current } };
  }
  // mouse 永远可以画
  return { action: 'draw', nextState: { ...current } };
}
