/**
 * 笔画骨架的等比缩放：画布尺寸变化时保持笔迹形状不变。
 *
 * 背景：横竖屏切换会改变画布的宽高比（iPad Pro 11：834×1194 → 1194×834）。
 * 如果 x / y 各自独立缩放，斜线会被拉歪、角度漂移（实测同一条斜线由 33.96° 偏到 30.21°）。
 * 这里统一用**同一个** scale 因子做等比缩放 —— 笔迹只整体变大/变小，形状不变。
 *
 * 两条规则：
 * 1. scale = min(宽比例, 高比例)：宁可整体略小，也不让笔迹超出新画布。
 * 2. 以旧画布中心为锚点：新坐标 = 新中心 + (旧坐标 - 旧中心) × scale，
 *    旋转后笔迹仍在新画布中央，不会缩到一角。
 *
 * 纯函数，不依赖 DOM 与 Vue。
 */

/** 缩放比例与 1 的差小于该阈值时视为「尺寸没变」，不做无谓的重建 */
const SCALE_EPSILON = 1e-6;

/**
 * 把一批笔画的骨架从旧画布尺寸等比缩放到新画布尺寸。
 *
 * @param {Array<{points: Array<{x: number, y: number}>}>} strokes 笔画列表（骨架是真相）
 * @param {{width: number, height: number}} fromSize 旧画布尺寸（CSS 像素）
 * @param {{width: number, height: number}} toSize 新画布尺寸（CSS 像素）
 * @returns {Array} 缩放后的新笔画数组；「尺寸没变」时**原样返回同一个数组引用**，
 *   调用方据此判断是否需要 store.restore
 */
export function scaleStrokesUniformly(strokes, fromSize, toSize) {
  if (!Array.isArray(strokes)) return [];

  const fromWidth = Number(fromSize?.width);
  const fromHeight = Number(fromSize?.height);
  const toWidth = Number(toSize?.width);
  const toHeight = Number(toSize?.height);

  // 尺寸无效（未初始化 / 0 宽高）→ 不动笔迹
  if (!(fromWidth > 0) || !(fromHeight > 0) || !(toWidth > 0) || !(toHeight > 0)) {
    return strokes;
  }

  // 取两轴里更小的那个比例：等比缩放后一定落在新画布内
  const scale = Math.min(toWidth / fromWidth, toHeight / fromHeight);
  if (!Number.isFinite(scale) || Math.abs(scale - 1) < SCALE_EPSILON) {
    return strokes;
  }

  // 锚点：旧画布中心 → 新画布中心
  const oldCenterX = fromWidth / 2;
  const oldCenterY = fromHeight / 2;
  const newCenterX = toWidth / 2;
  const newCenterY = toHeight / 2;

  return strokes.map((stroke) => ({
    ...stroke,
    points: (Array.isArray(stroke?.points) ? stroke.points : []).map((point) => ({
      ...point,
      x: newCenterX + (point.x - oldCenterX) * scale,
      y: newCenterY + (point.y - oldCenterY) * scale
    }))
  }));
}
