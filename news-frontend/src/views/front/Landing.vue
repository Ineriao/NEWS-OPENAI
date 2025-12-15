<template>
  <div class="landing-page">
    <!-- 波浪背景 -->
    <div class="waves-container" ref="wavesContainer">
      <svg class="waves-svg" ref="wavesSvg"></svg>
      <div class="cursor-dot" :style="cursorStyle"></div>
    </div>

    <!-- 主题切换按钮 -->
    <button class="theme-toggle" @click="themeStore.toggleTheme">
      <component :is="themeStore.isDark ? Sunny : Moon" class="theme-icon" />
    </button>

    <!-- 中央标题 -->
    <div class="hero-title">
      <h1>新闻发布系统</h1>
      <p>News Publishing System</p>
    </div>

    <!-- 底部导航栏 -->
    <nav class="bottom-nav">
      <div class="nav-container">
        <router-link to="/home" class="nav-item">首页</router-link>
        <span class="nav-divider">+</span>
        <router-link to="/login" class="nav-item">登录</router-link>
        <span class="nav-divider">+</span>
        <router-link to="/register" class="nav-item">注册</router-link>
        <span class="nav-divider">+</span>
        <router-link to="/admin" class="nav-item">后台管理</router-link>
      </div>
    </nav>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, computed, watch } from 'vue'
import { useThemeStore } from '@/stores/theme'
import { Sunny, Moon } from '@element-plus/icons-vue'

const themeStore = useThemeStore()

const wavesContainer = ref(null)
const wavesSvg = ref(null)

// 鼠标状态
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

// 光标样式
const cursorStyle = computed(() => ({
  transform: `translate(${mouse.value.sx}px, ${mouse.value.sy}px)`
}))

// 波浪线数据
let lines = []
let paths = []
let bounding = null
let animationId = null

// Perlin 噪声实现
const noise = (() => {
  const grad3 = [
    [1,1,0],[-1,1,0],[1,-1,0],[-1,-1,0],
    [1,0,1],[-1,0,1],[1,0,-1],[-1,0,-1],
    [0,1,1],[0,-1,1],[0,1,-1],[0,-1,-1]
  ]

  const p = [151,160,137,91,90,15,131,13,201,95,96,53,194,233,7,225,140,36,103,30,69,142,
    8,99,37,240,21,10,23,190,6,148,247,120,234,75,0,26,197,62,94,252,219,203,117,
    35,11,32,57,177,33,88,237,149,56,87,174,20,125,136,171,168,68,175,74,165,71,
    134,139,48,27,166,77,146,158,231,83,111,229,122,60,211,133,230,220,105,92,41,
    55,46,245,40,244,102,143,54,65,25,63,161,1,216,80,73,209,76,132,187,208,89,
    18,169,200,196,135,130,116,188,159,86,164,100,109,198,173,186,3,64,52,217,226,
    250,124,123,5,202,38,147,118,126,255,82,85,212,207,206,59,227,47,16,58,17,182,
    189,28,42,223,183,170,213,119,248,152,2,44,154,163,70,221,153,101,155,167,43,
    172,9,129,22,39,253,19,98,108,110,79,113,224,232,178,185,112,104,218,246,97,
    228,251,34,242,193,238,210,144,12,191,179,162,241,81,51,145,235,249,14,239,
    107,49,192,214,31,181,199,106,157,184,84,204,176,115,121,50,45,127,4,150,254,
    138,236,205,93,222,114,67,29,24,72,243,141,128,195,78,66,215,61,156,180]

  const perm = new Array(512)
  const gradP = new Array(512)

  function seed(s) {
    if (s > 0 && s < 1) s *= 65536
    s = Math.floor(s)
    if (s < 256) s |= s << 8
    for (let i = 0; i < 256; i++) {
      let v = (i & 1) ? p[i] ^ (s & 255) : p[i] ^ ((s >> 8) & 255)
      perm[i] = perm[i + 256] = v
      gradP[i] = gradP[i + 256] = grad3[v % 12]
    }
  }

  seed(Math.random())

  function fade(t) {
    return t * t * t * (t * (t * 6 - 15) + 10)
  }

  function lerp(a, b, t) {
    return (1 - t) * a + t * b
  }

  function dot2(g, x, y) {
    return g[0] * x + g[1] * y
  }

  function perlin2(x, y) {
    let X = Math.floor(x), Y = Math.floor(y)
    x = x - X; y = y - Y
    X = X & 255; Y = Y & 255

    const n00 = dot2(gradP[X + perm[Y]], x, y)
    const n01 = dot2(gradP[X + perm[Y + 1]], x, y - 1)
    const n10 = dot2(gradP[X + 1 + perm[Y]], x - 1, y)
    const n11 = dot2(gradP[X + 1 + perm[Y + 1]], x - 1, y - 1)

    const u = fade(x)
    return lerp(lerp(n00, n10, u), lerp(n01, n11, u), fade(y))
  }

  return { seed, perlin2 }
})()

