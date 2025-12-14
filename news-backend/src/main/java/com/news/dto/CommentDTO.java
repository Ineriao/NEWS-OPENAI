package com.news.dto;

import lombok.Data;

/**
 * 发表评论 DTO
 */
@Data
public class CommentDTO {

    /** 新闻ID */
    private Long newsId;

    /** 评论内容 */
    private String content;

    /** 父评论ID（如果是回复） */
    private Long parentId;
}
