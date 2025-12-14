package com.news.service;

import com.news.dto.LoginDTO;
import com.news.dto.RegisterDTO;
import com.news.entity.User;
import com.news.vo.LoginVO;

/**
 * 用户服务接口
 */
public interface UserService {

    /**
     * 用户注册
     *
     * @param dto 注册信息
     * @return 注册成功的用户
     */
    User register(RegisterDTO dto);

    /**
     * 用户登录
     *
     * @param dto 登录信息
     * @return 登录结果（包含Token）
     */
    LoginVO login(LoginDTO dto);

    /**
     * 根据ID获取用户
     *
     * @param id 用户ID
     * @return 用户信息
     */
    User getById(Long id);

    /**
     * 根据用户名获取用户
     *
     * @param username 用户名
     * @return 用户信息
     */
    User getByUsername(String username);

    /**
     * 检查用户名是否存在
     *
     * @param username 用户名
     * @return 是否存在
     */
    boolean existsByUsername(String username);

    /**
     * 更新用户信息
     *
     * @param user 用户对象
     * @return 是否成功
     */
    boolean updateById(User user);
}
