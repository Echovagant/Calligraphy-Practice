<template>
  <div class="my-practices-container">
    <div class="page-header">
      <h2 class="page-title">练习记录</h2>
      <p class="page-subtitle">跟踪和管理您的书法练习进度</p>
    </div>

    <!-- 练习统计：数据来自 GET /api/copybooks/statistics/user -->
    <div class="stats-overview" v-loading="statsLoading">
      <el-alert
        v-if="statsError"
        type="error"
        :title="statsError"
        show-icon
        :closable="false"
        class="state-alert"
      >
        <el-button size="small" type="primary" @click="loadStatistics">重试</el-button>
      </el-alert>

      <div class="stat-card" v-for="card in statCards" :key="card.key" v-show="statistics">
        <div class="stat-number">{{ card.value }}</div>
        <div class="stat-label">{{ card.label }}</div>
      </div>

      <el-empty
        v-if="!statsLoading && !statsError && !statistics"
        description="暂无统计数据"
        :image-size="80"
      />
    </div>

    <div class="filter-section">
      <el-date-picker
        v-model="dateRange"
        type="daterange"
        range-separator="至"
        start-placeholder="开始日期"
        end-placeholder="结束日期"
        value-format="YYYY-MM-DD"
        :disabled="loading"
        class="date-picker"
        @change="handleFilterChange"
      />

      <el-select
        v-model="filterCategory"
        placeholder="全部分类"
        clearable
        filterable
        allow-create
        default-first-option
        :disabled="loading"
        class="filter-select"
        @change="handleFilterChange"
      >
        <el-option v-for="item in categoryOptions" :key="item" :label="item" :value="item" />
      </el-select>

      <el-input
        v-model="searchKeyword"
        placeholder="搜索标题 / 内容 / 笔记"
        clearable
        :disabled="loading"
        class="filter-input"
        @input="handleSearch"
        @clear="handleSearch"
      />

      <el-button type="primary" :loading="loading" @click="refreshPractices">
        <el-icon><Refresh /></el-icon> 刷新数据
      </el-button>
    </div>
    <p class="filter-hint">共 {{ recordsTotal }} 条练习记录，日期范围、分类与关键词都在服务端筛选。</p>

    <div class="practice-content">
      <!-- 练习记录与日历共用同一份数据，错误提示只报一次 -->
      <el-alert
        v-if="loadError"
        type="error"
        :title="loadError"
        show-icon
        :closable="false"
        class="state-alert"
      >
        <el-button size="small" type="primary" @click="loadPractices">重试</el-button>
      </el-alert>

      <!-- 练习日历 -->
      <div class="calendar-section">
        <div class="section-title">练习日历</div>
        <p class="filter-hint">日历按当前页的记录统计，翻页可看其他日期。</p>
        <div v-loading="loading" class="calendar-wrapper">
          <div class="calendar-heatmap">
            <div class="calendar-month" v-for="month in calendarMonths" :key="month.key">
              <div class="month-label">{{ month.label }}</div>
              <div class="calendar-days">
                <div
                  v-for="day in month.days"
                  :key="day.key"
                  class="calendar-day"
                  :class="getCalendarDayClass(day)"
                  :title="getCalendarDayTooltip(day)"
                ></div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 练习记录表格 -->
      <div class="table-section">
        <div class="section-title">最近练习记录</div>

        <div v-loading="loading" class="table-wrapper">
          <el-table :data="practices" style="width: 100%" border>
            <el-table-column prop="title" label="标题" width="160" show-overflow-tooltip />
            <el-table-column prop="practiceDate" label="练习日期" width="150">
              <template #default="scope">
                {{ formatDate(scope.row.practiceDate || scope.row.createdAt) }}
              </template>
            </el-table-column>
            <el-table-column prop="category" label="分类" width="110">
              <template #default="scope">
                {{ scope.row.category || '未分类' }}
              </template>
            </el-table-column>
            <el-table-column prop="style" label="风格" width="110">
              <template #default="scope">
                {{ scope.row.style || '—' }}
              </template>
            </el-table-column>
            <el-table-column prop="duration" label="练习时长(分钟)" width="140">
              <template #default="scope">
                {{ formatMinutes(scope.row.duration) }}
              </template>
            </el-table-column>
            <el-table-column prop="characterCount" label="练习字数" width="110">
              <template #default="scope">
                {{ scope.row.characterCount ?? 0 }}
              </template>
            </el-table-column>
            <el-table-column prop="content" label="练习内容" show-overflow-tooltip>
              <template #default="scope">
                {{ scope.row.content || '无详细记录' }}
              </template>
            </el-table-column>
            <el-table-column prop="notes" label="练习笔记" show-overflow-tooltip />
            <el-table-column label="操作" width="110" fixed="right">
              <template #default="scope">
                <el-button type="primary" size="small" @click="viewPracticeDetail(scope.row)">
                  查看详情
                </el-button>
              </template>
            </el-table-column>
          </el-table>

          <el-empty
            v-if="!loading && !loadError && practices.length === 0"
            :description="hasFilter ? '没有符合筛选条件的练习记录' : '暂无练习记录'"
          />
        </div>

        <!-- 分页 -->
        <div class="pagination-container" v-if="totalPages > 1">
          <el-pagination
            v-model:current-page="currentPage"
            v-model:page-size="pageSize"
            :page-sizes="[10, 20, 50, 100]"
            layout="total, sizes, prev, pager, next, jumper"
            :total="recordsTotal"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
          />
        </div>
      </div>
    </div>

    <!-- 练习详情对话框 -->
    <el-dialog
      v-model="dialogVisible"
      title="练习详情"
      width="60%"
      :before-close="closeDialog"
    >
      <div class="practice-detail" v-if="selectedPractice">
        <div class="detail-row">
          <span class="detail-label">标题：</span>
          <span class="detail-value">{{ selectedPractice.title || '未命名练习' }}</span>
        </div>
        <div class="detail-row">
          <span class="detail-label">练习日期：</span>
          <span class="detail-value">{{ formatDate(selectedPractice.practiceDate || selectedPractice.createdAt) }}</span>
        </div>
        <div class="detail-row">
          <span class="detail-label">分类：</span>
          <span class="detail-value">{{ selectedPractice.category || '未分类' }}</span>
        </div>
        <div class="detail-row">
          <span class="detail-label">风格：</span>
          <span class="detail-value">{{ selectedPractice.style || '—' }}</span>
        </div>
        <div class="detail-row">
          <span class="detail-label">难度：</span>
          <span class="detail-value">{{ formatDifficulty(selectedPractice.difficulty, '—') }}</span>
        </div>
        <div class="detail-row">
          <span class="detail-label">练习时长：</span>
          <span class="detail-value">{{ formatMinutes(selectedPractice.duration) }} 分钟</span>
        </div>
        <div class="detail-row">
          <span class="detail-label">练习字数：</span>
          <span class="detail-value">{{ selectedPractice.characterCount ?? 0 }}</span>
        </div>
        <div class="detail-row">
          <span class="detail-label">练习内容：</span>
          <div class="detail-content">{{ selectedPractice.content || '无详细记录' }}</div>
        </div>
        <div class="detail-row">
          <span class="detail-label">练习笔记：</span>
          <div class="detail-notes">{{ selectedPractice.notes || '无笔记' }}</div>
        </div>
        <div v-if="selectedPractice.fileUrl" class="detail-row">
          <span class="detail-label">练习附件：</span>
          <div class="detail-attachments">
            <el-image
              :src="selectedPractice.fileUrl"
              :preview-src-list="[selectedPractice.fileUrl]"
              style="width: 100px; height: 100px;"
              fit="cover"
            />
          </div>
        </div>
      </div>

      <template #footer>
        <el-button @click="closeDialog">关闭</el-button>
        <el-button type="primary" @click="exportPracticeDetail(selectedPractice)">导出详情</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';
