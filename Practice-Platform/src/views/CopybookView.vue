<template>
  <div class="copybook-container">
    <div class="copybook-header">
      <h2 class="section-title">字帖生成</h2>
      <p class="section-subtitle">生成个性化书法字帖，提升你的书写水平</p>
    </div>

    <!-- 字帖库：数据来自 GET /api/copybooks -->
    <div class="library-section">
      <div class="section-header">
        <h3 class="config-title">字帖库</h3>
        <el-button size="small" :loading="copybookLoading" @click="loadCopybooks">刷新</el-button>
      </div>

      <div class="filter-bar">
        <el-select
          v-model="filters.category"
          placeholder="全部分类"
          clearable
          filterable
          allow-create
          default-first-option
          class="filter-select"
          @change="handleFilterChange"
        >
          <el-option v-for="item in categoryOptions" :key="item" :label="item" :value="item" />
        </el-select>
        <el-select
          v-model="filters.difficulty"
          placeholder="全部难度"
          clearable
          filterable
          allow-create
          default-first-option
          class="filter-select"
          @change="handleFilterChange"
        >
          <el-option v-for="item in difficultyOptions" :key="item" :label="item" :value="item" />
        </el-select>
        <span class="filter-hint">共 {{ copybookTotal }} 个字帖</span>
      </div>

      <el-alert
        v-if="copybookError"
        type="error"
        :title="copybookError"
        show-icon
        :closable="false"
        class="state-alert"
      >
        <el-button size="small" type="primary" @click="loadCopybooks">重试</el-button>
      </el-alert>

      <div v-loading="copybookLoading" class="copybook-grid">
        <el-card v-for="book in copybooks" :key="book.id" class="copybook-card" shadow="hover">
          <el-image v-if="book.imageUrl" :src="book.imageUrl" fit="cover" class="copybook-cover">
            <template #error>
              <div class="cover-fallback">暂无封面</div>
            </template>
          </el-image>
          <h4 class="copybook-title">{{ book.title }}</h4>
          <p class="copybook-desc">{{ book.description || '暂无简介' }}</p>
          <div class="copybook-meta">
            <el-tag size="small" type="info">{{ book.category || '未分类' }}</el-tag>
            <el-tag size="small">{{ book.difficulty || '未标难度' }}</el-tag>
          </div>
          <div class="copybook-stat">
            字帖字数 {{ book.characterCount ?? 0 }} · 已被使用 {{ book.usageCount ?? 0 }} 次
          </div>
          <el-button size="small" type="primary" @click="openDetail(book.id)">查看详情</el-button>
        </el-card>

        <el-empty
          v-if="!copybookLoading && !copybookError && copybooks.length === 0"
          description="暂无符合条件的字帖"
        />
      </div>

      <div v-if="copybookTotal > copybookPageSize" class="pagination-container">
        <el-pagination
          v-model:current-page="copybookPage"
          v-model:page-size="copybookPageSize"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          :total="copybookTotal"
          @size-change="handleCopybookSizeChange"
          @current-change="handleCopybookPageChange"
        />
      </div>
    </div>

    <!-- 热门字帖：数据来自 GET /api/copybooks/popular -->
    <div class="popular-section">
      <h3 class="config-title">热门字帖</h3>

      <el-alert
        v-if="popularError"
        type="error"
        :title="popularError"
        show-icon
        :closable="false"
        class="state-alert"
      >
        <el-button size="small" type="primary" @click="loadPopularCopybooks">重试</el-button>
      </el-alert>

      <div v-loading="popularLoading" class="popular-list">
        <div
          v-for="book in popularCopybooks"
          :key="book.id"
          class="popular-item"
          @click="openDetail(book.id)"
        >
          <span class="popular-title">{{ book.title }}</span>
          <span class="popular-count">使用 {{ book.usageCount ?? 0 }} 次</span>
        </div>

        <el-empty
          v-if="!popularLoading && !popularError && popularCopybooks.length === 0"
          description="暂无热门字帖"
          :image-size="80"
        />
      </div>
    </div>

    <!-- 字帖详情：数据来自 GET /api/copybooks/{copybookId} -->
    <el-dialog v-model="detailVisible" title="字帖详情" width="480px">
      <div v-loading="detailLoading" class="detail-body">
        <el-alert
          v-if="detailError"
          type="error"
          :title="detailError"
          show-icon
          :closable="false"
        >
          <el-button size="small" type="primary" @click="retryDetail">重试</el-button>
        </el-alert>

        <div v-else-if="selectedCopybook" class="detail-content">
          <h4 class="detail-title">{{ selectedCopybook.title }}</h4>
          <p class="detail-desc">{{ selectedCopybook.description || '暂无简介' }}</p>
          <div class="detail-row">
            <span class="detail-label">分类：</span>
            <span class="detail-value">{{ selectedCopybook.category || '未分类' }}</span>
          </div>
          <div class="detail-row">
            <span class="detail-label">难度：</span>
            <span class="detail-value">{{ selectedCopybook.difficulty || '未标难度' }}</span>
          </div>
          <div class="detail-row">
            <span class="detail-label">字帖字数：</span>
            <span class="detail-value">{{ selectedCopybook.characterCount ?? 0 }}</span>
          </div>
          <div class="detail-row">
            <span class="detail-label">使用次数：</span>
            <span class="detail-value">{{ selectedCopybook.usageCount ?? 0 }}</span>
          </div>
          <div class="detail-row">
            <span class="detail-label">创建时间：</span>
            <span class="detail-value">{{ formatDateTime(selectedCopybook.createdAt) }}</span>
          </div>
        </div>

        <el-empty v-else-if="!detailLoading" description="暂无详情" />
      </div>
    </el-dialog>

    <div class="copybook-layout">
      <!-- 配置面板 -->
      <div class="config-panel">
        <el-form :model="copybookConfig" :rules="configRules" ref="configFormRef" label-width="100px">
          <div class="config-section">
            <h3 class="config-title">内容设置</h3>

            <el-form-item label="输入文字" prop="text">
              <el-input
                :model-value="copybookConfig.text"
                @update:modelValue="handleConfigChange('text', $event)"
                type="textarea"
                :rows="4"
                placeholder="请输入要生成字帖的文字内容"
              />
              <div class="char-count">{{ copybookConfig.text.length }}/50</div>
            </el-form-item>

            <el-form-item label="示例文本">
              <el-button size="small" @click="useSampleText">填入示例文本</el-button>
              <span class="sample-hint">{{ sampleText }}</span>
            </el-form-item>
          </div>

          <div class="config-section">
            <h3 class="config-title">样式设置</h3>

            <el-form-item label="字体选择" prop="font">
              <el-select
                :model-value="copybookConfig.font"
                @update:modelValue="handleConfigChange('font', $event)"
                placeholder="请选择字体"
              >
                <el-option label="楷书（默认）" value="kai" />
                <el-option label="行书" value="xing" />
                <el-option label="草书" value="cao" />
                <el-option label="隶书" value="li" />
                <el-option label="篆书" value="zhuan" />
              </el-select>
            </el-form-item>

            <el-form-item label="布局方式" prop="layout">
              <el-radio-group
                :model-value="copybookConfig.layout"
                @update:modelValue="handleConfigChange('layout', $event)"
              >
                <el-radio-button label="grid">方格布局</el-radio-button>
                <el-radio-button label="line">横线布局</el-radio-button>
                <el-radio-button label="blank">空白布局</el-radio-button>
              </el-radio-group>
            </el-form-item>

            <el-form-item label="每页字数" prop="charsPerPage">
              <el-slider
                :model-value="copybookConfig.charsPerPage"
                @update:modelValue="handleConfigChange('charsPerPage', $event)"
                :min="10" :max="100" :step="5" show-input
              />
            </el-form-item>

            <el-form-item label="字体大小" prop="fontSize">
              <el-slider
                :model-value="copybookConfig.fontSize"
                @update:modelValue="handleConfigChange('fontSize', $event)"
                :min="20" :max="100" :step="5" show-input
              />
            </el-form-item>
          </div>

          <div class="config-section">
            <h3 class="config-title">纸张设置</h3>

            <el-form-item label="纸张颜色" prop="paperColor">
              <el-color-picker
                :model-value="copybookConfig.paperColor"
                @update:modelValue="handleConfigChange('paperColor', $event)"
                :show-alpha="false"
              />
            </el-form-item>

            <el-form-item label="网格颜色" prop="gridColor">
              <el-color-picker
                :model-value="copybookConfig.gridColor"
                @update:modelValue="handleConfigChange('gridColor', $event)"
                :show-alpha="false"
              />
            </el-form-item>
          </div>

          <div class="config-section">
            <div class="action-buttons">
              <el-button type="primary" @click="generateCopybook" :loading="isGenerating" class="generate-btn">
                {{ isGenerating ? '生成中...' : '生成字帖' }}
              </el-button>
              <el-button type="success" @click="downloadCopybook" :disabled="!hasGenerated" class="download-btn">
                下载字帖
              </el-button>
            </div>

            <div class="history-buttons">
              <el-button @click="showHistory = !showHistory" class="history-btn">
                {{ showHistory ? '隐藏历史' : '查看历史' }}
              </el-button>
              <el-button @click="showGenerated = !showGenerated" class="history-btn">
                {{ showGenerated ? '隐藏生成' : '查看生成' }}
              </el-button>
            </div>
            </div>
          </el-form>

          <!-- 历史记录显示 -->
          <div v-if="showHistory" class="history-panel">
            <div class="history-header">
              <h4>历史记录</h4>
              <el-button size="small" type="danger" @click="clearHistory">清空历史</el-button>
            </div>
            <el-table v-if="historyItems.length > 0" :data="historyItems" style="width: 100%; margin-top: 10px;" size="small">
              <el-table-column prop="title" label="标题" width="150"></el-table-column>
              <el-table-column prop="createdAt" label="创建时间" width="180">
                <template #default="scope">
                  {{ new Date(scope.row.createdAt).toLocaleString() }}
                </template>
              </el-table-column>
              <el-table-column label="操作" width="120" fixed="right">
                <template #default="scope">
                  <el-button size="small" @click="loadFromHistory(scope.row)">加载</el-button>
                  <el-button size="small" type="danger" @click="deleteHistoryItem(scope.row.id)">删除</el-button>
                </template>
              </el-table-column>
            </el-table>
            <div v-else class="empty-history">暂无历史记录</div>
          </div>

          <!-- 生成记录显示 -->
          <div v-if="showGenerated" class="generated-panel">
            <div class="generated-header">
              <h4>生成记录</h4>
              <el-button size="small" type="danger" @click="clearGenerated">清空记录</el-button>
            </div>
            <el-table v-if="generatedCopybooks.length > 0" :data="generatedCopybooks" style="width: 100%; margin-top: 10px;" size="small">
              <el-table-column prop="title" label="标题" width="150"></el-table-column>
              <el-table-column prop="pageCount" label="页数" width="80"></el-table-column>
              <el-table-column prop="createdAt" label="创建时间" width="180">
                <template #default="scope">
                  {{ new Date(scope.row.createdAt).toLocaleString() }}
                </template>
              </el-table-column>
              <el-table-column label="操作" width="120" fixed="right">
                <template #default="scope">
                  <el-button size="small" type="primary" @click="downloadCopybook">下载</el-button>
                  <el-button size="small" type="danger" @click="deleteHistoryItem(scope.row.id)">删除</el-button>
                </template>
              </el-table-column>
            </el-table>
            <div v-else class="empty-generated">暂无生成记录</div>
          </div>
        </div>

      <!-- 预览区域 -->
      <div class="preview-panel">
        <div class="preview-header">
          <h3 class="preview-title">预览</h3>
          <div class="preview-controls">
            <el-button-group>
              <el-button @click="prevPage" :disabled="currentPage <= 1" icon="el-icon-arrow-left"></el-button>
              <el-button disabled>{{ currentPage }} / {{ totalPages }}</el-button>
              <el-button @click="nextPage" :disabled="currentPage >= totalPages" icon="el-icon-arrow-right"></el-button>
            </el-button-group>
          </div>
        </div>

        <div class="preview-content">
          <div v-if="!hasGenerated" class="empty-preview">
            <div class="empty-icon">📚</div>
            <p>请输入文字内容并点击生成字帖</p>
          </div>
          <div v-else class="copybook-preview">
            <div
              class="paper-preview"
              :style="{ backgroundColor: copybookConfig.paperColor, fontFamily: fontFamily }"
              ref="paperRef"
            >
              <!-- 网格或横线 -->
              <div v-if="copybookConfig.layout === 'grid'" class="grid-container">
                <div
                  v-for="(char, index) in currentPageChars"
                  :key="index"
                  class="grid-cell"
                  :style="{ borderColor: copybookConfig.gridColor }"
                >
                  <div class="char-preview" :style="{ fontSize: copybookConfig.fontSize + 'px' }">
                    {{ char }}
                  </div>
                </div>
              </div>

              <div v-else-if="copybookConfig.layout === 'line'" class="line-container">
                <div
                  v-for="(char, index) in currentPageChars"
                  :key="index"
                  class="line-row"
                >
                  <div class="line" :style="{ backgroundColor: copybookConfig.gridColor }"></div>
                  <div class="char-preview line-char" :style="{ fontSize: copybookConfig.fontSize + 'px' }">
                    {{ char }}
                  </div>
                </div>
              </div>

              <div v-else class="blank-container">
                <div
                  v-for="(char, index) in currentPageChars"
                  :key="index"
                  class="blank-char"
                >
                  <div class="char-preview" :style="{ fontSize: copybookConfig.fontSize + 'px' }">
                    {{ char }}
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { useCopybookStore } from '../stores/copybookStore';
import { downloadCanvasAsImage } from '../utils';

