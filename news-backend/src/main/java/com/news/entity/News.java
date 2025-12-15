package com.news.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 新闻实体类
 * 对应数据库 news 表
 */
@Data
@TableName("news")
public class News {

    /** 新闻ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 标题 */
    private String title;

    /** 摘要 */
    private String summary;

    /** 内容 (富文本HTML) */
    private String content;

    /** 封面图片URL */
    private String coverImage;

    /** 分类ID */
    private Long categoryId;

    /** 作者ID (编辑) */
    private Long authorId;

    /** 审核人ID (总编) */
    private Long reviewerId;

    /**
     * 状态
     * 0=草稿, 1=待审核, 2=已发布, 3=已存档
     */
    private Integer status;

    /** 浏览次数 */
    private Integer viewCount;

    /** 点赞数 */
    private Integer likeCount;

    /** 收藏数 */
    private Integer collectionCount;

    /** 发布时间 */
    private LocalDateTime publishTime;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    // ==================== 状态常量 ====================

    public static final int STATUS_DRAFT = 0;       // 草稿
    public static final int STATUS_PENDING = 1;     // 待审核
    public static final int STATUS_PUBLISHED = 2;   // 已发布
    public static final int STATUS_ARCHIVED = 3;    // 已存档

    // ==================== 非数据库字段 (用于关联查询) ====================

    /** 分类名称 */
    @TableField(exist = false)
    private String categoryName;

    /** 作者用户名 */
    @TableField(exist = false)
    private String authorName;

    /** 审核人用户名 */
    @TableField(exist = false)
    private String reviewerName;

    /** 当前用户是否已点赞 */
    @TableField(exist = false)
    private Boolean liked;

    /** 当前用户是否已收藏 */
    @TableField(exist = false)
    private Boolean collected;

    /**
     * 获取状态名称
     */
    public String getStatusName() {
        return switch (this.status) {
            case STATUS_DRAFT -> "草稿";
            case STATUS_PENDING -> "待审核";
            case STATUS_PUBLISHED -> "已发布";
            case STATUS_ARCHIVED -> "已存档";
            default -> "未知";
        };
    }
}