import { ElMessage } from 'element-plus';
import { Refresh } from '@element-plus/icons-vue';
import { copybookAPI } from '../utils/api';
import { formatDifficulty } from '../utils/constants';
import { downloadFile } from '../utils';

// 筛选条件（日期是 yyyy-MM-dd 字符串，直接就是契约里 from / to 的格式）
const dateRange = ref([]);
const filterCategory = ref('');
const searchKeyword = ref('');

// 练习记录数据（当前页）
const practices = ref([]);
const recordsTotal = ref(0);
const currentPage = ref(1);
const pageSize = ref(10);
const loading = ref(false);
const loadError = ref('');

// 分类的可选值：来自后端返回的记录，不在前端写死。
// 分页之后每一页只带一部分记录，所以见过的分类累积起来，不随翻页丢掉。
const knownCategories = ref([]);
const categoryOptions = computed(() => knownCategories.value);

const rememberCategories = (list) => {
  const categories = new Set(knownCategories.value);
  list.forEach((practice) => {
    if (practice?.category) categories.add(practice.category);
  });
  knownCategories.value = [...categories];
};

const hasFilter = computed(
  () =>
    Boolean(searchKeyword.value?.trim()) ||
    Boolean(filterCategory.value) ||
    (Array.isArray(dateRange.value) && dateRange.value.length === 2)
);

