package com.news.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.news.common.CacheConstants;
import com.news.dao.CategoryMapper;
import com.news.dto.CategoryDTO;
import com.news.entity.Category;
import com.news.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 新闻分类服务实现类
 */
@Service
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    private CategoryMapper categoryMapper;

    @Override
    @Cacheable(value = CacheConstants.CACHE_CATEGORY, key = "'tree'")
    public List<Category> getTreeList() {
        // 1. 查询所有分类
        List<Category> allCategories = getFlatList();

        // 2. 构建树形结构
        return buildTree(allCategories);
    }

    /**
     * 构建树形结构
     * 核心算法：先按 parentId 分组，然后递归构建
     */
    private List<Category> buildTree(List<Category> allCategories) {
        // 按 parentId 分组
        Map<Long, List<Category>> parentIdMap = allCategories.stream()
                .filter(c -> c.getParentId() != null)
                .collect(Collectors.groupingBy(Category::getParentId));

        // 获取一级分类（parentId 为 null）
        List<Category> rootCategories = allCategories.stream()
                .filter(c -> c.getParentId() == null)
                .collect(Collectors.toList());

        // 为每个一级分类设置子分类
        for (Category root : rootCategories) {
            List<Category> children = parentIdMap.getOrDefault(root.getId(), new ArrayList<>());
            root.setChildren(children);
        }

        return rootCategories;
    }

    @Override
    public List<Category> getFlatList() {
        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(Category::getSortOrder)
               .orderByAsc(Category::getId);
        return categoryMapper.selectList(wrapper);
    }

    @Override
    public List<Category> getTopCategories() {
        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
        wrapper.isNull(Category::getParentId)
               .orderByAsc(Category::getSortOrder);
        return categoryMapper.selectList(wrapper);
    }

    @Override
    public List<Category> getByParentId(Long parentId) {
        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Category::getParentId, parentId)
               .orderByAsc(Category::getSortOrder);
        return categoryMapper.selectList(wrapper);
    }

    @Override
    public Category getById(Long id) {
        return categoryMapper.selectById(id);
    }

    @Override
    @Transactional
    @CacheEvict(value = CacheConstants.CACHE_CATEGORY, allEntries = true)
    public Category create(CategoryDTO dto) {
        // 1. 参数校验
        if (dto.getName() == null || dto.getName().trim().isEmpty()) {
            throw new RuntimeException("分类名称不能为空");
        }

        // 2. 如果有父分类，检查父分类是否存在
        if (dto.getParentId() != null) {
            Category parent = categoryMapper.selectById(dto.getParentId());
            if (parent == null) {
                throw new RuntimeException("父分类不存在");
            }
            // 检查父分类是否为一级分类（只支持二级）
            if (parent.getParentId() != null) {
                throw new RuntimeException("只支持二级分类");
            }
        }

        // 3. 创建分类
        Category category = new Category();
        category.setName(dto.getName().trim());
        category.setParentId(dto.getParentId());
        category.setSortOrder(dto.getSortOrder() != null ? dto.getSortOrder() : 0);

        categoryMapper.insert(category);
        return category;
    }

    @Override
    @Transactional
    @CacheEvict(value = CacheConstants.CACHE_CATEGORY, allEntries = true)
    public Category update(Long id, CategoryDTO dto) {
        // 1. 检查分类是否存在
        Category category = categoryMapper.selectById(id);
        if (category == null) {
            throw new RuntimeException("分类不存在");
        }

        // 2. 参数校验
        if (dto.getName() == null || dto.getName().trim().isEmpty()) {
            throw new RuntimeException("分类名称不能为空");
        }

        // 3. 如果修改了父分类
        if (dto.getParentId() != null && !dto.getParentId().equals(category.getParentId())) {
            // 不能将自己设为自己的父分类
            if (dto.getParentId().equals(id)) {
                throw new RuntimeException("不能将自己设为父分类");
            }
            // 检查父分类是否存在
            Category parent = categoryMapper.selectById(dto.getParentId());
            if (parent == null) {
                throw new RuntimeException("父分类不存在");
            }
            // 只支持二级分类
            if (parent.getParentId() != null) {
                throw new RuntimeException("只支持二级分类");
            }
        }

        // 4. 更新分类
        category.setName(dto.getName().trim());
        category.setParentId(dto.getParentId());
        if (dto.getSortOrder() != null) {
            category.setSortOrder(dto.getSortOrder());
        }

        categoryMapper.updateById(category);
        return category;
    }

    @Override
    @Transactional
    @CacheEvict(value = CacheConstants.CACHE_CATEGORY, allEntries = true)
    public void delete(Long id) {
        // 1. 检查分类是否存在
        Category category = categoryMapper.selectById(id);
        if (category == null) {
            throw new RuntimeException("分类不存在");
        }

        // 2. 检查是否有子分类
        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Category::getParentId, id);
        Long childCount = categoryMapper.selectCount(wrapper);
        if (childCount > 0) {
            throw new RuntimeException("该分类下有子分类，无法删除");
        }

        // 3. TODO: 检查是否有新闻使用该分类（后续实现）

        // 4. 删除分类
        categoryMapper.deleteById(id);
    }

    @Override
    public boolean existsById(Long id) {
        return categoryMapper.selectById(id) != null;
    }
}
