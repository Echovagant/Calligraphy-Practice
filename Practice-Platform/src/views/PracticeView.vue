<template>
  <div class="practice-app">
    <div class="practice-stage" :class="{ 'is-writing': isWriting }">
      <!-- 画布铺满整个舞台。位置全靠 absolute，不占文档流，
           所以舞台高度就是画布高度。 -->
      <div class="canvas-wrap" ref="canvasWrapRef" @pointerdown="handleCanvasPointerDown">
        <canvas ref="canvasRef"></canvas>
        <!-- 笔顺引导的轨迹层：Teleport 进 fabric 自己的容器，
             与画布同父同滚，不会因为容器被滚动而错位 -->
        <Teleport :to="overlayHost" :disabled="!overlayHost">
          <svg
            v-if="guideStatus === 'ready'"
            class="guide-overlay"
            :viewBox="`0 0 ${size.width} ${size.height}`"
          >
            <path
              v-for="(s, i) in guideStrokes"
              :key="i"
              :d="strokePath(guidePath(s.points))"
              :class="['overlay-stroke', { done: i < guideIndex, active: i === guideIndex }]"
            />
            <circle
              v-if="guideCurrent"
              :cx="guidePath(guideCurrent.points)[0][0]"
              :cy="guidePath(guideCurrent.points)[0][1]"
              r="22"
              class="overlay-start"
            />
          </svg>
        </Teleport>
      </div>

      <!-- 顶部浮层：身份、进度、专注模式 -->
      <div class="stage-top">
        <div class="stage-top__left">
          <button type="button" class="icon-btn icon-btn--floating" aria-label="回到首页" @click="goHome">
            <el-icon><House /></el-icon>
          </button>
          <button
            type="button"
            class="chip chip--book"
            :aria-expanded="activeTool === 'book'"
            @click="toggleTool('book')"
          >
            <el-icon><Document /></el-icon>
            <span class="chip__title">{{ currentCopybook ? `《${currentCopybook.title}》` : '未选字帖' }}</span>
            <span v-if="currentCopybook" class="chip__meta">{{ currentCopybook.category || '未分类' }}</span>
          </button>
        </div>

        <div class="stage-top__right">
          <span v-if="guideStatus === 'ready'" class="chip chip--guide">
            {{ guideChar }} · {{ guideIndex < guideTotal ? `${guideIndex + 1}/${guideTotal} 笔` : `已完成 ${guideTotal} 笔` }}
          </span>
          <span class="chip chip--count">共 {{ strokeCount }} 笔</span>
          <button
            type="button"
            class="chip chip--action"
            :aria-pressed="immersive"
            @click="toggleImmersive"
          >
            <el-icon><FullScreen /></el-icon>
            <span>{{ immersive ? '退出专注' : '专注' }}</span>
          </button>
        </div>
      </div>

      <!-- 工具轨：竖排图标。窄屏改成底部横排（见样式表）。 -->
      <nav class="tool-rail" role="toolbar" aria-label="练习工具">
        <button
          v-for="tool in TOOLS"
          :key="tool.id"
          type="button"
          class="rail-btn"
          :class="{ 'is-active': activeTool === tool.id }"
          :aria-pressed="activeTool === tool.id"
          :aria-label="tool.label"
          :title="tool.label"
          @click="toggleTool(tool.id)"
        >
          <el-icon><component :is="tool.icon" /></el-icon>
          <span class="rail-btn__text">{{ tool.label }}</span>
          <!-- 笔刷按钮上的徽标就是当前笔刷本身：颜色 + 粗细 -->
          <span v-if="tool.id === 'brush'" class="rail-btn__badge" :style="brushBadgeStyle"></span>
        </button>
      </nav>

      <!-- 遮罩：面板打开时，点面板以外的地方关掉面板。不做暗化，保持画布可读 -->
      <div v-if="activeTool" class="panel-scrim" @click="closeTool"></div>

      <!-- 工具面板：从工具轨一侧浮出，盖在画布上，不挤压画布 -->
      <Transition name="panel">
        <section
          v-if="activeTool"
          class="tool-panel"
          role="dialog"
          :aria-label="activeToolLabel"
        >
          <header class="tool-panel__head">
            <h3 class="tool-panel__title">{{ activeToolLabel }}</h3>
            <button type="button" class="icon-btn" aria-label="关闭面板" @click="closeTool">
              <el-icon><Close /></el-icon>
            </button>
          </header>

          <div class="tool-panel__body">
            <!-- 笔刷 -->
            <template v-if="activeTool === 'brush'">
              <div class="tool-group">
                <label class="tool-label" for="brush-size">笔刷大小</label>
                <el-slider
                  id="brush-size"
                  v-model="brushSize"
                  :min="1"
                  :max="20"
                  show-input
                  @change="scheduleBrushCollapse"
                />
              </div>
              <div class="tool-group">
                <label class="tool-label">笔刷颜色</label>
                <el-color-picker v-model="brushColor" :show-alpha="false" @change="scheduleBrushCollapse" />
              </div>
              <div class="tool-group">
                <label class="tool-label" for="brush-hardness">笔刷硬度</label>
                <el-slider
                  id="brush-hardness"
                  v-model="brushOpacity"
                  :min="0.1"
                  :max="1"
                  :step="0.1"
                  show-input
                  @change="scheduleBrushCollapse"
                />
              </div>
              <p class="tool-hint">{{ inputHint }}</p>
              <p class="tool-hint tool-hint--muted">改完自动收起，直接在画布上写。</p>
            </template>

            <!-- 笔画提示 -->
            <template v-else-if="activeTool === 'stroke'">
              <el-select
                v-model="manualStroke"
                placeholder="选择笔画查看规范写法"
                size="small"
                class="stroke-select"
                clearable
              >
                <el-option v-for="name in strokeNames" :key="name" :label="name" :value="name" />
              </el-select>

              <div v-if="strokeHint" class="stroke-hint">
                <div class="stroke-hint-head">
                  <span class="stroke-hint-title">{{ strokeHint.name }}</span>
                  <span class="stroke-hint-source">{{ strokeHint.source }}</span>
                </div>
                <ol class="stroke-hint-steps">
                  <li v-for="action in strokeHint.actions" :key="action.id">
                    <span class="stroke-hint-stage">{{ action.segment }}</span>
                    <span class="stroke-hint-text">{{ action.action }}</span>
                    <span v-if="action.measurable && action.measurable.stop_points && action.measurable.stop_points.length" class="stroke-hint-stops">
                      顿点：{{ action.measurable.stop_points.join('、') }}
                    </span>
                  </li>
                </ol>
              </div>
              <p v-else class="tool-hint">
                在画布上写一笔，这里会给出该笔画的规范写法：起笔、行笔、收笔分别怎么做、顿点落在哪。
              </p>
            </template>

            <!-- 笔顺引导 -->
            <template v-else-if="activeTool === 'guide'">
              <div v-if="guideStatus === 'ready'" class="guide-panel">
                <div class="guide-head">
                  <span class="guide-char">{{ guideChar }}</span>
                  <span class="guide-progress">
                    {{ guideIndex < guideTotal ? `第 ${guideIndex + 1} 笔 / 共 ${guideTotal} 笔` : `已完成 ${guideTotal} 笔` }}
                  </span>
                </div>
                <svg class="guide-preview" viewBox="0 0 1024 1024">
                  <path
                    v-for="(s, i) in guideStrokes"
                    :key="i"
                    :d="strokePath(s.points)"
                    :class="['guide-stroke', { done: i < guideIndex, active: i === guideIndex }]"
                  />
                  <circle
                    v-if="guideCurrent"
                    :cx="guideCurrent.points[0][0]"
                    :cy="guideCurrent.points[0][1]"
                    r="26"
                    class="guide-start"
                  />
                </svg>
                <p v-if="guideCurrent && guideCurrent.type" class="guide-type">
                  这一笔是「{{ guideCurrent.type }}」
                  <span v-if="guideCategoryName" class="guide-category">{{ guideCategoryName }}</span>
                </p>
                <div v-if="guideStructureGuide" class="guide-structure">
                  <div class="guide-structure-head">{{ guideStructureGuide.label }}要点</div>
                  <ul class="guide-structure-points">
                    <li v-for="(pt, i) in guideStructureGuide.points" :key="i">{{ pt }}</li>
                  </ul>
                </div>
                <p v-if="guideResult" :class="['guide-result', guideResult.ok ? 'ok' : 'bad']">
                  {{ guideResult.reason }}
                </p>
                <div class="guide-buttons">
                  <el-button size="small" @click="guideBack">上一笔</el-button>
                  <el-button size="small" @click="guideReset">重写</el-button>
                  <el-button size="small" type="primary" @click="stopGuide">结束引导</el-button>
                </div>
              </div>
              <div v-else>
                <el-select
                  v-model="guideTarget"
                  placeholder="选一个字开始笔顺引导"
                  size="small"
                  class="stroke-select"
                  filterable
                  @change="startGuide"
                >
                  <el-option v-for="ch in guideCandidates" :key="ch" :label="ch" :value="ch" />
                </el-select>
                <p v-if="guideStatus === 'error'" class="tool-hint guide-error">{{ guideError }}</p>
                <p v-else class="tool-hint">
                  开启后画布中央会显示这个字的笔顺：红点是这一笔的落笔位置，照着淡色轨迹写，写完一笔自动判下一笔。
                </p>
              </div>
            </template>

            <!-- 纸面 -->
            <template v-else-if="activeTool === 'paper'">
              <div class="tool-group">
                <label class="tool-label">宣纸颜色</label>
                <el-radio-group v-model="paperColor" size="small">
                  <el-radio-button label="#f5f5f0">米色</el-radio-button>
                  <el-radio-button label="#ffffff">白色</el-radio-button>
                  <el-radio-button label="#e8f5e9">淡绿</el-radio-button>
                </el-radio-group>
              </div>
              <div class="tool-group">
                <label class="tool-label">格子样式</label>
                <el-radio-group v-model="gridType" size="small">
                  <el-radio-button label="none">无</el-radio-button>
                  <el-radio-button label="square">方格</el-radio-button>
                  <el-radio-button label="cross">田字格</el-radio-button>
                  <el-radio-button label="horizontal">横线</el-radio-button>
                </el-radio-group>
              </div>
            </template>

            <!-- 字帖 -->
            <template v-else-if="activeTool === 'book'">
              <div v-if="currentCopybook" class="current-copybook">
                <div class="current-title">《{{ currentCopybook.title }}》</div>
                <div class="current-meta">
                  {{ currentCopybook.category || '未分类' }} · {{ formatDifficulty(currentCopybook.difficulty) }}
                  · 字帖字数 {{ currentCopybook.characterCount ?? 0 }}
                </div>
              </div>
              <div v-else class="current-empty">尚未选择参考字帖，请先从下方推荐字帖中挑选</div>

              <div v-if="referenceChars.length > 0" class="reference-list">
                <button
                  class="reference-item"
                  v-for="char in referenceChars"
                  :key="char"
                  type="button"
                  @click="showReference(char)"
                >
                  {{ char }}
                </button>
              </div>

              <h4 class="tool-subtitle">推荐字帖</h4>
              <el-alert
                v-if="recommendationsError"
                type="error"
                :title="recommendationsError"
                show-icon
                :closable="false"
                class="tool-alert"
              >
                <el-button size="small" type="primary" @click="loadRecommendations">重试</el-button>
              </el-alert>
              <div v-loading="recommendationsLoading" class="recommendation-list">
                <button
                  v-for="book in recommendations"
                  :key="book.id"
                  class="recommendation-item"
                  :class="{ active: currentCopybook && currentCopybook.id === book.id }"
                  type="button"
                  @click="selectCopybook(book)"
                >
                  <span class="recommendation-title">{{ book.title }}</span>
                  <span class="recommendation-desc">{{ book.description || '暂无简介' }}</span>
                </button>
              </div>
              <el-empty
                v-if="!recommendationsLoading && !recommendationsError && recommendations.length === 0"
                description="暂无推荐字帖"
                :image-size="60"
              />
            </template>
          </div>
        </section>
      </Transition>

      <!-- 底部操作条：高频动作常驻，不用展开面板 -->
      <div class="action-dock">
        <button
          type="button"
          class="dock-btn"
          :disabled="!canUndo"
          title="撤销（Ctrl / Cmd + Z）"
          @click="undoLast"
        >
          <el-icon><RefreshLeft /></el-icon>
          <span class="dock-btn__text">撤销</span>
        </button>
        <button
          type="button"
          class="dock-btn"
          :disabled="!canRedo"
          title="重做"
          @click="redoLast"
        >
          <el-icon><RefreshRight /></el-icon>
          <span class="dock-btn__text">重做</span>
        </button>
        <button type="button" class="dock-btn" title="清空画布" @click="clearCanvas">
          <el-icon><Delete /></el-icon>
          <span class="dock-btn__text">清空</span>
        </button>
        <button
          type="button"
          class="dock-btn dock-btn--primary"
          :disabled="isSaving"
          @click="saveImage"
        >
          <el-icon><Download /></el-icon>
          <span class="dock-btn__text">{{ isSaving ? '保存中' : '保存' }}</span>
        </button>
      </div>

      <!-- 参考字符显示 -->
      <div class="reference-display" v-if="showingReference">
        <div class="reference-char">{{ currentReference }}</div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, watch, computed, nextTick } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { ElMessage } from 'element-plus';
