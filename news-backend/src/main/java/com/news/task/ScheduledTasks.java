package com.news.task;

import com.news.controller.TaskController;
import com.news.service.ExternalNewsService;
import com.news.service.HotSearchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 定时任务调度器
 * 负责定时刷新热搜和外部新闻数据
 */
@Component
public class ScheduledTasks {

    private static final Logger logger = LoggerFactory.getLogger(ScheduledTasks.class);
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Autowired
    private HotSearchService hotSearchService;

    @Autowired
    private ExternalNewsService externalNewsService;

    @Value("${scheduler.enabled:true}")
    private boolean schedulerEnabled;

    /**
     * 刷新热搜数据
     * 默认每30分钟执行一次
     * cron: 秒 分 时 日 月 周
     */
    @Scheduled(cron = "${scheduler.hot-search.cron:0 0/30 * * * ?}")
    public void refreshHotSearch() {
        if (!schedulerEnabled) {
            return;
        }

        String startTime = LocalDateTime.now().format(formatter);
        logger.info("[定时任务] 开始刷新热搜数据 - {}", startTime);

        try {
            hotSearchService.refreshAndSaveAll();
            TaskController.updateLastHotSearchRefresh();
            String endTime = LocalDateTime.now().format(formatter);
            logger.info("[定时任务] 热搜数据刷新完成 - {}", endTime);
        } catch (Exception e) {
            logger.error("[定时任务] 热搜数据刷新失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 刷新外部新闻数据
     * 默认每小时执行一次
     */
    @Scheduled(cron = "${scheduler.external-news.cron:0 0 * * * ?}")
    public void refreshExternalNews() {
        if (!schedulerEnabled) {
            return;
        }

        String startTime = LocalDateTime.now().format(formatter);
        logger.info("[定时任务] 开始刷新外部新闻数据 - {}", startTime);

        try {
            externalNewsService.refreshAndSaveAll();
            TaskController.updateLastExternalNewsRefresh();
            String endTime = LocalDateTime.now().format(formatter);
            logger.info("[定时任务] 外部新闻数据刷新完成 - {}", endTime);
        } catch (Exception e) {
            logger.error("[定时任务] 外部新闻数据刷新失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 应用启动后首次刷新数据
     * 延迟10秒执行，确保服务完全启动
     */
    @Scheduled(initialDelay = 10000, fixedDelay = Long.MAX_VALUE)
    public void initialRefresh() {
        if (!schedulerEnabled) {
            logger.info("[定时任务] 定时任务已禁用，跳过初始刷新");
            return;
        }

        logger.info("[定时任务] 应用启动，执行初始数据刷新...");

        try {
            hotSearchService.refreshAndSaveAll();
            TaskController.updateLastHotSearchRefresh();
            logger.info("[定时任务] 初始热搜数据刷新完成");
        } catch (Exception e) {
            logger.error("[定时任务] 初始热搜数据刷新失败: {}", e.getMessage());
        }

        try {
            externalNewsService.refreshAndSaveAll();
            TaskController.updateLastExternalNewsRefresh();
            logger.info("[定时任务] 初始外部新闻数据刷新完成");
        } catch (Exception e) {
            logger.error("[定时任务] 初始外部新闻数据刷新失败: {}", e.getMessage());
        }
    }
}
