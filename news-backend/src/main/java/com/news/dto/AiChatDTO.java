package com.news.dto;

import lombok.Data;

/**
 * AI 聊天请求 DTO
 */
@Data
public class AiChatDTO {

    /** 用户消息 */
    private String message;

    /** 新闻ID (可选，用于总结新闻) */
    private Long newsId;

    /** 新闻内容 (可选，直接传递内容) */
    private String newsContent;
}