import {
  Delete,
  RefreshLeft,
  RefreshRight,
  Download,
  Brush,
  EditPen,
  Guide,
  Picture,
  Document,
  Close,
  FullScreen,
  House
} from '@element-plus/icons-vue';
import { usePracticeStore } from '../stores/practiceStore';
import { useUserStore } from '../stores/userStore';
import { formatDifficulty } from '../utils/constants';
import { downloadCanvasAsImage } from '../utils';
import { getToken, copybookAPI } from '../utils/api';
import { useInkCanvas } from '../composables/useInkCanvas.js';
import { useImmersiveMode } from '../composables/useImmersiveMode.js';
import { STROKE_DICTIONARY, findStroke } from '../data/strokeDictionary.js';
import { useStrokeOrder } from '../composables/useStrokeOrder.js';
import { strokeCount as strokeCountOf } from '../data/strokeOrderIndex.js';
import { STROKE_TYPES } from '../data/strokeTypes.js';
import { strokeTypeCategory } from '../data/strokeTypeMap.js';
import { characterStructure } from '../data/characterStructures.js';
import { structureGuide } from '../data/structureGuides.js';
import { transformStroke } from '../utils/ink/strokeMatch.js';

// 引入状态管理
const practiceStore = usePracticeStore();
const userStore = useUserStore();
const router = useRouter();
const route = useRoute();
const { immersive } = useImmersiveMode();

