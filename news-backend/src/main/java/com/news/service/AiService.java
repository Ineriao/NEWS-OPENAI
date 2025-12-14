package com.news.service;

import com.news.dto.AiChatDTO;
import com.news.vo.AiChatVO;

/**
 * AI 服务接口
 */
public interface AiService {

    /**
     * AI 对话
     *
     * @param dto 聊天请求
     * @return AI 回复
     */
    AiChatVO chat(AiChatDTO dto);

    /**
     * 总结新闻内容
     *
     * @param newsContent 新闻内容
     * @return AI 总结
     */
    AiChatVO summarize(String newsContent);
}
