package com.news.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.news.dto.AiChatDTO;
import com.news.service.AiService;
import com.news.vo.AiChatVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AI 服务实现类
 * 支持 OpenAI 兼容格式 API (Kimi/DeepSeek/OpenAI)
 */
@Service
public class AiServiceImpl implements AiService {

    private static final Logger log = LoggerFactory.getLogger(AiServiceImpl.class);

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${ai.api-key:}")
    private String apiKey;

    @Value("${ai.base-url:https://api.moonshot.cn/v1}")
    private String baseUrl;

    @Value("${ai.model:moonshot-v1-8k}")
    private String model;

    private static final String SYSTEM_PROMPT = "你是新闻发布系统的AI助手，专门帮助用户搜索、总结和解读新闻内容。请用简洁专业的语言回答用户的问题。";

    @Override
    public AiChatVO chat(AiChatDTO dto) {
        String userMessage = dto.getMessage();

        // 如果提供了新闻内容，添加到上下文
        if (dto.getNewsContent() != null && !dto.getNewsContent().isEmpty()) {
            userMessage = "以下是一篇新闻内容：\n\n" + dto.getNewsContent() + "\n\n用户问题：" + dto.getMessage();
        }

        return callAiApi(userMessage);
    }

    @Override
    public AiChatVO summarize(String newsContent) {
        String prompt = "请对以下新闻内容进行简要总结，提炼出核心要点：\n\n" + newsContent;
        return callAiApi(prompt);
    }

    /**
     * 调用 AI API
     */
    private AiChatVO callAiApi(String userMessage) {
        AiChatVO result = new AiChatVO();
        result.setModel(model);

        // 检查 API Key
        if (apiKey == null || apiKey.isEmpty()) {
            result.setReply("AI 服务未配置，请联系管理员设置 API Key");
            return result;
        }

        try {
            // 构建请求体
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", model);
            requestBody.put("temperature", 0.7);

            // 构建消息列表
            List<Map<String, String>> messages = new ArrayList<>();

            Map<String, String> systemMsg = new HashMap<>();
            systemMsg.put("role", "system");
            systemMsg.put("content", SYSTEM_PROMPT);
            messages.add(systemMsg);

            Map<String, String> userMsg = new HashMap<>();
            userMsg.put("role", "user");
            userMsg.put("content", userMessage);
            messages.add(userMsg);

            requestBody.put("messages", messages);

            // 设置请求头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            // 发送请求
            String url = baseUrl + "/chat/completions";
            ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                String.class
            );

            // 解析响应
            if (response.getStatusCode() == HttpStatus.OK) {
                JsonNode root = objectMapper.readTree(response.getBody());

                // 获取回复内容
                JsonNode choices = root.get("choices");
                if (choices != null && choices.isArray() && choices.size() > 0) {
                    JsonNode message = choices.get(0).get("message");
                    if (message != null && message.has("content")) {
                        result.setReply(message.get("content").asText());
                    }
                }

                // 获取 token 使用量
                JsonNode usage = root.get("usage");
                if (usage != null && usage.has("total_tokens")) {
                    result.setTokens(usage.get("total_tokens").asInt());
                }
            } else {
                result.setReply("AI 服务响应异常: " + response.getStatusCode());
            }

        } catch (Exception e) {
            log.error("调用 AI API 失败: {}", e.getMessage());
            result.setReply("AI 服务暂时不可用，请稍后再试");
        }

        return result;
    }
}
