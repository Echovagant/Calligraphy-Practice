/**
 * 笔输入层组装：把 ink 纯函数模块接到 fabric 上。
 *
 * 架构决策（见 docs/superpowers/specs/2026-10-23-ink-input-layer-design.md）：
 * - **AD1** 输入层自研：原生 Pointer Events 采集 → 自算笔画轮廓 → 生成 fabric.Path 填色。
 *   fabric 退守为「场景与存储」：网格、已保存作品、导出。
 * - **AD2** `enablePointerEvents: true`：把 fabric 的输入通道从 mouse/touch 换成 pointer，
 *   避免「笔的 pointerdown + 兼容鼠标事件」双通道打架。
 * - **AD3** 变宽笔画用填充轮廓多边形，不用描边路径。
 * - **AD4** 笔输入相关算法都在 `src/utils/ink/*`（纯函数、可单测），这里只做接线。
 *
 * 核心原则：**骨架是真相，轮廓是派生产物。**
 */

import { ref, onMounted, onUnmounted, nextTick } from 'vue';
import * as fabric from 'fabric';

import { detectPointerCapability, CAPABILITY_MODE } from '../utils/ink/capability.js';
import { decide as palmDecide, createPalmState, PALM_POLICY } from '../utils/ink/palmGuard.js';
import {
  computeWidths,
  computeSpeeds,
  averageSpeed,
  buildOutlinePath
} from '../utils/ink/strokeGeometry.js';
import { scaleStrokesUniformly } from '../utils/ink/strokeScale.js';
import { createStrokeStore } from '../utils/ink/strokeStore.js';
import { createDraftStore, draftKey } from '../utils/ink/draftStore.js';
import { classifyStroke } from '../utils/ink/strokeClassifier.js';

/** 采样点抽稀阈值：距离 ≥1px 或间隔 ≥8ms 才留 */
const MIN_SAMPLE_DISTANCE = 1;
const MIN_SAMPLE_INTERVAL = 8;

/** 默认每行格子数（无字帖时） */
const DEFAULT_CHARS_PER_ROW = 10;
const MIN_CHARS_PER_ROW = 4;
const MAX_CHARS_PER_ROW = 20;

