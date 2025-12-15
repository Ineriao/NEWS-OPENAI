package com.news.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.news.common.Result;
import com.news.entity.News;
import com.news.service.NewsCollectionService;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 新闻收藏控制器
 */
@RestController
@RequestMapping("/api")
public class NewsCollectionController {

    @Autowired
    private NewsCollectionService newsCollectionService;

    /**
     * 收藏/取消收藏 (toggle)
     * POST /api/news/{newsId}/collect
     */
    @PostMapping("/news/{newsId}/collect")
    public Result<Map<String, Object>> toggleCollection(HttpServletRequest request,
                                                         @PathVariable Long newsId) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.unauthorized();
        }

        try {
            Map<String, Object> result = newsCollectionService.toggleCollection(newsId, userId);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("操作失败: " + e.getMessage());
        }
    }

    /**
     * 获取收藏状态和数量
     * GET /api/public/news/{newsId}/collect
     * 公开接口，但如果用户已登录会返回是否已收藏
     */
    @GetMapping("/public/news/{newsId}/collect")
    public Result<Map<String, Object>> getCollectionStatus(HttpServletRequest request,
                                                            @PathVariable Long newsId) {
        Long userId = (Long) request.getAttribute("userId");
        Map<String, Object> result = newsCollectionService.getCollectionStatus(newsId, userId);
        return Result.success(result);
    }

    /**
     * 获取当前用户的收藏列表（分页）
     * GET /api/user/collections
     */
    @GetMapping("/user/collections")
    public Result<IPage<News>> getUserCollections(HttpServletRequest request,
                                                   @RequestParam(defaultValue = "1") int page,
                                                   @RequestParam(defaultValue = "10") int size) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.unauthorized();
        }

        try {
            IPage<News> collections = newsCollectionService.getUserCollections(userId, page, size);
            return Result.success(collections);
        } catch (Exception e) {
            return Result.error("获取收藏列表失败: " + e.getMessage());
        }
    }
}
