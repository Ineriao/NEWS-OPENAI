package com.news.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.news.entity.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户数据访问层
 * 继承 BaseMapper 后自动拥有:
 * - insert(User)
 * - deleteById(Long)
 * - updateById(User)
 * - selectById(Long)
 * - selectList(Wrapper)
 * - selectPage(Page, Wrapper)
 * 等方法
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

    // 基础 CRUD 由 BaseMapper 提供
    // 如果有复杂查询需求，可以在这里添加自定义方法
}
