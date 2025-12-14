package com.news.dto;

import lombok.Data;

/**
 * 分类创建/更新 DTO
 */
@Data
public class CategoryDTO {

    /** 分类名称 */
    private String name;

    /** 父分类ID，null表示一级分类 */
    private Long parentId;

    /** 排序顺序 */
    private Integer sortOrder;
}
