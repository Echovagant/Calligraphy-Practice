/**
 * 笔画几何：采样点 → 宽度序列 → 轮廓多边形 → SVG path。
 *
 * **核心设计：骨架是真相，轮廓是派生产物。**
 * 一条笔画真正需要长期保存的是它的骨架（中心线采样点 + 每点的宽度），
 * 「填充轮廓多边形」只是渲染时的派生结果。
 *
 * 好处：缩放重绘只需把骨架的点乘比例，不用去变换 SVG path 数据；
 * 草稿存骨架，数据量比存轮廓小一个数量级；
 * 将来笔顺引导模式要改的只是「宽度怎么算」，骨架格式不变。
 *
 * 纯函数，不依赖 DOM。
 */

import { pressureFactor, resolvePressure } from './widthProfile.js';

const DEFAULT_SMOOTH_WINDOW = 3;
const DEFAULT_PAUSE_RATIO = 0.25;
const DEFAULT_PHASE_BOOST = 1.6;
const DEFAULT_PAUSE_RADIUS = 3;

const EPSILON = 1e-9;

function distance(a, b) {
  return Math.hypot(b.x - a.x, b.y - a.y);
}

/**
 * 每个采样点的速度（像素/毫秒）。
 * speeds[i] 是 points[i] → points[i+1] 的速度；最后一个点沿用前一段。
 */
export function computeSpeeds(points) {
  const n = points.length;
  const speeds = new Array(n).fill(0);
  if (n < 2) return speeds;

  for (let i = 0; i < n - 1; i += 1) {
    const dt = points[i + 1].t - points[i].t;
    const dist = distance(points[i], points[i + 1]);
    speeds[i] = dt > 0 ? dist / dt : 0;
  }
  speeds[n - 1] = speeds[n - 2];
  return speeds;
}

/** 该笔的平均速度（只统计有效段） */
export function averageSpeed(speeds) {
  const valid = speeds.filter((value) => Number.isFinite(value) && value > 0);
  if (valid.length === 0) return 0;
  return valid.reduce((sum, value) => sum + value, 0) / valid.length;
}

/**
 * 顿点检测（自由书写模式，本期唯一的「阶段」来源）。
 * 速度低于该笔平均速度 thresholdRatio 的点即为顿点。
 */
export function detectPausePoints(points, speeds, { thresholdRatio = DEFAULT_PAUSE_RATIO } = {}) {
  const n = points.length;
  if (n < 3) return [];

  const avg = averageSpeed(speeds);
  if (avg <= 0) return [];
  const threshold = avg * thresholdRatio;

  const result = [];
  for (let i = 1; i < n - 1; i += 1) {
    const before = speeds[i - 1];
    const after = speeds[i];
    const slowest = Math.min(before, after);
    if (slowest < threshold) result.push(i);
  }
  return result;
}

/**
 * 阶段系数：顿点附近变宽。
 * 影响范围 = 以顿点为中心的 ±radius 个采样点，用线性权重衰减。
 */
export function phaseFactors(count, pauseIndices, { boost = DEFAULT_PHASE_BOOST, radius = DEFAULT_PAUSE_RADIUS } = {}) {
  const factors = new Array(count).fill(1);
  for (const index of pauseIndices) {
    for (let offset = -radius; offset <= radius; offset += 1) {
      const target = index + offset;
      if (target < 0 || target >= count) continue;
      const weight = 1 - Math.abs(offset) / (radius + 1);
      factors[target] = Math.max(factors[target], 1 + (boost - 1) * weight);
    }
  }
  return factors;
}

/** 滑动平均，消除宽度抖动 */
export function movingAverage(values, window = DEFAULT_SMOOTH_WINDOW) {
  const n = values.length;
  if (n === 0) return [];
  if (window <= 1) return [...values];

  const half = Math.floor(window / 2);
  return values.map((_, i) => {
    let sum = 0;
    let count = 0;
    for (let j = i - half; j <= i + half; j += 1) {
      if (j < 0 || j >= n) continue;
      sum += values[j];
      count += 1;
    }
    return count > 0 ? sum / count : values[i];
  });
}

/**
 * 宽度序列。
 * w[i] = baseWidth * phaseFactor(i) * pressureFactor(p[i])，再做滑动平均。
 */
export function computeWidths(points, {
  baseWidth = 1,
  hasPressure = false,
  averageSpeed: avgSpeed,
  minRatio = 0.35,
  gamma = 1.0,
  boost = DEFAULT_PHASE_BOOST,
  pauseRadius = DEFAULT_PAUSE_RADIUS,
  smoothWindow = DEFAULT_SMOOTH_WINDOW,
  shiftKey = false
} = {}) {
  const n = points.length;
  if (n === 0) return { widths: [], pauseIndices: [], phases: [], speeds: [] };

  const speeds = computeSpeeds(points);
  // 调用方不传平均速度时就自己算——传 0 会让速度映射退化成常数，是个静默出错的坑
  const effectiveAverageSpeed = avgSpeed === undefined ? averageSpeed(speeds) : avgSpeed;
  const pauseIndices = detectPausePoints(points, speeds);
  const phases = phaseFactors(n, pauseIndices, { boost, radius: pauseRadius });

  const raw = points.map((point, i) => {
    const pressure = resolvePressure({
      pressure: point.p,
      hasPressure,
      speed: speeds[i],
      averageSpeed: effectiveAverageSpeed,
      shiftKey
    });
    return baseWidth * phases[i] * pressureFactor(pressure, { minRatio, gamma });
  });

  return { widths: movingAverage(raw, smoothWindow), pauseIndices, phases, speeds };
}

