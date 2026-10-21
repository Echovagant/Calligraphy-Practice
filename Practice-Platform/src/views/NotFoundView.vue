<template>
  <div class="not-found-container">
    <div class="not-found-content">
      <div class="error-code">404</div>
      <h2 class="error-title">页面未找到</h2>
      <p class="error-message">
        抱歉，您访问的页面不存在或已被移除
      </p>
      <div class="action-buttons">
        <el-button type="primary" @click="goHome">
        返回首页
      </el-button>
        <el-button @click="goBack">
          返回上一页
        </el-button>
      </div>
    </div>
    
    <!-- 装饰元素 -->
    <div class="decorative-elements">
      <div class="calligraphy-brush"></div>
      <div class="ink-splash"></div>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router';

const router = useRouter();

// 返回首页
const goHome = () => {
  router.push('/');
};

// 返回上一页
const goBack = () => {
  router.back();
};
</script>

<style scoped>
.not-found-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #f8f9fa;
  position: relative;
  overflow: hidden;
}

.not-found-content {
  text-align: center;
  background: rgba(255, 255, 255, 0.95);
  padding: 3rem;
  border-radius: 12px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2);
  position: relative;
  z-index: 2;
  max-width: 500px;
  width: 90%;
}

.error-code {
  font-size: 6rem;
  font-weight: 700;
  color: #333;
  line-height: 1;
  margin-bottom: 1rem;
  font-family: 'SimSun', serif;
  text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.1);
}

.error-title {
  font-size: 2rem;
  color: #555;
  margin-bottom: 1rem;
  font-family: 'Microsoft YaHei', sans-serif;
}

.error-message {
  font-size: 1.1rem;
  color: #777;
  margin-bottom: 2rem;
  line-height: 1.6;
}

.action-buttons {
  display: flex;
  gap: 1rem;
  justify-content: center;
  flex-wrap: wrap;
}

/* 书法风格按钮 —— 主色走全局变量，不要在这里硬覆盖 Element Plus 的主色 */
:deep(.el-button) {
  border-radius: 4px;
  padding: 0.75rem 2rem;
  font-size: 1rem;
  font-family: 'Microsoft YaHei', sans-serif;
  /* 只过渡颜色与位移，不动宽高 */
  transition: color 0.3s ease, background-color 0.3s ease, border-color 0.3s ease,
    transform 0.3s ease, box-shadow 0.3s ease;
}

:deep(.el-button--primary:hover) {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(139, 69, 19, 0.3);
}

:deep(.el-button:not(.el-button--primary)) {
  background-color: transparent;
  border-color: var(--primary-color);
  color: var(--primary-color);
}

:deep(.el-button:not(.el-button--primary):hover) {
  background-color: #f3ece7;
  border-color: var(--el-color-primary-dark-2);
  color: var(--el-color-primary-dark-2);
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(139, 69, 19, 0.2);
}

.decorative-elements {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  pointer-events: none;
  z-index: 1;
}

.calligraphy-brush {
  position: absolute;
  top: 10%;
  right: 10%;
  width: 120px;
  height: 120px;
  background: url('data:image/svg+xml;utf8,<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 100 100"><path d="M20,10 Q40,5 60,10 Q80,15 90,40 Q95,65 80,80 Q65,95 40,90 Q15,85 10,60 Q5,35 20,10 Z" fill="none" stroke="%238B4513" stroke-width="1" opacity="0.1"/></svg>') no-repeat center;
  background-size: contain;
  transform: rotate(15deg);
}

.ink-splash {
  position: absolute;
  bottom: 10%;
  left: 10%;
  width: 150px;
  height: 150px;
  background: url('data:image/svg+xml;utf8,<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 100 100"><path d="M50,10 C65,20 80,35 85,55 C90,75 75,90 55,85 C35,80 20,65 10,50 C20,35 35,20 50,10 Z" fill="none" stroke="%23666" stroke-width="1" opacity="0.1"/></svg>') no-repeat center;
  background-size: contain;
  transform: rotate(-10deg);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .error-code {
    font-size: 4rem;
  }
  
  .error-title {
    font-size: 1.5rem;
  }
  
  .not-found-content {
    padding: 2rem 1.5rem;
  }
  
  .calligraphy-brush,
  .ink-splash {
    transform: scale(0.7);
  }
}

@media (max-width: 480px) {
  .error-code {
    font-size: 3rem;
  }
  
  .error-title {
    font-size: 1.2rem;
  }
  
  .action-buttons {
    flex-direction: column;
    gap: 0.5rem;
  }
}
</style>