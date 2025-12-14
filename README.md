# 新闻发布管理系统

一个完整的新闻发布管理系统，模拟新闻媒介从撰写、审核到发布的完整流程。支持多角色权限管理、新闻分类、评论互动以及实时热搜展示。

## 项目预览

- 前台首页：新闻列表 + 热搜榜
- 新闻详情：内容阅读 + 评论区
- 后台管理：新闻管理 + 审核 + 用户管理

## 技术栈

### 后端
| 技术 | 版本 | 说明 |
|------|------|------|
| Java | 17 | 开发语言 |
| Spring Boot | 2.7.18 | 快速开发框架 |
| MyBatis-Plus | 3.5.3.1 | ORM 框架 |
| MySQL | 8.0+ | 数据库 |
| Druid | 1.2.18 | 数据库连接池 |
| JWT | 0.11.5 | 用户认证 |
| Lombok | 1.18.30 | 简化代码 |

### 前端
| 技术 | 版本 | 说明 |
|------|------|------|
| Vue.js | 3.5 | 前端框架 |
| Vite | 7.x | 构建工具 |
| Element Plus | 2.12 | UI 组件库 |
| Pinia | 3.x | 状态管理 |
| Vue Router | 4.x | 路由管理 |
| Axios | 1.x | HTTP 客户端 |

### 外部服务
| 服务 | 说明 |
|------|------|
| DailyHotApi | 热搜数据接口（微博/抖音/知乎/百度） |

## 快速开始

### 环境要求

- JDK 17
- Node.js 18+
- MySQL 8.0+
- Maven 3.6+

### 1. 克隆项目

```bash
git clone <repository-url>
cd WEBdesign
```

### 2. 初始化数据库

```bash
# 登录 MySQL
mysql -u root -p

# 执行建表脚本
source database/news_system.sql
```

### 3. 启动后端服务

```bash
cd news-backend

# 修改数据库配置 (src/main/resources/application.yml)
# 编译打包
mvn clean package -DskipTests

# 启动服务
java -jar target/news-backend-1.0.0.jar
```

后端服务运行在：http://localhost:8080

### 4. 启动热搜服务（可选）

```bash
cd DailyHotApi
npm install
npm run build
npm run start
```

热搜服务运行在：http://localhost:6688

### 5. 启动前端服务

```bash
cd news-frontend
npm install
npm run dev
```

前端服务运行在：http://localhost:5173

### 6. 访问系统

- 前台首页：http://localhost:5173
- 后台管理：http://localhost:5173/admin

**默认管理员账号**：
- 用户名：admin
- 密码：admin123

## 项目结构

```
WEBdesign/
├── news-backend/                # 后端项目（Spring Boot）
│   ├── src/main/java/com/news/
│   │   ├── common/              # 通用类（Result）
│   │   ├── config/              # 配置类（JWT拦截器、CORS）
│   │   ├── controller/          # 控制器（REST API）
│   │   ├── service/             # 业务逻辑层
│   │   ├── dao/                 # 数据访问层（Mapper）
│   │   ├── entity/              # 实体类
│   │   ├── dto/                 # 数据传输对象
│   │   ├── vo/                  # 视图对象
│   │   └── utils/               # 工具类（JWT）
│   └── src/main/resources/
│       └── application.yml      # 应用配置
│
├── news-frontend/               # 前端项目（Vue 3）
│   ├── src/
│   │   ├── api/                 # API 接口
│   │   ├── router/              # 路由配置
│   │   ├── stores/              # Pinia 状态管理
│   │   ├── utils/               # 工具函数
│   │   └── views/               # 页面组件
│   │       ├── front/           # 前台页面
│   │       └── admin/           # 后台页面
│   └── vite.config.js           # Vite 配置
│
├── database/                    # 数据库脚本
│   ├── news_system.sql          # 建表脚本
│   └── test_data.sql            # 测试数据
│
├── docs/                        # 项目文档
│   ├── 开发文档.md
│   ├── 技术知识点.md
│   ├── 开发问题记录.md
│   ├── 热搜API接入文档.md
│   └── 数据库规范文档.md
│
└── DailyHotApi/                 # 热搜数据服务（第三方）
```

## 功能模块

### 用户角色

| 角色 | 权限 |
|------|------|
| 游客（0） | 浏览新闻、查看热搜 |
| 普通用户（1） | 登录、评论 |
| 编辑（2） | 撰写、编辑新闻 |
| 总编（3） | 审核、发布新闻 |
| 管理员（4） | 系统管理 |

### 新闻状态流转

```
草稿(0) → 待审核(1) → 已发布(2) → 已存档(3)
   ↑          │
   └──────────┘ (退回修改)
```

### API 接口

#### 公开接口（无需登录）
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/public/news | 新闻列表 |
| GET | /api/public/news/{id} | 新闻详情 |
| GET | /api/public/news/search | 搜索新闻 |
| GET | /api/public/categories | 分类列表 |
| GET | /api/public/comments/{newsId} | 评论列表 |
| GET | /api/public/hot/{platform} | 热搜数据 |

#### 认证接口
| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /api/auth/login | 登录 |
| POST | /api/auth/register | 注册 |

#### 管理接口（需要登录）
| 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|
| POST | /api/news | 编辑+ | 创建新闻 |
| PUT | /api/news/{id} | 编辑+ | 更新新闻 |
| PUT | /api/news/{id}/submit | 编辑+ | 提交审核 |
| PUT | /api/news/{id}/approve | 总编+ | 审核通过 |
| PUT | /api/news/{id}/reject | 总编+ | 退回修改 |

## 配置说明

### 后端配置（application.yml）

```yaml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:mysql://localhost:3306/news_system?useSSL=false&serverTimezone=Asia/Shanghai
    username: root
    password: 123456

jwt:
  secret: your-secret-key
  expiration: 86400000  # 24小时
```

### 前端配置（vite.config.js）

```javascript
export default defineConfig({
  server: {
    port: 5173,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  }
})
```

## 文档导航

| 文档 | 说明 |
|------|------|
| [开发文档](docs/开发文档.md) | 项目架构、API 设计、开发进度 |
| [技术知识点](docs/技术知识点.md) | 涉及的技术知识详解 |
| [开发问题记录](docs/开发问题记录.md) | 开发中遇到的问题及解决方案 |
| [热搜API接入文档](docs/热搜API接入文档.md) | DailyHotApi 集成说明 |
| [数据库规范文档](docs/数据库规范文档.md) | 数据库设计规范 |

## 常见问题

### 1. JDK 版本问题
推荐使用 JDK 17，JDK 21.0.6 与 Lombok 存在兼容性问题。

### 2. 端口占用
- 后端默认 8080
- 前端默认 5173
- 热搜服务默认 6688

如端口被占用，可在配置文件中修改。

### 3. 热搜服务不可用
热搜功能依赖 DailyHotApi 服务，如果服务未启动，热搜栏会显示"暂无数据"，不影响其他功能。

## License

MIT License