// 是否已登录：store 状态 + 本地是否留有令牌（与 GalleryView 同一套判断）
const isLoggedIn = computed(() => userStore.isLoggedIn || Boolean(getToken()));

// 画布相关
const canvasRef = ref(null);
const canvasWrapRef = ref(null);

// 笔刷设置
const brushSize = computed({
  get: () => practiceStore.practiceSettings.brushSize || 5,
  set: (value) => practiceStore.updatePracticeSettings({ brushSize: value })
});

const brushColor = computed({
  get: () => practiceStore.practiceSettings.brushColor || '#000000',
  set: (value) => practiceStore.updatePracticeSettings({ brushColor: value })
});

const brushOpacity = computed({
  get: () => practiceStore.practiceSettings.brushOpacity || 0.8,
  set: (value) => practiceStore.updatePracticeSettings({ brushOpacity: value })
});

const paperColor = computed({
  get: () => practiceStore.practiceSettings.paperColor || '#f5f5f0',
  set: (value) => practiceStore.updatePracticeSettings({ paperColor: value })
});

const gridType = computed({
  get: () => practiceStore.practiceSettings.gridType || 'square',
  set: (value) => practiceStore.updatePracticeSettings({ gridType: value })
});

// 当前参考的字帖（由推荐字帖选择而来）
const currentCopybook = computed(() => practiceStore.currentCopybook);

// 参考字符来自所选字帖自身的文字，不再使用前端写死的字表
const referenceChars = computed(() => {
  const book = currentCopybook.value;
  if (!book) return [];
  const source = [book.content, book.title, book.description].filter(Boolean).join('');
  return [...new Set(source.replace(/[^一-龥]/g, ''))].slice(0, 24);
});

const currentReference = ref('');
const showingReference = ref(false);

// 练习统计
const practiceStartTime = ref(null);
// 本次练习展示过的参考字 —— 即「练习字数」
const practicedChars = ref([]);
const isSaving = ref(false);

// 推荐字帖：数据来自后端字帖接口
const recommendations = ref([]);
const recommendationsLoading = ref(false);
const recommendationsError = ref('');

/**
 * 笔输入层。
 * 变宽笔画由「骨架 → 轮廓多边形」派生，撤销以一笔为单位，
 * 草稿落 localStorage，切后台不丢笔迹。
 */
const {
  canvas: inkCanvas,
  capability,
  size,
  strokeCount,
  canUndo,
  canRedo,
  lastStroke,
  undo,
  redo,
  clear: clearInk,
  setBackground,
  drawGrid,
  flushDraft
} = useInkCanvas({
  canvasRef,
  containerRef: canvasWrapRef,
  getBrushSize: () => brushSize.value,
  getBrushColor: () => brushColor.value,
  getBrushOpacity: () => brushOpacity.value,
  getGridType: () => gridType.value,
  getCharactersPerPage: () =>
    currentCopybook.value?.charactersPerPage || currentCopybook.value?.characterCount || 0,
  getDraftScope: () => ({
    userId: userStore.user?.id ?? null,
    copybookId: currentCopybook.value?.id ?? null
  }),
  onStrokeCommitted: (stroke) => handleStrokeCommitted(stroke)
});

/**
 * 笔顺引导：画布中央显示规范轨迹，用户照着写，写完一笔自动判定并跳下一笔。
 * 轨迹来自 1024×1024 的字形画布，这里换算到画布中央的一个方格。
 */
const {
  char: guideChar,
  strokes: guideStrokes,
  index: guideIndex,
  total: guideTotal,
  current: guideCurrent,
  status: guideStatus,
  error: guideError,
  result: guideResult,
  load: loadGuide,
  check: checkGuide,
  advance: advanceGuide,
  back: guideBack,
  reset: guideReset
} = useStrokeOrder();

/** 可引导的字：参考字帖里的字 + 已人工校核过笔画类型的高频字 */
const guideCandidates = computed(() => {
  const set = new Set(referenceChars.value);
  for (const ch of Object.keys(STROKE_TYPES)) set.add(ch);
  return [...set].filter((ch) => strokeCountOf(ch) > 0);
});

