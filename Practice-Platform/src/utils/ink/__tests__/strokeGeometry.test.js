import { describe, it, expect } from 'vitest';

import {
  computeSpeeds,
  averageSpeed,
  detectPausePoints,
  phaseFactors,
  movingAverage,
  computeWidths,
  computeNormals,
  buildOutlinePath,
  strokeToPath
} from '../strokeGeometry.js';

const line = (points) => points.map(([x, y, t, p]) => ({ x, y, t, p: p ?? 1 }));

describe('strokeGeometry', () => {
  describe('computeSpeeds / averageSpeed', () => {
    it('速度 = 距离 / 时间差', () => {
      const speeds = computeSpeeds(line([[0, 0, 0], [10, 0, 10]]));
      expect(speeds[0]).toBeCloseTo(1, 6);
    });

    it('最后一个点沿用前一段速度', () => {
      const speeds = computeSpeeds(line([[0, 0, 0], [10, 0, 10], [20, 0, 20]]));
      expect(speeds[2]).toBeCloseTo(speeds[1], 6);
    });

    it('平均速度只统计有效段', () => {
      expect(averageSpeed([0, 2, 4])).toBeCloseTo(3, 6);
      expect(averageSpeed([0, 0, 0])).toBe(0);
    });
  });

  describe('detectPausePoints', () => {
    it('中段明显变慢 → 判为顿点', () => {
      const points = line([
        [0, 0, 0],
        [10, 0, 10],
        [10.5, 0, 100],
        [20, 0, 110]
      ]);
      const speeds = computeSpeeds(points);
      const pauses = detectPausePoints(points, speeds);
      expect(pauses).toContain(2);
    });

    it('匀速一笔没有顿点', () => {
      const points = line([
        [0, 0, 0],
        [10, 0, 10],
        [20, 0, 20],
        [30, 0, 30]
      ]);
      const pauses = detectPausePoints(points, computeSpeeds(points));
      expect(pauses).toEqual([]);
    });

    it('点数太少不判顿点', () => {
      expect(detectPausePoints([], [])).toEqual([]);
      expect(detectPausePoints(line([[0, 0, 0], [1, 1, 1]]), [1, 1])).toEqual([]);
    });
  });

  describe('phaseFactors', () => {
    it('顿点处系数 = boost，邻域线性衰减', () => {
      const factors = phaseFactors(5, [2], { boost: 1.6, radius: 3 });
      expect(factors[2]).toBeCloseTo(1.6, 6);
      expect(factors[1]).toBeCloseTo(1.45, 6);
      expect(factors[0]).toBeCloseTo(1.3, 6);
    });

    it('没有顿点时全是 1', () => {
      expect(phaseFactors(4, [])).toEqual([1, 1, 1, 1]);
    });
  });

  describe('movingAverage', () => {
    it('窗口 3 的滑动平均', () => {
      expect(movingAverage([1, 2, 3], 3)).toEqual([1.5, 2, 2.5]);
    });

    it('窗口 1 时原样返回', () => {
      expect(movingAverage([1, 2, 3], 1)).toEqual([1, 2, 3]);
    });
  });

  describe('computeWidths', () => {
    it('压力递增 → 宽度递增', () => {
      const points = line([
        [0, 0, 0, 0.2],
        [10, 0, 10, 0.6],
        [20, 0, 20, 1]
      ]);
      const { widths } = computeWidths(points, { baseWidth: 10, hasPressure: true });
      expect(widths[0]).toBeLessThan(widths[1]);
      expect(widths[1]).toBeLessThan(widths[2]);
    });

    it('顿点处宽度局部增大（对比不加顿点增强）', () => {
      const points = line([
        [0, 0, 0],
        [10, 0, 10],
        [10.5, 0, 100],
        [20, 0, 110]
      ]);
      const boosted = computeWidths(points, { baseWidth: 10, boost: 1.6 });
      const plain = computeWidths(points, { baseWidth: 10, boost: 1 });
      expect(boosted.pauseIndices).toContain(2);
      expect(boosted.phases[2]).toBeCloseTo(1.6, 6);
      expect(plain.phases[2]).toBeCloseTo(1, 6);
      expect(boosted.widths[2]).toBeGreaterThan(plain.widths[2]);
    });

    it('没有压感的设备走速度映射，起点慢 → 起点粗', () => {
      const points = line([
        [0, 0, 0],
        [1, 0, 100],
        [20, 0, 110]
      ]);
      const { widths } = computeWidths(points, { baseWidth: 10, hasPressure: false });
      expect(widths[0]).toBeGreaterThan(widths[2]);
    });

    it('空点数组返回空结果', () => {
      const result = computeWidths([], {});
      expect(result.widths).toEqual([]);
      expect(result.pauseIndices).toEqual([]);
    });
  });

  describe('computeNormals', () => {
    it('水平向右的笔画法向朝上（屏幕坐标 y 向下）', () => {
      const normals = computeNormals(line([[0, 0, 0], [10, 0, 10]]));
      expect(normals[0].x).toBeCloseTo(0, 6);
      expect(normals[0].y).toBeCloseTo(-1, 6);
    });

    it('法向是单位向量', () => {
      const normals = computeNormals(line([[0, 0, 0], [3, 4, 10], [6, 8, 20]]));
      normals.forEach((normal) => {
        expect(Math.hypot(normal.x, normal.y)).toBeCloseTo(1, 6);
      });
    });
  });

  describe('buildOutlinePath', () => {
    it('单点笔画 → 一个圆（含两段圆弧并闭合）', () => {
      const { d } = buildOutlinePath(line([[5, 5, 0]]), [10]);
      expect(d.startsWith('M ')).toBe(true);
      expect(d.endsWith('Z')).toBe(true);
      expect((d.match(/A /g) || []).length).toBe(2);
    });

    it('两点笔画 → 左右轮廓相距一个笔画宽度', () => {
      const { left, right } = buildOutlinePath(line([[0, 0, 0], [100, 0, 10]]), [10, 10]);
      expect(left[0].x).toBeCloseTo(0, 6);
      expect(left[0].y).toBeCloseTo(-5, 6);
      expect(right[0].y).toBeCloseTo(5, 6);
      const distance = Math.hypot(left[1].x - right[1].x, left[1].y - right[1].y);
      expect(distance).toBeCloseTo(10, 6);
    });

    it('输出的 d 是合法 SVG path 结构', () => {
      const { d } = buildOutlinePath(line([[0, 0, 0], [10, 0, 10], [20, 5, 20]]), [8, 9, 10]);
      expect(d).toMatch(/^M [\d.-]+ [\d.-]+/);
      expect(d).toMatch(/L /);
      expect(d).toMatch(/A /);
      expect(d.endsWith('Z')).toBe(true);
      // 不能出现 NaN / undefined
      expect(d).not.toMatch(/NaN|undefined/);
    });

    it('端点用半圆弧收口：半径 = 笔画宽度/2，方向朝外', () => {
      const { d, left, right } = buildOutlinePath(line([[0, 0, 0], [100, 0, 10]]), [10, 10]);
      const arcs = d.match(/A (-?[\d.]+) (-?[\d.]+) 0 0 ([01]) (-?[\d.]+) (-?[\d.]+)/g);
      expect(arcs).toHaveLength(2);

      const parsed = arcs.map((arc) => {
        const parts = arc.split(' ');
        return { rx: Number(parts[1]), sweep: parts[5], x: Number(parts[6]), y: Number(parts[7]) };
      });
      parsed.forEach((arc) => {
        expect(arc.rx).toBeCloseTo(5, 6);
        // sweep=1 = 屏幕上顺时针：从上方绕到下方 = 顺着行进方向向外鼓
        expect(arc.sweep).toBe('1');
      });
      // 末端弧收在右轮廓的末端，起始弧回到左轮廓的起点
      expect(parsed[0].x).toBeCloseTo(right[1].x, 6);
      expect(parsed[0].y).toBeCloseTo(right[1].y, 6);
      expect(parsed[1].x).toBeCloseTo(left[0].x, 6);
      expect(parsed[1].y).toBeCloseTo(left[0].y, 6);
    });

    it('空点数组返回空路径', () => {
      expect(buildOutlinePath([], []).d).toBe('');
    });
  });

  describe('strokeToPath', () => {
    it('骨架一步生成可渲染路径', () => {
      const d = strokeToPath(line([[0, 0, 0], [10, 0, 10], [20, 0, 20]]), { baseWidth: 8 });
      expect(d.length).toBeGreaterThan(0);
      expect(d.startsWith('M ')).toBe(true);
      expect(d.endsWith('Z')).toBe(true);
    });
  });
});
