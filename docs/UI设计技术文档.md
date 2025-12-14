# 新闻发布系统 - UI 设计技术文档

本文档详细介绍本次 UI 统一化修改中使用的技术。


## 目录

| 章节 | 标题 | 内容概要 |
|------|------|----------|
| 一 | 玻璃拟态设计 | backdrop-filter、半透明背景 |
| 二 | CSS 渐变背景动画 | 动态渐变、配色方案 |
| 三 | SVG 波浪动画与 Perlin 噪声 | noise.js、自然随机效果 |
| 四 | SVG 贝塞尔曲线 | 路径命令、平滑曲线算法 |
| 五 | Web Components | 自定义元素、生命周期 |
| 六 | 鼠标交互动画 | 位置追踪、平滑跟随、物理模拟 |
| 七 | requestAnimationFrame | 高性能动画循环 |
| 八 | CSS 变量与动态样式 | 变量定义、JS 动态修改 |
| 九 | UI 统一化总结 | 设计规范、已统一页面清单 |
| 十 | 修改文件清单 | 前端页面、新增文件 |
| 十一 | 落地页集成 | HTML→Vue 转换、路由配置 |
| 十二 | 路由链接修复 | router-link、编程式导航 |
| 十三 | 启动脚本配置 | run.bat、stop.bat、多服务管理 |
| 十四 | 表单验证最佳实践 | 密码确认、联动验证 |
| 十五 | 测试数据管理 | 分类结构、数据导入 |
| 十六 | 前后端数据交互问题 | DTO 字段对齐、调试技巧 |
| 十七 | 登录跳转优化 | 默认跳转路径修改 |
| 十八 | 后台管理页面 UI 统一 | 深色侧边栏、紫色主题 |
| 十九 | 文件写入问题排查 | heredoc、编码、临时脚本 |
| 二十 | 用户权限与个人中心 | 角色体系、路由守卫、Profile 页面 |
| 二十一 | 登录跳转与角色分配 | 角色判断跳转、注册策略 |
| 二十二 | 密码安全与 BCrypt | 哈希 vs 加密、格式解析 |
| 二十三 | 登录状态持久化 | localStorage、Web 存储对比 |
| 二十四 | 测试账号管理 | 预置账号、用户表结构 |
| 二十五 | AI 新闻助手 | OpenAI 兼容 API、边缘触发侧边栏、交互优化 |
| 二十六 | 局域网访问配置 | Vite/Spring Boot/Hono 主机绑定、防火墙 |



---

## 一、玻璃拟态设计 (Glassmorphism)

### 1.1 什么是玻璃拟态？

玻璃拟态是一种现代 UI 设计风格，模拟磨砂玻璃效果，具有以下特点：
- 半透明背景
- 背景模糊效果
- 微妙的边框
- 柔和的阴影

### 1.2 核心 CSS 属性

```css
.glass-card {
  /* 半透明白色背景 */
  background: rgba(255, 255, 255, 0.9);

  /* 背景模糊 - 玻璃拟态核心 */
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);  /* Safari 兼容 */

  /* 圆角 */
  border-radius: 12px;

  /* 柔和阴影 */
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);

  /* 可选：微妙边框 */
  border: 1px solid rgba(255, 255, 255, 0.3);
}
```

### 1.3 backdrop-filter 详解

| 属性值 | 效果 |
|--------|------|
| `blur(10px)` | 模糊背景 |
| `brightness(1.2)` | 增加亮度 |
| `saturate(1.5)` | 增加饱和度 |
| `grayscale(1)` | 灰度化 |

可以组合使用：
```css
backdrop-filter: blur(10px) brightness(1.1) saturate(1.2);
```

### 1.4 浏览器兼容性

| 浏览器 | 支持情况 |
|--------|----------|
| Chrome 76+ | 支持 |
| Firefox 103+ | 支持 |
| Safari 9+ | 需要 `-webkit-` 前缀 |
| Edge 79+ | 支持 |

### 1.5 本项目应用示例

```css
/* 新闻列表卡片 */
.news-list {
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(10px);
  border-radius: 12px;
  padding: 25px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
}

/* 侧边栏卡片 */
.sidebar-card {
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(10px);
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
}
```

---

## 二、CSS 渐变背景动画

### 2.1 线性渐变基础

```css
/* 基本语法 */
background: linear-gradient(方向, 颜色1, 颜色2, ...);

/* 示例 */
background: linear-gradient(135deg, #A795BF, #6C5DAB);
```

### 2.2 多色渐变

```css
background: linear-gradient(
  135deg,
  #CBD4E5 0%,      /* 浅蓝灰 */
  #A795BF 25%,     /* 淡紫色 */
  #FEE9A1 50%,     /* 金黄色 */
  #6C5DAB 75%,     /* 深紫色 */
  #CCA2A7 100%     /* 玫瑰粉 */
);
```

### 2.3 动态渐变动画

```css
body {
  /* 渐变背景 */
  background: linear-gradient(
    135deg,
    #CBD4E5 0%,
    #A795BF 25%,
    #FEE9A1 50%,
    #6C5DAB 75%,
    #CCA2A7 100%
  );

  /* 放大背景，使动画有移动空间 */
  background-size: 400% 400%;

  /* 应用动画 */
  animation: gradientShift 15s ease infinite;
}

@keyframes gradientShift {
  0% {
    background-position: 0% 50%;
  }
  50% {
    background-position: 100% 50%;
  }
  100% {
    background-position: 0% 50%;
  }
}
```

### 2.4 关键点解析

| 属性 | 作用 |
|------|------|
| `background-size: 400% 400%` | 将背景放大4倍，创造移动空间 |
| `background-position` | 控制背景位置，动画通过改变位置实现流动效果 |
| `ease` | 缓动函数，使动画更自然 |
| `infinite` | 无限循环 |

### 2.5 本项目配色方案

| 颜色代码 | 用途 |
|----------|------|
| `#CBD4E5` | 浅蓝灰 - 背景过渡色 |
| `#A795BF` | 淡紫色 - 主题色（浅） |
| `#6C5DAB` | 深紫色 - 主题色（深）、hover 效果 |
| `#FEE9A1` | 金黄色 - 强调色、hover 高亮 |
| `#CCA2A7` | 玫瑰粉 - 背景过渡色 |

---

## 三、SVG 波浪动画与 Perlin 噪声

### 3.1 什么是 Perlin 噪声？

Perlin 噪声是一种程序化生成自然随机效果的算法，由 Ken Perlin 于 1983 年发明。特点：
- **连续性**：相邻点的值平滑过渡
- **自然感**：比纯随机更接近自然界的随机
- **可重复**：相同输入产生相同输出

### 3.2 应用场景

- 地形生成
- 云朵纹理
- **流体动画**（本项目使用）
- 粒子效果

### 3.3 noise.js 核心 API

```javascript
// 设置随机种子
noise.seed(Math.random());

// 2D Perlin 噪声
// 返回值范围：-1 到 1
let value = noise.perlin2(x, y);
```

### 3.4 波浪动画实现原理

```javascript
// 每个点的波浪运动
const move = noise.perlin2(
  (point.x + time * 0.0125) * 0.002,  // x 方向
  (point.y + time * 0.005) * 0.0015   // y 方向
) * 12;  // 振幅

// 计算偏移
point.wave.x = Math.cos(move) * 32;  // 水平偏移
point.wave.y = Math.sin(move) * 16;  // 垂直偏移
```

### 3.5 参数调节

| 参数 | 作用 | 值越大效果 |
|------|------|------------|
| `time * 0.0125` | 时间系数 | 波浪流动越快 |
| `* 0.002` | 频率系数 | 波浪越密集 |
| `* 12` | 振幅 | 波动幅度越大 |
| `* 32 / * 16` | xy偏移系数 | 波浪形状变化 |

---

## 四、SVG 贝塞尔曲线

### 4.1 SVG 路径命令