/**
 * 引导用的方格：画布中央，占短边的 72%。
 *
 * 全程用 **fabric 逻辑坐标**（= size，与笔迹 points 同一坐标系），不做任何 DOM 测量：
 * 轨迹层是盖在容器上的绝对定位 SVG，容器一旦被滚动（选下拉时浏览器会 scrollIntoView）
 * 就会和画布错位。逻辑坐标 + SVG viewBox 与容器等大，两边永远同步。
 */
const guideFrame = computed(() => {
  const w = size.value.width || 1024;
  const h = size.value.height || 1024;
  const s = Math.max(120, Math.min(w, h) * 0.72);
  return { x: (w - s) / 2, y: (h - s) / 2, size: s };
});

/** 轨迹点（1024 字形坐标）→ 画布逻辑坐标 */
const guidePath = (points) => transformStroke(points, guideFrame.value);

/** 轨迹层的挂载点：fabric 自己建的容器（与画布同父同滚） */
const overlayHost = ref(null);
watch(
  () => inkCanvas.value,
  (inst) => {
    overlayHost.value = inst?.wrapperEl || null;
  },
  { immediate: true }
);

/** 轨迹点 → SVG path */
const strokePath = (pts) => pts.map((p, i) => `${i ? 'L' : 'M'}${p[0]} ${p[1]}`).join(' ');

/** 当前笔的类型归到字典的哪一类，并把该类的笔画变体一并列出 */
const guideCategoryName = computed(() => {
  const type = guideCurrent.value?.type;
  if (!type) return '';
  const category = strokeTypeCategory(type);
  if (!category) return '';
  const names = STROKE_DICTIONARY.strokes
    .filter((s) => s.category === category)
    .map((s) => s.name)
    .filter((name, i, list) => list.indexOf(name) === i);
  return names.length ? names.join(' / ') : '';
});

const guideTarget = ref('');

/** 当前字的间架结构（依 IDS 分解判定）与对应的结构书写要领 */
const guideStructure = computed(() => characterStructure(guideChar.value));
const guideStructureGuide = computed(() => structureGuide(guideStructure.value));

/** 开始引导一个字的笔顺 */
const startGuide = async (ch) => {
  const ok = await loadGuide(ch);
  if (!ok) {
    ElMessage.warning(guideError.value || '这个字没有笔顺数据');
    return;
  }
  // 练习页整屏不滚动（.app-container--immersive 锁死高度），
  // 下拉定位不会再把整页顶走，这里不需要再补偿滚动位置。
  await nextTick();
  const wrap = canvasWrapRef.value;
  if (!wrap) return;
  wrap.scrollTop = 0;
  wrap.scrollLeft = 0;
};

/** 结束引导 */
const stopGuide = () => {
  guideReset();
  loadGuide('');
  guideTarget.value = '';
};

/**
 * 一笔写完 → 与规范轨迹比对。
 * 通过则推进到下一笔；不通过则保留当前笔，用户可以重写。
 */
function handleStrokeCommitted(stroke) {
  if (guideStatus.value !== 'ready' || !guideCurrent.value) return;
  const written = (stroke.points || []).map((p) => [p.x, p.y]);
  if (written.length < 2) return;
  const result = checkGuide(written, { frame: guideFrame.value });
  if (result.ok) {
    advanceGuide();
    if (guideIndex.value >= guideTotal.value) {
      ElMessage.success(`「${guideChar.value}」的 ${guideTotal.value} 笔都写对了`);
    }
  }
}

/** 输入设备提示：让用户知道当前这一笔是被当成什么写的 */
const inputHint = computed(() => {
  if (capability.value.pointerType === 'pen') {
    return capability.value.hasPressure
      ? '检测到压感笔，笔迹粗细随力度变化。'
      : '检测到压感笔，但设备未上报压力，笔迹粗细按运笔速度变化。';
  }
  if (capability.value.pointerType === 'mouse') {
    return '当前是鼠标输入，笔迹粗细按运笔速度变化（按住 Shift 可加粗）。';
  }
  return '未检测到笔或鼠标，可用手指书写。';
});

/** 字典里有的笔画名（用于下拉选择） */
const strokeNames = STROKE_DICTIONARY.strokes
  .map((stroke) => stroke.name)
  .filter((name, index, list) => name && list.indexOf(name) === index)
  .sort((a, b) => a.localeCompare(b, 'zh-Hans-CN'));

/** 手动选择的笔画（没写笔时也能查） */
const manualStroke = ref('');

/**
 * 笔画提示：优先用「刚写完那一笔」的走向判别结果，没有就用手选的。
 * 提示内容全部来自字幕抽取，附出处（分P + 时间码），原话可回查。
 */
const strokeHint = computed(() => {
  const kind = lastStroke.value?.kind || manualStroke.value;
  if (!kind) return null;
  const stroke = findStroke(kind);
  if (!stroke || !stroke.actions.length) return null;
  const source = stroke.source || {};
  const origin = source.title ? `${source.title}${source.t ? ' · ' + source.t : ''}` : '出处未记录';
  return {
    name: stroke.name,
    actions: stroke.actions,
    source: origin
  };
});

const loadRecommendations = async () => {
  recommendationsLoading.value = true;
  recommendationsError.value = '';
  try {
    const payload = await practiceStore.getRecommendations();
    const list = Array.isArray(payload)
      ? payload
      : (Array.isArray(payload?.content) ? payload.content : []);
    recommendations.value = list.slice(0, 6);
  } catch (error) {
    recommendations.value = [];
    recommendationsError.value = error?.message || '获取推荐字帖失败';
    ElMessage.error(recommendationsError.value);
    console.error('获取推荐字帖失败:', error);
  } finally {
    recommendationsLoading.value = false;
  }
};

const selectCopybook = (book) => {
  practiceStore.setCurrentCopybook(book);
  ElMessage.success(`已选择《${book.title || '未命名字帖'}》作为参考字帖`);
  closeTool();
};

// 从「我的字帖」带过来的字帖：GET /api/copybooks/{copybookId}
const loadCopybookFromQuery = async () => {
  // 同一个参数出现多次时 query 里拿到的是数组，取第一个就行
  const raw = route.query.copybookId;
  const copybookId = Array.isArray(raw) ? raw[0] : raw;
  if (!copybookId) return;
  try {
    const book = await copybookAPI.getCopybookDetail(copybookId);
    if (book && book.id) {
      practiceStore.setCurrentCopybook(book);
    }
  } catch (error) {
    ElMessage.error(error?.message || '加载指定字帖失败，请稍后重试');
    console.error('加载指定字帖失败:', error);
  }
};

