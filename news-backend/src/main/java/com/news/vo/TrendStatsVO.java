package com.news.vo;

import lombok.Data;

/**
 * 趋势统计 VO
 */
@Data
public class TrendStatsVO {

    /** 日期 */
    private String date;

    /** 数量 */
    private Long count;
}
