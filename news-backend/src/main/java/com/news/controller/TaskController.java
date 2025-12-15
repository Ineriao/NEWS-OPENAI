package com.news.controller;

import com.news.common.Result;
import com.news.service.ExternalNewsService;
import com.news.service.HotSearchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * 定时任务管理控制器
 * 提供手动触发和状态查询功能
 */
@RestController
@RequestMapping("/api/admin/tasks")
public class TaskController {

    private static final Logger logger = LoggerFactory.getLogger(TaskController.class);
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Autowired
    private HotSearchService hotSearchService;

    @Autowired
    private ExternalNewsService externalNewsService;

    @Value("${scheduler.enabled:true}")
    private boolean schedulerEnabled;

    @Value("${scheduler.hot-search.cron:0 0/30 * * * ?}")
    private String hotSearchCron;

    @Value("${scheduler.external-news.cron:0 0 * * * ?}")
    private String externalNewsCron;

    // 记录最后执行时间
    private static LocalDateTime lastHotSearchRefresh;
    private static LocalDateTime lastExternalNewsRefresh;

    /**
     * 获取定时任务状态
     * GET /api/admin/tasks/status
     */
    @GetMapping("/status")
    public Result<Map<String, Object>> getTaskStatus(HttpServletRequest request) {
        Integer role = (Integer) request.getAttribute("role");
        if (role == null || role < 4) {
            return Result.forbidden();
        }

        Map<String, Object> status = new HashMap<>();
        status.put("schedulerEnabled", schedulerEnabled);

        Map<String, Object> hotSearch = new HashMap<>();
        hotSearch.put("cron", hotSearchCron);
        hotSearch.put("description", "每30分钟刷新一次");
        hotSearch.put("lastRefresh", lastHotSearchRefresh != null ? lastHotSearchRefresh.format(formatter) : "未执行");
        status.put("hotSearch", hotSearch);

        Map<String, Object> externalNews = new HashMap<>();
        externalNews.put("cron", externalNewsCron);
        externalNews.put("description", "每小时整点刷新");
        externalNews.put("lastRefresh", lastExternalNewsRefresh != null ? lastExternalNewsRefresh.format(formatter) : "未执行");
        status.put("externalNews", externalNews);

        return Result.success(status);
    }

    /**
     * 手动刷新热搜数据
     * POST /api/admin/tasks/refresh/hot-search
     */
    @PostMapping("/refresh/hot-search")
    public Result<String> refreshHotSearch(HttpServletRequest request) {
        Integer role = (Integer) request.getAttribute("role");
        if (role == null || role < 4) {
            return Result.forbidden();
        }

        logger.info("[手动任务] 管理员触发热搜数据刷新");

        try {
            long startTime = System.currentTimeMillis();
            hotSearchService.refreshAndSaveAll();
            lastHotSearchRefresh = LocalDateTime.now();
            long duration = System.currentTimeMillis() - startTime;

            String message = String.format("热搜数据刷新完成，耗时 %d ms", duration);
            logger.info("[手动任务] {}", message);
            return Result.success(message, lastHotSearchRefresh.format(formatter));
        } catch (Exception e) {
            logger.error("[手动任务] 热搜数据刷新失败: {}", e.getMessage(), e);
            return Result.error("刷新失败: " + e.getMessage());
        }
    }

    /**
     * 手动刷新外部新闻数据
     * POST /api/admin/tasks/refresh/external-news
     */
    @PostMapping("/refresh/external-news")
    public Result<String> refreshExternalNews(HttpServletRequest request) {
        Integer role = (Integer) request.getAttribute("role");
        if (role == null || role < 4) {
            return Result.forbidden();
        }

        logger.info("[手动任务] 管理员触发外部新闻数据刷新");

        try {
            long startTime = System.currentTimeMillis();
            externalNewsService.refreshAndSaveAll();
            lastExternalNewsRefresh = LocalDateTime.now();
            long duration = System.currentTimeMillis() - startTime;

            String message = String.format("外部新闻数据刷新完成，耗时 %d ms", duration);
            logger.info("[手动任务] {}", message);
            return Result.success(message, lastExternalNewsRefresh.format(formatter));
        } catch (Exception e) {
            logger.error("[手动任务] 外部新闻数据刷新失败: {}", e.getMessage(), e);
            return Result.error("刷新失败: " + e.getMessage());
        }
    }

    /**
     * 手动刷新所有数据
     * POST /api/admin/tasks/refresh/all
     */
    @PostMapping("/refresh/all")
    public Result<Map<String, String>> refreshAll(HttpServletRequest request) {
        Integer role = (Integer) request.getAttribute("role");
        if (role == null || role < 4) {
            return Result.forbidden();
        }

        logger.info("[手动任务] 管理员触发全部数据刷新");

        Map<String, String> results = new HashMap<>();
        long totalStart = System.currentTimeMillis();

        // 刷新热搜
        try {
            long start = System.currentTimeMillis();
            hotSearchService.refreshAndSaveAll();
            lastHotSearchRefresh = LocalDateTime.now();
            results.put("hotSearch", String.format("成功 (%d ms)", System.currentTimeMillis() - start));
        } catch (Exception e) {
            results.put("hotSearch", "失败: " + e.getMessage());
            logger.error("[手动任务] 热搜刷新失败: {}", e.getMessage());
        }

        // 刷新外部新闻
        try {
            long start = System.currentTimeMillis();
            externalNewsService.refreshAndSaveAll();
            lastExternalNewsRefresh = LocalDateTime.now();
            results.put("externalNews", String.format("成功 (%d ms)", System.currentTimeMillis() - start));
        } catch (Exception e) {
            results.put("externalNews", "失败: " + e.getMessage());
            logger.error("[手动任务] 外部新闻刷新失败: {}", e.getMessage());
        }

        results.put("totalTime", String.format("%d ms", System.currentTimeMillis() - totalStart));
        logger.info("[手动任务] 全部数据刷新完成，总耗时 {} ms", System.currentTimeMillis() - totalStart);

        return Result.success("刷新完成", results);
    }

    /**
     * 更新最后刷新时间（供 ScheduledTasks 调用）
     */
    public static void updateLastHotSearchRefresh() {
        lastHotSearchRefresh = LocalDateTime.now();
    }

    public static void updateLastExternalNewsRefresh() {
        lastExternalNewsRefresh = LocalDateTime.now();
    }
}
