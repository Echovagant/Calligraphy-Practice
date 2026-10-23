import { describe, it, expect } from 'vitest';

import { createStrokeStore, STROKE_STORE_VERSION } from '../strokeStore.js';

const makeStroke = (x = 0) => ({
  points: [
    { x, y: 0, p: 0.5, t: 0 },
    { x: x + 10, y: 0, p: 0.6, t: 10 }
  ],
  widths: [8, 9],
  color: '#000000',
  opacity: 1
});

describe('strokeStore', () => {
  it('落笔后笔画数 +1', () => {
    const store = createStrokeStore();
    expect(store.size).toBe(0);
    expect(store.push(makeStroke())).toBe(true);
    expect(store.size).toBe(1);
    expect(store.canUndo).toBe(true);
  });

  it('空笔画不入栈', () => {
    const store = createStrokeStore();
    expect(store.push({ points: [], widths: [] })).toBe(false);
    expect(store.push(null)).toBe(false);
    expect(store.size).toBe(0);
  });

  it('撤销取回最后一笔', () => {
    const store = createStrokeStore();
    store.push(makeStroke(0));
    store.push(makeStroke(20));
    const removed = store.undo();
    expect(removed.points[0].x).toBe(20);
    expect(store.size).toBe(1);
    expect(store.canRedo).toBe(true);
  });

  it('撤销到空后返回 null', () => {
    const store = createStrokeStore();
    expect(store.undo()).toBeNull();
  });

  it('重做恢复被撤销的笔画', () => {
    const store = createStrokeStore();
    store.push(makeStroke(0));
    store.push(makeStroke(20));
    store.undo();
    const restored = store.redo();
    expect(restored.points[0].x).toBe(20);
    expect(store.size).toBe(2);
    expect(store.canRedo).toBe(false);
  });

  it('新落笔会清空重做栈', () => {
    const store = createStrokeStore();
    store.push(makeStroke(0));
    store.undo();
    expect(store.canRedo).toBe(true);
    store.push(makeStroke(40));
    expect(store.canRedo).toBe(false);
  });

  it('清空同时清掉两栈', () => {
    const store = createStrokeStore();
    store.push(makeStroke(0));
    store.undo();
    store.clear();
    expect(store.size).toBe(0);
    expect(store.canUndo).toBe(false);
    expect(store.canRedo).toBe(false);
  });

  it('序列化 → 恢复 往返一致（骨架不变形）', () => {
    const store = createStrokeStore();
    store.push(makeStroke(0));
    store.push(makeStroke(20));
    const snapshot = store.serialize();
    expect(snapshot.version).toBe(STROKE_STORE_VERSION);

    const restored = createStrokeStore();
    expect(restored.restore(JSON.stringify(snapshot))).toBe(true);
    expect(restored.size).toBe(2);
    expect(restored.strokes[1].points[0].x).toBe(20);
    expect(restored.strokes[1].widths).toEqual([8, 9]);
  });

  it('恢复非法数据时保持现状并返回 false', () => {
    const store = createStrokeStore();
    store.push(makeStroke(0));
    expect(store.restore('not json')).toBe(false);
    expect(store.restore({ nope: 1 })).toBe(false);
    expect(store.size).toBe(1);
  });

  it('恢复时丢弃没有采样点的空笔画', () => {
    const store = createStrokeStore();
    store.restore({ strokes: [makeStroke(0), { points: [] }] });
    expect(store.size).toBe(1);
  });

  it('骨架存的是点，不是轮廓路径 —— 体积与笔画数无关于路径复杂度', () => {
    const store = createStrokeStore();
    store.push(makeStroke(0));
    const snapshot = store.serialize();
    expect(snapshot.strokes[0]).not.toHaveProperty('path');
    expect(snapshot.strokes[0]).not.toHaveProperty('d');
  });
});