/**
 * 中心线法向。
 * 约定 perp(dx, dy) = (dy, -dx)，于是水平向右的笔画法向朝上（屏幕坐标 y 向下）。
 */
export function computeNormals(points) {
  const n = points.length;
  const normals = new Array(n);
  let fallback = { x: 0, y: -1 };

  for (let i = 0; i < n; i += 1) {
    const prev = points[Math.max(0, i - 1)];
    const next = points[Math.min(n - 1, i + 1)];
    let dx = next.x - prev.x;
    let dy = next.y - prev.y;

    if (Math.hypot(dx, dy) < EPSILON) {
      // 相邻点重合时退回到单段方向
      if (i < n - 1) {
        dx = points[i + 1].x - points[i].x;
        dy = points[i + 1].y - points[i].y;
      } else if (i > 0) {
        dx = points[i].x - points[i - 1].x;
        dy = points[i].y - points[i - 1].y;
      }
    }

    const length = Math.hypot(dx, dy);
    if (length < EPSILON) {
      normals[i] = fallback;
      continue;
    }
    const normal = { x: dy / length, y: -dx / length };
    normals[i] = normal;
    fallback = normal;
  }
  return normals;
}

function formatNumber(value) {
  return Number(value.toFixed(2));
}

function circlePath(x, y, r) {
  return [
    `M ${formatNumber(x - r)} ${formatNumber(y)}`,
    `A ${formatNumber(r)} ${formatNumber(r)} 0 1 0 ${formatNumber(x + r)} ${formatNumber(y)}`,
    `A ${formatNumber(r)} ${formatNumber(r)} 0 1 0 ${formatNumber(x - r)} ${formatNumber(y)}`,
    'Z'
  ].join(' ');
}

/**
 * 由骨架派生轮廓多边形并生成 SVG path。
 * 单点笔画 → 一个半径 w/2 的圆；两点以上 → 左右轮廓 + 两端半圆弧。
 */
export function buildOutlinePath(points, widths) {
  const n = points.length;
  if (n === 0) return { d: '', left: [], right: [] };

  const widthAt = (i) => {
    const value = widths[i];
    if (Number.isFinite(value)) return value;
    const fallback = widths[widths.length - 1];
    return Number.isFinite(fallback) ? fallback : 1;
  };

  if (n === 1) {
    const r = Math.max(widthAt(0) / 2, 0.1);
    return { d: circlePath(points[0].x, points[0].y, r), left: [], right: [] };
  }

  const normals = computeNormals(points);
  const left = [];
  const right = [];
  for (let i = 0; i < n; i += 1) {
    const half = widthAt(i) / 2;
    left.push({
      x: points[i].x + normals[i].x * half,
      y: points[i].y + normals[i].y * half
    });
    right.push({
      x: points[i].x - normals[i].x * half,
      y: points[i].y - normals[i].y * half
    });
  }

  const parts = [`M ${formatNumber(left[0].x)} ${formatNumber(left[0].y)}`];
  for (let i = 1; i < n; i += 1) {
    parts.push(`L ${formatNumber(left[i].x)} ${formatNumber(left[i].y)}`);
  }

  // 末端半圆弧：沿当前行进方向再往外鼓出 w/2
  const endRadius = Math.max(widthAt(n - 1) / 2, 0.1);
  parts.push(
    `A ${formatNumber(endRadius)} ${formatNumber(endRadius)} 0 0 1 ${formatNumber(right[n - 1].x)} ${formatNumber(right[n - 1].y)}`
  );

  for (let i = n - 2; i >= 0; i -= 1) {
    parts.push(`L ${formatNumber(right[i].x)} ${formatNumber(right[i].y)}`);
  }

  // 起端半圆弧：往回鼓出 w/2
  const startRadius = Math.max(widthAt(0) / 2, 0.1);
  parts.push(
    `A ${formatNumber(startRadius)} ${formatNumber(startRadius)} 0 0 1 ${formatNumber(left[0].x)} ${formatNumber(left[0].y)}`
  );
  parts.push('Z');

  return { d: parts.join(' '), left, right };
}

/**
 * 一步到位：骨架 → 可渲染的 SVG path。
 */
export function strokeToPath(points, widthOptions = {}) {
  const { widths } = computeWidths(points, widthOptions);
  return buildOutlinePath(points, widths).d;
}
