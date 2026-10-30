/**
 * 压感数据统计（纯函数，不依赖 DOM）。
 *
 * 浏览器对非压感设备会报告「模拟压感」：鼠标/触摸通常只有
 * 0 / 0.5 / 1 三个离散值；Apple Pencil 等真笔报连续值。
 * 诊断页据此判断设备拿到的是不是真实压感
 * （与 utils/ink/capability.js 的 NON_REAL_PRESSURES 判定口径一致）。
 */

/** 桌面模拟压感的取值集合（归一后比较） */
const NON_REAL_PRESSURES = new Set([0, 0.5, 1]);

/** distinct 列表最多展示多少个值，防止导出体积失控 */
export const MAX_DISTINCT_SHOWN = 16;

/** 按两位小数归一，避免 0.499999 与 0.5 记成两个值 */
export function roundPressure(p) {
  return Math.round(p * 100) / 100;
}

const EMPTY_SUMMARY = {
  count: 0,
  distinctCount: 0,
  distinct: [],
  min: null,
  max: null,
  mean: null,
  nonRealOnly: null,
  realPressureLikely: null
};

/**
 * 汇总一段指针事件的压感分布。
 * @param {Array<{pressure: number}>} samples 指针事件样本
 * @returns 压感摘要；realPressureLikely 为 null 表示没有可用样本
 */
export function summarizePressure(samples) {
  if (!Array.isArray(samples) || samples.length === 0) {
    return { ...EMPTY_SUMMARY };
  }
  const pressures = [];
  for (const s of samples) {
    if (s && typeof s.pressure === 'number' && Number.isFinite(s.pressure)) {
      pressures.push(s.pressure);
    }
  }
  if (pressures.length === 0) {
    return { ...EMPTY_SUMMARY };
  }
  const distinct = [...new Set(pressures.map(roundPressure))].sort((a, b) => a - b);
  const min = Math.min(...pressures);
  const max = Math.max(...pressures);
  const mean = pressures.reduce((sum, p) => sum + p, 0) / pressures.length;
  const nonRealOnly = distinct.every((p) => NON_REAL_PRESSURES.has(p));
  return {
    count: pressures.length,
    distinctCount: distinct.length,
    distinct: distinct.slice(0, MAX_DISTINCT_SHOWN),
    min: roundPressure(min),
    max: roundPressure(max),
    mean: roundPressure(mean),
    nonRealOnly,
    realPressureLikely: !nonRealOnly
  };
}