// 设置尺寸
function setSize() {
  if (!wavesContainer.value || !wavesSvg.value) return
  bounding = wavesContainer.value.getBoundingClientRect()
  wavesSvg.value.setAttribute('width', bounding.width)
  wavesSvg.value.setAttribute('height', bounding.height)

  // 初始化鼠标位置
  if (!mouse.value.set) {
    mouse.value.x = bounding.width / 2
    mouse.value.y = bounding.height / 2
    mouse.value.sx = mouse.value.x
    mouse.value.sy = mouse.value.y
    mouse.value.lx = mouse.value.x
    mouse.value.ly = mouse.value.y
  }
}

// 创建波浪线
function setLines() {
  if (!bounding || !wavesSvg.value) return

  const { width, height } = bounding
  lines = []
  paths.forEach(path => path.remove())
  paths = []

  const xGap = 10
  const yGap = 32
  const oWidth = width + 200
  const oHeight = height + 50
  const totalLines = Math.ceil(oWidth / xGap)
  const totalPoints = Math.ceil(oHeight / yGap)
  const xStart = (width - xGap * totalLines) / 2
  const yStart = (height - yGap * totalPoints) / 2

  // 根据主题设置波浪线颜色
  const strokeColor = themeStore.isDark ? 'rgba(255, 255, 255, 0.15)' : 'rgba(0, 0, 0, 0.18)'

  for (let i = 0; i <= totalLines; i++) {
    const points = []
    for (let j = 0; j <= totalPoints; j++) {
      points.push({
        x: xStart + xGap * i,
        y: yStart + yGap * j,
        wave: { x: 0, y: 0 },
        cursor: { x: 0, y: 0, vx: 0, vy: 0 }
      })
    }
    const path = document.createElementNS('http://www.w3.org/2000/svg', 'path')
    path.setAttribute('fill', 'none')
    path.setAttribute('stroke', strokeColor)
    path.setAttribute('stroke-width', '1')
    wavesSvg.value.appendChild(path)
    paths.push(path)
    lines.push(points)
  }
}

// 更新波浪线颜色
function updateWaveColors() {
  const strokeColor = themeStore.isDark ? 'rgba(255, 255, 255, 0.15)' : 'rgba(0, 0, 0, 0.18)'
  paths.forEach(path => {
    path.setAttribute('stroke', strokeColor)
  })
}

// 监听主题变化
watch(() => themeStore.isDark, () => {
  updateWaveColors()
})

// 更新点位置
function movePoints(time) {
  const m = mouse.value

  lines.forEach((points) => {
    points.forEach((p) => {
      // 波浪运动
      const move = noise.perlin2(
        (p.x + time * 0.0125) * 0.002,
        (p.y + time * 0.005) * 0.0015
      ) * 12

      p.wave.x = Math.cos(move) * 32
      p.wave.y = Math.sin(move) * 16

      // 鼠标交互
      const dx = p.x - m.sx
      const dy = p.y - m.sy
      const d = Math.hypot(dx, dy)
      const l = Math.max(200, m.vs * 2)

      if (d < l) {
        const s = 1 - d / l
        const f = Math.cos(d * 0.001) * s
        p.cursor.vx += Math.cos(m.a) * f * l * m.vs * 0.001
        p.cursor.vy += Math.sin(m.a) * f * l * m.vs * 0.001
      }

      // 弹性恢复
      p.cursor.vx += (0 - p.cursor.x) * 0.005
      p.cursor.vy += (0 - p.cursor.y) * 0.005

      // 摩擦力
      p.cursor.vx *= 0.92
      p.cursor.vy *= 0.92

      // 应用速度
      p.cursor.x += p.cursor.vx * 2.5
      p.cursor.y += p.cursor.vy * 2.5

      // 限制范围
      p.cursor.x = Math.min(120, Math.max(-120, p.cursor.x))
      p.cursor.y = Math.min(120, Math.max(-120, p.cursor.y))
    })
  })
}