// 撤销
const undoLast = () => {
  if (!undo()) {
    ElMessage.warning('没有可撤销的笔画');
  }
};

// 重做
const redoLast = () => {
  if (!redo()) {
    ElMessage.warning('没有可重做的笔画');
  }
};

// 清空画布
const clearCanvas = () => {
  clearInk();
  ElMessage.success('画布已清空');
};

// 保存图片
const saveImage = async () => {
  if (!inkCanvas.value) return;
  // 未登录时不要直接 POST —— api.js 拿不到令牌会把用户整页踢去登录页，
  // 用户莫名其妙被赶走。先给提示，再走路由内跳转。
  if (!isLoggedIn.value) {
    ElMessage.warning('请先登录后再保存作品');
    router.push('/login');
    return;
  }
  isSaving.value = true;
  try {
    const dataURL = inkCanvas.value.toDataURL({ format: 'png', quality: 1 });
    const fileName = `书法作品_${new Date().getTime()}.png`;

    // 保存到store（store 读 imageDataUrl 里的 data URL 字符串）
    await practiceStore.saveWork({
      title: `作品 ${new Date().toLocaleString()}`,
      imageDataUrl: dataURL,
      imageUrl: dataURL,
      duration: calculatePracticeTime(),
      strokes: strokeCount.value
    });

    // 下载图片
    downloadCanvasAsImage(inkCanvas.value, fileName);
    ElMessage.success('作品已保存');
  } catch (error) {
    ElMessage.error('作品保存失败，请稍后重试');
    console.error('保存作品失败:', error);
  } finally {
    isSaving.value = false;
  }
};

// 计算练习时间（秒，与契约的 duration 一致）
const calculatePracticeTime = () => {
  if (!practiceStartTime.value) return 0;
  return Math.floor((Date.now() - practiceStartTime.value) / 1000);
};

// 显示参考字符
const showReference = (char) => {
  currentReference.value = char;
  showingReference.value = true;
  practicedChars.value.push(char);
  // 看完参考字就该写字了 —— 把字帖面板收起来
  closeTool();
  setTimeout(() => {
    showingReference.value = false;
  }, 3000); // 3秒后自动隐藏
};

// 监听设置变化
watch(paperColor, (newColor) => {
  setBackground(newColor);
});

watch(gridType, () => {
  drawGrid();
});

watch(currentCopybook, () => {
  // 换字帖 → 每行格子数变了，网格要重画
  drawGrid();
});

// 结束练习时保存记录
const endPractice = async () => {
  if (!practiceStartTime.value) return;

  const duration = calculatePracticeTime();
  const book = practiceStore.currentCopybook;

  if (duration > 0 || strokeCount.value > 0) {
    if (!book || !book.id) {
      ElMessage.warning('未选择参考字帖，本次练习未记录到服务器');
    } else if (!userStore.isLoggedIn) {
      ElMessage.warning('请先登录后再保存练习记录');
    } else {
      try {
        // 契约的 PracticeRecordCreate：copybookId 走路径，其余是请求体字段
        await practiceStore.addPracticeRecord({
          copybookId: book.id,
          title: book.title,
          content: practicedChars.value.join(''),
          category: book.category,
          difficulty: book.difficulty,
          duration,
          characterCount: practicedChars.value.length,
          strokes: strokeCount.value,
          notes: ''
        });
      } catch (error) {
        ElMessage.error('练习记录保存失败，请稍后重试');
        console.error('保存练习记录失败:', error);
      }
    }
  }

  practiceStartTime.value = null;
  practicedChars.value = [];
};

/* ------------------------------------------------------------------ 工具面板 */

// 工具轨上的入口，按使用频率排：写的时候最先碰的是笔刷
const TOOLS = [
  { id: 'brush', label: '笔刷', icon: Brush },
  { id: 'guide', label: '笔顺引导', icon: Guide },
  { id: 'book', label: '字帖', icon: Document },
  { id: 'stroke', label: '笔画提示', icon: EditPen },
  { id: 'paper', label: '纸面', icon: Picture }
];

const activeTool = ref(null);

const activeToolLabel = computed(
  () => TOOLS.find((tool) => tool.id === activeTool.value)?.label || ''
);

/** 笔刷按钮上的徽标 = 当前笔刷本身（颜色 + 粗细），改完不用看面板也知道结果 */
const brushBadgeStyle = computed(() => ({
  '--badge-color': brushColor.value,
  '--badge-size': `${Math.min(12, 5 + brushSize.value * 0.4)}px`
}));

const toggleTool = (id) => {
  activeTool.value = activeTool.value === id ? null : id;
};

const closeTool = () => {
  activeTool.value = null;
};

/**
 * 笔刷设置改完自动收起。
 *
 * 延时 450ms 而不是立刻收：滑块松手瞬间面板就消失，用户会以为没生效；
 * 留半秒让新值在面板里显一下，再收。当前笔刷同时显示在工具轨按钮的徽标上。
 */
const BRUSH_COLLAPSE_DELAY = 450;
let brushCollapseTimer = null;

const scheduleBrushCollapse = () => {
  if (brushCollapseTimer) clearTimeout(brushCollapseTimer);
  brushCollapseTimer = setTimeout(() => {
    brushCollapseTimer = null;
    if (activeTool.value === 'brush') activeTool.value = null;
  }, BRUSH_COLLAPSE_DELAY);
};

const toggleImmersive = () => {
  immersive.value = !immersive.value;
};

const goHome = () => {
  router.push('/');
};

/* ------------------------------------------------------------------ 书写态 */

// 落笔时把浮层压暗：写的时候眼睛只看笔画，不看控件
const isWriting = ref(false);

const handleCanvasPointerDown = () => {
  isWriting.value = true;
};

const handlePointerUp = () => {
  isWriting.value = false;
};

const handleKeyDown = (event) => {
  if (event.key === 'Escape' && activeTool.value) {
    closeTool();
  }
};

onMounted(() => {
  nextTick(() => {
    // 从store加载设置
    practiceStore.loadLocalSettings();
    setBackground(paperColor.value);
    // 记录开始练习时间
    practiceStartTime.value = new Date();
    // 获取推荐练习内容
    loadRecommendations();
    // 从「我的字帖」跳转过来时，直接选中那一份字帖
    loadCopybookFromQuery();
  });

  window.addEventListener('pointerup', handlePointerUp);
  window.addEventListener('pointercancel', handlePointerUp);
  window.addEventListener('keydown', handleKeyDown);
});

