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
      <div class="canvas-container">
        <div class="canvas-wrapper" ref="canvasWrapRef">
          <canvas ref="canvasRef"></canvas>
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
  strokeCount,
  canUndo,
  canRedo,
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
  })
});

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

/* 画布区域样式 */
.canvas-container {
  position: relative;
  flex: 1 1 520px;
  min-width: 320px;
  max-width: 900px;
}

.canvas-wrapper {
  background-color: white;
  border-radius: 8px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
  overflow: hidden;
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

  .canvas-container {
    max-width: 100%;
  }

  /* 手机端只读，不给书写入口 */
  .canvas-wrapper {
    height: min(58vh, 480px);
  }
}
</style>
