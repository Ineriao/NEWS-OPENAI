package com.news.controller;

import com.news.common.RateLimit;
import com.news.common.Result;
import com.news.dto.LoginDTO;
import com.news.dto.RegisterDTO;
import com.news.entity.User;
import com.news.service.UserService;
import com.news.vo.LoginVO;
import com.news.vo.UserVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器
 * 处理登录、注册等不需要认证的接口
 * 路径以 /api/auth 开头，会被 JWT 拦截器排除
 */
@Tag(name = "用户认证", description = "登录、注册、用户名检查")
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    /**
     * 用户注册
     * POST /api/auth/register
     * 限制：同一IP每分钟最多5次
     */
    @Operation(summary = "用户注册", description = "注册新用户，同一IP每分钟最多5次")
    @PostMapping("/register")
    @RateLimit(limit = 5, period = 60)
    public Result<UserVO> register(@RequestBody RegisterDTO dto) {
        try {
            User user = userService.register(dto);
            return Result.success("注册成功", UserVO.fromUser(user));
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 用户登录
     * POST /api/auth/login
     * 限制：同一IP每分钟最多10次（防止暴力破解）
     */
    @Operation(summary = "用户登录", description = "登录获取 JWT Token")
    @PostMapping("/login")
    @RateLimit(limit = 10, period = 60)
    public Result<LoginVO> login(@RequestBody LoginDTO dto) {
        try {
            LoginVO loginVO = userService.login(dto);
            return Result.success("登录成功", loginVO);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 检查用户名是否可用
     * GET /api/auth/check-username?username=xxx
     * 限制：同一IP每分钟最多30次
     */
    @Operation(summary = "检查用户名", description = "检查用户名是否已被注册")
    @GetMapping("/check-username")
    @RateLimit(limit = 30, period = 60)
    public Result<Boolean> checkUsername(@RequestParam String username) {
        boolean exists = userService.existsByUsername(username);
        if (exists) {
            return Result.error("用户名已存在");
        }
        return Result.success("用户名可用", true);
    }
}