export function useInkCanvas(options = {}) {
  const {
    canvasRef,
    containerRef,
    getBrushSize = () => 5,
    getBrushColor = () => '#000000',
    getBrushOpacity = () => 0.8,
    getGridType = () => 'square',
    getCharactersPerPage = () => 0,
    getDraftScope = () => ({}),
    palmPolicy = PALM_POLICY.PEN_FIRST,
    onStrokeCommitted = null,
    onCanvasReady = null,
    onCapabilityDetected = null
  } = options;

  const canvas = ref(null);
  const size = ref({ width: 0, height: 0, dpr: 1 });
  const capability = ref({
    mode: CAPABILITY_MODE.TOUCH,
    hasPressure: false,
    pointerType: ''
  });
  const strokeCount = ref(0);
  // 撤销/重做可用性必须自己存成 ref：strokeStore 是普通对象（不是响应式的），
  // 直接 computed(() => store.size > 0) 永远不会重算，按钮会一直停在初始的禁用态。
  const canUndo = ref(false);
  const canRedo = ref(false);
  /** 刚写完那一笔的走向判别结果（横/竖/撇/捺/点），供练习页给出动作提示 */
  const lastStroke = ref(null);

  const store = createStrokeStore();
  let draft = null;

  /** 把笔画栈的状态同步到响应式 ref（store 不是响应式的，改动必须手动同步） */
  const syncStoreState = () => {
    strokeCount.value = store.size;
    canUndo.value = store.size > 0;
    canRedo.value = store.redoStack.length > 0;
  };
  let palmState = createPalmState();
  let gridObject = null;
  let previewObject = null;
  let activePoints = [];
  let activePointerId = null;
  let resizeObserver = null;
  let shiftDown = false;
  let disposed = false;
  let lastSize = { width: 0, height: 0 };
  let twoFingerTapCount = 0;
  let twoFingerTapTimer = null;

  // ---------------------------------------------------------------- 坐标

  /** 事件 → 场景坐标（fabric v6 的 getPointer 已废弃，用 getScenePoint） */
  const toScenePoint = (instance, event) => {
    if (typeof instance.getScenePoint === 'function') {
      const point = instance.getScenePoint(event);
      return { x: point.x, y: point.y };
    }
    const point = instance.getPointer(event);
    return { x: point.x, y: point.y };
  };

  // ---------------------------------------------------------------- 能力

  /**
   * 动态观察指针事件。
   * 能力探测是「一次性采样」，用户可能先摸鼠标后拿笔，所以这里按事件实时修正：
   * 出现过 pen，或者压力取值不在 {0, 0.5, 1}，就认为有真压感。
   */
  const observePointer = (event) => {
    const pointerType = event.pointerType || 'mouse';
    const pressure = Number(event.pressure);
    const realPressure = Number.isFinite(pressure) && pressure !== 0 && pressure !== 0.5 && pressure !== 1;
    const next = {
      mode: pointerType === 'pen' ? CAPABILITY_MODE.PEN : capability.value.hasPressure || realPressure ? CAPABILITY_MODE.POINTER : CAPABILITY_MODE.TOUCH,
      hasPressure: capability.value.hasPressure || pointerType === 'pen' || realPressure,
      pointerType
    };
    if (
      next.mode !== capability.value.mode ||
      next.hasPressure !== capability.value.hasPressure ||
      next.pointerType !== capability.value.pointerType
    ) {
      capability.value = next;
      if (onCapabilityDetected) onCapabilityDetected(next);
    }
  };

  // ---------------------------------------------------------------- 笔画

  const strokeOptions = () => {
    const speeds = computeSpeeds(activePoints);
    return {
      baseWidth: getBrushSize(),
      hasPressure: capability.value.hasPressure,
      averageSpeed: averageSpeed(speeds),
      shiftKey: shiftDown
    };
  };

  /** 骨架 → fabric.Path（轮廓是派生结果，不入库） */
  const buildStrokePath = (stroke) => {
    const widths =
      stroke.widths && stroke.widths.length ? stroke.widths : computeWidths(stroke.points, strokeOptions()).widths;
    const { d } = buildOutlinePath(stroke.points, widths);
    if (!d) return null;
    return new fabric.Path(d, {
      fill: stroke.color || '#000000',
      stroke: null,
      strokeWidth: 0,
      opacity: stroke.opacity ?? 1,
      selectable: false,
      evented: false,
      data: { type: 'ink-stroke' }
    });
  };

  const createPreview = () => {
    return new fabric.Polyline(
      activePoints.map((point) => ({ x: point.x, y: point.y })),
      {
        fill: null,
        stroke: getBrushColor(),
        strokeWidth: getBrushSize(),
        strokeLineCap: 'round',
        strokeLineJoin: 'round',
        opacity: getBrushOpacity(),
        selectable: false,
        evented: false,
        data: { type: 'ink-preview' }
      }
    );
  };

  const updatePreview = () => {
    if (!previewObject || !canvas.value) return;
    previewObject.set({
      points: activePoints.map((point) => ({ x: point.x, y: point.y }))
    });
    previewObject.setCoords();
    canvas.value.requestRenderAll();
  };

  /** 用骨架重新渲染全部笔画（尺寸变化 / 撤销重做后调用） */
  const redrawAll = () => {
    const instance = canvas.value;
    if (!instance) return;
    const strokes = instance.getObjects().filter((object) => object.data?.type === 'ink-stroke');
    strokes.forEach((object) => instance.remove(object));
    store.strokes.forEach((stroke) => {
      const path = buildStrokePath(stroke);
      if (path) instance.add(path);
    });
    if (gridObject) instance.sendObjectToBack(gridObject);
    instance.requestRenderAll();
  };

  // ---------------------------------------------------------------- 网格

  /** 每行格子数：字帖的 charactersPerPage 是「每页字数」，开方后才是每行字数 */
  const charsPerRow = () => {
    const total = Number(getCharactersPerPage()) || 0;
    if (total <= 0) return DEFAULT_CHARS_PER_ROW;
    const estimated = Math.round(Math.sqrt(total));
    return Math.min(MAX_CHARS_PER_ROW, Math.max(MIN_CHARS_PER_ROW, estimated));
  };

  const drawGrid = () => {
    const instance = canvas.value;
    if (!instance) return;
    const { width, height } = size.value;
    if (!width || !height) return;

    if (gridObject) {
      instance.remove(gridObject);
      gridObject = null;
    }

    const gridType = getGridType();
    if (gridType === 'none') {
      instance.requestRenderAll();
      return;
    }

    const cell = Math.floor(width / charsPerRow());
    if (!cell) {
      instance.requestRenderAll();
      return;
    }

    const gridColor = '#dddddd';
    const objects = [];

    const makeLine = (x1, y1, x2, y2, strokeWidth) => {
      const line = new fabric.Line([x1, y1, x2, y2], {
        stroke: gridColor,
        strokeWidth,
        selectable: false,
        evented: false
      });
      objects.push(line);
    };

    if (gridType === 'square' || gridType === 'horizontal' || gridType === 'cross') {
      for (let y = 0; y <= height; y += cell) makeLine(0, y, width, y, 0.5);
    }
    if (gridType === 'square' || gridType === 'cross') {
      for (let x = 0; x <= width; x += cell) makeLine(x, 0, x, height, 0.5);
    }
    // 田字格：在方格基础上补两条对角线 —— 练字最常用的格子
    if (gridType === 'cross') {
      for (let row = 0; row * cell < height; row += 1) {
        for (let col = 0; col * cell < width; col += 1) {
          const x = col * cell;
          const y = row * cell;
          makeLine(x, y, x + cell, y + cell, 0.4);
          makeLine(x, y + cell, x + cell, y, 0.4);
        }
      }
    }

    gridObject = new fabric.Group(objects, {
      selectable: false,
      evented: false,
      data: { type: 'grid' }
    });
    instance.add(gridObject);
    instance.sendObjectToBack(gridObject);
    instance.requestRenderAll();
  };

  // ---------------------------------------------------------------- 尺寸

  const resize = () => {
    const instance = canvas.value;
    if (!instance) return;
    const container = containerRef?.value;
    const rect = container
      ? container.getBoundingClientRect()
      : instance.upperCanvasEl.getBoundingClientRect();
    const cssWidth = Math.max(1, Math.floor(rect.width));
    const cssHeight = Math.max(1, Math.floor(rect.height));
    const dpr = window.devicePixelRatio || 1;

    // 骨架随尺寸等比缩放：横竖屏切换不丢笔迹、不变形
    if (lastSize.width && lastSize.height) {
      const scaled = scaleStrokesUniformly(store.strokes, lastSize, {
        width: cssWidth,
        height: cssHeight
      });
      // 尺寸没变时纯函数原样返回同一个数组引用 → 这里跳过无谓的 store.restore
      if (scaled !== store.strokes) {
        store.restore({ strokes: scaled });
      }
    }
    lastSize = { width: cssWidth, height: cssHeight };

    // fabric 自己按 devicePixelRatio 做 retina 缩放：setDimensions 会把 backstore
    // 设成 css × dpr、this.width 设成 css 像素。
    // 这里**不能**再手动乘一次 DPR、也不能再设 viewportTransform ——
    // 两者叠加会让渲染分辨率变成 dpr²（在 iPad 上 dpr=2 就是 4 倍像素，纯浪费显存与带宽）。
    // viewportTransform 保持单位矩阵，getScenePoint 返回的就是 CSS 像素。
    instance.setDimensions({ width: cssWidth, height: cssHeight });

    size.value = { width: cssWidth, height: cssHeight, dpr };
    drawGrid();
    redrawAll();
  };

  // ---------------------------------------------------------------- 草稿

  const persistDraft = () => {
    if (!draft) return;
    draft.save(store.serialize());
  };

  const flushDraft = () => {
    if (!draft) return;
    draft.flush();
  };

  // ---------------------------------------------------------------- 指针事件

  const onPointerDown = (event) => {
    const instance = canvas.value;
    if (!instance || activePointerId !== null) return;
    // preventDefault 会同时挡掉浏览器的兼容鼠标事件，避免双通道重复落笔
    if (typeof event.preventDefault === 'function') event.preventDefault();
    observePointer(event);

    const decision = palmDecide(palmState, event, Date.now(), palmPolicy);
    palmState = decision.nextState;
    if (decision.action !== 'draw') return;

    activePointerId = event.pointerId;
    try {
      instance.upperCanvasEl.setPointerCapture(event.pointerId);
    } catch {
      // 某些浏览器不支持指针捕获，忽略即可
    }

    const point = toScenePoint(instance, event);
    activePoints = [{ x: point.x, y: point.y, p: Number(event.pressure) || 0, t: event.timeStamp }];
    previewObject = createPreview();
    instance.add(previewObject);
    instance.requestRenderAll();
  };

  const onPointerMove = (event) => {
    if (activePointerId === null || event.pointerId !== activePointerId) return;
    if (typeof event.preventDefault === 'function') event.preventDefault();
    observePointer(event);

    const instance = canvas.value;
    const point = toScenePoint(instance, event);
    const last = activePoints[activePoints.length - 1];
    if (!last) return;
    const distance = Math.hypot(point.x - last.x, point.y - last.y);
    const elapsed = event.timeStamp - last.t;
    // 采样点抽稀：距离 ≥1px 或间隔 ≥8ms 才留
    if (distance < MIN_SAMPLE_DISTANCE && elapsed < MIN_SAMPLE_INTERVAL) return;

    activePoints.push({ x: point.x, y: point.y, p: Number(event.pressure) || 0, t: event.timeStamp });
    updatePreview();
  };

  const finishStroke = (event) => {
    if (activePointerId === null || event.pointerId !== activePointerId) return;
    if (typeof event.preventDefault === 'function') event.preventDefault();

    const instance = canvas.value;
    try {
      instance.upperCanvasEl.releasePointerCapture(event.pointerId);
    } catch {
      // 忽略
    }
    activePointerId = null;

    if (previewObject) {
      instance.remove(previewObject);
      previewObject = null;
    }
    if (activePoints.length === 0) return;

    const { widths } = computeWidths(activePoints, strokeOptions());
    const stroke = {
      points: activePoints,
      widths,
      color: getBrushColor(),
      opacity: getBrushOpacity()
    };
    activePoints = [];

    if (!store.push(stroke)) return;
    const path = buildStrokePath(stroke);
    if (path) instance.add(path);
    if (gridObject) instance.sendObjectToBack(gridObject);
    instance.requestRenderAll();

    const classification = classifyStroke(stroke.points);
    lastStroke.value = {
      kind: classification.kind,
      confidence: classification.confidence,
      reason: classification.reason
    };

    syncStoreState();
    persistDraft();
    if (onStrokeCommitted) onStrokeCommitted(stroke);
  };

  /** 触摸端双指双击 → 撤销（桌面用 Ctrl/Cmd+Z） */
  const onTouchEnd = (event) => {
    if (!event.changedTouches || event.changedTouches.length < 2) return;
    twoFingerTapCount += 1;
    if (twoFingerTapTimer) clearTimeout(twoFingerTapTimer);
    twoFingerTapTimer = setTimeout(() => {
      if (twoFingerTapCount >= 2) {
        twoFingerTapCount = 0;
        undo();
      } else {
        twoFingerTapCount = 0;
      }
    }, 500);
  };

  const onKeyDown = (event) => {
    if (event.key === 'Shift') shiftDown = true;
    const isUndo = (event.ctrlKey || event.metaKey) && !event.shiftKey && event.key.toLowerCase() === 'z';
    const isRedo =
      (event.ctrlKey || event.metaKey) && (event.key.toLowerCase() === 'y' || (event.shiftKey && event.key.toLowerCase() === 'z'));
    if (isUndo) {
      event.preventDefault();
      undo();
    } else if (isRedo) {
      event.preventDefault();
      redo();
    }
  };

  const onKeyUp = (event) => {
    if (event.key === 'Shift') shiftDown = false;
  };

  /** 老 Safari 的双指缩放手势会和书写打架，单独拦掉 */
  const onGestureStart = (event) => {
    if (typeof event.preventDefault === 'function') event.preventDefault();
  };

  const onVisibilityChange = () => {
    if (document.visibilityState === 'hidden') flushDraft();
  };

  // ---------------------------------------------------------------- 操作

  const undo = () => {
    const stroke = store.undo();
    if (!stroke) return false;
    redrawAll();
    syncStoreState();
    persistDraft();
    return true;
  };

  const redo = () => {
    const stroke = store.redo();
    if (!stroke) return false;
    redrawAll();
    syncStoreState();
    persistDraft();
    return true;
  };

  const clear = () => {
    store.clear();
    redrawAll();
    syncStoreState();
    persistDraft();
  };

  const setBackground = (color) => {
    if (!canvas.value) return;
    canvas.value.backgroundColor = color;
    canvas.value.requestRenderAll();
  };

  const restoreDraft = () => {
    if (!draft) return false;
    const payload = draft.restore();
    if (!payload || !store.restore(payload)) return false;
    redrawAll();
    syncStoreState();
    return true;
  };

  // ---------------------------------------------------------------- 生命周期

  const init = () => {
    const el = canvasRef?.value;
    if (!el || canvas.value) return;

    const instance = new fabric.Canvas(el, {
      // AD2：指针通道统一，避免兼容鼠标事件与笔事件双通道
      enablePointerEvents: true,
      // AD1：不用 fabric 的笔刷模型
      isDrawingMode: false,
      selection: false,
      skipTargetFind: true,
      preserveObjectStacking: true
    });
    canvas.value = instance;

    const upper = instance.upperCanvasEl;
    upper.style.touchAction = 'none';
    upper.addEventListener('pointerdown', onPointerDown);
    upper.addEventListener('pointermove', onPointerMove);
    upper.addEventListener('pointerup', finishStroke);
    upper.addEventListener('pointercancel', finishStroke);
    upper.addEventListener('touchend', onTouchEnd, { passive: true });
    upper.addEventListener('gesturestart', onGestureStart);

    window.addEventListener('keydown', onKeyDown);
    window.addEventListener('keyup', onKeyUp);
    document.addEventListener('visibilitychange', onVisibilityChange);

    draft = createDraftStore({ namespace: draftKey(getDraftScope()) });

    resize();
    restoreDraft();

    if (containerRef?.value && typeof ResizeObserver !== 'undefined') {
      resizeObserver = new ResizeObserver(() => resize());
      resizeObserver.observe(containerRef.value);
    }

    // 能力探测不阻塞交互：拿不到就靠运行时动态观察
    detectPointerCapability(upper).then((result) => {
      if (disposed) return;
      capability.value = result;
      if (onCapabilityDetected) onCapabilityDetected(result);
    });

    if (onCanvasReady) onCanvasReady(instance, size.value);
  };

  const destroy = () => {
    if (disposed) return;
    disposed = true;
    flushDraft();
    const instance = canvas.value;
    if (instance) {
      const upper = instance.upperCanvasEl;
      upper.removeEventListener('pointerdown', onPointerDown);
      upper.removeEventListener('pointermove', onPointerMove);
      upper.removeEventListener('pointerup', finishStroke);
      upper.removeEventListener('pointercancel', finishStroke);
      upper.removeEventListener('touchend', onTouchEnd);
      upper.removeEventListener('gesturestart', onGestureStart);
    }
    window.removeEventListener('keydown', onKeyDown);
    window.removeEventListener('keyup', onKeyUp);
    document.removeEventListener('visibilitychange', onVisibilityChange);
    if (twoFingerTapTimer) clearTimeout(twoFingerTapTimer);
    if (resizeObserver) {
      resizeObserver.disconnect();
      resizeObserver = null;
    }
    if (instance) {
      instance.dispose();
      canvas.value = null;
    }
  };

  onMounted(async () => {
    await nextTick();
    init();
  });

  onUnmounted(() => {
    destroy();
  });

  return {
    canvas,
    size,
    capability,
    strokeCount,
    canUndo,
    canRedo,
    lastStroke,
    init,
    destroy,
    undo,
    redo,
    clear,
    setBackground,
    drawGrid,
    redrawAll,
    resize,
    flushDraft,
    restoreDraft
  };
}
