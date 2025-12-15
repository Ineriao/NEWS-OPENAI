# 开发笔记

## 项目结构

### 后端 (news-backend)

```
src/main/java/com/news/
├── common/                    # 公共类
│   ├── Result.java           # 统一响应结果
│   ├── BusinessException.java # 自定义业务异常
│   └── RateLimit.java        # 速率限制注解
├── config/                    # 配置类
│   ├── WebConfig.java        # Web MVC 配置
│   ├── JwtInterceptor.java   # JWT 认证拦截器
│   ├── RateLimitInterceptor.java # 速率限制拦截器
│   ├── GlobalExceptionHandler.java # 全局异常处理
│   └── MyBatisPlusConfig.java # MyBatis-Plus 配置
├── controller/               # 控制器
├── service/                  # 服务层
├── dao/                      # 数据访问层
├── entity/                   # 实体类
├── dto/                      # 数据传输对象
├── vo/                       # 视图对象
└── utils/                    # 工具类
```

### 前端 (news-frontend)

```
src/
├── api/                      # API 接口
├── components/               # 公共组件
├── stores/                   # Pinia 状态管理
├── views/                    # 页面视图
│   ├── admin/               # 后台管理页面
│   └── front/               # 前台页面
└── utils/                    # 工具函数
```

---

## API 速率限制

### 功能说明

使用 `@RateLimit` 注解实现接口级别的速率限制，防止恶意请求和暴力破解。

### 相关文件

| 文件 | 路径 | 说明 |
|------|------|------|
| RateLimit.java | `common/RateLimit.java` | 速率限制注解 |
| RateLimitInterceptor.java | `config/RateLimitInterceptor.java` | 拦截器实现 |
| WebConfig.java | `config/WebConfig.java` | 拦截器配置 |

### 使用方法

```java
// 默认：60秒内最多10次，按IP限制
@RateLimit
@GetMapping("/api/example")
public Result example() { ... }

// 自定义：30秒内最多5次
@RateLimit(limit = 5, period = 30)
@PostMapping("/api/example")
public Result example() { ... }

// 按用户限制（需登录）
@RateLimit(limit = 10, period = 60, type = RateLimit.LimitType.USER)
@PostMapping("/api/example")
public Result example() { ... }

// 全局限制（所有请求共享配额）
@RateLimit(limit = 100, period = 60, type = RateLimit.LimitType.GLOBAL)
@GetMapping("/api/example")
public Result example() { ... }
```

### 限制类型

| 类型 | 说明 | 适用场景 |
|------|------|----------|
| `IP` | 按客户端IP限制 | 登录、注册等公开接口 |
| `USER` | 按用户ID限制 | 需登录的接口，如AI对话 |
| `GLOBAL` | 全局限制 | 共享资源接口 |

### 已配置的接口

| 接口 | 限制 | 说明 |
|------|------|------|
| `POST /api/auth/login` | 10次/分钟(IP) | 防止暴力破解 |
| `POST /api/auth/register` | 5次/分钟(IP) | 防止批量注册 |
| `GET /api/auth/check-username` | 30次/分钟(IP) | 防止用户名枚举 |
| `POST /api/ai/chat` | 10次/分钟(USER) | 控制AI调用成本 |
| `POST /api/ai/summarize` | 5次/分钟(USER) | 控制AI调用成本 |

### 超限响应

```json
{
  "code": 429,
  "message": "请求过于频繁，请稍后再试",
  "data": null
}
```

---

## 全局异常处理

### 相关文件

| 文件 | 路径 | 说明 |
|------|------|------|
| BusinessException.java | `common/BusinessException.java` | 自定义业务异常 |
| GlobalExceptionHandler.java | `config/GlobalExceptionHandler.java` | 全局异常处理器 |

### 使用方法

```java
// 抛出业务异常
throw new BusinessException("操作失败");
throw new BusinessException(400, "参数错误");

// 使用工厂方法
throw BusinessException.badRequest("参数错误");
throw BusinessException.notFound("用户不存在");
throw BusinessException.unauthorized("请先登录");
throw BusinessException.forbidden("权限不足");
```

### 异常处理映射

| 异常类型 | HTTP状态码 | 说明 |
|----------|-----------|------|
| `BusinessException` | 自定义 | 业务逻辑异常 |
| `MethodArgumentNotValidException` | 400 | @Valid 校验失败 |
| `MissingServletRequestParameterException` | 400 | 缺少必要参数 |
| `HttpRequestMethodNotSupportedException` | 405 | 请求方法不支持 |
| `NoHandlerFoundException` | 404 | 接口不存在 |
| `Exception` | 500 | 其他未知异常 |

---

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
