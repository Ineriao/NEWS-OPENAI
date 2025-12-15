package com.news.dto;

import lombok.Data;

/**
 * 用户信息更新 DTO
 */
@Data
public class UserUpdateDTO {

    /** 邮箱 */
    private String email;

    /** 头像URL */
    private String avatar;
}
