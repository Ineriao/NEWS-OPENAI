package com.news.vo;

import lombok.Data;

/**
 * 外部新闻视图对象
 */
@Data
public class ExternalNewsVO {

    /**
     * 新闻标题
     */
    private String title;

    /**
     * 新闻摘要
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
     * 来源平台
     */
    private String source;

    /**
     * 热度值
     */
    private String hot;

    /**
     * 抓取时间
     */
    private String fetchTime;
}
