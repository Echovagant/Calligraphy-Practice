<template>
  <div class="copybook-container">
    <div class="copybook-header">
      <h2 class="section-title">字帖生成</h2>
      <p class="section-subtitle">生成个性化书法字帖，提升你的书写水平</p>
    </div>

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
              <el-button @click="prevPage" :disabled="currentPage <= 1" :icon="ArrowLeft"></el-button>
              <el-button disabled>{{ currentPage }} / {{ totalPages }}</el-button>
              <el-button @click="nextPage" :disabled="currentPage >= totalPages" :icon="ArrowRight"></el-button>
            </el-button-group>
          </div>
        </div>

        <div class="preview-content">
          <div v-if="!hasGenerated" class="empty-preview">
            <el-icon class="empty-icon"><Notebook /></el-icon>
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
            <el-tag size="small">{{ formatDifficulty(book.difficulty) }}</el-tag>
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
          role="button"
          tabindex="0"
          @click="openDetail(book.id)"
          @keydown.enter="openDetail(book.id)"
          @keydown.space.prevent="openDetail(book.id)"
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

    <!-- 我的字帖：数据来自 GET /api/copybooks/mine（存到账号的自建字帖） -->
    <div class="mine-section">
      <div class="section-header">
        <h3 class="config-title">我的字帖</h3>
        <el-button size="small" :loading="myCopybookLoading" :disabled="!isLoggedIn" @click="loadMyCopybooks">
          刷新
        </el-button>
      </div>
      <p class="filter-hint">生成字帖时会自动存到账号，换设备登录也能看到。</p>

      <el-alert
        v-if="!isLoggedIn"
        type="info"
        title="请先登录后再保存字帖"
        show-icon
        :closable="false"
        class="state-alert"
      />

      <el-alert
        v-if="myCopybookError"
        type="error"
        :title="myCopybookError"
        show-icon
        :closable="false"
        class="state-alert"
      >
        <el-button size="small" type="primary" @click="loadMyCopybooks">重试</el-button>
      </el-alert>

      <div v-loading="myCopybookLoading" class="my-copybook-list">
        <div v-for="book in myCopybooks" :key="book.id" class="my-copybook-item">
          <div class="my-copybook-main">
            <div class="my-copybook-title">{{ book.title }}</div>
            <div class="my-copybook-meta">
              {{ characterCountOf(book) }} 字 · 创建于 {{ formatDateTime(book.createdAt) }}
            </div>
          </div>
          <el-button size="small" type="primary" @click="practiceWithCopybook(book)">
            用这份字帖练习
          </el-button>
        </div>

        <el-empty
          v-if="!myCopybookLoading && !myCopybookError && isLoggedIn && myCopybooks.length === 0"
          description="账号里还没有字帖，生成一份就会自动存进来"
          :image-size="80"
        />
      </div>

      <div v-if="myCopybookTotal > myCopybookPageSize" class="pagination-container">
        <el-pagination
          v-model:current-page="myCopybookPage"
          v-model:page-size="myCopybookPageSize"
          :page-sizes="[10, 20, 50]"
          layout="total, prev, pager, next"
          :total="myCopybookTotal"
          @size-change="handleMyCopybookSizeChange"
          @current-change="handleMyCopybookPageChange"
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
            <span class="detail-value">{{ formatDifficulty(selectedCopybook.difficulty) }}</span>
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

  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import { ArrowLeft, ArrowRight, Notebook } from '@element-plus/icons-vue';
import { useCopybookStore } from '../stores/copybookStore';
import { useUserStore } from '../stores/userStore';
import { formatDifficulty } from '../utils/constants';
import { downloadCanvasAsImage } from '../utils';
// 随包发布的开源中文字体（楷/行/草）。
// 放在本组件里而不是 main.js：字帖页不打开时就不下载这些 woff2。
import '@/assets/fonts.css';

