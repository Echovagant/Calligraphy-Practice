<template>
  <div class="my-practices-container">
    <div class="page-header">
      <h2 class="page-title">练习记录</h2>
      <p class="page-subtitle">跟踪和管理您的书法练习进度</p>
    </div>
    
    <div class="stats-overview">
      <div class="stat-card">
        <div class="stat-number">{{ totalPracticeDays }}</div>
        <div class="stat-label">练习天数</div>
      </div>
      <div class="stat-card">
        <div class="stat-number">{{ totalPracticeHours.toFixed(1) }}</div>
        <div class="stat-label">总练习时长(小时)</div>
      </div>
      <div class="stat-card">
        <div class="stat-number">{{ totalCharacters }}</div>
        <div class="stat-label">练习字数</div>
      </div>
      <div class="stat-card">
        <div class="stat-number">{{ maxStreakDays }}</div>
        <div class="stat-label">最长连续练习(天)</div>
      </div>
    </div>
    
    <div class="filter-section">
      <el-date-picker
        v-model="dateRange"
        type="daterange"
        range-separator="至"
        start-placeholder="开始日期"
        end-placeholder="结束日期"
        class="date-picker"
      />
      
      <el-select v-model="filterStyle" placeholder="筛选书法风格" class="filter-select">
        <el-option label="全部" value="" />
        <el-option label="楷书" value="regular" />
        <el-option label="行书" value="running" />
        <el-option label="草书" value="cursive" />
        <el-option label="隶书" value="official" />
        <el-option label="篆书" value="seal" />
      </el-select>
      
      <el-button type="primary" @click="refreshPractices">
        <i class="el-icon-refresh"></i> 刷新数据
      </el-button>
    </div>
    
    <div class="practice-content">
      <!-- 练习日历 -->
      <div class="calendar-section">
        <div class="section-title">练习日历</div>
        <div class="calendar-wrapper">
          <!-- 简化版日历视图 -->
          <div class="calendar-heatmap">
            <div class="calendar-month" v-for="month in 6" :key="month">
              <div class="month-label">{{ getMonthLabel(month) }}</div>
              <div class="calendar-days">
                <div 
                  v-for="day in 30" 
                  :key="day"
                  class="calendar-day"
                  :class="getCalendarDayClass(month, day)"
                  :title="getCalendarDayTooltip(month, day)"
                ></div>
              </div>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 练习记录表格 -->
      <div class="table-section">
        <div class="section-title">最近练习记录</div>
        <el-table :data="filteredPractices" style="width: 100%" border>
          <el-table-column prop="date" label="练习日期" width="150">
            <template #default="scope">
              {{ formatDate(scope.row.date) }}
            </template>
          </el-table-column>
          <el-table-column prop="style" label="书法风格" width="120">
            <template #default="scope">
              {{ getStyleName(scope.row.style) }}
            </template>
          </el-table-column>
          <el-table-column prop="duration" label="练习时长(分钟)" width="150">
            <template #default="scope">
              {{ scope.row.duration }}
            </template>
          </el-table-column>
          <el-table-column prop="charactersCount" label="练习字数" width="120" />
          <el-table-column prop="content" label="练习内容" show-overflow-tooltip>
            <template #default="scope">
              {{ scope.row.content || '无详细记录' }}
            </template>
          </el-table-column>
          <el-table-column prop="notes" label="练习笔记" show-overflow-tooltip />
          <el-table-column label="操作" width="150" fixed="right">
            <template #default="scope">
              <el-button type="primary" size="small" @click="viewPracticeDetail(scope.row)">
                查看详情
              </el-button>
              <el-button type="danger" size="small" @click="deletePractice(scope.row.id)">
                删除
              </el-button>
            </template>
          </el-table-column>
        </el-table>
        
        <!-- 分页 -->
        <div class="pagination-container" v-if="totalPages > 1">
          <el-pagination
            v-model:current-page="currentPage"
            v-model:page-size="pageSize"
            :page-sizes="[10, 20, 50, 100]"
            layout="total, sizes, prev, pager, next, jumper"
            :total="totalPractices"
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
      width="70%"
      :before-close="closeDialog"
    >
      <div class="practice-detail">
        <div class="detail-row">
          <span class="detail-label">练习日期：</span>
          <span class="detail-value">{{ formatDate(selectedPractice.date) }}</span>
        </div>
        <div class="detail-row">
          <span class="detail-label">书法风格：</span>
          <span class="detail-value">{{ getStyleName(selectedPractice.style) }}</span>
        </div>
        <div class="detail-row">
          <span class="detail-label">练习时长：</span>
          <span class="detail-value">{{ selectedPractice.duration }} 分钟</span>
        </div>
        <div class="detail-row">
          <span class="detail-label">练习字数：</span>
          <span class="detail-value">{{ selectedPractice.charactersCount }}</span>
        </div>
        <div class="detail-row">
          <span class="detail-label">练习内容：</span>
          <div class="detail-content">{{ selectedPractice.content || '无详细记录' }}</div>
        </div>
        <div class="detail-row">
          <span class="detail-label">练习笔记：</span>
          <div class="detail-notes">{{ selectedPractice.notes || '无笔记' }}</div>
        </div>
        <div v-if="selectedPractice.attachments && selectedPractice.attachments.length > 0" class="detail-row">
          <span class="detail-label">练习附件：</span>
          <div class="detail-attachments">
            <div 
              v-for="(attachment, index) in selectedPractice.attachments" 
              :key="index"
              class="attachment-item"
            >
              <el-image
                :src="attachment.url"
                :preview-src-list="selectedPractice.attachments.map(a => a.url)"
                style="width: 100px; height: 100px;"
                fit="cover"
              />
            </div>
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
import { ref, reactive, onMounted, computed } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';

