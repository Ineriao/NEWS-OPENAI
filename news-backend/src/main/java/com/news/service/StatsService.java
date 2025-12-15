package com.news.service;

import com.news.vo.CategoryStatsVO;
import com.news.vo.DashboardStatsVO;
import com.news.vo.TrendStatsVO;

import java.util.List;

/**
 * 统计服务接口
 */
public interface StatsService {

    /**
     * 获取 Dashboard 统计数据
     */
    DashboardStatsVO getDashboardStats();

    /**
     * 获取分类统计
     */
    List<CategoryStatsVO> getCategoryStats();

    /**
     * 获取近期发布趋势（近7天）
     */
    List<TrendStatsVO> getTrendStats();
}
