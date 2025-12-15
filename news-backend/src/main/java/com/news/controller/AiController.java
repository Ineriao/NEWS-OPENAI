package com.news.controller;

import com.news.common.RateLimit;
import com.news.common.Result;
import com.news.dto.AiChatDTO;
import com.news.service.AiService;
import com.news.vo.AiChatVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

/**
 * AI 控制器
 * 处理 AI 相关接口，需要登录认证
 * 路径以 /api/ai 开头，会被 JWT 拦截器检查
 */
@RestController
@RequestMapping("/api/ai")
public class AiController {

    @Autowired
    private AiService aiService;

    /**
     * AI 对话
     * POST /api/ai/chat
     * 限制：每用户每分钟最多10次（按用户限制）
     */
    @PostMapping("/chat")
    @RateLimit(limit = 10, period = 60, type = RateLimit.LimitType.USER)
    public Result<AiChatVO> chat(@RequestBody AiChatDTO dto, HttpServletRequest request) {
        try {
            // 可以从 request 获取用户信息 (如需记录日志)
            Long userId = (Long) request.getAttribute("userId");
            String username = (String) request.getAttribute("username");

            AiChatVO result = aiService.chat(dto);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("AI 服务暂时不可用: " + e.getMessage());
        }
    }

    /**
     * 总结新闻
     * POST /api/ai/summarize
     * 限制：每用户每分钟最多5次（按用户限制）
     */
    @PostMapping("/summarize")
    @RateLimit(limit = 5, period = 60, type = RateLimit.LimitType.USER)
    public Result<AiChatVO> summarize(@RequestBody AiChatDTO dto, HttpServletRequest request) {
        try {
            String content = dto.getNewsContent();
            if (content == null || content.isEmpty()) {
                return Result.error("新闻内容不能为空");
            }

            AiChatVO result = aiService.summarize(content);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("AI 服务暂时不可用: " + e.getMessage());
        }
    }
}