| 命令 | 含义 | 语法 |
|------|------|------|
| `M` | 移动到 | `M x y` |
| `L` | 直线到 | `L x y` |
| `Q` | 二次贝塞尔曲线 | `Q cx cy x y` |
| `C` | 三次贝塞尔曲线 | `C cx1 cy1 cx2 cy2 x y` |

### 4.2 二次贝塞尔曲线 (Quadratic Bezier)

```
Q cx cy x y
```
- `(cx, cy)` - 控制点
- `(x, y)` - 终点

曲线从当前点出发，被控制点"拉"向一个方向，最终到达终点。

### 4.3 折线 vs 曲线对比

```javascript
// 折线绘制（有锐角）
points.forEach((point, index) => {
  if (index === 0) {
    d = `M ${p.x} ${p.y}`;
  } else {
    d += ` L ${p.x} ${p.y}`;  // L = Line
  }
});

// 曲线绘制（平滑）
points.forEach((point, index) => {
  if (index === 0) {
    d = `M ${p.x} ${p.y}`;
  } else {
    const prev = points[index - 1];
    const midX = (prev.x + p.x) / 2;
    const midY = (prev.y + p.y) / 2;
    d += ` Q ${prev.x} ${prev.y} ${midX} ${midY}`;  // Q = Quadratic
  }
});
```

### 4.4 平滑曲线算法

使用相邻点的中点作为曲线终点，前一个点作为控制点：

```
点1 ----控制点(点2)---- 中点 ----控制点(点3)---- 中点 ...
```

---

## 五、Web Components (Custom Elements)

### 5.1 什么是 Web Components？

Web Components 是一组浏览器原生支持的 API，允许创建可复用的自定义 HTML 元素。

### 5.2 核心 API

| API | 作用 |
|-----|------|
| `Custom Elements` | 定义自定义元素 |
| `Shadow DOM` | 封装样式和结构 |
| `HTML Templates` | 定义可复用模板 |

### 5.3 自定义元素创建

```javascript
// 1. 定义类，继承 HTMLElement
class AWaves extends HTMLElement {
  // 元素被添加到 DOM 时调用
  connectedCallback() {
    this.init();
    this.bindEvents();
    this.startAnimation();
  }

  // 元素从 DOM 移除时调用
  disconnectedCallback() {
    this.cleanup();
  }
}

// 2. 注册自定义元素
customElements.define('a-waves', AWaves);
```

### 5.4 使用自定义元素

```html
<!-- 像普通 HTML 标签一样使用 -->
<a-waves>
  <svg class="js-svg"></svg>
</a-waves>
```

### 5.5 生命周期回调

| 方法 | 触发时机 |
|------|----------|
| `connectedCallback` | 元素添加到 DOM |
| `disconnectedCallback` | 元素从 DOM 移除 |
| `attributeChangedCallback` | 元素属性变化 |
| `adoptedCallback` | 元素被移动到新文档 |

---

## 六、鼠标交互动画

### 6.1 鼠标位置追踪

```javascript
// 鼠标状态对象
this.mouse = {
  x: 0,           // 当前 x
  y: 0,           // 当前 y
  lx: 0,          // 上一帧 x
  ly: 0,          // 上一帧 y
  sx: 0,          // 平滑后的 x
  sy: 0,          // 平滑后的 y
  v: 0,           // 瞬时速度
  vs: 0,          // 平滑速度
  a: 0,           // 移动角度
};

// 鼠标移动事件
onMouseMove(e) {
  this.mouse.x = e.clientX - this.bounding.left;
  this.mouse.y = e.clientY - this.bounding.top;
}
```

### 6.2 平滑跟随算法

```javascript
// 线性插值实现平滑跟随
mouse.sx += (mouse.x - mouse.sx) * 0.1;  // 0.1 是平滑系数
mouse.sy += (mouse.y - mouse.sy) * 0.1;

// 平滑系数越小，跟随越慢越平滑
// 0.1 = 每帧移动 10% 的差距
```

### 6.3 物理模拟参数

| 参数 | 作用 | 调节效果 |
|------|------|----------|
| 影响半径 `l` | 鼠标影响范围 | 越大影响越广 |
| 强度系数 `0.001` | 推力大小 | 越大反应越强 |
| 弹性系数 `0.005` | 恢复速度 | 越大恢复越快 |
| 摩擦系数 `0.92` | 减速程度 | 越小停止越快 |
| 速度倍数 `2.5` | 移动速度 | 越大移动越快 |

---

## 七、requestAnimationFrame 动画

### 7.1 vs setInterval/setTimeout

| 方法 | 帧率 | 性能 | 适用场景 |
|------|------|------|----------|
| `setInterval` | 固定 | 差 | 简单定时任务 |
| `setTimeout` | 固定 | 差 | 延迟执行 |
| `requestAnimationFrame` | 60fps | 优 | 动画、游戏 |

### 7.2 核心优势

1. **与屏幕刷新同步**：自动匹配显示器刷新率（通常 60fps）
2. **后台暂停**：标签页不可见时自动暂停，节省资源
3. **优化批处理**：浏览器统一处理所有动画

### 7.3 基本用法

```javascript
function animate(time) {
  // time 是高精度时间戳（毫秒）

  // 更新动画状态
  updateAnimation(time);

  // 渲染
  render();

  // 递归调用
  requestAnimationFrame(animate);
}

// 启动动画
requestAnimationFrame(animate);
```

---

## 八、CSS 变量与动态样式

### 8.1 CSS 变量定义

```css
/* 在根元素或组件上定义 */
:root {
  --primary-color: #6C5DAB;
  --accent-color: #FEE9A1;
  --glass-bg: rgba(255, 255, 255, 0.9);
  --blur-amount: 10px;
}

/* 使用变量 */
.card {
  background: var(--glass-bg);
  backdrop-filter: blur(var(--blur-amount));
}
```

### 8.2 JavaScript 动态修改 CSS 变量

```javascript
// 设置 CSS 变量
element.style.setProperty('--x', `${mouse.sx}px`);
element.style.setProperty('--y', `${mouse.sy}px`);
```

---

## 九、UI 统一化总结

### 9.1 设计规范

| 元素 | 规范 |
|------|------|
| 圆角 | 12px（卡片）、4-8px（按钮、输入框） |
| 阴影 | `0 4px 20px rgba(0, 0, 0, 0.08)` |
| 背景 | `rgba(255, 255, 255, 0.9)` + `backdrop-filter: blur(10px)` |
| 深色头部 | `rgba(0, 0, 0, 0.9)` |
| 主题色 | `#6C5DAB`（深紫）、`#A795BF`（浅紫） |
| 强调色 | `#FEE9A1`（金黄） |
| 文字色 | `rgba(0, 0, 0, 0.85)`（标题）、`rgba(0, 0, 0, 0.6)`（正文） |

### 9.2 已统一的页面

| 页面 | 文件 | 主要样式 |
|------|------|----------|
| 首页 | `Home.vue` | 轮播图渐变、新闻列表玻璃态、侧边栏深色头部 |
| 登录 | `Login.vue` | 深色卡片、渐变背景动画、渐变按钮 |
| 注册 | `Register.vue` | 同登录页风格 |
| 布局 | `FrontLayout.vue` | 深色头部/底部、logo样式 |
| 分类 | `Category.vue` | 玻璃态列表、紫色主题 |
| 搜索 | `Search.vue` | 玻璃态列表、紫色主题 |
| 详情 | `NewsDetail.vue` | 玻璃态卡片、深色评论头部 |
| 404 | `NotFound.vue` | 居中玻璃态卡片、渐变文字 |
| 预览页 | `preview-landing.html` | 波浪动画、渐变背景、底部导航 |
| AI 助手 | `AiSidebar.vue` | 深色头部、玻璃态侧边栏、边缘触发 |

### 9.3 学习要点总结

