import { describe, it, expect } from 'vitest';

import { classifyStroke, pathLength, STROKE_KINDS } from '../strokeClassifier.js';

const line = (from, to, steps = 10) => {
  const points = [];
  for (let i = 0; i <= steps; i += 1) {
    const t = i / steps;
    points.push({ x: from[0] + (to[0] - from[0]) * t, y: from[1] + (to[1] - from[1]) * t });
  }
  return points;
};

describe('strokeClassifier', () => {
  describe('pathLength', () => {
    it('累加相邻点距离', () => {
      expect(pathLength([{ x: 0, y: 0 }, { x: 3, y: 4 }, { x: 3, y: 14 }])).toBeCloseTo(15, 6);
    });

    it('单点长度为 0', () => {
      expect(pathLength([{ x: 1, y: 1 }])).toBe(0);
    });
  });

  describe('classifyStroke', () => {
    it('向右横 → 横', () => {
      const result = classifyStroke(line([0, 100], [200, 100]));
      expect(result.kind).toBe('横');
      expect(result.confidence).toBeGreaterThan(0.9);
    });

    it('向下竖 → 竖', () => {
      const result = classifyStroke(line([100, 0], [100, 200]));
      expect(result.kind).toBe('竖');
    });

    it('向左下斜 → 撇', () => {
      const result = classifyStroke(line([200, 0], [50, 150]));
      expect(result.kind).toBe('撇');
    });

    it('向右下斜 → 捺', () => {
      const result = classifyStroke(line([0, 0], [150, 150]));
      expect(result.kind).toBe('捺');
    });

    it('很短的一笔 → 点', () => {
      const result = classifyStroke(line([50, 50], [54, 52], 4), { minLength: 12 });
      expect(result.kind).toBe('点');
    });

    it('点数不足时不给结论', () => {
      expect(classifyStroke([{ x: 1, y: 1 }]).kind).toBe(null);
      expect(classifyStroke([]).kind).toBe(null);
      expect(classifyStroke(null).kind).toBe(null);
    });

    it('判别结果永远落在已知笔画集合里', () => {
      const samples = [
        line([0, 0], [100, 0]),
        line([0, 0], [0, 100]),
        line([0, 0], [-100, 100]),
        line([0, 0], [100, 100]),
        line([0, 0], [2, 2], 3)
      ];
      for (const sample of samples) {
        const { kind } = classifyStroke(sample);
        if (kind !== null) expect(STROKE_KINDS).toContain(kind);
      }
    });
  });
});
