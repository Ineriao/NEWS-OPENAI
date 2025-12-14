<template>
  <div class="not-found">
    <div class="not-found-content">
      <h1 class="error-code">404</h1>
      <p class="error-message">抱歉，页面未找到</p>
      <el-button type="primary" class="back-btn" @click="$router.push('/home')">
        返回首页
      </el-button>
    </div>
  </div>
</template>

<style scoped>
.not-found {
  min-height: 80vh;
  display: flex;
  align-items: center;
  justify-content: center;
}

.not-found-content {
  text-align: center;
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(10px);
  border-radius: 12px;
  padding: 60px 80px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
}

.error-code {
  font-size: 120px;
  font-weight: 900;
  color: rgba(0, 0, 0, 0.85);
  letter-spacing: 0.5rem;
  line-height: 1;
  margin-bottom: 20px;
  background: linear-gradient(135deg, #A795BF, #6C5DAB);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.error-message {
  font-size: 18px;
  color: rgba(0, 0, 0, 0.6);
  margin-bottom: 30px;
  letter-spacing: 0.1rem;
}

.back-btn {
  background: linear-gradient(135deg, #A795BF, #6C5DAB);
  border: none;
  font-weight: 600;
  letter-spacing: 0.1rem;
  padding: 12px 30px;
  height: auto;
}

.back-btn:hover {
  opacity: 0.9;
}
</style>