1. **玻璃拟态** - `backdrop-filter: blur()` 实现磨砂效果
2. **渐变动画** - `background-size` + `@keyframes` 实现流动背景
3. **SVG 路径** - `M/L/Q/C` 命令绘制直线和曲线
4. **贝塞尔曲线** - 使用中点和控制点实现平滑连接
5. **Perlin 噪声** - 程序化生成自然随机效果
6. **Web Components** - 创建可复用的自定义 HTML 元素
7. **物理模拟** - 速度、摩擦、弹性实现自然交互
8. **CSS 变量** - JavaScript 动态修改样式
9. **requestAnimationFrame** - 高性能动画循环

---

## 十、修改文件清单

### 前端页面样式修改

| 文件路径 | 修改内容 |
|----------|----------|
| `news-frontend/src/style.css` | 添加动态渐变背景动画 |
| `news-frontend/src/views/front/FrontLayout.vue` | 深色头部/底部导航栏 |
| `news-frontend/src/views/front/Home.vue` | 玻璃态卡片、紫色主题色 |
| `news-frontend/src/views/front/Login.vue` | 深色卡片、渐变按钮 |
| `news-frontend/src/views/front/Register.vue` | 深色卡片、渐变按钮 |
| `news-frontend/src/views/front/Category.vue` | 玻璃态新闻列表 |
| `news-frontend/src/views/front/Search.vue` | 玻璃态新闻列表 |
| `news-frontend/src/views/front/NewsDetail.vue` | 玻璃态卡片、深色评论区头部 |
| `news-frontend/src/views/front/NotFound.vue` | 玻璃态卡片、渐变文字 |
| `news-frontend/src/App.vue` | 集成 AI 侧边栏组件 |

### 新增文件

| 文件路径 | 说明 |
|----------|------|
| `news-frontend/preview-landing.html` | 带波浪动画的预览落地页 |
| `news-frontend/src/api/ai.js` | AI API 封装 |
| `news-frontend/src/components/AiSidebar.vue` | AI 侧边栏组件 |
| `news-backend/.../dto/AiChatDTO.java` | AI 聊天请求 DTO |
| `news-backend/.../vo/AiChatVO.java` | AI 聊天响应 VO |
| `news-backend/.../service/AiService.java` | AI 服务接口 |
| `news-backend/.../service/impl/AiServiceImpl.java` | AI 服务实现 |
| `news-backend/.../controller/AiController.java` | AI 控制器 |

### 波浪动画关键修改

- 引入 noisejs Perlin 噪声库
- 自定义 `<a-waves>` Web Component
- 使用贝塞尔曲线（Q命令）替代直线（L命令）实现平滑波浪
- 添加鼠标交互物理模拟

---

## 十一、落地页集成

### 11.1 概述

将独立的 HTML 预览页面 (`preview-landing.html`) 集成到 Vue 项目中，作为网站的入口落地页。

### 11.2 新增文件

| 文件路径 | 说明 |
|----------|------|
| `news-frontend/src/views/front/Landing.vue` | Vue 落地页组件 |

### 11.3 路由配置修改

**文件**: `news-frontend/src/router/index.js`

**路由结构变化**:

| 路径 | 修改前 | 修改后 |
|------|--------|--------|
| `/` | 首页 (Home.vue) | 落地页 (Landing.vue) |
| `/home` | 不存在 | 首页 (Home.vue) |
| `/home/category/:id` | `/category/:id` | 分类页 |
| `/home/news/:id` | `/news/:id` | 新闻详情 |
| `/home/search` | `/search` | 搜索页 |

**代码示例**:

```javascript
const routes = [
  // 落地页作为根路由
  {
    path: '/',
    name: 'Landing',
    component: () => import('@/views/front/Landing.vue'),
    meta: { title: '欢迎' }
  },

  // 前台路由移至 /home
  {
    path: '/home',
    component: () => import('@/views/front/FrontLayout.vue'),
    children: [
      {
        path: '',
        name: 'Home',
        component: () => import('@/views/front/Home.vue'),
        meta: { title: '首页' }
      },
      // ... 其他子路由
    ]
  }
]
```

### 11.4 HTML 到 Vue 组件转换

#### 11.4.1 关键转换点

| HTML/原生 JS | Vue 3 Composition API |
|--------------|----------------------|
| `let variable` | `const variable = ref()` |
| `document.querySelector()` | `ref()` + 模板 ref |
| `addEventListener` | `onMounted()` 中注册 |
| `removeEventListener` | `onUnmounted()` 中清理 |
| 内联 style 计算 | `computed()` 返回样式对象 |

#### 11.4.2 模板 ref 绑定

```vue
<template>
  <div ref="wavesContainer">
    <svg ref="wavesSvg"></svg>
  </div>
</template>

<script setup>
import { ref } from 'vue'

const wavesContainer = ref(null)
const wavesSvg = ref(null)
</script>
```

#### 11.4.3 响应式鼠标状态

```javascript
// 原生 JS
this.mouse = { x: 0, y: 0, ... }

// Vue 3
const mouse = ref({
  x: 0,
  y: 0,
  lx: 0,
  ly: 0,
  sx: 0,
  sy: 0,
  v: 0,
  vs: 0,
  a: 0,
  set: false
})
```

#### 11.4.4 计算属性绑定样式

```javascript
// 光标跟随效果
const cursorStyle = computed(() => ({
  transform: `translate(${mouse.value.sx}px, ${mouse.value.sy}px)`
}))
```

```vue
<div class="cursor-dot" :style="cursorStyle"></div>
```

#### 11.4.5 生命周期管理

```javascript
let animationId = null

onMounted(() => {
  // 初始化
  noise.seed(Math.random())
  setSize()
  setLines()

  // 注册事件
  window.addEventListener('resize', onResize)
  window.addEventListener('mousemove', onMouseMove)
  window.addEventListener('touchmove', onTouchMove, { passive: false })

  // 启动动画
  animationId = requestAnimationFrame(tick)
})

onUnmounted(() => {
  // 清理事件
  window.removeEventListener('resize', onResize)
  window.removeEventListener('mousemove', onMouseMove)
  window.removeEventListener('touchmove', onTouchMove)

  // 停止动画
  if (animationId) {
    cancelAnimationFrame(animationId)
  }
})
```

### 11.5 注意事项

1. **内存泄漏防护**: 必须在 `onUnmounted` 中清理事件监听器和动画帧
2. **ref 访问时机**: 只能在 `onMounted` 之后访问 DOM ref
3. **响应式陷阱**: 访问 ref 值需要 `.value`，但模板中自动解包
4. **passive 事件**: touchmove 需要 `{ passive: false }` 才能调用 `preventDefault()`

### 11.6 页面导航结构

```
Landing.vue (/)
    │
    ├── 首页 (/home)
    │     ├── 分类 (/home/category/:id)
    │     ├── 新闻详情 (/home/news/:id)
    │     └── 搜索 (/home/search)
    │
    ├── 登录 (/login)
    ├── 注册 (/register)
    └── 后台管理 (/admin)
```

---

## 十二、路由链接修复

### 12.1 问题描述

路由结构变更后（前台路由从 `/` 移至 `/home`），页面中的 `router-link` 和 `router.push()` 仍指向旧路径，导致点击链接时出现 404 错误。

### 12.2 修复清单

| 文件 | 修改前 | 修改后 | 位置 |
|------|--------|--------|------|
| `FrontLayout.vue` | `to="/"` | `to="/home"` | 导航栏首页链接 |
| `FrontLayout.vue` | `router.push(\`/category/\${id}\`)` | `router.push(\`/home/category/\${id}\`)` | 分类下拉菜单 |
| `FrontLayout.vue` | `to="/"` | `to="/home"` | 底部首页链接 |
| `Home.vue` | `router.push(\`/news/\${id}\`)` | `router.push(\`/home/news/\${id}\`)` | 新闻卡片点击 |
| `Category.vue` | `$router.push(\`/news/\${id}\`)` | `$router.push(\`/home/news/\${id}\`)` | 新闻列表项点击 |
| `Search.vue` | `$router.push(\`/news/\${id}\`)` | `$router.push(\`/home/news/\${id}\`)` | 搜索结果点击 |

### 12.3 路由链接类型