// 总页数由服务端的 total 决定
const totalPages = computed(() => Math.ceil(recordsTotal.value / pageSize.value));

// 统计数据
const statistics = ref(null);
const statsLoading = ref(false);
const statsError = ref('');

// 对话框
const dialogVisible = ref(false);
const selectedPractice = ref(null);

const statCards = computed(() => {
  const stats = statistics.value;
  return [
    { key: 'totalPractices', label: '练习次数', value: stats?.totalPractices ?? 0 },
    { key: 'totalDuration', label: '总练习时长(小时)', value: ((stats?.totalDuration ?? 0) / 3600).toFixed(1) },
    { key: 'characterCount', label: '累计练习字数', value: stats?.characterCount ?? 0 },
    { key: 'streakDays', label: '连续练习(天)', value: stats?.streakDays ?? 0 }
  ];
});

// 练习日历：按真实记录聚合每一天的练习次数 / 时长 / 字数
const practiceByDate = computed(() => {
  const map = new Map();
  practices.value.forEach(practice => {
    const raw = practice.practiceDate || practice.createdAt;
    if (!raw) return;
    const date = new Date(raw);
    if (Number.isNaN(date.getTime())) return;
    const key = `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`;
    const entry = map.get(key) || { count: 0, duration: 0, characterCount: 0 };
    entry.count += 1;
    entry.duration += practice.duration || 0;
    entry.characterCount += practice.characterCount || 0;
    map.set(key, entry);
  });
  return map;
});

const calendarMonths = computed(() => {
  const months = [];
  const now = new Date();
  for (let offset = 5; offset >= 0; offset--) {
    const cursor = new Date(now.getFullYear(), now.getMonth() - offset, 1);
    const year = cursor.getFullYear();
    const month = cursor.getMonth();
    const daysInMonth = new Date(year, month + 1, 0).getDate();
    const days = [];
    for (let day = 1; day <= daysInMonth; day++) {
      const date = new Date(year, month, day);
      const key = `${year}-${String(month + 1).padStart(2, '0')}-${String(day).padStart(2, '0')}`;
      const stat = practiceByDate.value.get(key);
      days.push({
        key,
        day,
        isFuture: date.getTime() > now.getTime(),
        count: stat?.count || 0,
        duration: stat?.duration || 0,
        characterCount: stat?.characterCount || 0
      });
    }
    months.push({
      key: `${year}-${month + 1}`,
      label: `${year}年${month + 1}月`,
      days
    });
  }
  return months;
});

// 生命周期钩子
onMounted(() => {
  loadPractices();
  loadStatistics();
});

// 加载练习记录：GET /api/copybooks/practice/history
// 分页与筛选都在服务端完成：from / to 是 yyyy-MM-dd（含当天）
const loadPractices = async () => {
  loading.value = true;
  loadError.value = '';
  try {
    const params = {
      page: currentPage.value - 1,
      size: pageSize.value
    };

    if (Array.isArray(dateRange.value) && dateRange.value.length === 2) {
      params.from = dateRange.value[0];
      params.to = dateRange.value[1];
    }
    if (filterCategory.value) {
      params.category = filterCategory.value;
    }
    const keyword = searchKeyword.value?.trim();
    if (keyword) {
      params.keyword = keyword;
    }

    // 列表端点返回分页信封 {content,page,size,total,totalPages}
    const payload = await copybookAPI.getPracticeHistory(params);
    const list = Array.isArray(payload)
      ? payload
      : (Array.isArray(payload?.content) ? payload.content : []);
    practices.value = list;
    recordsTotal.value = typeof payload?.total === 'number' ? payload.total : list.length;
    rememberCategories(list);
    if (typeof payload?.page === 'number' && payload.page + 1 !== currentPage.value) {
      currentPage.value = payload.page + 1;
    }
  } catch (error) {
    practices.value = [];
    recordsTotal.value = 0;
    loadError.value = error?.message || '加载练习记录失败';
    ElMessage.error(loadError.value);
    console.error('加载练习记录失败:', error);
  } finally {
    loading.value = false;
  }
};

