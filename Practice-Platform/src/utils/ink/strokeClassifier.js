/**
 * 笔画走向判别：从骨架点判断这一笔大概写的是哪个笔画。
 *
 * 只做粗分类（横 / 竖 / 撇 / 捺 / 点），不做精细形态识别——
 * 形态识别需要笔顺数据（哪个字该先写哪一笔），而字幕里没有这份数据。
 *
 * 用途：写完一笔后，练习页据此给出该笔画的规范写法提示。
 *
 * 纯函数，不依赖 DOM。
 */

export const STROKE_KINDS = ['横', '竖', '撇', '捺', '点'];

/** 骨架总长（累加相邻点距离） */
export function pathLength(points) {
  let total = 0;
  for (let i = 1; i < points.length; i += 1) {
    total += Math.hypot(points[i].x - points[i - 1].x, points[i].y - points[i - 1].y);
  }
  return total;
}

/**
 * 判别笔画走向。
 * @param {Array<{x:number,y:number}>} points 骨架采样点
 * @param {{minLength?: number}} options
 * @returns {{kind: string|null, confidence: number, reason: string}}
 */
export function classifyStroke(points, { minLength = 12 } = {}) {
  if (!Array.isArray(points) || points.length < 2) {
    return { kind: null, confidence: 0, reason: '采样点不足，无法判别' };
  }

  const first = points[0];
  const last = points[points.length - 1];
  const dx = last.x - first.x;
  const dy = last.y - first.y;

  if (pathLength(points) < minLength) {
    return { kind: '点', confidence: 0.6, reason: '笔画很短，按点处理' };
  }

  const ax = Math.abs(dx);
  const ay = Math.abs(dy);
  const total = ax + ay || 1;

  // 屏幕坐标 y 向下：dy > 0 是往下走
  if (ax >= ay * 2) {
    return { kind: '横', confidence: Math.min(1, ax / total), reason: '横向为主' };
  }
  if (ay >= ax * 2) {
    return { kind: '竖', confidence: Math.min(1, ay / total), reason: '纵向为主' };
  }
  if (dx < 0) {
    return { kind: '撇', confidence: Math.min(1, Math.abs(dx) / total), reason: '向左下斜出' };
  }
  return { kind: '捺', confidence: Math.min(1, Math.abs(dx) / total), reason: '向右下斜出' };
}