Vue Router 中有两种常见的路由跳转方式：

#### 12.3.1 声明式导航 (router-link)

```vue
<!-- 字符串路径 -->
<router-link to="/home">首页</router-link>

<!-- 命名路由 -->
<router-link :to="{ name: 'Home' }">首页</router-link>

<!-- 带参数 -->
<router-link :to="{ name: 'Category', params: { id: 1 } }">分类</router-link>

<!-- 带查询参数 -->
<router-link :to="{ name: 'Search', query: { keyword: 'vue' } }">搜索</router-link>
```

#### 12.3.2 编程式导航 (router.push)

```javascript
// 在 setup 中使用
import { useRouter } from 'vue-router'
const router = useRouter()

// 字符串路径
router.push('/home')

// 模板字符串（动态路径）
router.push(`/home/news/${newsId}`)

// 对象形式
router.push({ name: 'NewsDetail', params: { id: newsId } })

// 带查询参数
router.push({ name: 'Search', query: { keyword: searchText } })
```

#### 12.3.3 模板中直接使用 $router

```vue
<!-- Options API 或模板中 -->
<div @click="$router.push(`/home/news/${item.id}`)">
  {{ item.title }}
</div>
```

### 12.4 最佳实践

1. **优先使用命名路由**: 使用 `name` 而非硬编码路径，路由变更时只需改路由配置

```javascript
// 推荐 - 路由路径变更时无需修改
router.push({ name: 'NewsDetail', params: { id: 123 } })

// 不推荐 - 路径变更需要全局搜索替换
router.push(`/home/news/123`)
```

2. **集中管理路由常量**: 将路径定义为常量

```javascript
// constants/routes.js
export const ROUTES = {
  HOME: '/home',
  NEWS_DETAIL: (id) => `/home/news/${id}`,
  CATEGORY: (id) => `/home/category/${id}`
}

// 使用
import { ROUTES } from '@/constants/routes'
router.push(ROUTES.NEWS_DETAIL(123))
```

3. **使用相对路径**: 在嵌套路由中使用相对路径

```javascript
// 在 /home 下的子组件中
router.push('news/123')  // 相对于当前路由
```

### 12.5 排查技巧

当出现 404 问题时，可以使用以下方法快速定位：

```bash
# 搜索所有硬编码的旧路径
grep -rn "to=\"/" --include="*.vue" src/
grep -rn "push.*/" --include="*.vue" src/

# 或使用 VS Code 全局搜索
# 搜索正则: (to="|push\([\`'"])\/(?!admin|login|register)
```

---

## 十三、启动脚本配置

### 13.1 多服务启动 (run.bat)

项目包含两个后端服务，需要同时启动：

| 服务 | 端口 | 说明 |
|------|------|------|
| Spring Boot | 8080 | 主后端 API |
| DailyHotApi | 6688 | 热搜数据 API |

**run.bat 脚本**：

```batch
@echo off
cd /d "%~dp0"

REM Start DailyHotApi in a new window
start "DailyHotApi" cmd /c "cd /d D:\Learn\WEBdesign\DailyHotApi && npm start"

REM Wait a moment for DailyHotApi to start
timeout /t 3 /nobreak >nul

REM Start Spring Boot backend
set JAVA_HOME=D:\jdk17
set PATH=%JAVA_HOME%\bin;%PATH%
call D:\maven\apache-maven-3.9.11\bin\mvn spring-boot:run %*
```

**关键点**：
- `start "title" cmd /c "..."` - 在新窗口中启动命令
- `timeout /t 3 /nobreak >nul` - 等待 3 秒，不显示输出

### 13.2 多服务停止 (stop.bat)

```batch
@echo off
cd /d "%~dp0"

REM Stop Spring Boot (port 8080)
for /f "tokens=5" %%a in ('netstat -ano ^| findstr :8080 ^| findstr LISTENING') do (
    taskkill /PID %%a /F
)

REM Stop DailyHotApi (port 6688)
for /f "tokens=5" %%a in ('netstat -ano ^| findstr :6688 ^| findstr LISTENING') do (
    taskkill /PID %%a /F
)
pause
```

**关键点**：
- `netstat -ano | findstr :PORT | findstr LISTENING` - 查找监听端口的进程
- `for /f "tokens=5"` - 提取第 5 列（PID）
- `taskkill /PID xxx /F` - 强制终止进程

### 13.3 Windows 批处理编码

**问题**：Windows CMD 默认 GBK 编码，UTF-8 中文会乱码。

**解决方案**：使用纯英文避免编码问题，或在脚本开头添加 `chcp 65001 >nul`。

**注意**：UTF-8 BOM（﻿）会导致 `@echo off` 失效。

---

## 十四、表单验证最佳实践

### 14.1 密码确认验证问题

**问题**：两次密码一致却报错"两次密码不一致"。

**原因**：空值也触发比较，`'' !== '123456'` 为 true。

**错误写法**：

```javascript
const validateConfirmPassword = (rule, value, callback) => {
  if (value !== form.password) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}
```

**正确写法**：

```javascript
const validateConfirmPassword = (rule, value, callback) => {
  if (!value) {
    callback() // 空值由 required 规则处理
  } else if (value !== form.password) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}
```

### 14.2 联动验证

密码改变时重新验证确认密码：

```javascript
import { watch } from 'vue'

watch(() => form.password, () => {
  if (form.confirmPassword) {
    formRef.value?.validateField('confirmPassword')
  }
})
```

---

## 十五、测试数据管理

### 15.1 数据库分类结构

| ID | 名称 | 父分类 |
|----|------|--------|
| 1-5 | 一级分类（时政/体育/科技/娱乐/财经） | NULL |
| 6-11 | 二级分类（国内时政/国际时政/足球/篮球/人工智能/互联网） | 1-3 |

### 15.2 测试数据导入

```bash
mysql -u root -p123456 --default-character-set=utf8mb4 news_system -e "source D:/Learn/WEBdesign/database/test_data.sql"
```

**注意**：使用 `--default-character-set=utf8mb4` 确保中文正确导入。

---

## 十六、前后端数据交互问题

### 16.1 注册接口字段缺失

**问题现象**：前端验证通过，但后端返回"两次密码输入不一致"。

**排查过程**：

1. 前端 console.log 显示验证通过：
```
验证确认密码: {value: '123456', password: '123456', match: true}
```

2. 但报错来自 `request.js:56`（响应拦截器），说明是后端返回的错误。

**根本原因**：

后端 `RegisterDTO` 需要 `confirmPassword` 字段：

```java
// RegisterDTO.java
public class RegisterDTO {
    private String username;
    private String password;
    private String confirmPassword;  // 后端需要这个字段
}

// UserServiceImpl.java 第 41 行
if (!dto.getPassword().equals(dto.getConfirmPassword())) {
    throw new RuntimeException("两次密码输入不一致");
}
```

但前端只发送了两个字段：

```javascript
// 错误 - 缺少 confirmPassword
await register({
  username: form.username,
  password: form.password
})
```

后端收到 `confirmPassword = null`，导致 `"123456".equals(null)` 返回 `false`。

**修复方案**：

```javascript
// 正确 - 包含 confirmPassword
await register({
  username: form.username,
  password: form.password,
  confirmPassword: form.confirmPassword
})
```

### 16.2 调试技巧

**前端调试**：
```javascript
// 在验证函数中添加 console.log
const validateConfirmPassword = (rule, value, callback) => {
  console.log('验证:', { value, password: form.password, match: value === form.password })
  // ...
}
```

**判断错误来源**：
- 看报错堆栈中的文件位置
- `Register.vue` 行号 → 前端验证问题
- `request.js` 行号 → 后端返回的错误

**检查后端 DTO**：
确保前端发送的字段与后端 DTO 定义一致：

| 后端 DTO 字段 | 前端是否发送 |
|---------------|--------------|
| username | ✓ |
| password | ✓ |
| confirmPassword | ✗ (漏了) |

### 16.3 预防措施

