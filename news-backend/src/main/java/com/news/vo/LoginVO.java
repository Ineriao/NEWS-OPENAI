package com.news.vo;

import lombok.Data;

/**
 * 登录成功响应 VO
 */
@Data
public class LoginVO {

    /** JWT Token */
    private String token;

    /** 用户ID */
    private Long userId;

    /** 用户名 */
    private String username;

    /** 角色 */
    private Integer role;

    /** 角色名称 */
    private String roleName;

    /** 头像 */
    private String avatar;
}
