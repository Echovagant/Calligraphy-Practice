<template>
  <div class="practice-container">
    <div class="practice-header">
      <h2 class="section-title">书法练习</h2>
      <p class="section-subtitle">在这里练习你的书法技巧，体验毛笔书写的乐趣</p>
    </div>

    <div class="practice-layout">
      <!-- 工具面板 -->
      <div class="tool-panel">
        <div class="tool-section">
          <h3 class="tool-title">笔刷设置</h3>
          <div class="tool-group">
            <label class="tool-label">笔刷大小</label>
            <el-slider v-model="brushSize" :min="1" :max="20" show-input />
          </div>
          <div class="tool-group">
            <label class="tool-label">笔刷颜色</label>
            <el-color-picker v-model="brushColor" :show-alpha="false" />
          </div>
          <div class="tool-group">
            <label class="tool-label">笔刷硬度</label>
            <el-slider v-model="brushOpacity" :min="0.1" :max="1" :step="0.1" show-input />
          </div>
          <p class="tool-hint">
            {{ inputHint }}
          </p>
        </div>

        <div class="tool-section">
          <h3 class="tool-title">笔画提示</h3>
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
        </div>

        <div class="tool-section">
          <h3 class="tool-title">笔顺引导</h3>
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
        </div>

        <div class="tool-section">
          <h3 class="tool-title">背景设置</h3>
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
        </div>

        <div class="tool-section">
          <h3 class="tool-title">操作</h3>
          <div class="tool-buttons">
            <el-button type="primary" :icon="Delete" @click="clearCanvas">清空画布</el-button>
            <el-button type="success" :icon="RefreshLeft" :disabled="!canUndo" @click="undoLast">撤销</el-button>
            <el-button
              type="success"
              :icon="RefreshRight"
              :disabled="!canRedo"
              plain
              @click="redoLast"
            >
              重做
            </el-button>
            <el-button type="warning" :icon="Download" :loading="isSaving" @click="saveImage">保存作品</el-button>
          </div>
          <p class="tool-hint">
            共 {{ strokeCount }} 笔 · 撤销以「一笔」为单位（Ctrl / Cmd + Z，触摸端双指双击）
          </p>
        </div>

        <div class="tool-section">
          <h3 class="tool-title">参考字帖</h3>
          <div v-if="currentCopybook" class="current-copybook">
            <div class="current-title">《{{ currentCopybook.title }}》</div>
            <div class="current-meta">
              {{ currentCopybook.category || '未分类' }} · {{ formatDifficulty(currentCopybook.difficulty) }}
              · 字帖字数 {{ currentCopybook.characterCount ?? 0 }}
            </div>
          </div>
          <div v-else class="current-empty">尚未选择参考字帖，请先从下方推荐字帖中挑选</div>

          <div v-if="referenceChars.length > 0" class="reference-list">
            <div
              class="reference-item"
              v-for="char in referenceChars"
              :key="char"
              role="button"
              tabindex="0"
              @click="showReference(char)"
              @keydown.enter="showReference(char)"
              @keydown.space.prevent="showReference(char)"
            >
              {{ char }}
            </div>
          </div>
        </div>

        <div class="tool-section">
          <h3 class="tool-title">推荐字帖</h3>
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
            <div
              v-for="book in recommendations"
              :key="book.id"
              class="recommendation-item"
              :class="{ active: currentCopybook && currentCopybook.id === book.id }"
              role="button"
              tabindex="0"
              @click="selectCopybook(book)"
              @keydown.enter="selectCopybook(book)"
              @keydown.space.prevent="selectCopybook(book)"
            >
              <span class="recommendation-title">{{ book.title }}</span>
              <span class="recommendation-desc">{{ book.description || '暂无简介' }}</span>
            </div>
          </div>
          <el-empty
            v-if="!recommendationsLoading && !recommendationsError && recommendations.length === 0"
            description="暂无推荐字帖"
            :image-size="60"
          />
        </div>
      </div>

      <!-- 画布区域 -->
      <div class="canvas-area">
        <div class="canvas-wrapper" ref="canvasWrapRef">
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

        <!-- 参考字符显示 -->
        <div class="reference-display" v-if="showingReference">
          <div class="reference-char">{{ currentReference }}</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, watch, computed, nextTick } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { ElMessage } from 'element-plus';