// 状态管理
const copybookStore = useCopybookStore();
const configFormRef = ref(null);
const paperRef = ref(null);
const isGenerating = ref(false);
const hasGenerated = ref(false);
const currentPage = ref(1);
const showHistory = ref(false);
const showGenerated = ref(false);

// 初始化
onMounted(() => {
  copybookStore.initialize();
  loadCopybooks();
  loadPopularCopybooks();
});

// ---------------------------------------------------------------- 字帖库（后端数据）

const copybooks = ref([]);
const copybookTotal = ref(0);
const copybookPage = ref(1);
const copybookPageSize = ref(20);
const copybookLoading = ref(false);
const copybookError = ref('');
const filters = reactive({ category: '', difficulty: '' });

// 过滤项的可选值来自后端返回的数据，不在前端写死
const knownCategories = ref([]);
const knownDifficulties = ref([]);
const categoryOptions = computed(() => knownCategories.value);
const difficultyOptions = computed(() => knownDifficulties.value);

const rememberOptions = (list) => {
  const categories = new Set(knownCategories.value);
  const difficulties = new Set(knownDifficulties.value);
  list.forEach((book) => {
    if (book.category) categories.add(book.category);
    if (book.difficulty) difficulties.add(book.difficulty);
  });
  knownCategories.value = [...categories];
  knownDifficulties.value = [...difficulties];
};

