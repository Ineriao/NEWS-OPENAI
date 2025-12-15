package com.news.controller;

import com.news.common.Result;
import com.news.service.NewsLikeService;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 新闻点赞控制器
 */
@RestController
@RequestMapping("/api")
public class NewsLikeController {

    @Autowired
    private NewsLikeService newsLikeService;

    /**
     * 点赞/取消点赞 (toggle)
     * POST /api/news/{newsId}/like
     */
    @PostMapping("/news/{newsId}/like")
    public Result<Map<String, Object>> toggleLike(HttpServletRequest request,
                                                   @PathVariable Long newsId) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.unauthorized();
        }

        try {
            Map<String, Object> result = newsLikeService.toggleLike(newsId, userId);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("操作失败: " + e.getMessage());
        }
    }

    /**
     * 获取点赞状态和数量
     * GET /api/news/{newsId}/like
     * 公开接口，但如果用户已登录会返回是否已点赞
     */
    @GetMapping("/public/news/{newsId}/like")
    public Result<Map<String, Object>> getLikeStatus(HttpServletRequest request,
                                                      @PathVariable Long newsId) {
        Long userId = (Long) request.getAttribute("userId");
        Map<String, Object> result = newsLikeService.getLikeStatus(newsId, userId);
        return Result.success(result);
    }
}
