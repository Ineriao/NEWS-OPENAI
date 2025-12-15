package com.news.controller;

import com.news.common.Result;
import com.news.entity.User;
import com.news.service.StatsService;
import com.news.vo.CategoryStatsVO;
import com.news.vo.DashboardStatsVO;
import com.news.vo.TrendStatsVO;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 统计控制器
 * 需要编辑及以上权限
 */
@RestController
@RequestMapping("/api/stats")
public class StatsController {

    @Autowired
    private StatsService statsService;

    /**
     * 获取 Dashboard 统计数据
     * GET /api/stats/dashboard
     */
    @GetMapping("/dashboard")
    public Result<DashboardStatsVO> getDashboardStats(HttpServletRequest request) {
        Integer role = (Integer) request.getAttribute("role");
        if (role == null || role < User.ROLE_EDITOR) {
            return Result.forbidden();
        }

        DashboardStatsVO stats = statsService.getDashboardStats();
        return Result.success(stats);
    }

    /**
     * 获取分类统计
     * GET /api/stats/category
     */
    @GetMapping("/category")
    public Result<List<CategoryStatsVO>> getCategoryStats(HttpServletRequest request) {
        Integer role = (Integer) request.getAttribute("role");
        if (role == null || role < User.ROLE_EDITOR) {
            return Result.forbidden();
        }

        List<CategoryStatsVO> stats = statsService.getCategoryStats();
        return Result.success(stats);
    }

    /**
     * 获取发布趋势（近7天）
     * GET /api/stats/trend
     */
    @GetMapping("/trend")
    public Result<List<TrendStatsVO>> getTrendStats(HttpServletRequest request) {
        Integer role = (Integer) request.getAttribute("role");
        if (role == null || role < User.ROLE_EDITOR) {
            return Result.forbidden();
        }

        List<TrendStatsVO> stats = statsService.getTrendStats();
        return Result.success(stats);
    }
}