// 组件卸载时清理
onUnmounted(() => {
  endPractice();
  // 草稿立刻落盘，别让切页面丢笔迹
  flushDraft();
  window.removeEventListener('pointerup', handlePointerUp);
  window.removeEventListener('pointercancel', handlePointerUp);
  window.removeEventListener('keydown', handleKeyDown);
  if (brushCollapseTimer) clearTimeout(brushCollapseTimer);
});
</script>

<style scoped>
/* ================================================================
   练习工作区：画布铺满，控件浮在上面。
   舞台用 position:relative + 绝对定位的子层，
   画布因此能拿到舞台的全部尺寸，不受任何面板宽度影响。
   ================================================================ */

.practice-app {
  height: 100%;
  min-height: 0;
}

.practice-stage {
  position: relative;
  height: 100%;
  overflow: hidden;
  /* 舞台底色：宣纸灰。画布铺满后基本看不到，只在画布边缘露一线 */
  background: #efe8d8;
}

/* 画布铺满舞台 */
.canvas-wrap {
  position: absolute;
  inset: 0;
  overflow: clip;
  /* 触摸滚动只在这里关掉：工具条与弹窗仍然要能滚。
     touch-action: none 同时让 iOS 不把书写当成滚动手势。 */
  touch-action: none;
  -webkit-user-select: none;
  user-select: none;
}

/* ---------------- 顶部浮层 ---------------- */

.stage-top {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 0.75rem;
  padding: 0.75rem 0.9rem;
  pointer-events: none;
  transition: opacity 0.22s ease;
}

.stage-top__left,
.stage-top__right {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  min-width: 0;
  pointer-events: auto;
}

.stage-top__left {
  flex: 1 1 auto;
}

.stage-top__right {
  flex: none;
}

.chip {
  display: inline-flex;
  align-items: center;
  gap: 0.4rem;
  height: 36px;
  padding: 0 0.7rem;
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.94);
  border: 1px solid rgba(139, 69, 19, 0.16);
  box-shadow: 0 2px 8px rgba(60, 45, 30, 0.12);
  color: #4a4038;
  font: inherit;
  font-size: 13px;
  white-space: nowrap;
}

button.chip {
  cursor: pointer;
  transition: border-color 0.2s ease, box-shadow 0.2s ease;
}

button.chip:hover {
  border-color: rgba(139, 69, 19, 0.4);
  box-shadow: 0 3px 12px rgba(60, 45, 30, 0.18);
}

.chip--book {
  max-width: min(46vw, 420px);
  overflow: hidden;
}

.chip__title {
  font-weight: 600;
  color: var(--primary-color);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.chip__meta {
  color: #6b6259;
  font-size: 12px;
  padding-left: 0.5rem;
  border-left: 1px solid rgba(139, 69, 19, 0.18);
  overflow: hidden;
  text-overflow: ellipsis;
}

.chip--guide {
  background: rgba(214, 72, 60, 0.1);
  border-color: rgba(214, 72, 60, 0.3);
  color: #9c3629;
  font-weight: 600;
}

.chip--count {
  color: #6b6259;
}

.chip--action {
  color: var(--primary-color);
}

.chip--action[aria-pressed='true'] {
  background: var(--primary-color);
  border-color: var(--primary-color);
  color: #fff;
}

.icon-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  border-radius: 8px;
  border: 1px solid transparent;
  background: transparent;
  color: #4a4038;
  cursor: pointer;
  transition: background-color 0.2s ease, color 0.2s ease;
}

.icon-btn:hover {
  background: rgba(139, 69, 19, 0.1);
  color: var(--primary-color);
}

.icon-btn--floating {
  width: 36px;
  height: 36px;
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.94);
  border: 1px solid rgba(139, 69, 19, 0.16);
  box-shadow: 0 2px 8px rgba(60, 45, 30, 0.12);
  flex: none;
}

/* ---------------- 工具轨 ---------------- */

.tool-rail {
  position: absolute;
  left: 14px;
  top: 50%;
  transform: translateY(-50%);
  display: flex;
  flex-direction: column;
  gap: 6px;
  padding: 6px;
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.94);
  border: 1px solid rgba(139, 69, 19, 0.16);
  box-shadow: 0 6px 20px rgba(60, 45, 30, 0.16);
  transition: opacity 0.22s ease;
  z-index: 3;
}

.rail-btn {
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 2px;
  width: 52px;
  height: 52px;
  border-radius: 12px;
  border: 1px solid transparent;
  background: transparent;
  color: #5a5048;
  cursor: pointer;
  transition: background-color 0.2s ease, color 0.2s ease, border-color 0.2s ease;
}

.rail-btn .el-icon {
  font-size: 19px;
}

.rail-btn__text {
  font-size: 10px;
  line-height: 1;
  letter-spacing: 0.2px;
}

.rail-btn:hover {
  background: rgba(139, 69, 19, 0.1);
  color: var(--primary-color);
}

.rail-btn.is-active {
  background: var(--primary-color);
  border-color: var(--primary-color);
  color: #fff;
}

/* 笔刷徽标：颜色是当前笔色，尺寸随笔刷粗细变化。
   放右上角 —— 放右下角会压在按钮的文字标签上。 */
.rail-btn__badge {
  position: absolute;
  right: 3px;
  top: 3px;
  width: var(--badge-size, 10px);
  height: var(--badge-size, 10px);
  border-radius: 50%;
  background: var(--badge-color, #000);
  border: 1.5px solid #fff;
  box-shadow: 0 0 0 1px rgba(60, 45, 30, 0.25);
}

/* ---------------- 遮罩与面板 ---------------- */

.panel-scrim {
  position: absolute;
  inset: 0;
  z-index: 4;
  background: transparent;
}

.tool-panel {
  position: absolute;
  left: 88px;
  top: 50%;
  transform: translateY(-50%);
  z-index: 5;
  width: 320px;
  max-height: calc(100% - 40px);
  display: flex;
  flex-direction: column;
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.97);
  border: 1px solid rgba(139, 69, 19, 0.18);
  box-shadow: 0 10px 34px rgba(50, 38, 26, 0.22);
  overflow: hidden;
}

