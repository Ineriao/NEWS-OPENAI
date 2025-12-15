package com.news.vo;

import lombok.Data;

/**
 * Dashboard 统计数据 VO
 */
@Data
public class DashboardStatsVO {

    /** 新闻总数 */
    private Long newsCount;

    /** 用户总数 */
    private Long userCount;

    /** 评论总数 */
    private Long commentCount;

    /** 待审核新闻数 */
    private Long pendingCount;

    /** 已发布新闻数 */
    private Long publishedCount;
}
