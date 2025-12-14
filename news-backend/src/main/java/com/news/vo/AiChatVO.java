package com.news.vo;

import lombok.Data;

/**
 * AI 聊天响应 VO
 */
@Data
public class AiChatVO {

    /** AI 回复内容 */
    private String reply;

    /** 模型名称 */
    private String model;

    /** Token 使用量 (可选) */
    private Integer tokens;
}