import { Delete, RefreshLeft, RefreshRight, Download } from '@element-plus/icons-vue';
import { usePracticeStore } from '../stores/practiceStore';
import { useUserStore } from '../stores/userStore';
import { formatDifficulty } from '../utils/constants';
import { downloadCanvasAsImage } from '../utils';
import { getToken, copybookAPI } from '../utils/api';
import { useInkCanvas } from '../composables/useInkCanvas.js';
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
  // 打开/关闭下拉时浏览器会 scrollIntoView（关闭时 focus 归还输入框），
  // 会把整页滚走、把练习区顶出视口（实测整页上移 467px）。
  // 画布容器已是 overflow:clip（自身不可滚），所以这里要把**页面**滚回来。
  // 滚动发生在下拉关闭之后，因此要延迟补偿两次。
  await nextTick();
  const wrap = canvasWrapRef.value;
  if (!wrap) return;
  wrap.scrollTop = 0;
  wrap.scrollLeft = 0;
  const restore = () => {
    const rect = wrap.getBoundingClientRect();
    if (rect.top < 0 || rect.bottom > window.innerHeight) {
      window.scrollBy({
        top: rect.top - (window.innerHeight - rect.height) / 2,
        behavior: 'auto'
      });
    }
  };
  setTimeout(restore, 120);
  setTimeout(restore, 450);
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
const inputHint = computed(() => {  if (capability.value.pointerType === 'pen') {
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
};

// 从「我的字帖」带过来的字帖：GET /api/copybooks/{copybookId}
const loadCopybookFromQuery = async () => {
  // 同一个参数出现多次时 query 里拿到的是数组，取第一个就行
  const raw = route.query.copybookId
  const copybookId = Array.isArray(raw) ? raw[0] : raw
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
});

// 组件卸载时清理
onUnmounted(() => {
  endPractice();
  // 草稿立刻落盘，别让切页面丢笔迹
  flushDraft();
});
</script>

<style scoped>
.practice-container {
  padding: 2rem 0;
}

.practice-header {
  text-align: center;
  margin-bottom: 2rem;
}

.section-title {
  font-size: 2rem;
  font-weight: 600;
  margin-bottom: 0.5rem;
  color: #333;
  font-family: 'SimSun', serif;
}

.section-subtitle {
  color: #666;
}

.practice-layout {
  display: flex;
  gap: 2rem;
  justify-content: center;
  flex-wrap: wrap;
  align-items: flex-start;
}

/* 工具面板样式 */
.tool-panel {
  width: 280px;
  background-color: rgba(255, 255, 255, 0.95);
  padding: 1.5rem;
  border-radius: 8px;
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.1);
}

.tool-section {
  margin-bottom: 2rem;
}

.tool-title {
  font-size: 1.1rem;
  font-weight: 600;
  margin-bottom: 1rem;
  color: #333;
  border-bottom: 1px solid #eee;
  padding-bottom: 0.5rem;
}

.tool-group {
  margin-bottom: 1rem;
}

.tool-label {
  display: block;
  margin-bottom: 0.5rem;
  color: #666;
  font-size: 0.9rem;
}

.tool-buttons {
  display: flex;
  flex-direction: column;
  gap: 0.8rem;
}

.tool-hint {
  margin-top: 0.75rem;
  font-size: 0.78rem;
  color: #6b6259;
  line-height: 1.6;
}

.tool-alert {
  margin-bottom: 0.8rem;
}

/* 笔画提示 */
.stroke-select {
  width: 100%;
  margin-bottom: 0.75rem;
}

.stroke-hint {
  border-left: 3px solid var(--primary-color);
  padding: 0.5rem 0 0.5rem 0.75rem;
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
  font-size: 0.95rem;
  font-weight: 600;
  color: var(--primary-color);
  font-family: 'SimSun', serif;
}

.stroke-hint-source {
  font-size: 0.7rem;
  color: #6b6259;
  text-align: right;
}

.stroke-hint-steps {
  margin: 0;
  padding-left: 1.1rem;
  font-size: 0.8rem;
  line-height: 1.7;
  color: #444;
}

.stroke-hint-stage {
  display: inline-block;
  min-width: 2.6em;
  margin-right: 0.4em;
  padding: 0 0.3em;
  font-size: 0.72rem;
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
  font-size: 0.74rem;
  color: #8f6524;
}

/* 当前参考字帖 */
.current-copybook {
  padding: 0.6rem 0.8rem;
  background-color: #f9f9f9;
  border: 1px solid #eee;
  border-radius: 4px;
  margin-bottom: 0.8rem;
}

.current-title {
  color: #333;
  font-weight: 600;
  font-size: 0.9rem;
  margin-bottom: 0.3rem;
}

.current-meta {
  color: #6b6259;
  font-size: 0.78rem;
}

.current-empty {
  color: #6b6259;
  font-size: 0.8rem;
  margin-bottom: 0.8rem;
}

/* 参考字符样式 */
.reference-list {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 0.5rem;
}