// 列表端点返回分页信封 {content,page,size,total,totalPages}，
// 这里同时兼容 store 直接返回数组的情况，统一取出 content 与 total。
const readPage = (payload, fallbackList, fallbackTotal) => {
  if (Array.isArray(payload)) {
    return {
      content: payload,
      total: typeof fallbackTotal === 'number' ? fallbackTotal : payload.length
    };
  }
  if (payload && Array.isArray(payload.content)) {
    return { content: payload.content, total: payload.total ?? payload.content.length };
  }
  return {
    content: Array.isArray(fallbackList) ? fallbackList : [],
    total: typeof fallbackTotal === 'number' ? fallbackTotal : 0
  };
};

const buildCopybookParams = () => {
  const params = {
    page: copybookPage.value - 1,
    size: copybookPageSize.value
  };
  if (filters.category) params.category = filters.category;
  if (filters.difficulty) params.difficulty = filters.difficulty;
  return params;
};

const loadCopybooks = async () => {
  copybookLoading.value = true;
  copybookError.value = '';
  try {
    const payload = await copybookStore.fetchCopybooks(buildCopybookParams());
    const page = readPage(payload, copybookStore.copybooks, copybookStore.copybookTotal);
    copybooks.value = page.content;
    copybookTotal.value = page.total;
    rememberOptions(page.content);
  } catch (error) {
    copybooks.value = [];
    copybookTotal.value = 0;
    copybookError.value = error?.message || '获取字帖列表失败';
    ElMessage.error(copybookError.value);
    console.error('获取字帖列表失败:', error);
  } finally {
    copybookLoading.value = false;
  }
};