1. **前后端字段对齐**：开发前先对齐 API 文档
2. **使用 TypeScript**：定义接口类型，编译时检查
3. **网络面板检查**：F12 → Network → 查看实际发送的请求体

---

## 十七、登录跳转优化

### 17.1 问题描述

用户登录成功后默认跳转到落地页 `/`，而非新闻首页 `/home`，用户体验不佳。

### 17.2 修复文件清单

| 文件 | 修改内容 |
|------|----------|
| `Login.vue` | 默认跳转从 `/` 改为 `/home` |
| `FrontLayout.vue` | 退出登录后跳转到 `/home` |
| `NotFound.vue` | 返回首页按钮跳转到 `/home` |
| `AdminLayout.vue` | 面包屑首页链接改为 `/home`，返回首页改为 `/home` |

### 17.3 代码修改

**Login.vue**：
```javascript
// 初版修改
const redirect = route.query.redirect || '/home'

// 最终版本（见第21章）- 根据角色自动跳转
if (route.query.redirect) {
  router.push(route.query.redirect)
} else if (res.data.role >= 2) {
  router.push('/admin/dashboard')  // 管理人员→后台
} else {
  router.push('/home')              // 普通用户→首页
}
```

**FrontLayout.vue**：
```javascript
// 修改前
router.push('/')

// 修改后
router.push('/home')
```

---

## 十八、后台管理页面 UI 统一

### 18.1 概述

将后台管理页面 (`AdminLayout.vue`) 的 UI 风格与前台页面统一，实现一致的视觉体验。

### 18.2 修改前后对比

| 元素 | 修改前 | 修改后 |
|------|--------|--------|
| 侧边栏背景 | `#304156` (蓝灰色) | `rgba(0, 0, 0, 0.95)` (深黑色) |
| 侧边栏宽度 | 200px | 220px |
| Logo 样式 | 图标 + 中文标题 | `NEWS + 管理后台` 统一风格 |
| 菜单激活色 | `#409eff` (蓝色) | 紫色渐变 `#A795BF → #6C5DAB` |
| 菜单悬停色 | 默认 | `#FEE9A1` (金黄色) |
| 主体背景 | `#f5f5f5` (灰色) | 动态渐变背景动画 |
| 顶部栏 | 白色 + 简单阴影 | 玻璃拟态 + 模糊效果 |
| 角色标签 | 灰色背景 | 紫色渐变背景 |
| 头像 | 默认 | 紫色渐变背景 |

### 18.3 新增样式特性

#### 18.3.1 深色侧边栏

```css
.aside {
  background: rgba(0, 0, 0, 0.95);
  border-right: 1px solid rgba(255, 255, 255, 0.1);
}

.logo {
  height: 70px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.logo-text {
  font-size: 20px;
  font-weight: 900;
  color: #fff;
  letter-spacing: 0.15rem;
}

.logo-divider {
  color: #FEE9A1;  /* 金色分隔符 */
}
```

#### 18.3.2 菜单紫色主题

```css
.admin-menu :deep(.el-menu-item.is-active) {
  background: linear-gradient(135deg, #A795BF, #6C5DAB);
  color: #fff;
}

.admin-menu :deep(.el-menu-item:hover) {
  background: rgba(255, 255, 255, 0.1);
  color: #FEE9A1;  /* 金色悬停 */
}

.admin-menu :deep(.el-sub-menu .el-menu) {
  background: rgba(0, 0, 0, 0.3);  /* 子菜单更深 */
}
```

#### 18.3.3 主体区域动态渐变

```css
.main-container {
  background: linear-gradient(135deg, #CBD4E5 0%, #A795BF 25%, #FEE9A1 50%, #6C5DAB 75%, #CCA2A7 100%);
  background-size: 400% 400%;
  animation: gradientShift 15s ease infinite;
}
```

#### 18.3.4 玻璃拟态顶部栏

```css
.header {
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(10px);
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
}
```

#### 18.3.5 全局卡片样式覆盖

通过 `:deep()` 选择器覆盖 Element Plus 卡片默认样式：

```css
.main :deep(.el-card) {
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(10px);
  border-radius: 12px;
  border: none;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
}

.main :deep(.el-card__header) {
  background: rgba(0, 0, 0, 0.95);
  color: #fff;
  border-radius: 12px 12px 0 0;
}

.main :deep(.el-button--primary) {
  background: linear-gradient(135deg, #A795BF, #6C5DAB);
  border: none;
}
```

### 18.4 Vue :deep() 选择器

在 Vue 3 的 `<style scoped>` 中，使用 `:deep()` 穿透子组件样式：

```css
/* Vue 3 语法 */
.parent :deep(.child-class) {
  color: red;
}

/* 等价于 Vue 2 的 ::v-deep 或 /deep/ */
.parent ::v-deep .child-class {
  color: red;
}
```

**应用场景**：
- 覆盖 Element Plus 等 UI 库的默认样式
- 在父组件中修改子组件的样式
- 全局样式穿透

### 18.5 统一后的页面风格

| 页面区域 | 风格特点 |
|----------|----------|
| 前台头部/底部 | 深黑色 `rgba(0,0,0,0.95)` |
| 前台内容区 | 玻璃态卡片 + 渐变背景 |
| 后台侧边栏 | 深黑色，与前台头部一致 |
| 后台内容区 | 玻璃态卡片 + 渐变背景 |
| 登录/注册页 | 深色卡片 + 渐变背景动画 |
| 主题色 | 紫色渐变 `#A795BF → #6C5DAB` |
| 强调色 | 金黄色 `#FEE9A1` |

---

## 十九、文件写入问题排查

### 19.1 问题现象

在 Windows 环境下使用 Claude Code 时，多种文件写入方式失败：
- Bash heredoc 语法错误
- PowerShell 字符编码错误
- Edit 工具报 "文件已被修改"

### 19.2 原因分析

| 方法 | 失败原因 |
|------|----------|
| Bash heredoc | Windows Git Bash 对带单引号的大型多行字符串处理异常 |
| PowerShell | 中文 Windows 系统字符编码问题 (GBK vs UTF-8) |
| Edit 工具 | VS Code 等编辑器实时监视文件，导致文件修改冲突 |

### 19.3 解决方案

**方法一：临时 JS 脚本**（推荐）

```javascript
// 1. 创建临时脚本文件 temp_update.js
const fs = require('fs');
const content = \`... 文件内容 ...\`;
fs.writeFileSync('./path/to/file.vue', content, 'utf8');

// 2. 执行脚本
// node temp_update.js

// 3. 删除临时脚本
// rm temp_update.js
```

**方法二：关闭文件监视**

在修改文件前，关闭 VS Code 或其他编辑器对该文件的监视。

**方法三：小块编辑**

将大型修改拆分为多个小的 Edit 操作，减少冲突概率。

### 19.4 最佳实践

1. 优先使用 Edit 工具进行小范围修改
2. 大文件重写时使用临时 JS 脚本方式
3. 避免在 Windows 上使用复杂的 bash heredoc
4. 确保文件没有被其他进程锁定

---

## 二十、用户权限与个人中心系统

### 20.1 用户角色体系

系统采用数字角色等级，数值越大权限越高：

| 角色值 | 角色名称 | 权限范围 |
|--------|----------|----------|
| 1 | 普通用户 | 浏览新闻、评论 |
| 2 | 编辑 | 发布/编辑新闻 |
| 3 | 总编 | 审核新闻 |
| 4 | 管理员 | 所有后台功能 |

### 20.2 路由权限控制

#### 20.2.1 路由 Meta 配置

```javascript
{
  path: '/admin',
  meta: {
    requiresAuth: true,      // 需要登录
    requiresAdmin: true      // 需要管理权限 (role >= 2)
  },
  children: [
    {
      path: 'news',
      meta: { roles: [2, 3, 4] }  // 特定角色限制
    }
  ]
}
```

#### 20.2.2 路由守卫逻辑

