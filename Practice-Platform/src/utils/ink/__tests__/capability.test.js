import { describe, it, expect, vi, afterEach } from 'vitest';

import { detectPointerCapability, CAPABILITY_MODE } from '../capability.js';

/** 手写的假事件目标：只需要 addEventListener / removeEventListener */
function createFakeTarget() {
  const listeners = new Map();
  return {
    addEventListener(type, handler) {
      if (!listeners.has(type)) listeners.set(type, []);
      listeners.get(type).push(handler);
    },
    removeEventListener(type, handler) {
      const list = listeners.get(type) || [];
      const index = list.indexOf(handler);
      if (index >= 0) list.splice(index, 1);
    },
    dispatch(type, event) {
      (listeners.get(type) || []).slice().forEach((handler) => handler(event));
    },
    listenerCount(type) {
      return (listeners.get(type) || []).length;
    }
  };
}

describe('capability', () => {
  afterEach(() => {
    vi.useRealTimers();
  });

  it('超时（没有任何指针事件）→ 判为 touch 设备', async () => {
    vi.useFakeTimers();
    const target = createFakeTarget();
    const promise = detectPointerCapability(target, { sampleMs: 100 });
    vi.advanceTimersByTime(100);
    const result = await promise;
    expect(result.mode).toBe(CAPABILITY_MODE.TOUCH);
    expect(result.hasPressure).toBe(false);
    expect(result.pointerType).toBe('');
  });

  it('鼠标只报 0.5 压力 → 判为无真压感', async () => {
    vi.useFakeTimers();
    const target = createFakeTarget();
    const promise = detectPointerCapability(target, { sampleMs: 100 });
    target.dispatch('pointerdown', { pointerType: 'mouse', pressure: 0.5 });
    target.dispatch('pointermove', { pointerType: 'mouse', pressure: 0.5 });
    vi.advanceTimersByTime(100);
    const result = await promise;
    expect(result.hasPressure).toBe(false);
    expect(result.pointerType).toBe('mouse');
  });

  it('出现 0.37 这类连续压力值 → 判为有真压感', async () => {
    vi.useFakeTimers();
    const target = createFakeTarget();
    const promise = detectPointerCapability(target, { sampleMs: 100 });
    target.dispatch('pointerdown', { pointerType: 'mouse', pressure: 0.5 });
    target.dispatch('pointermove', { pointerType: 'mouse', pressure: 0.37 });
    vi.advanceTimersByTime(100);
    const result = await promise;
    expect(result.hasPressure).toBe(true);
    expect(result.mode).toBe(CAPABILITY_MODE.POINTER);
  });

  it('pointerType 为 pen → mode = pen', async () => {
    vi.useFakeTimers();
    const target = createFakeTarget();
    const promise = detectPointerCapability(target, { sampleMs: 100 });
    target.dispatch('pointerdown', { pointerType: 'pen', pressure: 0.5 });
    vi.advanceTimersByTime(100);
    const result = await promise;
    expect(result.mode).toBe(CAPABILITY_MODE.PEN);
    expect(result.pointerType).toBe('pen');
  });

  it('缺少 pointerType 时按 mouse 记', async () => {
    vi.useFakeTimers();
    const target = createFakeTarget();
    const promise = detectPointerCapability(target, { sampleMs: 100 });
    target.dispatch('pointerdown', { pressure: 0.5 });
    vi.advanceTimersByTime(100);
    const result = await promise;
    expect(result.pointerType).toBe('mouse');
  });

  it('结束后解绑监听，不留悬挂的处理器', async () => {
    vi.useFakeTimers();
    const target = createFakeTarget();
    const promise = detectPointerCapability(target, { sampleMs: 100 });
    target.dispatch('pointerdown', { pointerType: 'pen', pressure: 0.5 });
    vi.advanceTimersByTime(100);
    await promise;
    expect(target.listenerCount('pointerdown')).toBe(0);
    expect(target.listenerCount('pointermove')).toBe(0);
  });

  it('没有 target 时直接回 touch，不抛错', async () => {
    const result = await detectPointerCapability(null, {});
    expect(result.mode).toBe(CAPABILITY_MODE.TOUCH);
  });
});