const handleFilterChange = () => {
  copybookPage.value = 1;
  loadCopybooks();
};

const handleCopybookSizeChange = () => {
  copybookPage.value = 1;
  loadCopybooks();
};

const handleCopybookPageChange = () => {
  loadCopybooks();
};

// ---------------------------------------------------------------- 热门字帖

const popularCopybooks = ref([]);
const popularLoading = ref(false);
const popularError = ref('');

const loadPopularCopybooks = async () => {
  popularLoading.value = true;
  popularError.value = '';
  try {
    const payload = await copybookStore.fetchPopularCopybooks();
    const page = readPage(payload, copybookStore.popularCopybooks);
    popularCopybooks.value = page.content;
    rememberOptions(page.content);
  } catch (error) {
    popularCopybooks.value = [];
    popularError.value = error?.message || '获取热门字帖失败';
    ElMessage.error(popularError.value);
    console.error('获取热门字帖失败:', error);
  } finally {
    popularLoading.value = false;
  }
};

// ---------------------------------------------------------------- 字帖详情

const detailVisible = ref(false);
const detailLoading = ref(false);
const detailError = ref('');
const selectedCopybook = ref(null);
let detailCopybookId = null;

const openDetail = async (copybookId) => {
  detailCopybookId = copybookId;
  detailVisible.value = true;
  await loadDetail();
};

