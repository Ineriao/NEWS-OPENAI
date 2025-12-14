package com.news.dto;

import lombok.Data;

/**
 * 新闻创建/更新 DTO
 */
@Data
public class NewsDTO {

    /** 标题 */
    private String title;

    /** 摘要 */
    private String summary;

    /** 内容 (富文本HTML) */
    private String content;

    /** 封面图片URL */
    private String coverImage;

    /** 分类ID */
    private Long categoryId;
}
