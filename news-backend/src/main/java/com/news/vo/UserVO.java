package com.news.vo;

import com.news.entity.User;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 用户信息 VO (不包含密码)
 */
@Data
public class UserVO {

    /** 用户ID */
    private Long id;

    /** 用户名 */
    private String username;

    /** 邮箱 */
    private String email;

    /** 头像 */
    private String avatar;

    /** 角色 */
    private Integer role;

    /** 角色名称 */
    private String roleName;

    /** 状态 */
    private Integer status;

    /** 创建时间 */
    private LocalDateTime createTime;

    /**
     * 从 User 实体转换为 UserVO
     */
    public static UserVO fromUser(User user) {
        if (user == null) {
            return null;
        }
        UserVO vo = new UserVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setEmail(user.getEmail());
        vo.setAvatar(user.getAvatar());
        vo.setRole(user.getRole());
        vo.setRoleName(user.getRoleName());
        vo.setStatus(user.getStatus());
        vo.setCreateTime(user.getCreateTime());
        return vo;
    }
}
