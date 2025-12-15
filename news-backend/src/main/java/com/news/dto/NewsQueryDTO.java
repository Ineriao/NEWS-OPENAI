package com.news.dto;

import lombok.Data;

/**
 * 新闻查询 DTO
 */
@Data
public class NewsQueryDTO {

    /** 状态 */
    private Integer status;

    /** 分类ID */
    private Long categoryId;

    /** 作者ID */
    private Long authorId;

    /** 关键词（标题/摘要搜索） */
    private String keyword;

    /** 当前页码（默认1） */
    private Integer pageNum = 1;

    /** 每页条数（默认10） */
    private Integer pageSize = 10;

    /** 排序字段（publishTime, viewCount） */
    private String sortBy;

    /** 排序方向（asc, desc，默认desc） */
    private String sortOrder = "desc";
}