.tool-panel__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 0.5rem;
  padding: 0.6rem 0.5rem 0.6rem 0.9rem;
  border-bottom: 1px solid rgba(139, 69, 19, 0.14);
  flex: none;
}

.tool-panel__title {
  font-size: 14px;
  font-weight: 700;
  color: var(--primary-color);
  font-family: 'SimSun', serif;
  letter-spacing: 1px;
}

.tool-panel__body {
  padding: 0.9rem;
  overflow-y: auto;
  overscroll-behavior: contain;
}

.tool-subtitle {
  font-size: 12px;
  font-weight: 600;
  color: #6b6259;
  margin: 0.9rem 0 0.5rem;
  padding-top: 0.7rem;
  border-top: 1px solid rgba(139, 69, 19, 0.12);
}

.tool-group {
  margin-bottom: 1rem;
}

.tool-group:last-of-type {
  margin-bottom: 0.5rem;
}

.tool-label {
  display: block;
  margin-bottom: 0.4rem;
  color: #6b6259;
  font-size: 12px;
}

.tool-hint {
  margin-top: 0.6rem;
  font-size: 12px;
  color: #6b6259;
  line-height: 1.65;
}

.tool-hint--muted {
  color: #8a8078;
  font-size: 11px;
}

.tool-alert {
  margin-bottom: 0.6rem;
}

/* 笔画提示 */
.stroke-select {
  width: 100%;
  margin-bottom: 0.7rem;
}

.stroke-hint {
  border-left: 3px solid var(--primary-color);
  padding: 0.5rem 0 0.5rem 0.7rem;
  background: rgba(139, 69, 19, 0.04);
}

.stroke-hint-head {
  display: flex;
  justify-content: space-between;
  align-items: baseline;
  gap: 0.5rem;
  margin-bottom: 0.4rem;
}

.stroke-hint-title {
  font-size: 0.92rem;
  font-weight: 600;
  color: var(--primary-color);
  font-family: 'SimSun', serif;
}

.stroke-hint-source {
  font-size: 0.68rem;
  color: #6b6259;
  text-align: right;
}

.stroke-hint-steps {
  margin: 0;
  padding-left: 1.1rem;
  font-size: 0.78rem;
  line-height: 1.7;
  color: #444;
}

.stroke-hint-stage {
  display: inline-block;
  min-width: 2.6em;
  margin-right: 0.4em;
  padding: 0 0.3em;
  font-size: 0.7rem;
  color: #fff;
  background: var(--primary-color);
  border-radius: 2px;
  text-align: center;
}

.stroke-hint-text {
  display: block;
}

.stroke-hint-stops {
  display: block;
  font-size: 0.72rem;
  color: #8f6524;
}

/* 当前参考字帖 */
.current-copybook {
  padding: 0.55rem 0.7rem;
  background-color: #faf7f0;
  border: 1px solid rgba(139, 69, 19, 0.16);
  border-radius: 6px;
  margin-bottom: 0.7rem;
}

.current-title {
  color: #3a332c;
  font-weight: 600;
  font-size: 0.86rem;
  margin-bottom: 0.25rem;
}

.current-meta {
  color: #6b6259;
  font-size: 0.75rem;
  line-height: 1.5;
}

.current-empty {
  color: #6b6259;
  font-size: 0.78rem;
  margin-bottom: 0.7rem;
}

/* 参考字 */
.reference-list {
  display: grid;
  grid-template-columns: repeat(6, 1fr);
  gap: 0.35rem;
}

.reference-item {
  aspect-ratio: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0;
  background-color: #faf7f0;
  border: 1px solid rgba(139, 69, 19, 0.16);
  border-radius: 4px;
  cursor: pointer;
  font-size: 1.05rem;
  color: #3a332c;
  font-family: 'SimSun', serif;
  transition: background-color 0.2s ease, border-color 0.2s ease;
}

.reference-item:hover {
  background-color: rgba(139, 69, 19, 0.12);
  border-color: var(--primary-color);
}

/* 推荐字帖 */
.recommendation-list {
  display: flex;
  flex-direction: column;
  gap: 0.4rem;
  min-height: 60px;
}

.recommendation-item {
  display: block;
  width: 100%;
  text-align: left;
  padding: 0.45rem 0.6rem;
  background-color: #faf7f0;
  border: 1px solid rgba(139, 69, 19, 0.14);
  border-radius: 5px;
  cursor: pointer;
  transition: border-color 0.2s ease, background-color 0.2s ease;
}

.recommendation-item:hover,
.recommendation-item.active {
  border-color: var(--primary-color);
  background-color: rgba(139, 69, 19, 0.07);
}

.recommendation-title {
  display: block;
  font-weight: 600;
  font-size: 0.84rem;
  color: #3a332c;
}