// 日期范围 / 分类变化：回到第一页重新拉
const handleFilterChange = () => {
  currentPage.value = 1;
  loadPractices();
};

// 关键词输入防抖：避免每敲一个字就打一次接口
let searchTimer = null;
const handleSearch = () => {
  clearTimeout(searchTimer);
  searchTimer = setTimeout(() => {
    currentPage.value = 1;
    loadPractices();
  }, 400);
};

// 加载练习统计
const loadStatistics = async () => {
  statsLoading.value = true;
  statsError.value = '';
  try {
    const payload = await copybookAPI.getUserStatistics();
    statistics.value = payload || null;
  } catch (error) {
    statistics.value = null;
    statsError.value = error?.message || '加载练习统计失败';
    ElMessage.error(statsError.value);
    console.error('加载练习统计失败:', error);
  } finally {
    statsLoading.value = false;
  }
};

// 刷新练习记录
const refreshPractices = async () => {
  currentPage.value = 1;
  await Promise.all([loadPractices(), loadStatistics()]);
  ElMessage.success('数据已刷新');
};

// 查看练习详情
const viewPracticeDetail = (practice) => {
  selectedPractice.value = practice;
  dialogVisible.value = true;
};

// 导出练习详情
const exportPracticeDetail = (practice) => {
  if (!practice) return;
  const payload = {
    id: practice.id,
    标题: practice.title || '',
    练习日期: formatDate(practice.practiceDate || practice.createdAt),
    分类: practice.category || '',
    风格: practice.style || '',
    难度: practice.difficulty || '',
    练习时长秒: practice.duration ?? 0,
    练习字数: practice.characterCount ?? 0,
    练习内容: practice.content || '',
    练习笔记: practice.notes || ''
  };
  try {
    downloadFile(
      JSON.stringify(payload, null, 2),
      `练习详情_${practice.id || Date.now()}.json`,
      'application/json'
    );
    ElMessage.success('练习详情已导出');
  } catch (error) {
    ElMessage.error('练习详情导出失败，请稍后重试');
    console.error('导出练习详情失败:', error);
  }
};

// 关闭对话框
const closeDialog = () => {
  dialogVisible.value = false;
  selectedPractice.value = null;
};

// 分页处理
const handleSizeChange = () => {
  currentPage.value = 1;
  loadPractices();
};

const handleCurrentChange = () => {
  // 页码变化由 v-model:current-page 处理
};

// 工具函数
const formatDate = (dateStr) => {
  if (!dateStr) return '';
  const date = new Date(dateStr);
  if (Number.isNaN(date.getTime())) return '';
  return `${date.getFullYear()}年${date.getMonth() + 1}月${date.getDate()}日`;
};

// 契约里 duration 的单位是秒
const formatMinutes = (seconds) => {
  const value = Number(seconds);
  if (!Number.isFinite(value) || value <= 0) return '0.0';
  return (value / 60).toFixed(1);
};

const getCalendarDayClass = (day) => {
  if (day.isFuture) return 'is-future';
  if (day.count === 0) return 'intensity-0';
  if (day.count === 1) return 'intensity-1';
  if (day.count === 2) return 'intensity-2';
  return 'intensity-3';
};

const getCalendarDayTooltip = (day) => {
  const label = `${day.key}`;
  if (day.isFuture) return `${label}: 未开始`;
  if (day.count === 0) return `${label}: 未练习`;
  return `${label}: 练习 ${day.count} 次, ${formatMinutes(day.duration)} 分钟, ${day.characterCount} 字`;
};
</script>