// 状态管理
const copybookStore = useCopybookStore();
const userStore = useUserStore();
const router = useRouter();
const configFormRef = ref(null);
const paperRef = ref(null);
const isGenerating = ref(false);
const hasGenerated = ref(false);
const currentPage = ref(1);
const showHistory = ref(false);
const showGenerated = ref(false);

// 是否已登录：没登录时生成结果只落在本机，不写账号
const isLoggedIn = computed(() => userStore.isLoggedIn);

// 初始化
onMounted(() => {
  copybookStore.initialize();
  loadCopybooks();
  loadPopularCopybooks();
  loadMyCopybooks();
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

// ---------------------------------------------------------------- 我的字帖（账号里的自建字帖）

const myCopybooks = ref([]);
const myCopybookTotal = ref(0);
const myCopybookPage = ref(1);
const myCopybookPageSize = ref(10);
const myCopybookLoading = ref(false);
const myCopybookError = ref('');

// 列表端点返回分页信封 {content,page,size,total,totalPages}
const loadMyCopybooks = async () => {
  if (!isLoggedIn.value) {
    myCopybooks.value = [];
    myCopybookTotal.value = 0;
    myCopybookError.value = '';
    return;
  }

  myCopybookLoading.value = true;
  myCopybookError.value = '';
  try {
    const payload = await copybookStore.fetchMyCopybooks({
      page: myCopybookPage.value - 1,
      size: myCopybookPageSize.value
    });
    const page = readPage(payload, copybookStore.myCopybooks, copybookStore.myCopybookTotal);
    myCopybooks.value = page.content;
    myCopybookTotal.value = page.total;
  } catch (error) {
    myCopybooks.value = [];
    myCopybookTotal.value = 0;
    myCopybookError.value = error?.message || '获取我的字帖失败';
    ElMessage.error(myCopybookError.value);
    console.error('获取我的字帖失败:', error);
  } finally {
    myCopybookLoading.value = false;
  }
};

const handleMyCopybookPageChange = () => {
  loadMyCopybooks();
};

const handleMyCopybookSizeChange = () => {
  myCopybookPage.value = 1;
  loadMyCopybooks();
};

// 字数：后端给了 characterCount 就用它，没给就按正文长度算
const characterCountOf = (book) => {
  if (typeof book?.characterCount === 'number') {
    return book.characterCount;
  }
  return book?.content?.length || 0;
};

// 用这份字帖去练习：带上字帖 id 跳到练习页
const practiceWithCopybook = (book) => {
  if (!book?.id) {
    ElMessage.warning('这份字帖缺少 ID，无法直接练习');
    return;
  }
  router.push({ path: '/practice', query: { copybookId: book.id } });
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

// 字体设置映射到真实字体族。
// 楷/行/草用随包发布的开源字体（CP-WenKai / CP-LongCang / CP-MaoCao），
// 系统字体只作为字集外的回退，避免出现豆腐块。
// 隶/篆暂时仍用系统字体：找不到许可清晰且覆盖足够的开源替代。
const FONT_FAMILIES = {
  kai: "'CP-WenKai', 'Kaiti SC', 'STKaiti', 'Songti SC', 'SimSun', serif",
  xing: "'CP-LongCang', 'Kaiti SC', 'STKaiti', 'Songti SC', 'SimSun', serif",
  cao: "'CP-MaoCao', 'Kaiti SC', 'STKaiti', 'Songti SC', 'SimSun', serif",
  li: "'LiSu', 'SimSun', serif",
  zhuan: "'FZZhuan-LeiZhen-Z03', 'SimSun', serif"
};
const fontFamily = computed(() => FONT_FAMILIES[copybookConfig.value.font] || "'SimSun', serif");
// document.fonts.load 只认字体栈里的第一个族名，后面的只是渲染回退，
// 所以要单独取出首选族名来触发 webfont 加载。
const primaryFontFamily = computed(
  () => (FONT_FAMILIES[copybookConfig.value.font] || "'SimSun', serif").split(',')[0].trim()
);

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

    // 调用store的生成方法：本地生成 + 存到账号
    const result = await copybookStore.generateCopybook();

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

    if (result.savedToAccount) {
      ElMessage.success('字帖已保存到你的账号，可在「我的字帖」中查看');
      await loadMyCopybooks();
    } else if (result.reason === 'not-logged-in') {
      ElMessage.info('字帖已生成（仅本机保存），登录后可存到账号');
    } else {
      ElMessage.warning(`字帖已生成，但没能存到账号：${result.error?.message || '请稍后重试'}`);
    }

  } catch (error) {
    if (error !== false) {
      ElMessage.error(error.message || '生成失败，请检查输入');
      console.error('Generate copybook error:', error);
    }
  } finally {
    isGenerating.value = false;
  }
};

// canvas 绘制不会等 webfont：字体没就绪时 ctx.font 拿不到真实字形，
// 会用回退字体画字，于是「页面预览看着对、下载下来的 PNG 是错的」。
// 所以画之前先把要用的那些字对应的字体分片加载完。
const ensureFontLoaded = async (fontSize, family, text) => {
  if (!document.fonts || typeof document.fonts.load !== 'function') return;
  const spec = `${fontSize}px ${family}`;
  try {
    await document.fonts.load(spec, text || ' ');
    await document.fonts.ready;
  } catch (error) {
    // 加载失败不能拖垮整个导出 —— 退回系统字体也比导不出强
    console.warn('字体加载失败，导出将使用回退字体:', error);
  }
};

// 把当前页的字渲染成 PNG 并下载
const renderCurrentPage = async () => {
  const chars = currentPageChars.value;
  const columns = 4;
  const rows = Math.ceil(chars.length / columns);
  const cell = Math.max(80, copybookConfig.value.fontSize + 32);

  const canvas = document.createElement('canvas');
  canvas.width = columns * cell;
  canvas.height = rows * cell;
  const ctx = canvas.getContext('2d');
  if (!ctx) throw new Error('当前浏览器不支持 Canvas 导出');

  // 先等字体就绪，再设置 ctx.font
  await ensureFontLoaded(
    copybookConfig.value.fontSize,
    primaryFontFamily.value,
    chars.join('')
  );

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
const downloadCopybook = async () => {
  if (!hasGenerated.value || currentPageChars.value.length === 0) {
    ElMessage.warning('请先生成字帖');
    return;
  }

  try {
    const canvas = await renderCurrentPage();
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
/* ============================================================
   字帖生成页版式
   - 页面容器与全站内容列共用一条左右边界（不再是「三块白卡一套宽、
     下面双栏又一套宽」）
   - 间距刻度：4 / 8 / 12 / 16 / 24 / 32 / 48，组内小、组间大
   ============================================================ */
.copybook-container {
  --sp-1: 4px;
  --sp-2: 8px;
  --sp-3: 12px;
  --sp-4: 16px;
  --sp-5: 24px;
  --sp-6: 32px;
  --sp-7: 48px;

  width: 100%;
  max-width: 1200px;
  margin-inline: auto;
  padding-block: var(--sp-2);
}

/* 页头：左对齐 + 墨线，全站同一套写法 */
.copybook-header {
  padding-left: var(--sp-3);
  border-left: 3px solid var(--primary-color);
  margin-bottom: var(--sp-6);
}

.section-title {
  font-size: clamp(1.5rem, 1.2rem + 1vw, 2rem);
  color: #333;
  font-family: 'SimSun', serif;
  letter-spacing: 0.08em;
  margin-bottom: var(--sp-1);
}

.section-subtitle {
  color: var(--el-text-color-secondary);
}

/* 三块白色卡片：同宽、同内边距、同圆角 */
.library-section,
.popular-section,
.mine-section {
  background-color: rgba(255, 255, 255, 0.95);
  padding: var(--sp-5);
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(90, 60, 30, 0.08);
  margin-bottom: var(--sp-5);
}

/* 卡片内的分区标题：标题与内容之间的距离比区块之间更小 */
.config-title {
  font-size: 1.05rem;
  margin-bottom: var(--sp-4);
  color: #333;
  font-family: 'SimSun', serif;
  letter-spacing: 0.06em;
  padding-bottom: var(--sp-2);
  border-bottom: 1px solid rgba(139, 69, 19, 0.14);
}

/* 标题 + 右侧操作的一行：分隔线画在整行下方 */
.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: var(--sp-3);
  padding-bottom: var(--sp-2);
  margin-bottom: var(--sp-4);
  border-bottom: 1px solid rgba(139, 69, 19, 0.14);
}

.section-header .config-title {
  margin-bottom: 0;
  padding-bottom: 0;
  border-bottom: none;
}

.filter-bar {
  display: flex;
  align-items: center;
  gap: var(--sp-3);
  flex-wrap: wrap;
  margin-bottom: var(--sp-4);
}

.filter-select {
  width: 200px;
}

.filter-hint {
  color: var(--el-text-color-secondary);
  font-size: 0.85rem;
}

.state-alert {
  margin-bottom: var(--sp-4);
}

.copybook-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: var(--sp-4);
  min-height: 120px;
}

.copybook-card {
  display: flex;
  flex-direction: column;
  gap: var(--sp-2);
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
  color: var(--el-text-color-secondary);
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
  color: var(--el-text-color-secondary);
  font-size: 0.85rem;
  min-height: 2.4em;
  overflow: hidden;
}

.copybook-meta {
  display: flex;
  gap: var(--sp-2);
}

.copybook-stat {
  color: var(--el-text-color-secondary);
  font-size: 0.8rem;
}

.pagination-container {
  margin-top: var(--sp-4);
  display: flex;
  justify-content: center;
}

/* 热门字帖 */
.popular-list {
  display: flex;
  flex-direction: column;
  gap: var(--sp-2);
  min-height: 60px;
}

.popular-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: var(--sp-3);
  padding: var(--sp-3) var(--sp-4);
  background-color: rgba(243, 236, 231, 0.5);
  border: 1px solid rgba(139, 69, 19, 0.12);
  border-radius: 4px;
  cursor: pointer;
  transition: background-color 0.3s, border-color 0.3s, color 0.3s;
}

.popular-item:hover {
  border-color: var(--primary-color);
  color: var(--primary-color);
}

.popular-count {
  color: var(--el-text-color-secondary);
  font-size: 0.8rem;
  white-space: nowrap;
}

/* 我的字帖 */
.my-copybook-list {
  display: flex;
  flex-direction: column;
  gap: var(--sp-2);
  min-height: 60px;
}

.my-copybook-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: var(--sp-4);
  flex-wrap: wrap;
  padding: var(--sp-3) var(--sp-4);
  background-color: rgba(243, 236, 231, 0.5);
  border: 1px solid rgba(139, 69, 19, 0.12);
  border-radius: 4px;
}

