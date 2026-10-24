/**
 * 笔顺引导：加载汉字的逐笔轨迹、维护「当前该写第几笔」、判定用户写的一笔是否合格。
 *
 * 纯前端逻辑，不依赖 fabric；与画布的交互由调用方（PracticeView）负责。
 * 数据说明见 docs/stroke-order-data.md。
 */
import { ref, computed } from 'vue';
import { strokeCount, strokeShard } from '../data/strokeOrderIndex.js';
import { strokeTypes } from '../data/strokeTypes.js';
import { matchStroke, transformStroke } from '../utils/ink/strokeMatch.js';

/** 分片缓存：同一片只取一次 */
const shardCache = new Map();

/** base64 → [[x,y], ...]（每个点 4 字节：x uint16 大端、y uint16 大端） */
function decodePoints(b64) {
  const bin = atob(b64);
  const bytes = new Uint8Array(bin.length);
  for (let i = 0; i < bin.length; i++) bytes[i] = bin.charCodeAt(i);
  const view = new DataView(bytes.buffer);
  const pts = [];
  for (let i = 0; i + 3 < bytes.length; i += 4) {
    pts.push([view.getUint16(i, false), view.getUint16(i + 2, false)]);
  }
  return pts;
}

/** 把扁平点集按每笔点数切回逐笔 */
function splitStrokes(flat, counts) {
  const strokes = [];
  let k = 0;
  for (const c of counts) {
    strokes.push(flat.slice(k, k + c));
    k += c;
  }
  return strokes;
}

async function loadShard(file) {
  if (shardCache.has(file)) return shardCache.get(file);
  const p = fetch(`${import.meta.env.BASE_URL}${file}`).then((r) => {
    if (!r.ok) throw new Error(`加载分片失败 ${file}：HTTP ${r.status}`);
    return r.json();
  });
  shardCache.set(file, p);
  try {
    return await p;
  } catch (e) {
    shardCache.delete(file);
    throw e;
  }
}

export function useStrokeOrder() {
  /** 当前引导的字 */
  const char = ref('');
  /** 逐笔轨迹，每项 { points: [[x,y]...]（1024 字形坐标）, type: string|null } */
  const strokes = ref([]);
  /** 当前待写的笔序（0 起） */
  const index = ref(0);
  const status = ref('idle'); // idle | loading | ready | error
  const error = ref('');
  /** 最近一次判定的结果 */
  const result = ref(null);

  const total = computed(() => strokes.value.length);
  const current = computed(() => strokes.value[index.value] || null);
  const finished = computed(() => index.value >= strokes.value.length);
  const progress = computed(() => (total.value ? Math.min(1, index.value / total.value) : 0));

  /** 该字有没有笔顺数据 */
  function hasData(ch) {
    return Boolean(ch) && strokeCount(ch) > 0;
  }

  /** 加载一个字；没有数据时 status 置为 error 并给出可读原因 */
  async function load(ch) {
    char.value = ch || '';
    strokes.value = [];
    index.value = 0;
    result.value = null;
    error.value = '';
    if (!ch) { status.value = 'idle'; return false; }
    if (!hasData(ch)) {
      status.value = 'error';
      error.value = `「${ch}」没有笔顺数据`;
      return false;
    }
    status.value = 'loading';
    try {
      const file = strokeShard(ch);
      const shard = await loadShard(file);
      const rec = shard[ch];
      if (!rec) throw new Error(`分片里找不到「${ch}」`);
      const flat = decodePoints(rec.t);
      const per = splitStrokes(flat, rec.c);
      const types = strokeTypes(ch);
      strokes.value = per.map((points, i) => ({ points, type: types[i] || null }));
      index.value = 0;
      status.value = 'ready';
      return true;
    } catch (e) {
      status.value = 'error';
      error.value = e && e.message ? e.message : String(e);
      strokes.value = [];
      return false;
    }
  }

  /**
   * 判定用户写的一笔（画布坐标）。
   * 传 options.frame 时，目标笔会先从 1024 字形坐标变换到该 frame（画布坐标）再比对。
   */
  function check(writtenPoints, options = {}) {
    if (!current.value) {
      result.value = { ok: false, reason: '没有待写的笔画' };
      return result.value;
    }
    const frame = options.frame;
    const target = frame ? transformStroke(current.value.points, frame) : current.value.points;
    const r = matchStroke(target, writtenPoints, {
      ...options,
      size: options.size || (frame ? frame.size : 1024),
    });
    result.value = r;
    return r;
  }

  /** 判定通过后推进到下一笔 */
  function advance() {
    if (index.value < strokes.value.length) index.value += 1;
    result.value = null;
  }

  /** 退回到上一笔 */
  function back() {
    if (index.value > 0) index.value -= 1;
    result.value = null;
  }

  function reset() {
    index.value = 0;
    result.value = null;
  }

  /** 把目标笔轨迹变换到画布坐标 */
  function toCanvas(strokeIndex, frame) {
    const s = strokes.value[strokeIndex];
    if (!s) return [];
    return transformStroke(s.points, frame);
  }

  return {
    char, strokes, index, total, current, status, error, result, finished, progress,
    hasData, load, check, advance, back, reset, toCanvas,
  };
}
