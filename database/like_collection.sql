-- =====================================================
-- 点赞和收藏功能 - 数据库表
-- =====================================================

-- 1. 点赞表
CREATE TABLE IF NOT EXISTS `news_like` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `news_id` BIGINT NOT NULL COMMENT '新闻ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY `uk_news_user` (`news_id`, `user_id`),
    KEY `idx_news_id` (`news_id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='新闻点赞表';

-- 2. 收藏表
CREATE TABLE IF NOT EXISTS `news_collection` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `news_id` BIGINT NOT NULL COMMENT '新闻ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY `uk_news_user` (`news_id`, `user_id`),
    KEY `idx_news_id` (`news_id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='新闻收藏表';

-- 3. 评论点赞表
CREATE TABLE IF NOT EXISTS `comment_like` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `comment_id` BIGINT NOT NULL COMMENT '评论ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY `uk_comment_user` (`comment_id`, `user_id`),
    KEY `idx_comment_id` (`comment_id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评论点赞表';

-- 4. 修改 news 表添加点赞数和收藏数字段 (忽略已存在的列错误)
ALTER TABLE `news` ADD COLUMN `like_count` INT DEFAULT 0 COMMENT '点赞数';
ALTER TABLE `news` ADD COLUMN `collection_count` INT DEFAULT 0 COMMENT '收藏数';

-- 5. 修改 comment 表添加点赞数字段
ALTER TABLE `comment` ADD COLUMN `like_count` INT DEFAULT 0 COMMENT '点赞数';
