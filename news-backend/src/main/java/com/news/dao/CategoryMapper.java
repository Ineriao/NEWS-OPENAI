package com.news.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.news.entity.Category;
import org.apache.ibatis.annotations.Mapper;

/**
 * 新闻分类数据访问层
 */
@Mapper
public interface CategoryMapper extends BaseMapper<Category> {

    // 基础 CRUD 由 BaseMapper 提供
    // 树形结构查询在 Service 层用 Java 代码处理
}
