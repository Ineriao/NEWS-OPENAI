package com.news.dto;

import lombok.Data;

/**
 * 注册请求 DTO
 */
@Data
public class RegisterDTO {

    /** 用户名 */
    private String username;

    /** 密码 */
    private String password;

    /** 确认密码 */
    private String confirmPassword;

    /** 邮箱 */
    private String email;
}