const loadDetail = async () => {
  if (!detailCopybookId) return;
  detailLoading.value = true;
  detailError.value = '';
  selectedCopybook.value = null;
  try {
    const payload = await copybookStore.fetchCopybookDetail(detailCopybookId);
    selectedCopybook.value = payload && payload.id ? payload : copybookStore.selectedCopybook;
  } catch (error) {
    selectedCopybook.value = null;
    detailError.value = error?.message || '获取字帖详情失败';
    ElMessage.error(detailError.value);
    console.error('获取字帖详情失败:', error);
  } finally {
    detailLoading.value = false;
  }
};

const retryDetail = () => {
  loadDetail();
};

const formatDateTime = (value) => {
  if (!value) return '—';
  const date = new Date(value);
  return Number.isNaN(date.getTime()) ? '—' : date.toLocaleString();
};

// ---------------------------------------------------------------- 字帖生成（本地）

const sampleText = computed(() => copybookStore.getSampleText());

// 计算属性 - 从store获取配置和数据
const copybookConfig = computed({
  get: () => ({
    text: copybookStore.getSettings.content,
    font: copybookStore.getSettings.font,
    layout: copybookStore.getSettings.layout,
    charsPerPage: copybookStore.getSettings.charactersPerPage,
    fontSize: copybookStore.getSettings.fontSize,
    paperColor: copybookStore.getSettings.paperColor,
    gridColor: copybookStore.getSettings.gridColor
  }),
  set: (newConfig) => {
    copybookStore.updateSettings({
      content: newConfig.text,
      font: newConfig.font,
      layout: newConfig.layout,
      charactersPerPage: newConfig.charsPerPage,
      fontSize: newConfig.fontSize,
      paperColor: newConfig.paperColor,
      gridColor: newConfig.gridColor
    });
  }
});

