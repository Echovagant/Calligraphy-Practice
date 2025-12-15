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
              <div class="example-texts">
                <el-tag
                  v-for="example in exampleTexts"
                  :key="example"
                  @click="useExampleText(example)"
                  class="example-tag"
                  effect="plain"
                >
                  {{ example }}
                </el-tag>
              </div>
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
              :style="{ backgroundColor: copybookConfig.paperColor }"
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
import { ref,  computed, onMounted } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { useCopybookStore } from '../stores/copybookStore';

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
});

// 示例文本
const exampleTexts = [
  '永字八法',
  '天行健，君子以自强不息',
  '上善若水',
  '厚德载物',
  '宁静致远'
];

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
const useExampleText = (text) => {
  copybookStore.setContent(text);
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

// 下载字帖
const downloadCopybook = () => {
  if (!hasGenerated.value || !paperRef.value) return;

  // 调用store的下载方法
  copybookStore.downloadCopybook();

  // 这里应该实现实际的下载功能
  // 由于是模拟环境，我们只显示提示
  ElMessage.success('字帖已加入下载队列（模拟）');

  // 实际项目中，可以使用html2canvas等库将预览区域转换为图片下载
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

.copybook-layout {
  display: flex;
  gap: 2rem;
  justify-content: center;
  flex-wrap: wrap;
}

/* 配置面板样式 */
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
  color: #999;
  margin-top: 0.5rem;
}

.example-texts {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
}

.example-tag {
  cursor: pointer;
  transition: all 0.3s;
}

.example-tag:hover {
  color: #8B4513;
  border-color: #8B4513;
}

.action-buttons {
  display: flex;
  gap: 1rem;
}

.generate-btn,
.download-btn {
  flex: 1;
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
  color: #999;
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

/* 横线布局 */
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
  font-family: 'SimSun', serif;
  color: #333;
}

.history-buttons {
  display: flex;
  gap: 1rem;
  margin-top: 1rem;
}

.history-btn {
  flex: 1;
}

.history-panel,
.generated-panel {
  margin-top: 1.5rem;
  padding: 1rem;
  background-color: #f8f9fa;
  border-radius: 6px;
  border: 1px solid #e9ecef;
}

.history-header,
.generated-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1rem;
}

.history-header h4,
.generated-header h4 {
  margin: 0;
  color: #333;
  font-size: 1rem;
}

.empty-history,
.empty-generated {
  text-align: center;
  color: #999;
  padding: 1rem;
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
