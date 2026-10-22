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
              <el-radio-button label="horizontal">横线</el-radio-button>
            </el-radio-group>
          </div>
        </div>

        <div class="tool-section">
          <h3 class="tool-title">操作</h3>
          <div class="tool-buttons">
            <el-button type="primary" @click="clearCanvas" icon="el-icon-delete">清空画布</el-button>
            <el-button type="success" @click="undoLast" icon="el-icon-refresh-left">撤销</el-button>
            <el-button type="warning" @click="saveImage" icon="el-icon-download" :loading="isSaving">保存作品</el-button>
          </div>
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
            <div class="reference-item" v-for="char in referenceChars" :key="char" @click="showReference(char)">
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
              @click="selectCopybook(book)"
            >
              <span class="recommendation-title">{{ book.title }}</span>
              <span class="recommendation-desc">{{ book.description || '暂无简介' }}</span>
            </div>
            <el-empty
              v-if="!recommendationsLoading && !recommendationsError && recommendations.length === 0"
              description="暂无推荐字帖"
              :image-size="60"
            />
          </div>
        </div>
      </div>

      <!-- 画布区域 -->
      <div class="canvas-container">
        <div class="canvas-wrapper">
          <canvas ref="canvasRef" :width="canvasWidth" :height="canvasHeight"></canvas>
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
import { ref, onMounted, onUnmounted, watch, nextTick, computed } from 'vue';
import * as fabric from 'fabric';
import { ElMessage } from 'element-plus';
import { usePracticeStore } from '../stores/practiceStore';
import { useUserStore } from '../stores/userStore';
import { formatDifficulty } from '../utils/constants';
import { downloadCanvasAsImage } from '../utils';

// 引入状态管理
const practiceStore = usePracticeStore();
const userStore = useUserStore();

// 画布相关
const canvasRef = ref(null);
let canvas = null;
const canvasWidth = 800;
const canvasHeight = 600;

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
  return [...new Set(source.replace(/[^\u4e00-\u9fa5]/g, ''))].slice(0, 24);
});

const currentReference = ref('');
const showingReference = ref(false);

// 历史记录（用于撤销功能）
const history = ref([]);
const historyIndex = ref(-1);

// 练习统计
const practiceStartTime = ref(null);
const strokeCount = ref(0);
// 本次练习展示过的参考字 —— 即「练习字数」
const practicedChars = ref([]);
const isSaving = ref(false);

// 推荐字帖：数据来自后端字帖接口
const recommendations = ref([]);
const recommendationsLoading = ref(false);
const recommendationsError = ref('');

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

const selectCopybook = (copybook) => {
  practiceStore.setCurrentCopybook(copybook);
  ElMessage.success(`已选择《${copybook.title || '未命名字帖'}》作为参考字帖`);
};

// 初始化画布
const initCanvas = () => {
  if (!canvasRef.value) return;

  // 从store加载设置
  practiceStore.loadLocalSettings();

  canvas = new fabric.Canvas(canvasRef.value, {
    backgroundColor: paperColor.value,
    width: canvasWidth,
    height: canvasHeight,
    preserveObjectStacking: true
  });

  // 设置笔刷
  setupBrush();

  // 绘制网格
  drawGrid();

  // 监听画布事件，记录历史
  canvas.on('object:added', () => {
    saveToHistory();
  });
};

// 设置笔刷
const setupBrush = () => {
  // 创建自定义笔刷
  const customBrush = new fabric.PencilBrush(canvas);
  customBrush.width = brushSize.value;
  customBrush.color = brushColor.value;
  customBrush.opacity = brushOpacity.value;
  customBrush.shadow = new fabric.Shadow({
    blur: 1,
    offsetX: 0,
    offsetY: 0,
    affectStroke: true
  });

  // 设置为当前笔刷
  canvas.isDrawingMode = true;
  canvas.freeDrawingBrush = customBrush;

  // 监听绘图开始，增加笔画计数
  canvas.on('path:created', () => {
    strokeCount.value++;
  });
};

