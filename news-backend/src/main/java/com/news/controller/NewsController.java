package com.news.controller;

import com.news.common.Result;
import com.news.dto.NewsDTO;
import com.news.dto.NewsQueryDTO;
import com.news.entity.News;
import com.news.entity.User;
import com.news.service.NewsSearchService;
import com.news.service.NewsService;
import com.news.vo.NewsDetailVO;
import com.news.vo.NewsListVO;
import com.news.vo.PageVO;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 新闻控制器
 */
@RestController
@RequestMapping("/api")
public class NewsController {

    @Autowired
    private NewsService newsService;

    @Autowired
    private NewsSearchService newsSearchService;

    // ==================== 公开接口（游客可访问） ====================

    /**
     * 获取已发布新闻列表（带缓存）
     * GET /api/public/news
     */
    @GetMapping("/public/news")
    public Result<PageVO<NewsListVO>> getPublishedNews(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String sortBy,
            @RequestParam(defaultValue = "desc") String sortOrder) {
        PageVO<NewsListVO> page = newsService.getPublishedPage(
                categoryId, pageNum, pageSize, sortBy, sortOrder);
        return Result.success(page);
    }

    /**
     * 获取新闻详情（公开，增加浏览量）
     * GET /api/public/news/{id}
     */
    @GetMapping("/public/news/{id}")
    public Result<NewsDetailVO> getPublicNewsDetail(@PathVariable Long id) {
        NewsDetailVO detail = newsService.getDetailAndView(id);
        if (detail == null) {
            return Result.notFound("新闻不存在");
        }
        // 只返回已发布的新闻
        if (detail.getStatus() != News.STATUS_PUBLISHED) {
            return Result.notFound("新闻不存在");
        }
        return Result.success(detail);
    }

    /**
     * 搜索新闻（使用 Elasticsearch）
     * GET /api/public/news/search?keyword=xxx
     */
    @GetMapping("/public/news/search")
    public Result<PageVO<NewsListVO>> searchNews(
            @RequestParam String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String sortBy,
            @RequestParam(defaultValue = "desc") String sortOrder) {
        try {
            PageVO<NewsListVO> page = newsSearchService.search(
                    keyword, categoryId, pageNum, pageSize, sortBy, sortOrder);
            return Result.success(page);
        } catch (Exception e) {
            // ES 不可用时降级到 MySQL 查询
            NewsQueryDTO query = new NewsQueryDTO();
            query.setKeyword(keyword);
            query.setStatus(News.STATUS_PUBLISHED);
            query.setCategoryId(categoryId);
            query.setPageNum(pageNum);
            query.setPageSize(pageSize);
            query.setSortBy(sortBy);
            query.setSortOrder(sortOrder);
            PageVO<NewsListVO> page = newsService.getPage(query);
            return Result.success(page);
        }
    }

    // ==================== 编辑接口（需要编辑权限） ====================

    /**
     * 获取新闻管理列表（编辑/总编/管理员）
     * GET /api/news
     */
    @GetMapping("/news")
    public Result<PageVO<NewsListVO>> getNewsList(HttpServletRequest request,
                                                   NewsQueryDTO query) {
        Long userId = (Long) request.getAttribute("userId");
        Integer role = (Integer) request.getAttribute("role");

        // 编辑只能看自己的新闻
        if (role == User.ROLE_EDITOR) {
            query.setAuthorId(userId);
        }
        // 总编和管理员可以看所有新闻

        PageVO<NewsListVO> page = newsService.getPage(query);
        return Result.success(page);
    }

    /**
     * 获取新闻详情（管理用）
     * GET /api/news/{id}
     */
    @GetMapping("/news/{id}")
    public Result<NewsDetailVO> getNewsDetail(HttpServletRequest request,
                                               @PathVariable Long id) {
        Long userId = (Long) request.getAttribute("userId");
        Integer role = (Integer) request.getAttribute("role");

        NewsDetailVO detail = newsService.getDetail(id);
        if (detail == null) {
            return Result.notFound("新闻不存在");
        }

        // 编辑只能查看自己的新闻
        if (role == User.ROLE_EDITOR && !detail.getAuthorId().equals(userId)) {
            return Result.forbidden();
        }

        return Result.success(detail);
    }

