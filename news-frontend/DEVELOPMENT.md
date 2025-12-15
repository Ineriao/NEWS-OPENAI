# 开发笔记

## Vue Scoped CSS 与深色模式

### 问题描述

在 Vue 单文件组件中使用 scoped 样式时，`:global(html.dark)` 选择器可能无法正确生效。

### 问题原因

Vue 的 scoped 样式会为选择器添加唯一的属性选择器（如 `[data-v-xxxxx]`），当使用 `:global()` 包裹时，虽然理论上应该跳过作用域限制，但在某些复杂选择器组合下（特别是结合 `:deep()` 时），可能出现以下问题：

1. **选择器优先级问题**：scoped 生成的属性选择器可能影响最终的 CSS 优先级
2. **选择器解析问题**：`:global(html.dark)` 与 `:deep()` 组合使用时，Vue 编译器的处理可能不符合预期
3. **样式隔离冲突**：scoped 的设计初衷是隔离样式，与全局选择器组合时可能产生意外行为

### 错误示例

```vue
<style scoped>
/* 这种写法可能不生效 */
:global(html.dark) .my-component :deep(.el-button) {
  color: #fff;
}
</style>
```

### 正确解决方案

使用**独立的非 scoped `<style>` 块**来处理深色模式样式：

```vue
<template>
  <div class="my-component">
    <!-- ... -->
  </div>
</template>

<style scoped>
/* 浅色模式样式 */
.my-component .el-button {
  color: #333;
}
</style>

<!-- 深色模式样式 - 非 scoped -->
<style>
html.dark .my-component .el-button {
  color: #fff !important;
}
</style>
```

### 关键点

1. **使用明确的父级选择器**：如 `.my-component`，确保样式只影响当前组件
2. **非 scoped 块单独处理深色模式**：避免与 scoped 机制冲突
3. **使用 `!important`**：确保覆盖其他样式（如第三方组件库）

### 实际案例

在 `AdminLayout.vue` 中处理表格删除按钮的深色模式样式：

```vue
<style scoped>
/* 浅色模式：删除按钮黑色字体 */
.main :deep(.el-table) .el-button--danger.is-link {
  color: #303133 !important;
}
</style>

<!-- 深色模式样式 - 非 scoped -->
<style>
html.dark .admin-layout .main .el-table .el-button--danger.is-link {
  color: #fff !important;
}
</style>
```

---

## CSS 兼容性处理

### Safari backdrop-filter

Safari 需要 `-webkit-` 前缀：

```css
/* 正确写法 */
.card {
  -webkit-backdrop-filter: blur(10px);
  backdrop-filter: blur(10px);
}
```

### 无障碍访问（a11y）

图标按钮需要添加 `title` 和 `aria-label` 属性：

```vue
<el-button
  circle
  @click="toggleTheme"
  :title="isDark ? '切换到浅色模式' : '切换到深色模式'"
  :aria-label="isDark ? '切换到浅色模式' : '切换到深色模式'"
>
  <el-icon><Moon /></el-icon>
</el-button>
```

---

## 已知问题和忽略项

以下 CSS 警告来自第三方库或浏览器扩展，可以安全忽略：

- `-moz-appearance`：Firefox 特有属性，来自 Element Plus
- `-ms-touch-action`：IE 特有属性，来自第三方库
- `user-select` 无前缀警告：现代浏览器已原生支持
