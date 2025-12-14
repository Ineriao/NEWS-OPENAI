package com.news.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 外部新闻实体类（从外部 API 抓取的新闻）
 */
@Data
@TableName("external_news")
public class ExternalNews {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 新闻标题
     */
    private String title;

    /**
     * 新闻摘要/描述
     */
    private String description;

    /**
     * 原文链接
     */
    private String url;

    /**
     * 封面图片
     */
    private String coverImage;

    /**
     * 来源平台（36kr/ithome/thepaper/toutiao等）
     */
    private String source;

    /**
     * 热度值
     */
    private String hot;

    /**
     * 抓取时间
     */
    private LocalDateTime fetchTime;
}