// 获取移动后的点坐标
function moved(point, withCursor = true) {
  return {
    x: Math.round((point.x + point.wave.x + (withCursor ? point.cursor.x : 0)) * 10) / 10,
    y: Math.round((point.y + point.wave.y + (withCursor ? point.cursor.y : 0)) * 10) / 10
  }
}

// 绘制曲线
function drawLines() {
  lines.forEach((points, lIndex) => {
    let d = ''

    points.forEach((point, pIndex) => {
      const isLast = pIndex === points.length - 1
      const isFirst = pIndex === 0
      const p = moved(point, !isLast)

      if (isFirst) {
        d = `M ${p.x} ${p.y}`
      } else {
        const prevPoint = points[pIndex - 1]
        const prev = moved(prevPoint, true)
        const midX = (prev.x + p.x) / 2
        const midY = (prev.y + p.y) / 2
        d += ` Q ${prev.x} ${prev.y} ${midX} ${midY}`
      }
    })

    // 最后一个点
    const lastPoint = points[points.length - 1]
    const last = moved(lastPoint, false)
    d += ` L ${last.x} ${last.y}`

    paths[lIndex]?.setAttribute('d', d)
  })
}

// 动画循环
function tick(time) {
  const m = mouse.value

  // 平滑跟随
  m.sx += (m.x - m.sx) * 0.1
  m.sy += (m.y - m.sy) * 0.1

  // 计算速度
  const dx = m.x - m.lx
  const dy = m.y - m.ly
  const d = Math.hypot(dx, dy)

  m.v = d
  m.vs += (d - m.vs) * 0.1
  m.vs = Math.min(100, m.vs)

  m.lx = m.x
  m.ly = m.y
  m.a = Math.atan2(dy, dx)

  // 更新动画
  movePoints(time)
  drawLines()

  animationId = requestAnimationFrame(tick)
}

// 事件处理
function onResize() {
  setSize()
  setLines()
}

function onMouseMove(e) {
  if (!bounding) return
  mouse.value.x = e.clientX - bounding.left
  mouse.value.y = e.clientY - bounding.top

  if (!mouse.value.set) {
    mouse.value.sx = mouse.value.x
    mouse.value.sy = mouse.value.y
    mouse.value.lx = mouse.value.x
    mouse.value.ly = mouse.value.y
    mouse.value.set = true
  }
}

function onTouchMove(e) {
  e.preventDefault()
  const touch = e.touches[0]
  if (!bounding) return
  mouse.value.x = touch.clientX - bounding.left
  mouse.value.y = touch.clientY - bounding.top

  if (!mouse.value.set) {
    mouse.value.sx = mouse.value.x
    mouse.value.sy = mouse.value.y
    mouse.value.lx = mouse.value.x
    mouse.value.ly = mouse.value.y
    mouse.value.set = true
  }
}

onMounted(() => {
  noise.seed(Math.random())
  setSize()
  setLines()

  window.addEventListener('resize', onResize)
  window.addEventListener('mousemove', onMouseMove)
  window.addEventListener('touchmove', onTouchMove, { passive: false })

  animationId = requestAnimationFrame(tick)
})

onUnmounted(() => {
  window.removeEventListener('resize', onResize)
  window.removeEventListener('mousemove', onMouseMove)
  window.removeEventListener('touchmove', onTouchMove)

  if (animationId) {
    cancelAnimationFrame(animationId)
  }
})
</script>

<style scoped>
.landing-page {
  width: 100vw;
  height: 100vh;
  overflow: hidden;
  font-family: 'Helvetica Neue', Helvetica, 'PingFang SC', 'Microsoft YaHei', sans-serif;
  background: var(--gradient-bg);
  background-size: 400% 400%;
  animation: gradientShift 15s ease infinite;
}

@keyframes gradientShift {
  0% { background-position: 0% 50%; }
  50% { background-position: 100% 50%; }
  100% { background-position: 0% 50%; }
}

.waves-container {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: calc(100% - 120px);
  overflow: hidden;
}

.waves-svg {
  display: block;
  width: 100%;
  height: 100%;
}

.cursor-dot {
  position: absolute;
  top: 0;
  left: 0;
  width: 0.6rem;
  height: 0.6rem;
  background: var(--text-primary);
  border-radius: 50%;
  pointer-events: none;
  z-index: 10;
  margin-left: -0.3rem;
  margin-top: -0.3rem;
  opacity: 0.7;
}