.reference-item {
  padding: 1rem;
  text-align: center;
  background-color: #f9f9f9;
  border: 1px solid #eee;
  border-radius: 4px;
  cursor: pointer;
  font-size: 1.2rem;
  transition: transform 0.3s, box-shadow 0.3s, background-color 0.3s, color 0.3s;
  font-family: 'SimSun', serif;
}

.reference-item:hover {
  background-color: #f0f0f0;
  border-color: var(--primary-color);
  /* 只做 2px 位移，不再用 scale —— 缩放会让字块整块放大，读起来跳 */
  transform: translateY(-2px);
}

/* 推荐字帖 */
.recommendation-list {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
  min-height: 60px;
}

.recommendation-item {
  padding: 0.5rem 0.7rem;
  background-color: #f9f9f9;
  border: 1px solid #eee;
  border-radius: 4px;
  cursor: pointer;
  transition: transform 0.3s, box-shadow 0.3s, background-color 0.3s, color 0.3s;
}

.recommendation-item:hover,
.recommendation-item.active {
  border-color: #8B4513;
  color: #8B4513;
}

.recommendation-title {
  display: block;
  font-weight: 600;
  font-size: 0.9rem;
}

.recommendation-desc {
  display: block;
  color: #6b6259;
  font-size: 0.75rem;
  margin-top: 0.2rem;
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
}
.guide-progress {
  font-size: 12px;
  color: #666;
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
.guide-stroke.done { stroke: #7d9c7d; }
.guide-stroke.active { stroke: #d6483c; }
.guide-start { fill: #d6483c; }
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
.guide-result.ok { color: #2f7d43; }
.guide-result.bad { color: #c0392b; }
.guide-buttons {
  display: flex;
  gap: 6px;
}
.guide-error { color: #c0392b; }

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
.overlay-stroke.done { stroke: rgba(96, 132, 96, 0.4); }
.overlay-stroke.active { stroke: rgba(214, 72, 60, 0.55); }
.overlay-start { fill: rgba(214, 72, 60, 0.78); }

/* 画布区域样式 */
.canvas-area {
  position: relative;
  flex: 1 1 520px;
  min-width: 320px;
  max-width: 900px;
}

.canvas-wrapper {
  background-color: white;
  border-radius: 8px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
  /* overflow: clip 而不是 hidden —— hidden 会建立可滚动容器，
     浏览器 scrollIntoView（下拉定位）能把画布内容整体滚出容器（实测上移 467px）；
     clip 保留圆角裁切但不可滚动。
     注意：这里不要加 position:relative —— 笔顺轨迹层已用 Teleport 挂进 fabric 自己的容器，
     加了会改变 fabric 容器的定位基准，把画布顶到容器外面去。 */
  overflow: clip;
  /* 画布跟随设备尺寸，不再固定 800×600 */
  width: 100%;
  height: min(72vh, 720px);
  /* 触摸滚动只在这里关掉：工具条与弹窗仍然要能滚。
     touch-action: none 同时让 iOS 不把书写当成滚动手势。 */
  touch-action: none;
  -webkit-user-select: none;
  user-select: none;
}

/* 参考字符显示 */
.reference-display {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  background-color: rgba(255, 255, 255, 0.95);
  padding: 2rem;
  border-radius: 50%;
  width: 120px;
  height: 120px;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.2);
  animation: fadeIn 0.3s ease;
  pointer-events: none;
}

/* 系统开启「减少动态效果」时，参考字直接落位，不再缩放淡入。
   出现这个字本身是状态反馈，必须保留。 */
@media (prefers-reduced-motion: reduce) {
  .reference-display {
    animation: none !important;
  }
}

.reference-char {
  font-size: 3rem;
  color: #8B4513;
  font-family: 'SimSun', serif;
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

/* 粗指针设备（触屏 / 数位板）：
   关掉 hover 位移效果，按钮与可点区域抬到 44×44 —— 手指点得中。 */
@media (pointer: coarse) {
  .reference-item:hover {
    transform: none;
    background-color: #f9f9f9;
  }

  .recommendation-item:hover {
    border-color: #eee;
    color: inherit;
  }

  .reference-item,
  .recommendation-item {
    min-height: 44px;
  }
}

/* 响应式设计 */
@media (max-width: 768px) {
  .practice-layout {
    flex-direction: column;
    align-items: center;
  }

  .tool-panel {
    width: 100%;
    max-width: 400px;
  }

  .canvas-area {
    max-width: 100%;
  }

  /* 手机端只读，不给书写入口 */
  .canvas-wrapper {
    height: min(58vh, 480px);
  }
}
</style>
