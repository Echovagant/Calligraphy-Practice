import { describe, it, expect } from 'vitest';

import {
  PALM_POLICY,
  PALM_EXCLUSIVE_MS,
  createPalmState,
  normalizePointerType,
  decide
} from '../palmGuard.js';

describe('palmGuard', () => {
  describe('normalizePointerType', () => {
    it('缺失的 pointerType 按 mouse 处理', () => {
      expect(normalizePointerType(undefined)).toBe('mouse');
      expect(normalizePointerType('')).toBe('mouse');
      expect(normalizePointerType('pen')).toBe('pen');
    });
  });

  describe('pen-first（默认，iPad）', () => {
    it('笔 → 画，并记下见过笔', () => {
      const result = decide(createPalmState(), { pointerType: 'pen' }, 1000, PALM_POLICY.PEN_FIRST);
      expect(result.action).toBe('draw');
      expect(result.nextState.penSeen).toBe(true);
      expect(result.nextState.penExclusiveUntil).toBe(1000 + PALM_EXCLUSIVE_MS);
    });

    it('见过笔之后短时间内触摸 → 忽略（手掌搭屏）', () => {
      const state = { penSeen: true, penExclusiveUntil: 3000 };
      const result = decide(state, { pointerType: 'touch' }, 2000, PALM_POLICY.PEN_FIRST);
      expect(result.action).toBe('ignore');
    });

    it('见过笔但超过排他窗口 → 触摸可以画', () => {
      const state = { penSeen: true, penExclusiveUntil: 3000 };
      const result = decide(state, { pointerType: 'touch' }, 5000, PALM_POLICY.PEN_FIRST);
      expect(result.action).toBe('draw');
    });

    it('从未见过笔时触摸可以画（没带笔用手指写）', () => {
      const result = decide(createPalmState(), { pointerType: 'touch' }, 1000, PALM_POLICY.PEN_FIRST);
      expect(result.action).toBe('draw');
      expect(result.nextState.penSeen).toBe(false);
    });

    it('鼠标永远可以画', () => {
      const result = decide(createPalmState(), { pointerType: 'mouse' }, 1000, PALM_POLICY.PEN_FIRST);
      expect(result.action).toBe('draw');
    });

    it('状态对象不被就地修改', () => {
      const state = createPalmState();
      decide(state, { pointerType: 'pen' }, 1000, PALM_POLICY.PEN_FIRST);
      expect(state.penSeen).toBe(false);
    });
  });

  describe('touch-only（手机布局）', () => {
    it('触摸 → 忽略', () => {
      const result = decide(createPalmState(), { pointerType: 'touch' }, 1000, PALM_POLICY.TOUCH_ONLY);
      expect(result.action).toBe('ignore');
    });

    it('鼠标 → 忽略', () => {
      const result = decide(createPalmState(), { pointerType: 'mouse' }, 1000, PALM_POLICY.TOUCH_ONLY);
      expect(result.action).toBe('ignore');
    });

    it('笔 → 画', () => {
      const result = decide(createPalmState(), { pointerType: 'pen' }, 1000, PALM_POLICY.TOUCH_ONLY);
      expect(result.action).toBe('draw');
    });

    it('pointerType 缺失也按鼠标处理 → 忽略', () => {
      const result = decide(createPalmState(), {}, 1000, PALM_POLICY.TOUCH_ONLY);
      expect(result.action).toBe('ignore');
    });
  });
});
