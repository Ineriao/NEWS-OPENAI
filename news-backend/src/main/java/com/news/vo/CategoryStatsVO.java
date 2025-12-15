package com.news.vo;

import lombok.Data;

/**
 * 分类统计 VO
 */
@Data
public class CategoryStatsVO {

    /** 分类名称 */
    private String name;

    /** 新闻数量 */
    private Long count;
}