.recommendation-desc {
  display: block;
  color: #6b6259;
  font-size: 0.72rem;
  margin-top: 0.15rem;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* 笔顺引导 */
.guide-panel {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.guide-head {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
}

.guide-char {
  font-size: 28px;
  font-weight: 700;
  line-height: 1;
  font-family: 'SimSun', serif;
}

.guide-progress {
  font-size: 12px;
  color: #6b6259;
}

.guide-preview {
  width: 100%;
  max-width: 148px;
  align-self: center;
  background: #fdfcf7;
  border: 1px solid #e0ddd4;
  border-radius: 4px;
}

.guide-stroke {
  fill: none;
  stroke: #cdc8bc;
  stroke-width: 26;
  stroke-linecap: round;
  stroke-linejoin: round;
}

.guide-stroke.done {
  stroke: #7d9c7d;
}

.guide-stroke.active {
  stroke: #d6483c;
}

.guide-start {
  fill: #d6483c;
}

.guide-type {
  font-size: 13px;
  margin: 0;
}

.guide-structure {
  border-left: 2px solid #d8d3c6;
  padding-left: 8px;
}

.guide-structure-head {
  font-size: 12px;
  color: #6b6259;
  margin-bottom: 2px;
}

.guide-structure-points {
  margin: 0;
  padding-left: 16px;
  font-size: 12px;
  line-height: 1.6;
  color: #4a443c;
}

.guide-category {
  color: #888;
  font-size: 12px;
  margin-left: 6px;
}

.guide-result {
  font-size: 13px;
  margin: 0;
}

.guide-result.ok {
  color: #2f7d43;
}

.guide-result.bad {
  color: #c0392b;
}

.guide-buttons {
  display: flex;
  gap: 6px;
}

.guide-error {
  color: #c0392b;
}

/* ---------------- 底部操作条 ---------------- */

.action-dock {
  position: absolute;
  right: 14px;
  bottom: 14px;
  z-index: 3;
  display: flex;
  gap: 4px;
  padding: 5px;
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.94);
  border: 1px solid rgba(139, 69, 19, 0.16);
  box-shadow: 0 6px 20px rgba(60, 45, 30, 0.16);
  transition: opacity 0.22s ease;
}

.dock-btn {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 1px;
  min-width: 54px;
  height: 46px;
  padding: 0 0.5rem;
  border-radius: 9px;
  border: 1px solid transparent;
  background: transparent;
  color: #4a4038;
  cursor: pointer;
  font: inherit;
  transition: background-color 0.2s ease, color 0.2s ease, opacity 0.2s ease;
}

.dock-btn .el-icon {
  font-size: 17px;
}

.dock-btn__text {
  font-size: 10px;
  line-height: 1;
}

.dock-btn:hover:not(:disabled) {
  background: rgba(139, 69, 19, 0.1);
  color: var(--primary-color);
}

.dock-btn:disabled {
  color: #b3aaa1;
  cursor: not-allowed;
}

.dock-btn--primary {
  background: var(--primary-color);
  color: #fff;
}

.dock-btn--primary:hover:not(:disabled) {
  background: #6f370f;
  color: #fff;
}

.dock-btn--primary:disabled {
  background: rgba(139, 69, 19, 0.35);
  color: #fff;
}

/* ---------------- 落笔时压暗浮层 ---------------- */

.practice-stage.is-writing .stage-top,
.practice-stage.is-writing .tool-rail,
.practice-stage.is-writing .action-dock {
  opacity: 0.16;
}

.practice-stage.is-writing .tool-panel {
  opacity: 0.3;
}

/* ---------------- 参考字符显示 ---------------- */

.reference-display {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  background-color: rgba(255, 255, 255, 0.95);
  padding: 1.6rem;
  border-radius: 50%;
  width: 108px;
  height: 108px;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.2);
  animation: fadeIn 0.3s ease;
  pointer-events: none;
  z-index: 2;
}

.reference-char {
  font-size: 2.8rem;
  color: #8B4513;
  font-family: 'SimSun', serif;
}

/* 画布上的笔顺轨迹层：绝对定位，不接收指针事件，不进画布对象列表 */
.guide-overlay {
  position: absolute;
  left: 0;
  top: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
}

.overlay-stroke {
  fill: none;
  stroke: rgba(122, 118, 106, 0.26);
  stroke-width: 30;
  stroke-linecap: round;
  stroke-linejoin: round;
}

.overlay-stroke.done {
  stroke: rgba(96, 132, 96, 0.4);
}

.overlay-stroke.active {
  stroke: rgba(214, 72, 60, 0.55);
}

.overlay-start {
  fill: rgba(214, 72, 60, 0.78);
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translate(-50%, -50%) scale(0.8);
  }
  to {
    opacity: 1;
    transform: translate(-50%, -50%) scale(1);
  }
}

/* 系统开启「减少动态效果」时，参考字直接落位，不再缩放淡入。
   出现这个字本身是状态反馈，必须保留。 */
@media (prefers-reduced-motion: reduce) {
  .reference-display {
    animation: none !important;
  }

  .stage-top,
  .tool-rail,
  .action-dock,
  .tool-panel {
    transition: none !important;
  }
}

/* ================================================================
   窄屏（iPad 竖屏、小平板、手机）：
   工具轨改成底部横排（拇指够得到），面板改成底部抽屉，
   操作条并到底部工具行的下一行。
   ================================================================ */
@media (max-width: 900px) {
  .tool-rail {
    left: 0;
    right: 0;
    top: auto;
    bottom: calc(48px + env(safe-area-inset-bottom));
    transform: none;
    flex-direction: row;
    justify-content: center;
    gap: 2px;
    padding: 4px 6px;
    border-radius: 14px 14px 0 0;
    border-left: none;
    border-right: none;
    border-bottom: none;
    box-shadow: 0 -2px 12px rgba(60, 45, 30, 0.1);
  }

  .rail-btn {
    width: 52px;
    height: 48px;
  }

  .action-dock {
    left: 0;
    right: 0;
    bottom: 0;
    justify-content: flex-end;
    gap: 2px;
    padding: 4px 6px calc(4px + env(safe-area-inset-bottom));
    border-radius: 0;
    border-left: none;
    border-right: none;
    border-bottom: none;
    box-shadow: 0 -2px 12px rgba(60, 45, 30, 0.1);
  }

  .dock-btn {
    min-width: 52px;
    height: 44px;
  }

  .tool-panel {
    left: 0;
    right: 0;
    top: auto;
    bottom: calc(96px + env(safe-area-inset-bottom));
    transform: none;
    width: 100%;
    max-height: 62%;
    border-radius: 14px 14px 0 0;
    border-bottom: none;
  }

  .stage-top {
    padding: 0.5rem 0.6rem;
  }

  .chip {
    height: 32px;
    font-size: 12px;
  }

  .chip--book {
    max-width: 52vw;
  }

  .chip--count {
    display: none;
  }
}

/* 手机：工具轨按钮收窄，浮层按钮只留图标 */
@media (max-width: 600px) {
  .rail-btn {
    width: 46px;
    height: 46px;
  }

  .rail-btn__text {
    display: none;
  }

  .tool-rail {
    bottom: calc(44px + env(safe-area-inset-bottom));
  }

  .dock-btn {
    min-width: 48px;
    height: 42px;
  }

  .dock-btn__text {
    display: none;
  }

  .tool-panel {
    bottom: calc(88px + env(safe-area-inset-bottom));
    max-height: 66%;
  }

  .reference-list {
    grid-template-columns: repeat(8, 1fr);
  }
}

/* 粗指针（触屏 / 数位板）：把可点目标抬到 44×44 */
@media (pointer: coarse) {
  .rail-btn,
  .dock-btn {
    min-width: 44px;
    min-height: 44px;
  }

  .icon-btn {
    min-width: 44px;
    min-height: 44px;
  }

  .icon-btn--floating {
    width: 44px;
    height: 44px;
  }

  .chip {
    min-height: 44px;
  }

  .reference-item {
    min-height: 40px;
  }
}
</style>
