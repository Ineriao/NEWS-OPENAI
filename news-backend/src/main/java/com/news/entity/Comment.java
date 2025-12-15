package com.news.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 评论实体类
 * 支持回复: parent_id 为 null 表示一级评论
 */
@Data
@TableName("comment")
public class Comment {

    /** 评论ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 新闻ID */
    private Long newsId;

    /** 用户ID */
    private Long userId;

    /** 评论内容 */
    private String content;

    /** 父评论ID，null表示一级评论 */
    private Long parentId;

    /**
     * 状态
     * 0=隐藏, 1=显示
     */
    private Integer status;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    // ==================== 状态常量 ====================

    public static final int STATUS_HIDDEN = 0;    // 隐藏
    public static final int STATUS_VISIBLE = 1;   // 显示

    // ==================== 非数据库字段 ====================

    /** 用户名 */
    @TableField(exist = false)
    private String username;

    /** 用户头像 */
    @TableField(exist = false)
    private String userAvatar;

    /** 回复列表 (二级评论) */
    @TableField(exist = false)
    private List<Comment> replies;

    /** 被回复的用户名 (如果是回复评论) */
    @TableField(exist = false)
    private String replyToUsername;

    /** 新闻标题 (用于用户评论历史) */
    @TableField(exist = false)
    private String newsTitle;
}
