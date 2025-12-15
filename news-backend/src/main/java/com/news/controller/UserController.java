package com.news.controller;

import com.news.common.Result;
import com.news.dto.UserUpdateDTO;
import com.news.entity.User;
import com.news.service.UserService;
import com.news.vo.UserVO;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 用户控制器
 * 处理需要认证的用户相关接口
 */
@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    /**
     * 获取当前登录用户信息
     * GET /api/user/info
     */
    @GetMapping("/info")
    public Result<UserVO> getCurrentUser(HttpServletRequest request) {
        // 从 JWT 拦截器中获取用户ID
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.unauthorized();
        }

        User user = userService.getById(userId);
        if (user == null) {
            return Result.error("用户不存在");
        }

        return Result.success(UserVO.fromUser(user));
    }

    /**
     * 根据ID获取用户信息
     * GET /api/user/{id}
     */
    @GetMapping("/{id}")
    public Result<UserVO> getUserById(@PathVariable Long id) {
        User user = userService.getById(id);
        if (user == null) {
            return Result.notFound("用户不存在");
        }
        return Result.success(UserVO.fromUser(user));
    }

    /**
     * 更新用户信息
     * PUT /api/user/update
     */
    @PutMapping("/update")
    public Result<UserVO> updateUser(HttpServletRequest request,
                                      @RequestBody UserUpdateDTO dto) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.unauthorized();
        }

        User user = userService.getById(userId);
        if (user == null) {
            return Result.error("用户不存在");
        }

        // 更新邮箱
        if (dto.getEmail() != null && !dto.getEmail().trim().isEmpty()) {
            // 验证邮箱格式
            if (!dto.getEmail().matches("^[\\w-]+(\\.[\\w-]+)*@[\\w-]+(\\.[\\w-]+)+$")) {
                return Result.error("邮箱格式不正确");
            }
            user.setEmail(dto.getEmail().trim());
        }

        // 更新头像
        if (dto.getAvatar() != null) {
            user.setAvatar(dto.getAvatar().trim());
        }

        userService.updateById(user);
        return Result.success("更新成功", UserVO.fromUser(user));
    }

    /**
     * 修改密码
     * PUT /api/user/password
     */
    @PutMapping("/password")
    public Result<Void> changePassword(HttpServletRequest request,
                                        @RequestBody Map<String, String> params) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.unauthorized();
        }

        String oldPassword = params.get("oldPassword");
        String newPassword = params.get("newPassword");

        if (oldPassword == null || newPassword == null) {
            return Result.error("参数不完整");
        }

        if (newPassword.length() < 6) {
            return Result.error("新密码长度不能少于6位");
        }

        User user = userService.getById(userId);
        if (user == null) {
            return Result.error("用户不存在");
        }

        // 验证旧密码
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            return Result.error("当前密码不正确");
        }

        // 更新密码
        user.setPassword(passwordEncoder.encode(newPassword));
        userService.updateById(user);

        return Result.success("密码修改成功", null);
    }
}