// 状态管理 - userStore已废弃，使用后端API获取用户信息

// 筛选条件
const dateRange = ref([]);
const filterStyle = ref('');
const searchKeyword = ref('');

// 练习记录数据
const practices = ref([]);
const currentPage = ref(1);
const pageSize = ref(10);

// 统计数据
const totalPracticeDays = ref(0);
const totalPracticeHours = ref(0);
const totalCharacters = ref(0);
const maxStreakDays = ref(0);

// 对话框
const dialogVisible = ref(false);
const selectedPractice = reactive({
  id: null,
  date: '',
  style: '',
  duration: 0,
  charactersCount: 0,
  content: '',
  notes: '',
  attachments: []
});

// 计算属性
const filteredPractices = computed(() => {
  let filtered = [...practices.value];
  
  // 日期筛选
  if (dateRange.value && dateRange.value.length === 2) {
    const startDate = new Date(dateRange.value[0]);
    const endDate = new Date(dateRange.value[1]);
    filtered = filtered.filter(practice => {
      const practiceDate = new Date(practice.date);
      return practiceDate >= startDate && practiceDate <= endDate;
    });
  }
  
  // 风格筛选
  if (filterStyle.value) {
    filtered = filtered.filter(practice => practice.style === filterStyle.value);
  }
  
  // 按日期排序（最新的在前）
  filtered.sort((a, b) => new Date(b.date) - new Date(a.date));
  
  // 更新总数（避免在computed中直接修改ref值）
  // const totalFiltered = filtered.length;
  
  // 分页
  const start = (currentPage.value - 1) * pageSize.value;
  const end = start + pageSize.value;
  return filtered.slice(start, end);
});

const totalPractices = computed(() => {
  let filtered = practices.value;
  
  // 按搜索关键词过滤
  if (searchKeyword.value) {
    filtered = filtered.filter(practice =>
      practice.title.includes(searchKeyword.value) ||
      practice.content.includes(searchKeyword.value)
    );
  }
  
  // 按字体样式过滤
  if (filterStyle.value) {
    filtered = filtered.filter(practice => practice.style === filterStyle.value);
  }
  
  return filtered.length;
});
const totalPages = computed(() => {
  return Math.ceil(totalPractices.value / pageSize.value);
});

// 统计数据 - 使用纯函数计算，避免副作用
const calculateTotalPracticeDays = () => {
  const uniqueDays = new Set(practices.value.map(p => p.date));
  return uniqueDays.size;
};

const calculateTotalPracticeHours = () => {
  const totalMinutes = practices.value.reduce((sum, p) => sum + p.duration, 0);
  return totalMinutes / 60;
};

const calculateTotalCharacters = () => {
  return practices.value.reduce((sum, p) => sum + p.charactersCount, 0);
};

const calculateMaxStreakDays = () => {
  const sortedDates = practices.value
    .map(p => new Date(p.date))
    .sort((a, b) => a - b);
    
  if (sortedDates.length === 0) return 0;
  
  let maxStreak = 1;
  let currentStreak = 1;
  
  for (let i = 1; i < sortedDates.length; i++) {
    const prevDate = new Date(sortedDates[i - 1]);
    const currDate = new Date(sortedDates[i]);
    const dayDiff = (currDate - prevDate) / (1000 * 60 * 60 * 24);
    
    if (dayDiff === 1) {
      currentStreak++;
      maxStreak = Math.max(maxStreak, currentStreak);
    } else if (dayDiff > 1) {
      currentStreak = 1;
    }
  }
  
  return maxStreak;
};

// 生命周期钩子
onMounted(() => {
  loadPractices();
  // 初始化统计数据
  totalPracticeDays.value = calculateTotalPracticeDays();
  totalPracticeHours.value = calculateTotalPracticeHours();
  totalCharacters.value = calculateTotalCharacters();
  maxStreakDays.value = calculateMaxStreakDays();
});

