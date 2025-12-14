package com.news.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 用户实体类
 * 对应数据库 user 表
 */
@Data
@TableName("user")  // 指定表名
public class User {

    /** 用户ID */
    @TableId(type = IdType.AUTO)  // 主键自增
    private Long id;

    /** 用户名 */
    private String username;

    /** 密码 (BCrypt加密) */
    private String password;

    /** 邮箱 */
    private String email;

    /** 头像URL */
    private String avatar;

    /**
     * 角色
     * 1=普通用户, 2=编辑, 3=总编, 4=管理员
     */
    private Integer role;

    /**
     * 状态
     * 0=禁用, 1=正常
     */
    private Integer status;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)  // 插入时自动填充
    private LocalDateTime createTime;

    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)  // 插入和更新时自动填充
    private LocalDateTime updateTime;

    // ==================== 角色常量 ====================

    public static final int ROLE_USER = 1;      // 普通用户
    public static final int ROLE_EDITOR = 2;    // 编辑
    public static final int ROLE_CHIEF = 3;     // 总编
    public static final int ROLE_ADMIN = 4;     // 管理员

    // ==================== 状态常量 ====================

    public static final int STATUS_DISABLED = 0;  // 禁用
    public static final int STATUS_ENABLED = 1;   // 正常

    /**
     * 获取角色名称
     */
    public String getRoleName() {
        return switch (this.role) {
            case ROLE_USER -> "普通用户";
            case ROLE_EDITOR -> "编辑";
            case ROLE_CHIEF -> "总编";
            case ROLE_ADMIN -> "管理员";
            default -> "未知";
        };
    }
}