<style scoped>
.my-practices-container {
  padding: 2rem;
  max-width: 1200px;
  margin: 0 auto;
}

.page-header {
  text-align: center;
  margin-bottom: 2rem;
}

.page-title {
  font-size: 2rem;
  margin-bottom: 0.5rem;
  color: #333;
  font-family: 'SimSun', serif;
}

.page-subtitle {
  color: #666;
  font-size: 1rem;
}

.stats-overview {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 1rem;
  margin-bottom: 2rem;
}

.stat-card {
  background-color: rgba(255, 255, 255, 0.95);
  padding: 1.5rem;
  border-radius: 12px;
  text-align: center;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
  border-left: 4px solid #8B4513;
}

.stat-number {
  font-size: 2rem;
  font-weight: bold;
  color: #8B4513;
  margin-bottom: 0.5rem;
}

.stat-label {
  color: #666;
  font-size: 0.9rem;
}

.filter-section {
  display: flex;
  gap: 1rem;
  margin-bottom: 0.5rem;
  flex-wrap: wrap;
  align-items: center;
}

.filter-hint {
  color: #6b6259;
  font-size: 0.8rem;
  margin: 0 0 1.5rem;
}

.date-picker {
  width: 300px;
}

.filter-select {
  min-width: 150px;
}

.filter-input {
  width: 220px;
}

.state-alert {
  margin-bottom: 1rem;
}

.practice-content {
  display: flex;
  flex-direction: column;
  gap: 2rem;
}

.section-title {
  font-size: 1.2rem;
  font-weight: 600;
  color: #333;
  margin-bottom: 1rem;
  padding-bottom: 0.5rem;
  border-bottom: 2px solid #8B4513;
}

.calendar-section {
  background-color: rgba(255, 255, 255, 0.95);
  padding: 1.5rem;
  border-radius: 12px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
}

.calendar-wrapper {
  overflow-x: auto;
}

.calendar-heatmap {
  display: flex;
  gap: 1rem;
  padding-bottom: 0.5rem;
}

.calendar-month {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.month-label {
  font-size: 0.9rem;
  color: #666;
  margin-bottom: 0.5rem;
}

.calendar-days {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 2px;
}

.calendar-day {
  width: 12px;
  height: 12px;
  border-radius: 2px;
  background-color: #ebedf0;
  cursor: pointer;
  transition: transform 0.2s;
}

.calendar-day:hover {
  /* 原来的 scale(1.5) 会把 12px 的日历格子放大到 18px，直接顶掉相邻格子。
     收敛成 2px 位移。 */
  transform: translateY(-2px);
}

.intensity-0 { background-color: #ebedf0; }
.intensity-1 { background-color: #c6e48b; }
.intensity-2 { background-color: #7bc96f; }
.intensity-3 { background-color: #196127; }
.is-future { background-color: #f5f5f5; }

.table-section {
  background-color: rgba(255, 255, 255, 0.95);
  padding: 1.5rem;
  border-radius: 12px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
}

.table-wrapper {
  min-height: 120px;
}

.pagination-container {
  margin-top: 1rem;
  display: flex;
  justify-content: center;
}

/* 详情对话框样式 */
.practice-detail {
  padding: 1rem;
}

.detail-row {
  margin-bottom: 1rem;
}

.detail-label {
  font-weight: 600;
  color: #333;
  margin-right: 0.5rem;
}

.detail-value {
  color: #666;
}

.detail-content,
.detail-notes {
  margin-top: 0.5rem;
  padding: 1rem;
  background-color: #f9f9f9;
  border-radius: 4px;
  color: #666;
  line-height: 1.6;
  white-space: pre-wrap;
}

.detail-attachments {
  margin-top: 0.5rem;
  display: flex;
  flex-wrap: wrap;
  gap: 1rem;
}

.attachment-item {
  border: 1px solid #e0e0e0;
  border-radius: 4px;
  overflow: hidden;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .my-practices-container {
    padding: 1rem;
  }

  .stats-overview {
    grid-template-columns: 1fr;
  }

  .filter-section {
    flex-direction: column;
    align-items: stretch;
  }

  .date-picker,
  .filter-select,
  .filter-input {
    width: 100%;
  }

  .calendar-heatmap {
    min-width: 600px;
  }
}
</style>