// 字体设置映射到真实字体族
const FONT_FAMILIES = {
  kai: "'KaiTi', 'STKaiti', 'SimSun', serif",
  xing: "'STXingkai', 'KaiTi', 'SimSun', serif",
  cao: "'STXingkai', 'KaiTi', 'SimSun', serif",
  li: "'LiSu', 'SimSun', serif",
  zhuan: "'FZZhuan-LeiZhen-Z03', 'SimSun', serif"
};
const fontFamily = computed(() => FONT_FAMILIES[copybookConfig.value.font] || "'SimSun', serif");

const historyItems = computed(() => copybookStore.getHistory);
const generatedCopybooks = computed(() => copybookStore.getGeneratedCopybooks);

// 配置验证规则
const configRules = {
  text: [
    { required: true, message: '请输入文字内容', trigger: 'blur' },
    { max: 50, message: '文字内容不能超过50个字符', trigger: 'blur' }
  ]
};

// 处理后的文字数据
const processedText = ref('');
const pages = ref([]);

// 计算属性
const totalPages = computed(() => pages.value.length);
const currentPageChars = computed(() => {
  return pages.value[currentPage.value - 1] || [];
});

// 使用示例文本
const useSampleText = () => {
  const text = copybookStore.getSampleText();
  if (!text) {
    ElMessage.warning('当前没有可用的示例文本');
    return;
  }
  copybookStore.setContent(text);
  ElMessage.success('已填入示例文本');
};

// 处理配置变化
const handleConfigChange = (field, value) => {
  const configMap = {
    text: 'content',
    charsPerPage: 'charactersPerPage',
    fontSize: 'fontSize',
    paperColor: 'paperColor',
    gridColor: 'gridColor',
    font: 'font',
    layout: 'layout'
  };

  copybookStore.updateSettings({ [configMap[field] || field]: value });
};

// 从历史记录加载
const loadFromHistory = (item) => {
  copybookStore.loadFromHistory(item.id);
  showHistory.value = false;
  ElMessage.success('已加载历史配置');
};

// 删除历史记录
const deleteHistoryItem = (id) => {
  ElMessageBox.confirm('确定要删除这条历史记录吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    copybookStore.deleteCopybook(id);
    ElMessage.success('删除成功');
  });
};

// 清空历史记录
const clearHistory = () => {
  ElMessageBox.confirm('确定要清空所有历史记录吗？', '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    copybookStore.clearHistory();
    ElMessage.success('历史记录已清空');
  });
};

// 清空生成记录
const clearGenerated = () => {
  ElMessageBox.confirm('确定要清空所有生成记录吗？', '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    copybookStore.clearGeneratedCopybooks();
    ElMessage.success('生成记录已清空');
  });
};

// 生成字帖
const generateCopybook = async () => {
  try {
    // 表单验证
    await configFormRef.value.validate();

    if (!copybookStore.getSettings.content.trim()) {
      ElMessage.warning('请输入有效的文字内容');
      return;
    }

    isGenerating.value = true;

    // 调用store的生成方法
    await copybookStore.generateCopybook();

    // 处理文本，去除空格和换行
    processedText.value = copybookStore.getSettings.content.replace(/\s+/g, '');

    // 分页
    pages.value = [];
    for (let i = 0; i < processedText.value.length; i += copybookStore.getSettings.charactersPerPage) {
      const pageChars = processedText.value.slice(i, i + copybookStore.getSettings.charactersPerPage).split('');
      pages.value.push(pageChars);
    }

    currentPage.value = 1;
    hasGenerated.value = true;

    ElMessage.success('字帖生成成功！');

  } catch (error) {
    if (error !== false) {
      ElMessage.error(error.message || '生成失败，请检查输入');
      console.error('Generate copybook error:', error);
    }
  } finally {
    isGenerating.value = false;
  }
};