    /**
     * 创建新闻
     * POST /api/news
     */
    @PostMapping("/news")
    public Result<News> createNews(HttpServletRequest request,
                                   @RequestBody NewsDTO dto) {
        Long userId = (Long) request.getAttribute("userId");
        Integer role = (Integer) request.getAttribute("role");

        // 只有编辑及以上角色可以创建新闻
        if (role < User.ROLE_EDITOR) {
            return Result.forbidden();
        }

        try {
            News news = newsService.create(dto, userId);
            return Result.success("创建成功", news);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 更新新闻
     * PUT /api/news/{id}
     */
    @PutMapping("/news/{id}")
    public Result<News> updateNews(HttpServletRequest request,
                                   @PathVariable Long id,
                                   @RequestBody NewsDTO dto) {
        Long userId = (Long) request.getAttribute("userId");
        Integer role = (Integer) request.getAttribute("role");

        try {
            News news = newsService.update(id, dto, userId, role);
            return Result.success("更新成功", news);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 删除新闻
     * DELETE /api/news/{id}
     */
    @DeleteMapping("/news/{id}")
    public Result<Void> deleteNews(HttpServletRequest request,
                                   @PathVariable Long id) {
        Long userId = (Long) request.getAttribute("userId");
        Integer role = (Integer) request.getAttribute("role");

        try {
            newsService.delete(id, userId, role);
            return Result.success("删除成功", null);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    // ==================== 状态流转接口 ====================

    /**
     * 提交审核
     * PUT /api/news/{id}/submit
     */
    @PutMapping("/news/{id}/submit")
    public Result<Void> submitForReview(HttpServletRequest request,
                                        @PathVariable Long id) {
        Long userId = (Long) request.getAttribute("userId");

        try {
            newsService.submitForReview(id, userId);
            return Result.success("提交审核成功", null);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 审核通过（总编）
     * PUT /api/news/{id}/approve
     */
    @PutMapping("/news/{id}/approve")
    public Result<Void> approve(HttpServletRequest request,
                                @PathVariable Long id) {
        Long userId = (Long) request.getAttribute("userId");
        Integer role = (Integer) request.getAttribute("role");

        // 只有总编及以上可以审核
        if (role < User.ROLE_CHIEF) {
            return Result.forbidden();
        }

        try {
            newsService.approve(id, userId);
            return Result.success("审核通过", null);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 审核退回（总编）
     * PUT /api/news/{id}/reject
     */
    @PutMapping("/news/{id}/reject")
    public Result<Void> reject(HttpServletRequest request,
                               @PathVariable Long id) {
        Long userId = (Long) request.getAttribute("userId");
        Integer role = (Integer) request.getAttribute("role");

        // 只有总编及以上可以审核
        if (role < User.ROLE_CHIEF) {
            return Result.forbidden();
        }

        try {
            newsService.reject(id, userId);
            return Result.success("已退回修改", null);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 存档
     * PUT /api/news/{id}/archive
     */
    @PutMapping("/news/{id}/archive")
    public Result<Void> archive(HttpServletRequest request,
                                @PathVariable Long id) {
        Long userId = (Long) request.getAttribute("userId");
        Integer role = (Integer) request.getAttribute("role");

        try {
            newsService.archive(id, userId, role);
            return Result.success("存档成功", null);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 全量同步新闻到 ES（管理员）
     * POST /api/admin/news/sync-es
     */
    @PostMapping("/admin/news/sync-es")
    public Result<Void> syncToES(HttpServletRequest request) {
        Integer role = (Integer) request.getAttribute("role");
        if (role < User.ROLE_ADMIN) {
            return Result.forbidden();
        }

        try {
            newsSearchService.syncAll();
            return Result.success("同步完成", null);
        } catch (Exception e) {
            return Result.error("同步失败: " + e.getMessage());
        }
    }
}
