/**
 * 宽度曲线：压力 / 速度 → 宽度比例。
 *
 * 本模块只负责「压力 → 宽度比例」这一条映射，不关心笔画的整体宽度；
 * 整体宽度由调用方乘上 baseWidth（笔刷大小滑杆）。
 *
 * 纯函数，不依赖 DOM 与 Vue。
 */

const DEFAULT_MIN_RATIO = 0.35;
const DEFAULT_GAMMA = 1.0;

export function clamp01(value) {
  if (!Number.isFinite(value)) return 0;
  if (value < 0) return 0;
  if (value > 1) return 1;
  return value;
}

/**
 * 压感 → 宽度比例。
 * @param {number} pressure 0..1
 * @param {{minRatio?: number, gamma?: number}} options
 * @returns {number} minRatio..1 之间的宽度比例
 */
export function pressureFactor(pressure, { minRatio = DEFAULT_MIN_RATIO, gamma = DEFAULT_GAMMA } = {}) {
  const p = clamp01(pressure);
  return minRatio + (1 - minRatio) * Math.pow(p, gamma);
}

/**
 * 速度 → 等效压力（鼠标降级用）：慢 = 重，快 = 轻。
 * 速度等于平均速度时给 0.5，两倍平均速度时给 0。
 */
export function speedToPressure(speed, averageSpeed) {
  if (!Number.isFinite(speed) || !Number.isFinite(averageSpeed) || averageSpeed <= 0) {
    return 0.5;
  }
  return clamp01(1 - speed / averageSpeed / 2);
}

/**
 * 解析某个采样点的等效压力。
 * - Shift 按住 → 1（重笔）
 * - 真压感设备 → 直接用设备压力
 * - 其余（鼠标 / 老设备不上报压力）→ 速度映射
 */
export function resolvePressure({
  pressure,
  hasPressure = false,
  speed = 0,
  averageSpeed = 0,
  shiftKey = false
} = {}) {
  if (shiftKey) return 1;
  if (hasPressure && Number.isFinite(pressure)) return clamp01(pressure);
  return speedToPressure(speed, averageSpeed);
}
