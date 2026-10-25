import { describe, it, expect } from 'vitest';

import { scaleStrokesUniformly } from '../strokeScale.js';

/** 造一笔：points 用 [x, y] 数组写，附带压感与时间戳 */
const stroke = (points, extra = {}) => ({
  points: points.map(([x, y], index) => ({ x, y, p: 1, t: index * 10 })),
  widths: points.map(() => 8),
  color: '#000000',
  opacity: 1,
  ...extra
});

/** 线段角度（度） */
const angleOf = (points) =>
  (Math.atan2(points[1].y - points[0].y, points[1].x - points[0].x) * 180) / Math.PI;

/** 笔画包围盒中心 */
const centerOf = (points) => ({
  x: (points[0].x + points[points.length - 1].x) / 2,
  y: (points[0].y + points[points.length - 1].y) / 2
});

/** iPad Pro 11 竖屏 → 横屏 */
const PORTRAIT = { width: 834, height: 1194 };
const LANDSCAPE = { width: 1194, height: 834 };

describe('strokeScale', () => {
  describe('scaleStrokesUniformly', () => {
    it('等比缩放：x / y 用同一个因子', () => {
      const strokes = [stroke([[100, 100], [900, 700]])];
      const scaled = scaleStrokesUniformly(strokes, PORTRAIT, LANDSCAPE);

      const dx = scaled[0].points[1].x - scaled[0].points[0].x;
      const dy = scaled[0].points[1].y - scaled[0].points[0].y;
      // 原始向量 (800, 600)
      expect(dx / 800).toBeCloseTo(dy / 600, 6);
      // 缩放因子 = min(1194/834, 834/1194)
      expect(dx / 800).toBeCloseTo(834 / 1194, 6);
      expect(dy / 600).toBeCloseTo(834 / 1194, 6);
    });

    it('等比缩放：斜线角度不变', () => {
      const strokes = [stroke([[0, 0], [934, 629]])];
      const scaled = scaleStrokesUniformly(strokes, PORTRAIT, LANDSCAPE);
      expect(angleOf(scaled[0].points)).toBeCloseTo(angleOf(strokes[0].points), 6);
    });

    it('回归：iPad 竖屏 → 横屏，旧实现偏 3.75° 的角度漂移不再出现', () => {
      const strokes = [stroke([[0, 0], [934, 629]])];
      const before = angleOf(strokes[0].points);
      // 旧实现（x / y 独立缩放）实测：33.96° → 30.21°
      expect(before).toBeCloseTo(33.96, 1);

      const scaled = scaleStrokesUniformly(strokes, PORTRAIT, LANDSCAPE);
      const after = angleOf(scaled[0].points);
      expect(after).toBeCloseTo(before, 6);
      expect(Math.abs(after - 30.21)).toBeGreaterThan(3);
    });

    it('中心锚定：缩放后笔迹中心仍在新画布中心附近', () => {
      // 笔迹原本就落在旧画布中心
      const strokes = [stroke([[417 - 100, 597 - 100], [417 + 100, 597 + 100]])];
      const scaled = scaleStrokesUniformly(strokes, PORTRAIT, LANDSCAPE);

      const center = centerOf(scaled[0].points);
      expect(center.x).toBeCloseTo(LANDSCAPE.width / 2, 6);
      expect(center.y).toBeCloseTo(LANDSCAPE.height / 2, 6);
    });

    it('中心锚定：偏心的笔迹不会被拉到新画布中心', () => {
      // 笔迹在旧画布左上角附近
      const strokes = [stroke([[10, 10], [110, 110]])];
      const scaled = scaleStrokesUniformly(strokes, PORTRAIT, LANDSCAPE);

      const center = centerOf(scaled[0].points);
      const oldCenter = centerOf(strokes[0].points);
      const oldCanvasCenter = { x: PORTRAIT.width / 2, y: PORTRAIT.height / 2 };
      // 相对旧画布中心的偏移方向与比例保持（只是被等比缩放 + 平移）
      expect(center.x - LANDSCAPE.width / 2).toBeCloseTo(
        (oldCenter.x - oldCanvasCenter.x) * (834 / 1194),
        6
      );
      expect(center.y - LANDSCAPE.height / 2).toBeCloseTo(
        (oldCenter.y - oldCanvasCenter.y) * (834 / 1194),
        6
      );
    });

    it('小幅尺寸变化：scale 与 1 的差 < 1e-6 时原样返回同一个数组引用', () => {
      const strokes = [stroke([[100, 100], [900, 700]])];
      const scaled = scaleStrokesUniformly(
        strokes,
        { width: 800, height: 600 },
        { width: 800.0004, height: 600.0004 }
      );
      // 同一个引用 → 调用方可以据此跳过 store.restore
      expect(scaled).toBe(strokes);
      expect(scaled[0].points[0].x).toBe(100);
      expect(scaled[0].points[0].y).toBe(100);
    });

    it('小幅尺寸变化：坐标几乎不动', () => {
      const strokes = [stroke([[100, 100], [900, 700]])];
      const scaled = scaleStrokesUniformly(
        strokes,
        { width: 800, height: 600 },
        { width: 800.002, height: 600.002 }
      );
      expect(scaled).not.toBe(strokes);
      expect(Math.abs(scaled[0].points[0].x - 100)).toBeLessThan(0.01);
      expect(Math.abs(scaled[0].points[0].y - 100)).toBeLessThan(0.01);
    });

    it('空数组输入不报错', () => {
      const scaled = scaleStrokesUniformly([], PORTRAIT, LANDSCAPE);
      expect(Array.isArray(scaled)).toBe(true);
      expect(scaled).toHaveLength(0);
    });

    it('极端比例：宽度翻倍、高度不变时结果仍在新画布范围内', () => {
      const from = { width: 800, height: 600 };
      const to = { width: 1600, height: 600 };
      // 笔迹贴着旧画布的四条边
      const strokes = [stroke([[0, 0], [800, 600]])];
      const scaled = scaleStrokesUniformly(strokes, from, to);

      scaled[0].points.forEach((point) => {
        expect(point.x).toBeGreaterThanOrEqual(-1e-9);
        expect(point.x).toBeLessThanOrEqual(to.width + 1e-9);
        expect(point.y).toBeGreaterThanOrEqual(-1e-9);
        expect(point.y).toBeLessThanOrEqual(to.height + 1e-9);
      });
    });

    it('极端比例：高度缩一半、宽度不变时结果仍在新画布范围内', () => {
      const from = { width: 800, height: 600 };
      const to = { width: 800, height: 300 };
      const strokes = [stroke([[0, 0], [800, 600]])];
      const scaled = scaleStrokesUniformly(strokes, from, to);

      scaled[0].points.forEach((point) => {
        expect(point.x).toBeGreaterThanOrEqual(-1e-9);
        expect(point.x).toBeLessThanOrEqual(to.width + 1e-9);
        expect(point.y).toBeGreaterThanOrEqual(-1e-9);
        expect(point.y).toBeLessThanOrEqual(to.height + 1e-9);
      });
    });

    it('笔画的其他字段（宽度 / 颜色 / 不透明度）原样保留', () => {
      const strokes = [stroke([[10, 10], [20, 20]], { color: '#ff0000', opacity: 0.5 })];
      const scaled = scaleStrokesUniformly(strokes, PORTRAIT, LANDSCAPE);
      expect(scaled[0].color).toBe('#ff0000');
      expect(scaled[0].opacity).toBe(0.5);
      expect(scaled[0].widths).toEqual([8, 8]);
    });

    it('纯函数：不修改传入的笔画', () => {
      const strokes = [stroke([[10, 10], [20, 20]])];
      const snapshot = JSON.parse(JSON.stringify(strokes));
      scaleStrokesUniformly(strokes, PORTRAIT, LANDSCAPE);
      expect(strokes).toEqual(snapshot);
    });

    it('尺寸无效时不改动笔迹', () => {
      const strokes = [stroke([[10, 10], [20, 20]])];
      expect(scaleStrokesUniformly(strokes, { width: 0, height: 0 }, LANDSCAPE)).toBe(strokes);
      expect(scaleStrokesUniformly(strokes, PORTRAIT, null)).toBe(strokes);
    });
  });
});