/* ========== 主题切换按钮 ========== */
.theme-toggle {
  position: fixed;
  top: 20px;
  left: 20px;
  width: 44px;
  height: 44px;
  border-radius: 50%;
  border: 1px solid var(--border-color);
  background: var(--bg-nav);
  color: var(--text-primary);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 100;
  transition: all 0.3s ease;
  box-shadow: 0 2px 10px var(--shadow-color);
}

.theme-toggle:hover {
  background: var(--color-primary);
  border-color: var(--color-primary);
  color: #fff;
  transform: scale(1.1);
}

.theme-icon {
  width: 20px;
  height: 20px;
}

/* 深色模式下发光效果 */
:global(html.dark) .theme-toggle {
  background: transparent;
  border-color: rgba(255, 255, 255, 0.3);
  color: #fff;
  box-shadow: 0 0 15px rgba(255, 255, 255, 0.3);
}

:global(html.dark) .theme-toggle:hover {
  background: rgba(255, 255, 255, 0.1);
  border-color: rgba(255, 255, 255, 0.5);
  box-shadow: 0 0 25px rgba(255, 255, 255, 0.5);
}

.hero-title {
  position: absolute;
  top: 45%;
  left: 50%;
  transform: translate(-50%, -50%);
  text-align: center;
  z-index: 20;
  pointer-events: none;
}

.hero-title h1 {
  font-size: 4rem;
  font-weight: 900;
  color: var(--text-primary);
  letter-spacing: 0.5rem;
  text-transform: uppercase;
  margin-bottom: 1rem;
  text-shadow: 0 0 10px var(--shadow-color);
  animation: breatheGlow 3s ease-in-out infinite;
}

@keyframes breatheGlow {
  0%, 100% {
    text-shadow:
      0 0 10px var(--color-primary-light),
      0 0 20px var(--color-primary-light),
      0 0 30px var(--color-primary);
    opacity: 0.9;
  }
  50% {
    text-shadow:
      0 0 20px var(--color-primary-light),
      0 0 40px var(--color-primary),
      0 0 60px var(--color-primary),
      0 0 80px var(--color-accent);
    opacity: 1;
  }
}

.hero-title p {
  font-size: 1.2rem;
  color: var(--text-secondary);
  letter-spacing: 0.3rem;
}

/* ========== 底部导航栏 - 深浅色适配 ========== */
.bottom-nav {
  position: fixed;
  bottom: 0;
  left: 0;
  width: 100%;
  height: 120px;
  background: var(--bg-nav);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 100;
  border-top: 1px solid var(--border-color);
  transition: background 0.3s ease, border-color 0.3s ease;
}

.nav-container {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0;
  max-width: 1400px;
  width: 100%;
  padding: 0 40px;
}

.nav-item {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  height: 120px;
  text-decoration: none;
  color: var(--text-primary);
  font-size: 1.1rem;
  font-weight: 700;
  letter-spacing: 0.15rem;
  text-transform: uppercase;
  position: relative;
  transition: all 0.3s ease;
  border-right: 1px solid var(--border-color);
}

.nav-item:last-child {
  border-right: none;
}

.nav-item:hover {
  background: var(--bg-secondary);
  color: var(--color-primary);
}

.nav-item::before {
  content: '';
  position: absolute;
  bottom: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 0;
  height: 3px;
  background: linear-gradient(90deg, var(--color-primary-light), var(--color-primary), var(--color-accent));
  transition: width 0.3s ease;
}

.nav-item:hover::before {
  width: 80%;
}

.nav-divider {
  color: var(--text-muted);
  font-size: 2rem;
  font-weight: 300;
  padding: 0 20px;
}

/* 响应式 */
@media (max-width: 768px) {
  .hero-title h1 {
    font-size: 2rem;
    letter-spacing: 0.2rem;
  }

  .nav-item {
    font-size: 0.8rem;
    letter-spacing: 0.05rem;
  }

  .bottom-nav {
    height: 80px;
  }

  .nav-item {
    height: 80px;
  }

  .waves-container {
    height: calc(100% - 80px);
  }

  .nav-divider {
    padding: 0 10px;
    font-size: 1.5rem;
  }

  .theme-toggle {
    top: 10px;
    left: 10px;
    width: 36px;
    height: 36px;
  }

  .theme-icon {
    width: 16px;
    height: 16px;
  }
}
</style>
