/**
 * 笔画匹配：把用户写的一笔与笔顺数据里目标笔的轨迹做比对，判定这一笔是否合格。
 *
 * 纯函数，不依赖 DOM / fabric，可单独测试。
 *
 * 坐标系约定：调用方必须先把两者转到**同一坐标系**再传进来。
 * 笔顺数据是 1024×1024 的字形画布（屏幕坐标系，y 向下）；
 * 用户笔画是画布坐标 —— 用 transformStroke() 把目标轨迹变换到画布坐标后再比对。
 */

const DEFAULTS = {
  /** 落笔点容差（相对字幅） */
  startToleranceRatio: 0.12,
  /** 收笔点容差（相对字幅） */
  endToleranceRatio: 0.14,
  /** 方向容差（度） */
  angleTolerance: 50,
  /** 整条轨迹的最大偏离容差（相对字幅） */
  deviationToleranceRatio: 0.13,
};

function dist(a, b) {
  return Math.hypot(a[0] - b[0], a[1] - b[1]);
}

/** 点到线段的距离 */
function pointSegDist(p, a, b) {
  const dx = b[0] - a[0], dy = b[1] - a[1];
  const len2 = dx * dx + dy * dy;
  if (len2 === 0) return dist(p, a);
  let t = ((p[0] - a[0]) * dx + (p[1] - a[1]) * dy) / len2;
  t = Math.max(0, Math.min(1, t));
  return Math.hypot(p[0] - (a[0] + t * dx), p[1] - (a[1] + t * dy));
}

/** 点到折线的最短距离 */
function pointPolylineDist(p, pts) {
  let best = Infinity;
  for (let i = 1; i < pts.length; i++) {
    const d = pointSegDist(p, pts[i - 1], pts[i]);
    if (d < best) best = d;
  }
  return best;
}

/** 两个向量的夹角（度，0~180） */
function angleBetween(a, b) {
  const la = Math.hypot(a[0], a[1]);
  const lb = Math.hypot(b[0], b[1]);
  if (la === 0 || lb === 0) return 180;
  const dot = (a[0] * b[0] + a[1] * b[1]) / (la * lb);
  return Math.acos(Math.max(-1, Math.min(1, dot))) * 180 / Math.PI;
}

/**
 * 把 1024 字形画布上的轨迹变换到画布坐标。
 * @param {Array<Array<number>>} pts 字形画布坐标
 * @param {{ x: number, y: number, size: number }} frame 字在画布上的位置与边长
 */
export function transformStroke(pts, frame) {
  const k = frame.size / 1024;
  return pts.map(([x, y]) => [frame.x + x * k, frame.y + y * k]);
}

/**
 * 判定用户写的一笔是否合格。
 *
 * @param {Array<Array<number>>} target 目标笔轨迹（已与 written 同坐标系）
 * @param {Array<Array<number>>} written 用户写的这一笔
 * @param {object} [options]
 * @param {number} [options.startToleranceRatio]
 * @param {number} [options.endToleranceRatio]
 * @param {number} [options.angleTolerance]
 * @param {number} [options.deviationToleranceRatio]
 * @param {number} [options.size] 字幅（默认 1024），容差按它缩放
 * @returns {{ ok: boolean, startDist: number, endDist: number, angle: number,
 *            deviation: number, reversed: boolean, reason: string }}
 */
export function matchStroke(target, written, options = {}) {
  const opt = { ...DEFAULTS, ...options };
  const size = opt.size || 1024;

  if (!target || target.length < 2) {
    return { ok: false, startDist: Infinity, endDist: Infinity, angle: 180, deviation: Infinity, reversed: false, reason: '目标笔无轨迹' };
  }
  if (!written || written.length < 2) {
    return { ok: false, startDist: Infinity, endDist: Infinity, angle: 180, deviation: Infinity, reversed: false, reason: '还没有画出这一笔' };
  }

  const startDist = dist(written[0], target[0]);
  const endDist = dist(written[written.length - 1], target[target.length - 1]);

  const wVec = [written[written.length - 1][0] - written[0][0], written[written.length - 1][1] - written[0][1]];
  const tVec = [target[target.length - 1][0] - target[0][0], target[target.length - 1][1] - target[0][1]];
  const angle = angleBetween(wVec, tVec);
  const reversed = angle > 135;

  // 双向偏离：用户折线离规范轨迹多远、规范轨迹离用户折线多远，各取最大。
  // 用「最大」而不是「平均」——平均会被端点稀释（中段拱起 300px 也能算出 100px 的平均值），
  // 而「这一笔弯出轨道了」正是最大偏离的语义。
  let maxFromWritten = 0;
  for (const p of written) maxFromWritten = Math.max(maxFromWritten, pointPolylineDist(p, target));
  let maxFromTarget = 0;
  for (const p of target) maxFromTarget = Math.max(maxFromTarget, pointPolylineDist(p, written));
  const deviation = Math.max(maxFromWritten, maxFromTarget);

  const tolStart = opt.startToleranceRatio * size;
  const tolEnd = opt.endToleranceRatio * size;
  const tolDev = opt.deviationToleranceRatio * size;

  let reason = '';
  let ok = true;
  if (reversed) { ok = false; reason = '方向反了：这一笔应该从另一个端点起笔'; }
  else if (startDist > tolStart) { ok = false; reason = '落笔位置偏了：红点才是这一笔的起点'; }
  else if (angle > opt.angleTolerance) { ok = false; reason = `走向不对：与规范方向差了 ${Math.round(angle)} 度`; }
  else if (endDist > tolEnd) { ok = false; reason = '收笔位置偏了：要写到这一笔的末端'; }
  else if (deviation > tolDev) { ok = false; reason = '笔画形状偏离规范轨迹太远'; }

  if (ok) reason = '这一笔写对了';

  return { ok, startDist, endDist, angle, deviation, reversed, reason };
}

/**
 * 判断用户写的这一笔，最像目标序列里的哪一笔。
 * 用于「不按笔顺写也能认出是哪一笔」的场景。
 * @param {Array<Array<Array<number>>>} targets 全部目标笔轨迹
 * @param {Array<Array<number>>} written 用户笔画
 */
export function identifyStroke(targets, written, options = {}) {
  let best = -1;
  let bestScore = Infinity;
  let bestDetail = null;
  targets.forEach((t, i) => {
    const r = matchStroke(t, written, options);
    const score = r.startDist + r.deviation;
    if (score < bestScore) { bestScore = score; best = i; bestDetail = r; }
  });
  return { index: best, ...(bestDetail || {}) };
}