```javascript
router.beforeEach((to, from, next) => {
  const userStore = useUserStore()

  // 需要登录但未登录
  if (to.meta.requiresAuth && !userStore.isLoggedIn) {
    next({ name: 'Login', query: { redirect: to.fullPath } })
    return
  }

  // 需要管理员权限但没有
  if (to.meta.requiresAdmin && !userStore.hasAdminAccess) {
    next({ name: 'Home' })
    return
  }

  // 特定角色限制
  if (to.meta.roles && !to.meta.roles.includes(userStore.role)) {
    next({ name: 'Dashboard' })
    return
  }

  next()
})
```

### 20.3 用户下拉菜单扩展

**前台头像下拉菜单结构**：

```html
<el-dropdown-menu>
  <!-- 所有用户可见 -->
  <el-dropdown-item @click="router.push('/home/profile')">
    <el-icon><User /></el-icon> 个人中心
  </el-dropdown-item>

  <!-- 仅管理人员可见 (role >= 2) -->
  <el-dropdown-item v-if="userStore.hasAdminAccess" @click="router.push('/admin')">
    <el-icon><Setting /></el-icon> 管理后台
  </el-dropdown-item>

  <!-- 所有用户可见 -->
  <el-dropdown-item divided @click="handleLogout">
    <el-icon><SwitchButton /></el-icon> 退出登录
  </el-dropdown-item>
</el-dropdown-menu>
```

**角色标签显示**：
```javascript
const roleTagType = computed(() => {
  const typeMap = {
    1: 'info',    // 普通用户 - 灰色
    2: 'warning', // 编辑 - 橙色
    3: 'success', // 总编 - 绿色
    4: 'danger'   // 管理员 - 红色
  }
  return typeMap[userStore.role] || 'info'
})
```

### 20.4 个人中心页面 (Profile.vue)

**功能模块**：

1. **账户信息展示**
   - 用户ID、用户名、角色
   - 权限标签显示

2. **密码修改**
   - 表单验证（当前密码、新密码、确认密码）
   - 修改成功后自动退出登录

3. **快捷操作**
   - 管理员可快速进入后台
   - 退出登录确认

**页面样式**：
```css
.profile-header {
  background: linear-gradient(135deg, rgba(167, 149, 191, 0.3), rgba(108, 93, 171, 0.3));
  border-radius: 16px;
  padding: 40px;
}

.user-avatar {
  background: linear-gradient(135deg, #A795BF, #6C5DAB);
  font-size: 36px;
}
```

### 20.5 后端密码修改接口

**接口定义**：
```
PUT /api/user/password
Authorization: Bearer {token}
Content-Type: application/json

{
  "oldPassword": "当前密码",
  "newPassword": "新密码"
}
```

**后端实现 (UserController.java)**：
```java
@PutMapping("/password")
public Result<Void> changePassword(HttpServletRequest request,
                                    @RequestBody Map<String, String> params) {
    Long userId = (Long) request.getAttribute("userId");
    String oldPassword = params.get("oldPassword");
    String newPassword = params.get("newPassword");

    // 验证旧密码
    if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
        return Result.error("当前密码不正确");
    }

    // 更新密码
    user.setPassword(passwordEncoder.encode(newPassword));
    userService.updateById(user);

    return Result.success("密码修改成功", null);
}
```

### 20.6 文件修改清单

| 文件 | 修改内容 |
|------|----------|
| `router/index.js` | 添加 `requiresAdmin` 检查，新增 Profile 路由 |
| `FrontLayout.vue` | 扩展下拉菜单，添加个人中心入口和角色标签 |
| `Profile.vue` | 新建个人中心页面 |
| `api/auth.js` | 添加 `changePassword` API |
| `UserController.java` | 添加修改密码接口 |

### 20.7 权限判断最佳实践

**前端**：
```javascript
// 使用 computed 缓存权限判断
const hasAdminAccess = computed(() => role.value >= 2)

// 在模板中使用
v-if="userStore.hasAdminAccess"
```

**后端**：
```java
// 从请求属性获取用户信息 (JWT 拦截器注入)
Long userId = (Long) request.getAttribute("userId");
Integer role = (Integer) request.getAttribute("role");

// 角色常量定义
public static final int ROLE_USER = 1;
public static final int ROLE_EDITOR = 2;
public static final int ROLE_CHIEF = 3;
public static final int ROLE_ADMIN = 4;
```

---

## 二十一、登录跳转与角色分配策略

### 21.1 设计方案

采用**统一登录入口 + 角色自动识别**方案：

```
登录页面 (/login)
      ↓
  输入账号密码
      ↓
  系统验证 + 获取角色
      ↓
  ┌─────────────────────────────────┐
  │  role >= 2  →  /admin/dashboard │  管理人员进入后台
  │  role = 1   →  /home            │  普通用户进入首页
  └─────────────────────────────────┘
```

### 21.2 登录跳转逻辑

**Login.vue 核心代码**：

```javascript
const handleLogin = async () => {
  // ... 表单验证 ...

  const res = await login(form)
  userStore.login(res.data)
  ElMessage.success('登录成功')

  // 根据角色自动跳转
  if (route.query.redirect) {
    // 有指定跳转地址（如从需要登录的页面跳转来）
    router.push(route.query.redirect)
  } else if (res.data.role >= 2) {
    // 管理人员（编辑及以上）跳转后台
    router.push('/admin/dashboard')
  } else {
    // 普通用户跳转首页
    router.push('/home')
  }
}
```

### 21.3 注册策略

**原则**：公开注册只能创建普通用户，管理权限由管理员分配。

```java
// UserServiceImpl.java - register 方法
user.setRole(User.ROLE_USER);  // 默认为普通用户 (role=1)
```

### 21.4 角色分配流程

| 角色 | 值 | 获取方式 |
|------|-----|----------|
| 普通用户 | 1 | 自行注册 |
| 编辑 | 2 | 管理员在后台"用户管理"中分配 |
| 总编 | 3 | 管理员在后台"用户管理"中分配 |
| 管理员 | 4 | 超级管理员分配 / 数据库直接设置 |

### 21.5 完整用户流程

**普通用户**：
```
注册 → 登录 → 首页 → 浏览新闻/评论 → 个人中心
```

**管理人员**：
```
(管理员分配角色) → 登录 → 后台工作台 → 管理新闻/用户等
                              ↓
                     可通过头像菜单切换到前台
```

### 21.6 跳转优先级

1. **redirect 参数** - 最高优先级，用于登录后返回原页面
2. **角色判断** - 无 redirect 时，根据角色跳转
3. **默认首页** - 兜底跳转

**应用场景**：
```javascript
// 场景1：未登录访问后台
访问 /admin/news → 重定向到 /login?redirect=/admin/news → 登录后返回 /admin/news

// 场景2：普通用户直接登录
访问 /login → 登录 → 跳转 /home

// 场景3：管理员直接登录
访问 /login → 登录 → 跳转 /admin/dashboard
```

### 21.7 安全考虑

1. **注册限制**：公开注册只能是普通用户
2. **后台保护**：路由守卫检查 `requiresAdmin`
3. **角色提升**：只有管理员可以修改用户角色
4. **前端隐藏**：普通用户看不到"管理后台"入口

---

## 二十二、密码安全与 BCrypt 哈希

### 22.1 哈希 vs 加密

| 特性 | 哈希 (Hash) | 加密 (Encryption) |
|------|-------------|-------------------|
| **方向** | 单向，不可逆 | 双向，可解密 |
| **目的** | 验证数据完整性/密码 | 保护数据机密性 |
| **密钥** | 无密钥 | 需要密钥 |
| **还原** | 无法还原原文 | 可用密钥解密 |

### 22.2 为什么用哈希存储密码？

| 加密存储 | 哈希存储 |
|----------|----------|
| 数据库泄露 + 密钥泄露 = 所有密码暴露 | 数据库泄露也无法还原密码 |
| 管理员能看到用户密码 | 管理员也不知道用户密码 |
| 不安全 | 安全 |

### 22.3 BCrypt 算法特点

