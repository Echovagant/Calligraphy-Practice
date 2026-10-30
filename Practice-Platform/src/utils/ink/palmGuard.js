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
  PEN_ONLY: 'pen-only',
  /**
   * 严格防误触：见过笔之后，触摸**永久**忽略。
   *
   * 「笔优先」只在笔刚用过的那两秒里忽略触摸 —— 过了窗口，
   * 搭在屏上的手掌又开始画线。而 iPad 上写字时手掌是**一直**搭着的，
   * 所以真正的防误触必须记住「这台设备有笔」，而不是记时间。
   */
  PEN_STRICT: 'pen-strict',
  /** 不防误触：笔、触摸、鼠标都照写（手指书写、手掌误触都可能出现） */
  ALL_ALLOWED: 'all-allowed'
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
export function decide(state, event, now = 0, policy = PALM_POLICY.PEN_STRICT) {
  const current = state || createPalmState();
  const pointerType = normalizePointerType(event && event.pointerType);

  // 不防误触：谁碰谁写
  if (policy === PALM_POLICY.ALL_ALLOWED) {
    return {
      action: 'draw',
      nextState: pointerType === 'pen'
        ? { ...current, penSeen: true, penExclusiveUntil: now + PALM_EXCLUSIVE_MS }
        : { ...current }
    };
  }

  // 只认笔：触摸与鼠标一律忽略
  if (policy === PALM_POLICY.PEN_ONLY) {
    if (pointerType !== 'pen') {
      return { action: 'ignore', nextState: { ...current } };
    }
    return {
      action: 'draw',
      nextState: { penSeen: true, penExclusiveUntil: now + PALM_EXCLUSIVE_MS }
    };
  }

  // 笔 → 永远可以画，并记下「这台设备有笔」
  if (pointerType === 'pen') {
    return {
      action: 'draw',
      nextState: { penSeen: true, penExclusiveUntil: now + PALM_EXCLUSIVE_MS }
    };
  }

  if (pointerType === 'touch') {
    if (policy === PALM_POLICY.PEN_STRICT) {
      // 严格防误触：见过笔之后触摸永久忽略 —— 手掌搭在屏上不画线
      if (current.penSeen) {
        return { action: 'ignore', nextState: { ...current } };
      }
      return { action: 'draw', nextState: { ...current } };
    }
    // pen-first（宽松）：只在笔刚用过的一段时间里忽略触摸
    if (current.penSeen && now < current.penExclusiveUntil) {
      return { action: 'ignore', nextState: { ...current } };
    }
    return { action: 'draw', nextState: { ...current } };
  }

  // mouse 永远可以画
  return { action: 'draw', nextState: { ...current } };
}
