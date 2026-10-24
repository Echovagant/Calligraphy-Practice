import { describe, it, expect } from 'vitest';
import { matchStroke, transformStroke, identifyStroke } from '../strokeMatch.js';

/** 一条典型的横：从 (200,300) 走到 (800,300) */
const HENG = [[200, 300], [400, 300], [600, 300], [800, 300]];
/** 一条典型的竖：从 (500,200) 走到 (500,800) */
const SHU = [[500, 200], [500, 400], [500, 600], [500, 800]];

describe('transformStroke', () => {
  it('把 1024 字形画布坐标缩放平移到画布坐标', () => {
    const frame = { x: 100, y: 50, size: 512 };
    const out = transformStroke([[0, 0], [1024, 1024]], frame);
    expect(out[0]).toEqual([100, 50]);
    expect(out[1]).toEqual([612, 562]);
  });

  it('空轨迹返回空数组', () => {
    expect(transformStroke([], { x: 0, y: 0, size: 100 })).toEqual([]);
  });
});

describe('matchStroke', () => {
  it('照着轨迹写 → 合格', () => {
    const r = matchStroke(HENG, [[205, 305], [500, 302], [795, 298]]);
    expect(r.ok).toBe(true);
    expect(r.reason).toBe('这一笔写对了');
  });

  it('完全一致时偏差为 0', () => {
    const r = matchStroke(HENG, HENG);
    expect(r.ok).toBe(true);
    expect(r.deviation).toBe(0);
    expect(r.angle).toBe(0);
  });

  it('落笔位置偏了 → 提示落笔位置', () => {
    // 起点偏 300px，超过 0.12*1024≈123 的容差
    const r = matchStroke(HENG, [[500, 300], [800, 300]]);
    expect(r.ok).toBe(false);
    expect(r.reason).toContain('落笔位置');
    expect(r.startDist).toBeCloseTo(300, 5);
  });

  it('方向写反了 → 提示方向', () => {
    const r = matchStroke(HENG, [[800, 300], [600, 300], [400, 300], [200, 300]]);
    expect(r.ok).toBe(false);
    expect(r.reversed).toBe(true);
    expect(r.reason).toContain('方向反了');
  });

  it('走向偏了（角度差过大）→ 提示走向', () => {
    // 起点对，终点方向偏 90 度
    const r = matchStroke(HENG, [[200, 300], [500, 700]]);
    expect(r.ok).toBe(false);
    expect(r.reason).toContain('走向');
  });

  it('收笔位置偏了 → 提示收笔位置', () => {
    // 起点对、方向对，但停得太早
    const r = matchStroke(HENG, [[200, 300], [500, 300]]);
    expect(r.ok).toBe(false);
    expect(r.reason).toContain('收笔位置');
  });

  it('形状偏离太远 → 提示偏离', () => {
    // 起点、终点、方向都对，但中段拱出轨道
    const r = matchStroke(HENG, [[200, 300], [500, 950], [800, 300]]);
    expect(r.ok).toBe(false);
    expect(r.reason).toContain('偏离');
  });

  it('还没写 → 提示还没写', () => {
    const r = matchStroke(HENG, []);
    expect(r.ok).toBe(false);
    expect(r.reason).toContain('还没有画出');
  });

  it('目标无轨迹 → 提示目标笔无轨迹', () => {
    const r = matchStroke([[]], [[0, 0], [1, 1]]);
    expect(r.ok).toBe(false);
    expect(r.reason).toContain('目标笔无轨迹');
  });

  it('容差可以放大', () => {
    const off = [[500, 300], [800, 300]];
    expect(matchStroke(HENG, off).ok).toBe(false);
    expect(matchStroke(HENG, off, { startToleranceRatio: 0.5, deviationToleranceRatio: 0.5 }).ok).toBe(true);
  });

  it('size 影响容差（字越小，同样的像素偏移越容不下）', () => {
    const off = [[250, 300], [800, 300]];   // 起点偏 50px
    expect(matchStroke(HENG, off, { size: 1024 }).ok).toBe(true);
    expect(matchStroke(HENG, off, { size: 256 }).ok).toBe(false);
  });
});

describe('identifyStroke', () => {
  it('认出写的是哪一笔', () => {
    const targets = [SHU, HENG];
    const r = identifyStroke(targets, [[205, 305], [500, 302], [795, 298]]);
    expect(r.index).toBe(1);
  });

  it('全是乱写时仍返回最近的索引', () => {
    const targets = [SHU, HENG];
    const r = identifyStroke(targets, [[100, 100], [110, 120]]);
    expect(r.index).toBeGreaterThanOrEqual(0);
  });
});