1. **加盐 (Salt)** - 相同密码每次生成不同哈希
2. **慢速设计** - 防止暴力破解
3. **成本因子** - 可调节计算复杂度

### 22.4 BCrypt 哈希格式

```
$2b$10$kMwh6fMQhIxycUROwoQVXu9V9KPMrDolfyZyaIvFi10OTlJqvBjBW
│  │  │                      │
│  │  │                      └── 哈希值
│  │  └── 盐值 (22字符)
│  └── 成本因子 (10 = 2^10 次迭代)
└── 算法版本 (2a/2b)
```

### 22.5 密码验证流程

```
用户登录输入 "123456"
        ↓
   BCrypt 验证
        ↓
与数据库哈希值比对 → 匹配则登录成功
```

**后端代码**：
```java
// Spring Security BCryptPasswordEncoder
passwordEncoder.matches(inputPassword, storedHash)  // 返回 true/false
```

### 22.6 生成 BCrypt 哈希

**Node.js (bcryptjs)**：
```javascript
const bcrypt = require('bcryptjs');
const hash = bcrypt.hashSync('123456', 10);  // 10 = cost factor
```

**Java (Spring)**：
```java
BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
String hash = encoder.encode("123456");
```

---

## 二十三、登录状态持久化

### 23.1 存储机制

系统使用 `localStorage` 保存登录状态：

```javascript
// stores/user.js
const token = ref(localStorage.getItem('token') || '')
const userInfo = ref(JSON.parse(localStorage.getItem('userInfo') || 'null'))

function login(data) {
  localStorage.setItem('token', data.token)
  localStorage.setItem('userInfo', JSON.stringify({
    id: data.userId,
    username: data.username,
    role: data.role
  }))
}

function logout() {
  localStorage.removeItem('token')
  localStorage.removeItem('userInfo')
}
```

### 23.2 Web 存储方式对比

| 存储方式 | 生命周期 | 容量 | 场景 |
|----------|----------|------|------|
| **localStorage** | 永久（手动清除） | 5-10MB | 登录状态、用户偏好 |
| **sessionStorage** | 关闭标签页清除 | 5-10MB | 临时表单数据 |
| **Cookie** | 可设置过期时间 | 4KB | 服务端需要的数据 |

### 23.3 为什么选择 localStorage？

1. **用户体验** - 关闭浏览器后不需要重新登录
2. **容量足够** - 存储 token 和基本用户信息绑绑有余
3. **简单易用** - 纯前端操作，无需服务端配合

### 23.4 清除登录状态的方法

**方法一：退出登录按钮**
```javascript
userStore.logout()  // 调用 logout 函数清除
```

**方法二：浏览器控制台**
```javascript
localStorage.clear()
location.reload()
```

**方法三：开发者工具**
```
F12 → Application → Local Storage → 选择域名 → 右键清除
```

### 23.5 安全考虑

| 风险 | 防护措施 |
|------|----------|
| XSS 攻击读取 token | 设置 HttpOnly Cookie（更安全方案） |
| Token 泄露 | 设置合理过期时间 |
| 跨站请求 | 验证 Token + CSRF 防护 |

---

## 二十四、测试账号管理

### 24.1 系统预置账号

| 用户名 | 密码 | 角色 | 权限 |
|--------|------|------|------|
| admin | 123456 | 管理员 (4) | 所有后台功能 |
| chief | 123456 | 总编 (3) | 审核新闻 |
| editor | 123456 | 编辑 (2) | 发布/编辑新闻 |
| user1 | 123456 | 普通用户 (1) | 浏览/评论 |

### 24.2 重置密码 SQL

```sql
-- 生成 BCrypt 哈希后更新
UPDATE user SET password='$2b$10$...' WHERE username='admin';

-- 或者使用应用注册新用户，再修改角色
UPDATE user SET role=4 WHERE username='newadmin';
```

### 24.3 用户表结构

```sql
CREATE TABLE user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,      -- BCrypt 哈希
    email VARCHAR(100),
    avatar VARCHAR(255),
    role TINYINT NOT NULL DEFAULT 1,     -- 1=用户 2=编辑 3=总编 4=管理员
    status TINYINT NOT NULL DEFAULT 1,   -- 0=禁用 1=正常
    create_time DATETIME,
    update_time DATETIME
);
```

### 24.4 角色权限对照

```
role=1 (普通用户)
  └── 浏览新闻、发表评论、个人中心

role=2 (编辑)
  └── 以上 + 发布新闻、编辑新闻

role=3 (总编)
  └── 以上 + 审核新闻

role=4 (管理员)
  └── 以上 + 用户管理、分类管理、评论管理
```


---

## 二十五、AI 新闻助手

### 25.1 功能概述

集成 AI 助手功能，帮助用户搜索、总结、解读新闻内容。

| 特性 | 说明 |
|------|------|
| **API 格式** | OpenAI 兼容格式 (支持 Kimi/DeepSeek/OpenAI) |
| **交互方式** | 鼠标靠近窗口右边缘触发侧边栏 |
| **权限控制** | 登录用户可用；未登录显示登录提示 |
| **UI 风格** | 与整体设计一致 (深色头部、玻璃拟态) |

### 25.2 后端实现

**新增文件**:

| 文件 | 说明 |
|------|------|
| `dto/AiChatDTO.java` | 聊天请求 DTO |
| `vo/AiChatVO.java` | 聊天响应 VO |
| `service/AiService.java` | AI 服务接口 |
| `service/impl/AiServiceImpl.java` | AI 服务实现 |
| `controller/AiController.java` | AI 控制器 (需认证) |

**配置文件** (`application.yml`):

```yaml
# AI 配置 - OpenAI 兼容格式
ai:
  api-key: sk-xxx                              # API Key
  base-url: https://api.moonshot.cn/v1         # API 地址
  model: moonshot-v1-8k                        # 模型名称
```

**支持的模型提供商**:

| 提供商 | API Base URL | 模型示例 |
|--------|-------------|----------|
| Kimi | https://api.moonshot.cn/v1 | moonshot-v1-8k |
| DeepSeek | https://api.deepseek.com/v1 | deepseek-chat |
| OpenAI | https://api.openai.com/v1 | gpt-3.5-turbo |

**API 接口**:

```
POST /api/ai/chat        # 普通对话 (需认证)
POST /api/ai/summarize   # 总结新闻 (需认证)
```

### 25.3 前端实现

**新增文件**:

| 文件 | 说明 |
|------|------|
| `api/ai.js` | AI API 封装 |
| `components/AiSidebar.vue` | AI 侧边栏组件 |

**修改文件**:

| 文件 | 修改内容 |
|------|----------|
| `App.vue` | 集成 AiSidebar 组件 (全局可用) |

### 25.4 边缘触发交互

```javascript
// 右侧触发区域 - 20px 宽度
<div class="ai-trigger-zone" @mouseenter="showSidebar"></div>

// 悬浮提示标签 - 固定在屏幕右侧中间
<div class="ai-trigger-tab" v-show="!isOpen" @mouseenter="showSidebar">
  <el-icon><ChatDotRound /></el-icon>
  <span>AI</span>
</div>
```

**交互逻辑**:
- 鼠标靠近右边缘或悬停标签 → 侧边栏滑出
- 鼠标离开侧边栏 → 延迟 500ms 后收起
- 鼠标重新进入侧边栏 → 取消关闭定时器
- 点击关闭按钮 → 立即收起

### 25.5 登录状态检查

```vue
<template>
  <!-- 未登录提示 -->
  <div v-if="!userStore.isLoggedIn" class="login-prompt">
    <el-icon><Lock /></el-icon>
    <p>登录后使用</p>
    <el-button @click="goLogin">去登录</el-button>
  </div>

  <!-- 已登录：聊天界面 -->
  <template v-else>
    <!-- 消息列表 + 输入框 -->
  </template>
</template>
```

### 25.6 样式设计

**触发标签** (与深色头部风格一致):
```css
.ai-trigger-tab {
  background: rgba(0, 0, 0, 0.9);
  color: #fff;
  border-radius: 8px 0 0 8px;
}

.ai-trigger-tab:hover .tab-text {
  color: #FEE9A1;  /* 金黄色强调 */
}
```

