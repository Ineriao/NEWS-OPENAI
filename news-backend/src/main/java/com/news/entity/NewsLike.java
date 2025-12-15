package com.news.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 新闻点赞实体类
 * 对应数据库 news_like 表
 */
@Data
@TableName("news_like")
public class NewsLike {

    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 新闻ID */
    private Long newsId;

    /** 用户ID */
    private Long userId;

    /** 创建时间 */
    private LocalDateTime createTime;
}
