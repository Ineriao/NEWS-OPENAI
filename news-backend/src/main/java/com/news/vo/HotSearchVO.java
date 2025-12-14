package com.news.vo;

import lombok.Data;

/**
 * 热搜条目 VO
 */
@Data
public class HotSearchVO {

    /** 排名 */
    private Integer rank;

    /** 标题 */
    private String title;

    /** 热度值 */
    private String hot;

    /** 链接 */
    private String url;

    /** 来源平台 */
    private String source;
}
