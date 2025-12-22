package com.news.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.news.common.Result;
import com.news.dao.UserMapper;
import com.news.entity.User;
import com.news.service.UserService;
import com.news.vo.PageVO;
import com.news.vo.UserVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 管理员用户管理控制器
 * 处理用户列表、角色修改、状态修改等管理功能
 */
@RestController
@RequestMapping("/api/users")
public class AdminUserController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserMapper userMapper;

    /**
     * 获取用户列表（分页）
     * GET /api/users
     */
    @GetMapping
    public Result<PageVO<UserVO>> getUserList(
            HttpServletRequest request,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Integer role = (Integer) request.getAttribute("role");
        if (role == null || role < 4) {
            return Result.forbidden("仅管理员可访问");
        }

        Page<User> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(User::getCreateTime);
        Page<User> result = userMapper.selectPage(pageParam, wrapper);

        List<UserVO> voList = result.getRecords().stream()
                .map(UserVO::fromUser)
                .collect(Collectors.toList());

        return Result.success(PageVO.of(voList, result.getTotal(), result.getPages(),
                result.getCurrent(), result.getSize()));
    }

    /**
     * 修改用户角色
     * PUT /api/users/{id}/role
     */
    @PutMapping("/{id}/role")
    public Result<Void> updateRole(
            HttpServletRequest request,
            @PathVariable Long id,
            @RequestBody Map<String, Integer> params) {
        Integer operatorRole = (Integer) request.getAttribute("role");
        if (operatorRole == null || operatorRole < 4) {
            return Result.forbidden("仅管理员可修改角色");
        }

        Integer newRole = params.get("role");
        if (newRole == null || newRole < 1 || newRole > 4) {
            return Result.error("无效的角色值");
        }

        User user = userService.getById(id);
        if (user == null) {
            return Result.notFound("用户不存在");
        }

        user.setRole(newRole);
        userService.updateById(user);
        return Result.success("角色修改成功", null);
    }

    /**
     * 修改用户状态
     * PUT /api/users/{id}/status
     */
    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(
            HttpServletRequest request,
            @PathVariable Long id,
            @RequestBody Map<String, Integer> params) {
        Integer operatorRole = (Integer) request.getAttribute("role");
        if (operatorRole == null || operatorRole < 4) {
            return Result.forbidden("仅管理员可修改状态");
        }

        Integer newStatus = params.get("status");
        if (newStatus == null || (newStatus != 0 && newStatus != 1)) {
            return Result.error("无效的状态值");
        }

        User user = userService.getById(id);
        if (user == null) {
            return Result.notFound("用户不存在");
        }

        user.setStatus(newStatus);
        userService.updateById(user);
        return Result.success(newStatus == 1 ? "用户已启用" : "用户已禁用", null);
    }
}