.my-copybook-main {
  min-width: 0;
}

.my-copybook-title {
  color: var(--text-color);
  font-weight: 600;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.my-copybook-meta {
  color: var(--el-text-color-secondary);
  font-size: 0.8rem;
  margin-top: var(--sp-1);
}

/* 字帖详情 */
.detail-body {
  min-height: 120px;
}

.detail-title {
  margin: 0 0 var(--sp-2);
  color: #333;
}

.detail-desc {
  margin: 0 0 var(--sp-4);
  color: var(--el-text-color-secondary);
}

.detail-row {
  margin-bottom: var(--sp-2);
}

.detail-label {
  font-weight: 600;
  color: #333;
  margin-right: var(--sp-2);
}

.detail-value {
  color: var(--el-text-color-secondary);
}

/* 内容设置 / 预览 双栏：
   栅格铺满整条内容列 —— 上面三块白卡与下面两栏共用同一组左右边界。
   两栏等高（align-items 默认 stretch），预览栏内部纵向撑开，
   不再在表单下方留出四百多像素的空白。 */
.copybook-layout {
  display: grid;
  grid-template-columns: minmax(340px, 420px) minmax(0, 1fr);
  gap: var(--sp-5);
  align-items: stretch;
}

.config-panel {
  background-color: rgba(255, 255, 255, 0.95);
  padding: var(--sp-5);
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(90, 60, 30, 0.08);
  min-width: 0;
  display: flex;
  flex-direction: column;
}

.config-section {
  margin-bottom: var(--sp-6);
}

.config-section:last-child {
  margin-bottom: 0;
}

/* 表单本身占满面板高度，生成/下载这组主操作钉在面板底部 ——
   不跟着上面表单项的多少上下漂。 */
.config-panel > .el-form {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.config-panel > .el-form > .config-section:last-child {
  margin-top: auto;
  margin-bottom: 0;
}

.char-count {
  text-align: right;
  font-size: 0.8rem;
  color: var(--el-text-color-secondary);
  margin-top: var(--sp-1);
}

/* 示例文本：提示文字挤在按钮右边只够放下几个字 —— 换到下一行整段可读 */
.config-panel .el-form-item__content {
  flex-wrap: wrap;
}

.sample-hint {
  flex: 1 1 100%;
  margin-left: 0;
  margin-top: var(--sp-2);
  color: var(--el-text-color-secondary);
  font-size: 0.8rem;
  line-height: 1.5;
}

.action-buttons {
  display: flex;
  gap: var(--sp-3);
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

/* 预览面板：与左侧表单等高。纵向 flex + 内容区 flex:1，
   纸张预览在剩余空间里居中，不再是「456px 的小盒子 + 下方一大片空」。 */
.preview-panel {
  background-color: rgba(255, 255, 255, 0.95);
  padding: var(--sp-5);
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(90, 60, 30, 0.08);
  position: sticky;
  top: 76px;
  min-width: 0;
  display: flex;
  flex-direction: column;
}

.preview-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: var(--sp-3);
  margin-bottom: var(--sp-4);
}

.preview-title {
  font-size: 1.15rem;
  color: #333;
  font-family: 'SimSun', serif;
  letter-spacing: 0.06em;
}

.preview-content {
  flex: 1;
  min-height: 360px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.empty-preview {
  text-align: center;
  color: var(--el-text-color-secondary);
}

.empty-icon {
  font-size: 2.75rem;
  margin-bottom: var(--sp-3);
  color: rgba(139, 69, 19, 0.45);
}

.copybook-preview {
  width: 100%;
}

.paper-preview {
  width: 100%;
  min-height: 360px;
  border: 1px solid #ddd;
  border-radius: 4px;
  padding: var(--sp-5);
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

/* 触摸设备：筛选下拉与列表行都是可点目标，抬到 44px */
@media (pointer: coarse) {
  :deep(.filter-select .el-select__wrapper) {
    min-height: 44px;
  }

  .popular-item,
  .my-copybook-item {
    min-height: 44px;
  }
}

/* 响应式设计 */
@media (max-width: 900px) {
  .copybook-layout {
    grid-template-columns: 1fr;
  }

  .preview-panel {
    position: static;
  }
}

@media (max-width: 768px) {
  .library-section,
  .popular-section,
  .mine-section,
  .config-panel,
  .preview-panel {
    padding: var(--sp-4);
  }

  .filter-select {
    width: 100%;
  }

  .filter-bar {
    flex-direction: column;
    align-items: stretch;
  }

  .preview-header {
    flex-direction: column;
    align-items: flex-start;
  }

  .grid-container,
  .blank-container {
    grid-template-columns: repeat(3, 1fr);
  }
}

@media (max-width: 480px) {
  .grid-container,
  .blank-container {
    grid-template-columns: repeat(2, 1fr);
  }

  .paper-preview {
    padding: var(--sp-3);
  }
}

/* 系统开启「减少动态效果」时：保留状态反馈，去掉位移 */
@media (prefers-reduced-motion: reduce) {
  .popular-item,
  .copybook-card:hover {
    transform: none !important;
  }
}
</style>