**侧边栏** (玻璃拟态):
```css
.ai-sidebar {
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(10px);
  box-shadow: -4px 0 20px rgba(0, 0, 0, 0.08);
}

.sidebar-header {
  background: rgba(0, 0, 0, 0.9);
  color: #fff;
  font-weight: 700;
  letter-spacing: 0.1rem;
  text-transform: uppercase;
}
```

**消息气泡**:
```css
/* 用户消息 - 深色 */
.message.user .message-bubble {
  background: rgba(0, 0, 0, 0.9);
  color: #fff;
}

/* AI 消息 - 浅灰 */
.message.ai .message-bubble {
  background: rgba(0, 0, 0, 0.05);
  color: rgba(0, 0, 0, 0.85);
}
```

### 25.7 RestTemplate 调用 API

```java
// 构建请求头
HttpHeaders headers = new HttpHeaders();
headers.setContentType(MediaType.APPLICATION_JSON);
headers.setBearerAuth(apiKey);  // Bearer Token 认证

// 构建请求体 (OpenAI 格式)
Map<String, Object> requestBody = new HashMap<>();
requestBody.put("model", model);
requestBody.put("temperature", 0.7);
requestBody.put("messages", List.of(
    Map.of("role", "system", "content", "你是新闻助手..."),
    Map.of("role", "user", "content", userMessage)
));

// 发送请求
HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
ResponseEntity<String> response = restTemplate.exchange(
    baseUrl + "/chat/completions",
    HttpMethod.POST,
    entity,
    String.class
);
```

### 25.8 获取 API Key

| 提供商 | 平台地址 |
|--------|----------|
| Kimi (月之暗面) | https://platform.moonshot.cn/ |
| DeepSeek | https://platform.deepseek.com/ |
| OpenAI | https://platform.openai.com/ |

### 25.9 侧边栏交互优化

#### 25.9.1 问题描述

原始实现中，鼠标离开侧边栏时设置 300ms 延迟关闭，但鼠标重新进入时未取消定时器，导致：
- 鼠标在侧边栏边缘移动时反复触发开关
- 用户体验不佳，侧边栏"闪烁"

#### 25.9.2 解决方案

**核心改动**：

```javascript
// 离开定时器
let leaveTimer = null

// 取消离开定时器
const cancelLeaveTimer = () => {
  if (leaveTimer) {
    clearTimeout(leaveTimer)
    leaveTimer = null
  }
}

// 显示侧边栏
const showSidebar = () => {
  cancelLeaveTimer()  // 显示时取消任何待执行的关闭定时器
  isOpen.value = true
}

// 鼠标离开处理（延迟关闭）
const handleMouseLeave = () => {
  cancelLeaveTimer()  // 先清除之前的定时器
  leaveTimer = setTimeout(() => {
    hideSidebar()
  }, 500)  // 增加延迟到 500ms
}
```

**模板绑定**：

```html
<div class="ai-sidebar"
     @mouseenter="cancelLeaveTimer"
     @mouseleave="handleMouseLeave">
```

#### 25.9.3 优化要点

| 改动 | 说明 |
|------|------|
| 添加 `cancelLeaveTimer()` | 统一管理定时器清除逻辑 |
| `showSidebar()` 调用 `cancelLeaveTimer()` | 打开时确保无待执行的关闭 |
| 侧边栏添加 `@mouseenter` | 鼠标进入时取消关闭定时器 |
| 延迟从 300ms 增加到 500ms | 给用户更多移动缓冲时间 |

#### 25.9.4 交互流程图

```
鼠标移入触发区/标签
       ↓
  showSidebar()
       ↓
  取消定时器 + 打开侧边栏
       ↓
  ┌─────────────────────────────────┐
  │  鼠标在侧边栏内                  │
  │       ↓                         │
  │  cancelLeaveTimer() [mouseenter]│
  │       ↓                         │
  │  保持打开                        │
  └─────────────────────────────────┘
       ↓
  鼠标离开侧边栏
       ↓
  handleMouseLeave()
       ↓
  设置 500ms 定时器
       ↓
  ┌─────────────────┬─────────────────┐
  │ 500ms 内返回     │ 500ms 后未返回   │
  │       ↓         │       ↓         │
  │ cancelLeaveTimer│ hideSidebar()   │
  │       ↓         │       ↓         │
  │ 保持打开         │ 侧边栏关闭       │
  └─────────────────┴─────────────────┘
```


---

## 二十六、局域网访问配置

### 26.1 概述

配置项目支持局域网内其他设备访问，方便移动端测试和多设备调试。

### 26.2 配置修改

#### 26.2.1 前端 Vite 配置

**文件**: `news-frontend/vite.config.js`

```javascript
server: {
  port: 5173,
  host: '0.0.0.0',  // 允许局域网访问
  proxy: {
    '/api': {
      target: 'http://localhost:8080',
      changeOrigin: true
    }
  }
}
```

**关键配置**:
- `host: '0.0.0.0'` - 绑定所有网络接口，允许外部访问

#### 26.2.2 后端 Spring Boot 配置

**文件**: `news-backend/src/main/resources/application.yml`

```yaml
server:
  port: 8080
  address: 0.0.0.0  # 允许局域网访问
```

**关键配置**:
- `address: 0.0.0.0` - 绑定所有网络接口

#### 26.2.3 DailyHotApi 配置

**文件**: `DailyHotApi/src/index.ts`

```typescript
const apiServer = serve({
  fetch: app.fetch,
  port,
  hostname: '0.0.0.0',  // 允许局域网访问
});
```

**关键配置**:
- `hostname: '0.0.0.0'` - @hono/node-server 的主机名配置

### 26.3 修改文件清单

| 文件路径 | 修改内容 |
|----------|----------|
| `news-frontend/vite.config.js` | 添加 `host: '0.0.0.0'` |
| `news-backend/src/main/resources/application.yml` | 添加 `address: 0.0.0.0` |
| `DailyHotApi/src/index.ts` | 添加 `hostname: '0.0.0.0'` |

### 26.4 访问地址

| 服务 | 本机访问 | 局域网访问 |
|------|----------|------------|
| 前端 | http://localhost:5173 | http://{IP}:5173 |
| 后端 API | http://localhost:8080 | http://{IP}:8080 |
| DailyHotApi | http://localhost:6688 | http://{IP}:6688 |

**获取本机 IP**:
```bash
# Windows
ipconfig

# Linux/Mac
ifconfig
# 或
ip addr
```

### 26.5 防火墙配置

如果局域网设备无法访问，需要检查 Windows 防火墙设置：

1. **控制面板** → **Windows Defender 防火墙** → **高级设置**
2. **入站规则** → **新建规则**
3. 选择 **端口** → **TCP** → 输入 `5173, 8080, 6688`
4. 选择 **允许连接** → 完成

或使用命令行：
```powershell
# 以管理员身份运行
netsh advfirewall firewall add rule name="News System" dir=in action=allow protocol=tcp localport=5173,8080,6688
```

### 26.6 验证测试

```bash
# 测试各服务是否监听 0.0.0.0
netstat -an | findstr "5173 8080 6688"

# 期望输出（LISTENING 在 0.0.0.0 表示成功）:
# TCP    0.0.0.0:5173    0.0.0.0:0    LISTENING
# TCP    0.0.0.0:6688    0.0.0.0:0    LISTENING
# TCP    0.0.0.0:8080    0.0.0.0:0    LISTENING
```

### 26.7 注意事项

1. **安全性**: 局域网访问配置仅适用于开发环境，生产环境应配置正确的域名和 HTTPS
2. **IP 变化**: 如果使用 DHCP，本机 IP 可能会变化，需要重新获取
3. **代理问题**: 前端的 API 代理仍指向 localhost:8080，在局域网设备上需要确保后端也可访问
4. **移动端调试**: 手机等移动设备需要连接同一 WiFi 网络