// 把当前页的字渲染成 PNG 并下载
const renderCurrentPage = () => {
  const chars = currentPageChars.value;
  const columns = 4;
  const rows = Math.ceil(chars.length / columns);
  const cell = Math.max(80, copybookConfig.value.fontSize + 32);

  const canvas = document.createElement('canvas');
  canvas.width = columns * cell;
  canvas.height = rows * cell;
  const ctx = canvas.getContext('2d');
  if (!ctx) throw new Error('当前浏览器不支持 Canvas 导出');

  ctx.fillStyle = copybookConfig.value.paperColor || '#ffffff';
  ctx.fillRect(0, 0, canvas.width, canvas.height);
  ctx.font = `${copybookConfig.value.fontSize}px ${fontFamily.value}`;
  ctx.textAlign = 'center';
  ctx.textBaseline = 'middle';
  ctx.fillStyle = '#333';

  chars.forEach((char, index) => {
    const col = index % columns;
    const row = Math.floor(index / columns);
    const originX = col * cell;
    const originY = row * cell;

    if (copybookConfig.value.layout !== 'blank') {
      ctx.strokeStyle = copybookConfig.value.gridColor || '#dddddd';
      ctx.lineWidth = 1;
      ctx.strokeRect(originX, originY, cell, cell);
    }
    if (copybookConfig.value.layout === 'line') {
      ctx.beginPath();
      ctx.moveTo(originX, originY + cell - 20);
      ctx.lineTo(originX + cell, originY + cell - 20);
      ctx.stroke();
    }

    ctx.fillText(char, originX + cell / 2, originY + cell / 2);
  });

  return canvas;
};

// 下载字帖
const downloadCopybook = () => {
  if (!hasGenerated.value || currentPageChars.value.length === 0) {
    ElMessage.warning('请先生成字帖');
    return;
  }

  try {
    const canvas = renderCurrentPage();
    downloadCanvasAsImage(canvas, `字帖_${Date.now()}.png`);
    ElMessage.success('字帖已下载');
  } catch (error) {
    ElMessage.error('字帖导出失败，请稍后重试');
    console.error('导出字帖失败:', error);
  }
};

// 翻页功能
const prevPage = () => {
  if (currentPage.value > 1) {
    currentPage.value--;
  }
};

const nextPage = () => {
  if (currentPage.value < totalPages.value) {
    currentPage.value++;
  }
};
</script>

<style scoped>
.copybook-container {
  padding: 2rem 0;
}

.copybook-header {
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

/* 字帖库 */
.library-section,
.popular-section {
  background-color: rgba(255, 255, 255, 0.95);
  padding: 1.5rem;
  border-radius: 8px;
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.1);
  margin-bottom: 2rem;
  max-width: 1200px;
  margin-left: auto;
  margin-right: auto;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.filter-bar {
  display: flex;
  align-items: center;
  gap: 1rem;
  flex-wrap: wrap;
  margin-bottom: 1rem;
}

.filter-select {
  width: 200px;
}

.filter-hint {
  color: #6b6259;
  font-size: 0.85rem;
}

.state-alert {
  margin-bottom: 1rem;
}

.copybook-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 1rem;
  min-height: 120px;
}

.copybook-card {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.copybook-cover {
  width: 100%;
  height: 140px;
  border-radius: 4px;
  background-color: #f5f5f5;
}

.cover-fallback {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: #6b6259;
  font-size: 0.85rem;
}

.copybook-title {
  margin: 0;
  font-size: 1rem;
  color: #333;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.copybook-desc {
  margin: 0;
  color: #666;
  font-size: 0.85rem;
  min-height: 2.4em;
  overflow: hidden;
}

.copybook-meta {
  display: flex;
  gap: 0.5rem;
}

.copybook-stat {
  color: #6b6259;
  font-size: 0.8rem;
}

.pagination-container {
  margin-top: 1rem;
  display: flex;
  justify-content: center;
}

/* 热门字帖 */
.popular-list {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
  min-height: 60px;
}

.popular-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0.6rem 0.8rem;
  background-color: #f9f9f9;
  border: 1px solid #eee;
  border-radius: 4px;
  cursor: pointer;
  transition: transform 0.3s, box-shadow 0.3s, background-color 0.3s, border-color 0.3s, color 0.3s;
}

