package com.news.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 新闻分类实体类
 * 支持二级分类: parent_id 为 null 表示一级分类
 */
@Data
@TableName("category")
public class Category {

    /** 分类ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 分类名称 */
    private String name;

    /** 父分类ID，null表示一级分类 */
    private Long parentId;

    /** 排序顺序 */
    private Integer sortOrder;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    // ==================== 非数据库字段 ====================

    /** 子分类列表 (用于树形结构) */
    @TableField(exist = false)  // 标记为非数据库字段
    private List<Category> children;
}