// 加载练习记录
const loadPractices = async () => {
  try {
    // 在实际项目中，这里应该调用API获取用户练习记录
    // const practiceData = await userStore.fetchPractices();
    // practices.value = practiceData;
    
    // 这里使用模拟数据
    console.log('加载练习记录');
    
    // 更新统计数据
    totalPracticeDays.value = calculateTotalPracticeDays();
    totalPracticeHours.value = calculateTotalPracticeHours();
    totalCharacters.value = calculateTotalCharacters();
    maxStreakDays.value = calculateMaxStreakDays();
  } catch (error) {
    console.error('加载练习记录失败:', error);
    ElMessage.error('加载练习记录失败，请稍后重试');
  }
};

// 刷新练习记录
const refreshPractices = () => {
  currentPage.value = 1;
  loadPractices();
  ElMessage.success('数据已刷新');
};

// 查看练习详情
const viewPracticeDetail = (practice) => {
  Object.assign(selectedPractice, practice);
  dialogVisible.value = true;
};

// 删除练习记录
const deletePractice = async (practiceId) => {
  try {
    await ElMessageBox.confirm(
      '确定要删除这个练习记录吗？此操作不可撤销',
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    );
    
    // 在实际项目中，这里应该调用API删除练习记录
    const index = practices.value.findIndex(p => p.id === practiceId);
    if (index !== -1) {
      practices.value.splice(index, 1);
      ElMessage.success('练习记录已删除');
    }
  } catch (error) {
    // 如果用户取消确认，则不处理错误
    if (error !== 'cancel') {
      console.error('删除练习记录失败:', error);
      ElMessage.error('删除练习记录失败，请稍后重试');
    }
  }
};

// 导出练习详情
const exportPracticeDetail = (practice) => {
  // 在实际项目中，这里应该实现导出功能
  ElMessage.success('练习详情已导出为PDF');
  console.log('导出练习详情:', practice);
};

// 关闭对话框
const closeDialog = () => {
  dialogVisible.value = false;
  // 重置选中的练习
  Object.keys(selectedPractice).forEach(key => {
    selectedPractice[key] = key === 'duration' || key === 'charactersCount' ? 0 : 
                          key === 'attachments' ? [] : '';
  });
};

// 分页处理
const handleSizeChange = (newSize) => {
  pageSize.value = newSize;
  currentPage.value = 1;
};

const handleCurrentChange = (newCurrent) => {
  currentPage.value = newCurrent;
};

// 工具函数
const getStyleName = (style) => {
  const styleMap = {
    'regular': '楷书',
    'running': '行书',
    'cursive': '草书',
    'official': '隶书',
    'seal': '篆书'
  };
  return styleMap[style] || style;
};

const formatDate = (dateStr) => {
  if (!dateStr) return '';
  const date = new Date(dateStr);
  return `${date.getFullYear()}年${date.getMonth() + 1}月${date.getDate()}日`;
};

const getMonthLabel = (monthOffset) => {
  const now = new Date();
  const targetDate = new Date(now.getFullYear(), now.getMonth() - monthOffset + 1, 1);
  return new Intl.DateTimeFormat('zh-CN', { month: 'short' }).format(targetDate);
};

const getCalendarDayClass = () => {
  // 模拟日历热力图数据
  const intensity = Math.floor(Math.random() * 4);
  return `intensity-${intensity}`;
};

const getCalendarDayTooltip = () => {
  // 模拟日历提示信息
  const intensity = Math.floor(Math.random() * 4);
  const hours = intensity * 0.5;
  const characters = intensity * 100;
  
  const now = new Date();
  const targetDate = new Date(now.getFullYear(), now.getMonth(), 1);
  const dateStr = formatDate(targetDate);
  
  if (intensity === 0) {
    return `${dateStr}: 未练习`;
  }
  return `${dateStr}: 练习 ${hours} 小时, ${characters} 字`;
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
  color: #333;
  margin-bottom: 0.5rem;
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
  margin-bottom: 2rem;
  flex-wrap: wrap;
  align-items: center;
}

.date-picker {
  width: 300px;
}

.filter-select {
  min-width: 150px;
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
  transform: scale(1.5);
}

.intensity-0 { background-color: #ebedf0; }
.intensity-1 { background-color: #c6e48b; }
.intensity-2 { background-color: #7bc96f; }
.intensity-3 { background-color: #196127; }

.table-section {
  background-color: rgba(255, 255, 255, 0.95);
  padding: 1.5rem;
  border-radius: 12px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
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
  .filter-select {
    width: 100%;
  }
  
  .calendar-heatmap {
    min-width: 600px;
  }
}
</style>