// 绘制网格
  const drawGrid = () => {
    // 清除现有网格
    canvas.getObjects().forEach(obj => {
      if (obj.data && obj.data.type === 'grid') {
        canvas.remove(obj);
      }
    });

    if (gridType.value === 'none') return;

    const gridColor = '#dddddd';
    const gridSize = 50;

    const grid = new fabric.Group([], {
      selectable: false,
      evented: false,
      data: { type: 'grid' }
    });

    if (gridType.value === 'square' || gridType.value === 'horizontal') {
      // 绘制横线
      for (let i = 0; i <= canvasHeight / gridSize; i++) {
        const line = new fabric.Line([0, i * gridSize, canvasWidth, i * gridSize], {
          stroke: gridColor,
          strokeWidth: 0.5,
          selectable: false,
          evented: false
        });
        grid.add(line);
      }
    }

    if (gridType.value === 'square') {
      // 绘制竖线
      for (let i = 0; i <= canvasWidth / gridSize; i++) {
        const line = new fabric.Line([i * gridSize, 0, i * gridSize, canvasHeight], {
          stroke: gridColor,
          strokeWidth: 0.5,
          selectable: false,
          evented: false
        });
        grid.add(line);
      }
    }

    canvas.add(grid);
    canvas.sendToBack && canvas.sendToBack(grid);
  };

// 保存到历史记录
const saveToHistory = () => {
  const json = canvas.toJSON();

  // 如果在历史记录中间进行了新操作，删除后面的历史
  if (historyIndex.value < history.value.length - 1) {
    history.value = history.value.slice(0, historyIndex.value + 1);
  }

  history.value.push(json);
  historyIndex.value = history.value.length - 1;
};

// 撤销操作
const undoLast = () => {
  if (historyIndex.value > 0) {
    historyIndex.value--;
    canvas.loadFromJSON(history.value[historyIndex.value], () => {
      canvas.renderAll();
      setupBrush();
    });
    ElMessage.success('已撤销');
  } else {
    ElMessage.warning('没有可撤销的操作');
  }
};

// 清空画布
const clearCanvas = () => {
  canvas.clear();
  canvas.backgroundColor = paperColor.value;
  drawGrid();
  saveToHistory();
  ElMessage.success('画布已清空');
};

// 保存图片
const saveImage = async () => {
  if (!canvas) return;
  isSaving.value = true;
  try {
    const dataURL = canvas.toDataURL({ format: 'png', quality: 1 });
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
    downloadCanvasAsImage(canvas, fileName);
    ElMessage.success('作品已保存');

    // 重置笔画计数
    strokeCount.value = 0;
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
watch([brushSize, brushColor, brushOpacity], () => {
  setupBrush();
});

watch(paperColor, (newColor) => {
  canvas.backgroundColor = newColor;
  canvas.renderAll();
});

watch(gridType, () => {
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
  strokeCount.value = 0;
  practicedChars.value = [];
};

onMounted(() => {
  nextTick(() => {
    initCanvas();
    // 记录开始练习时间
    practiceStartTime.value = new Date();
    // 获取推荐练习内容
    loadRecommendations();
  });
});

// 组件卸载时清理
onUnmounted(() => {
  endPractice();
  if (canvas) {
    canvas.dispose();
  }
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
  transition: transform 0.3s, box-shadow 0.3s, background-color 0.3s, border-color 0.3s, color 0.3s;
  font-family: 'SimSun', serif;
}

.reference-item:hover {
  background-color: #f0f0f0;
  border-color: #8B4513;
  transform: scale(1.05);
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
  transition: transform 0.3s, box-shadow 0.3s, background-color 0.3s, border-color 0.3s, color 0.3s;
}

.recommendation-item:hover,
.recommendation-item.active {
  border-color: #8B4513;
  color: #8B4513;
}

.recommendation-title {
  display: block;
  font-size: 0.9rem;
  font-weight: 600;
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
}

.canvas-wrapper {
  background-color: white;
  border-radius: 8px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
  overflow: hidden;
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

  .canvas-wrapper {
    max-width: 100%;
    overflow-x: auto;
  }
}
</style>