.popular-item:hover {
  border-color: #8B4513;
  color: #8B4513;
}

.popular-count {
  color: #6b6259;
  font-size: 0.8rem;
}

/* 字帖详情 */
.detail-body {
  min-height: 120px;
}

.detail-title {
  margin: 0 0 0.5rem;
  color: #333;
}

.detail-desc {
  margin: 0 0 1rem;
  color: #666;
}

.detail-row {
  margin-bottom: 0.6rem;
}

.detail-label {
  font-weight: 600;
  color: #333;
  margin-right: 0.5rem;
}

.detail-value {
  color: #666;
}

/* 配置面板样式 */
.copybook-layout {
  display: flex;
  gap: 2rem;
  justify-content: center;
  flex-wrap: wrap;
}

.config-panel {
  width: 380px;
  background-color: rgba(255, 255, 255, 0.95);
  padding: 1.5rem;
  border-radius: 8px;
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.1);
}

.config-section {
  margin-bottom: 2rem;
}

.config-title {
  font-size: 1.1rem;
  margin-bottom: 1rem;
  color: #333;
  border-bottom: 1px solid #eee;
  padding-bottom: 0.5rem;
}

.char-count {
  text-align: right;
  font-size: 0.8rem;
  color: #6b6259;
  margin-top: 0.5rem;
}

.sample-hint {
  margin-left: 0.8rem;
  color: #6b6259;
  font-size: 0.8rem;
}

.action-buttons {
  display: flex;
  gap: 1rem;
}

.generate-btn,
.download-btn {
  flex: 1;
}

/* Element Plus 的 success 实心按钮在禁用态是「浅绿底 + 白字」——
   白字压 #afb8a9 只有 2.05，糊成一团。框架没管这个组合，这里补上：
   浅绿底 + 深绿字（4.65），既保留「不可点」的样子，文字也读得出来。 */
.download-btn.is-disabled {
  color: #5f7153;
  background-color: #eff1ee;
  border-color: #cfd4cb;
}

/* 预览面板样式 */
.preview-panel {
  background-color: rgba(255, 255, 255, 0.95);
  padding: 1.5rem;
  border-radius: 8px;
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.1);
  width: 100%;
  max-width: 600px;
}

.preview-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1.5rem;
}

.preview-title {
  font-size: 1.2rem;
  color: #333;
}

.preview-content {
  min-height: 400px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.empty-preview {
  text-align: center;
  color: #6b6259;
}

.empty-icon {
  font-size: 3rem;
  margin-bottom: 1rem;
}

.copybook-preview {
  width: 100%;
}

.paper-preview {
  width: 100%;
  min-height: 400px;
  border: 1px solid #ddd;
  border-radius: 4px;
  padding: 2rem;
  box-shadow: inset 0 0 10px rgba(0, 0, 0, 0.05);
}

/* 网格布局 */
.grid-container {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 10px;
}

.grid-cell {
  aspect-ratio: 1;
  border: 1px solid;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 4px;
}

.line-container {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.line-row {
  position: relative;
  height: 60px;
  display: flex;
  align-items: center;
}

.line {
  position: absolute;
  bottom: 20px;
  left: 0;
  right: 0;
  height: 1px;
}

.line-char {
  margin-left: 10px;
}

/* 空白布局 */
.blank-container {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
}

.blank-char {
  text-align: center;
  padding: 10px;
}

.char-preview {
  color: #333;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .copybook-layout {
    flex-direction: column;
    align-items: center;
  }

  .config-panel {
    width: 100%;
    max-width: 400px;
  }

  .preview-header {
    flex-direction: column;
    gap: 1rem;
    align-items: flex-start;
  }

  .grid-container,
  .blank-container {
    grid-template-columns: repeat(3, 1fr);
  }
}
</style>
