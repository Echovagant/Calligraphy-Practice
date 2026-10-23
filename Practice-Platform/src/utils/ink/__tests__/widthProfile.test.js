import { describe, it, expect } from 'vitest';

import {
  clamp01,
  pressureFactor,
  speedToPressure,
  resolvePressure
} from '../widthProfile.js';

describe('widthProfile', () => {
  describe('clamp01', () => {
    it('把越界与非数值都夹到 [0,1]', () => {
      expect(clamp01(-1)).toBe(0);
      expect(clamp01(2)).toBe(1);
      expect(clamp01(0.42)).toBe(0.42);
      expect(clamp01(Number.NaN)).toBe(0);
    });
  });

  describe('pressureFactor', () => {
    it('压力 0 → minRatio，压力 1 → 1', () => {
      expect(pressureFactor(0)).toBeCloseTo(0.35, 6);
      expect(pressureFactor(1)).toBeCloseTo(1, 6);
    });

    it('中间值按 minRatio + (1-minRatio) * p^gamma 线性插值', () => {
      expect(pressureFactor(0.5)).toBeCloseTo(0.35 + 0.65 * 0.5, 6);
    });

    it('单调递增', () => {
      const values = [0, 0.2, 0.4, 0.6, 0.8, 1].map((p) => pressureFactor(p));
      for (let i = 1; i < values.length; i += 1) {
        expect(values[i]).toBeGreaterThan(values[i - 1]);
      }
    });

    it('gamma > 1 时曲线更凹（低压区更细）', () => {
      expect(pressureFactor(0.5, { gamma: 2 })).toBeLessThan(pressureFactor(0.5, { gamma: 1 }));
    });

    it('minRatio 可配置', () => {
      expect(pressureFactor(0, { minRatio: 0.8 })).toBeCloseTo(0.8, 6);
    });
  });

  describe('speedToPressure', () => {
    it('慢 = 重：静止时压力最大', () => {
      expect(speedToPressure(0, 100)).toBeCloseTo(1, 6);
    });

    it('等于平均速度时给 0.5', () => {
      expect(speedToPressure(100, 100)).toBeCloseTo(0.5, 6);
    });

    it('两倍平均速度时归零', () => {
      expect(speedToPressure(200, 100)).toBeCloseTo(0, 6);
    });

    it('单调递减', () => {
      const values = [0, 25, 50, 75, 100, 150].map((s) => speedToPressure(s, 100));
      for (let i = 1; i < values.length; i += 1) {
        expect(values[i]).toBeLessThan(values[i - 1]);
      }
    });

    it('平均速度无效时回落到 0.5', () => {
      expect(speedToPressure(10, 0)).toBeCloseTo(0.5, 6);
      expect(speedToPressure(Number.NaN, 10)).toBeCloseTo(0.5, 6);
    });
  });

  describe('resolvePressure', () => {
    it('Shift 按住 → 1（重笔）', () => {
      expect(resolvePressure({ pressure: 0.2, hasPressure: true, shiftKey: true })).toBe(1);
    });

    it('有真压感时直接用设备压力', () => {
      expect(resolvePressure({ pressure: 0.73, hasPressure: true, speed: 0, averageSpeed: 0 })).toBeCloseTo(0.73, 6);
    });

    it('无压感时走速度映射', () => {
      const value = resolvePressure({ pressure: 0.5, hasPressure: false, speed: 0, averageSpeed: 100 });
      expect(value).toBeCloseTo(1, 6);
    });
  });
});
