package com.news.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 新闻收藏实体类
 * 对应数据库 news_collection 表
 */
@Data
@TableName("news_collection")
public class NewsCollection {

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
