package com.news.service;

import com.news.dto.CategoryDTO;
import com.news.entity.Category;
import java.util.List;

/**
 * 新闻分类服务接口
 */
public interface CategoryService {

    /**
     * 获取所有分类（树形结构）
     * @return 一级分类列表，每个一级分类包含其子分类
     */
    List<Category> getTreeList();

    /**
     * 获取所有分类（平铺列表）
     * @return 所有分类的平铺列表
     */
    List<Category> getFlatList();

    /**
     * 获取一级分类列表
     * @return 一级分类列表
     */
    List<Category> getTopCategories();

    /**
     * 根据父分类ID获取子分类
     * @param parentId 父分类ID
     * @return 子分类列表
     */
    List<Category> getByParentId(Long parentId);

    /**
     * 根据ID获取分类
     * @param id 分类ID
     * @return 分类信息
     */
    Category getById(Long id);

    /**
     * 创建分类
     * @param dto 分类信息
     * @return 创建的分类
     */
    Category create(CategoryDTO dto);

    /**
     * 更新分类
     * @param id 分类ID
     * @param dto 分类信息
     * @return 更新后的分类
     */
    Category update(Long id, CategoryDTO dto);

    /**
     * 删除分类
     * @param id 分类ID
     */
    void delete(Long id);

    /**
     * 检查分类是否存在
     * @param id 分类ID
     * @return 是否存在
     */
    boolean existsById(Long id);
}
