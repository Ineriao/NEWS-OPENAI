-- =====================================================
-- 新闻发布管理系统 - 数据库设计
-- 数据库: MySQL 8.0+
-- 字符集: utf8mb4
-- =====================================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS news_system
DEFAULT CHARACTER SET utf8mb4
DEFAULT COLLATE utf8mb4_unicode_ci;

USE news_system;

-- =====================================================
-- 1. 用户表 (user)
-- 角色: 0=游客(不存储), 1=普通用户, 2=编辑, 3=总编, 4=管理员
-- =====================================================
CREATE TABLE `user` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID',
    `username` VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    `password` VARCHAR(255) NOT NULL COMMENT '密码(BCrypt加密)',
    `email` VARCHAR(100) COMMENT '邮箱',
    `avatar` VARCHAR(255) COMMENT '头像URL',
    `role` TINYINT NOT NULL DEFAULT 1 COMMENT '角色: 1=普通用户, 2=编辑, 3=总编, 4=管理员',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 0=禁用, 1=正常',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX `idx_username` (`username`),
    INDEX `idx_role` (`role`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- =====================================================
-- 2. 新闻分类表 (category)
-- 支持二级分类: parent_id为NULL表示一级分类
-- =====================================================
CREATE TABLE `category` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '分类ID',
    `name` VARCHAR(50) NOT NULL COMMENT '分类名称',
    `parent_id` BIGINT DEFAULT NULL COMMENT '父分类ID，NULL表示一级分类',
    `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序顺序',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    FOREIGN KEY (`parent_id`) REFERENCES `category`(`id`) ON DELETE SET NULL,
    INDEX `idx_parent` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='新闻分类表';

-- =====================================================
-- 3. 新闻表 (news)
-- 状态: 0=草稿, 1=待审核, 2=已发布, 3=已存档
-- =====================================================
CREATE TABLE `news` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '新闻ID',
    `title` VARCHAR(200) NOT NULL COMMENT '标题',
    `summary` VARCHAR(500) COMMENT '摘要',
    `content` LONGTEXT NOT NULL COMMENT '内容(富文本HTML)',
    `cover_image` VARCHAR(255) COMMENT '封面图片URL',
    `category_id` BIGINT NOT NULL COMMENT '分类ID',
    `author_id` BIGINT NOT NULL COMMENT '作者ID(编辑)',
    `reviewer_id` BIGINT DEFAULT NULL COMMENT '审核人ID(总编)',
    `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态: 0=草稿, 1=待审核, 2=已发布, 3=已存档',
    `view_count` INT NOT NULL DEFAULT 0 COMMENT '浏览次数',
    `publish_time` DATETIME DEFAULT NULL COMMENT '发布时间',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (`category_id`) REFERENCES `category`(`id`),
    FOREIGN KEY (`author_id`) REFERENCES `user`(`id`),
    FOREIGN KEY (`reviewer_id`) REFERENCES `user`(`id`),
    INDEX `idx_category` (`category_id`),
    INDEX `idx_author` (`author_id`),
    INDEX `idx_status` (`status`),
    INDEX `idx_publish_time` (`publish_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='新闻表';

-- =====================================================
-- 4. 评论表 (comment)
-- 支持回复: parent_id为NULL表示一级评论
-- =====================================================
CREATE TABLE `comment` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '评论ID',
    `news_id` BIGINT NOT NULL COMMENT '新闻ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `content` TEXT NOT NULL COMMENT '评论内容',
    `parent_id` BIGINT DEFAULT NULL COMMENT '父评论ID，NULL表示一级评论',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 0=隐藏, 1=显示',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    FOREIGN KEY (`news_id`) REFERENCES `news`(`id`) ON DELETE CASCADE,
    FOREIGN KEY (`user_id`) REFERENCES `user`(`id`),
    FOREIGN KEY (`parent_id`) REFERENCES `comment`(`id`) ON DELETE CASCADE,
    INDEX `idx_news` (`news_id`),
    INDEX `idx_user` (`user_id`),
    INDEX `idx_parent` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评论表';

-- =====================================================
-- 初始化数据
-- =====================================================

-- 插入管理员账号 (密码: admin123, BCrypt加密后的值)
INSERT INTO `user` (`username`, `password`, `email`, `role`, `status`) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', 'admin@example.com', 4, 1);

-- 插入示例新闻分类
INSERT INTO `category` (`name`, `parent_id`, `sort_order`) VALUES
('时政新闻', NULL, 1),
('体育新闻', NULL, 2),
('科技新闻', NULL, 3),
('娱乐新闻', NULL, 4),
('财经新闻', NULL, 5);

-- 插入二级分类示例
INSERT INTO `category` (`name`, `parent_id`, `sort_order`) VALUES
('国内时政', 1, 1),
('国际时政', 1, 2),
('足球', 2, 1),
('篮球', 2, 2),
('人工智能', 3, 1),
('互联网', 3, 2);

-- =====================================================
-- 5. 热搜表 (hot_search)
-- 存储各平台热搜数据
-- =====================================================
CREATE TABLE IF NOT EXISTS `hot_search` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '热搜ID',
    `title` VARCHAR(255) NOT NULL COMMENT '热搜标题',
    `hot` VARCHAR(50) COMMENT '热度值',
    `url` VARCHAR(500) COMMENT '链接地址',
    `source` VARCHAR(20) NOT NULL COMMENT '来源平台(微博/抖音/知乎/百度)',
    `rank_num` INT NOT NULL COMMENT '排名',
    `fetch_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '抓取时间',
    INDEX `idx_source` (`source`),
    INDEX `idx_fetch_time` (`fetch_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='热搜表';

-- =====================================================
-- 6. 外部新闻表 (external_news)
-- 存储从外部API抓取的新闻数据
-- =====================================================
CREATE TABLE IF NOT EXISTS `external_news` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '新闻ID',
    `title` VARCHAR(255) NOT NULL COMMENT '新闻标题',
    `description` TEXT COMMENT '新闻摘要',
    `url` VARCHAR(500) COMMENT '原文链接',
    `cover_image` VARCHAR(500) COMMENT '封面图片',
    `source` VARCHAR(50) NOT NULL COMMENT '来源平台(36氪/IT之家/澎湃新闻/今日头条)',
    `hot` VARCHAR(50) COMMENT '热度值',
    `fetch_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '抓取时间',
    INDEX `idx_source` (`source`),
    INDEX `idx_fetch_time` (`fetch_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='外部新闻表';
