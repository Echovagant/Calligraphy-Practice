import { describe, it, expect } from 'vitest';

import { summarizePressure, roundPressure } from '../pressureStats.js';

describe('summarizePressure', () => {
  it('无样本 → 空摘要，realPressureLikely 为 null', () => {
    const r = summarizePressure([]);
    expect(r.count).toBe(0);
    expect(r.realPressureLikely).toBeNull();
  });

  it('非数组入参 → 空摘要', () => {
    expect(summarizePressure(undefined).count).toBe(0);
    expect(summarizePressure(null).count).toBe(0);
  });

  it('鼠标式模拟压感（只报 0/0.5/1）→ 判为非真实压感', () => {
    const r = summarizePressure([
      { pressure: 0 },
      { pressure: 0.5 },
      { pressure: 0.5 },
      { pressure: 1 }
    ]);
    expect(r.count).toBe(4);
    expect(r.distinct).toEqual([0, 0.5, 1]);
    expect(r.nonRealOnly).toBe(true);
    expect(r.realPressureLikely).toBe(false);
  });

  it('真笔式连续压感 → 判为真实压感', () => {
    const r = summarizePressure([{ pressure: 0.12 }, { pressure: 0.34 }, { pressure: 0.87 }]);
    expect(r.realPressureLikely).toBe(true);
    expect(r.min).toBe(0.12);
    expect(r.max).toBe(0.87);
  });

  it('0.499999 与 0.5 记为同一个值', () => {
    const r = summarizePressure([{ pressure: 0.499999 }, { pressure: 0.5 }]);
    expect(r.distinctCount).toBe(1);
  });

  it('roundPressure 保留两位小数', () => {
    expect(roundPressure(0.123456)).toBe(0.12);
    expect(roundPressure(0.9999)).toBe(1);
  });

  it('缺失/非有限压感的样本被忽略', () => {
    const r = summarizePressure([{ pressure: 'x' }, {}, { pressure: Number.NaN }]);
    expect(r.count).toBe(0);
    expect(r.realPressureLikely).toBeNull();
  });

  it('distinct 超出展示上限时截断但不影响 distinctCount', () => {
    const samples = [];
    for (let i = 0; i < 30; i++) {
      samples.push({ pressure: i / 100 });
    }
    const r = summarizePressure(samples);
    expect(r.distinctCount).toBe(30);
    expect(r.distinct.length).toBeLessThanOrEqual(16);
  });
});
