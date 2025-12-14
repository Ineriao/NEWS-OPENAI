package com.news.controller;

import com.news.common.Result;
import com.news.dto.CategoryDTO;
import com.news.entity.Category;
import com.news.entity.User;
import com.news.service.CategoryService;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 新闻分类控制器
 */
@RestController
@RequestMapping("/api")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    // ==================== 公开接口（不需要登录） ====================

    /**
     * 获取分类列表（树形结构）
     * GET /api/public/categories
     */
    @GetMapping("/public/categories")
    public Result<List<Category>> getTreeList() {
        List<Category> categories = categoryService.getTreeList();
        return Result.success(categories);
    }

    /**
     * 获取分类列表（平铺）
     * GET /api/public/categories/flat
     */
    @GetMapping("/public/categories/flat")
    public Result<List<Category>> getFlatList() {
        List<Category> categories = categoryService.getFlatList();
        return Result.success(categories);
    }

    /**
     * 获取一级分类
     * GET /api/public/categories/top
     */
    @GetMapping("/public/categories/top")
    public Result<List<Category>> getTopCategories() {
        List<Category> categories = categoryService.getTopCategories();
        return Result.success(categories);
    }

    /**
     * 根据父分类获取子分类
     * GET /api/public/categories/{parentId}/children
     */
    @GetMapping("/public/categories/{parentId}/children")
    public Result<List<Category>> getChildren(@PathVariable Long parentId) {
        List<Category> categories = categoryService.getByParentId(parentId);
        return Result.success(categories);
    }

    /**
     * 获取单个分类详情（公开）
     * GET /api/public/categories/detail/{id}
     */
    @GetMapping("/public/categories/detail/{id}")
    public Result<Category> getPublicById(@PathVariable Long id) {
        Category category = categoryService.getById(id);
        if (category == null) {
            return Result.notFound("分类不存在");
        }
        return Result.success(category);
    }

    // ==================== 管理接口（需要管理员权限） ====================

    /**
     * 创建分类
     * POST /api/categories
     */
    @PostMapping("/categories")
    public Result<Category> create(HttpServletRequest request,
                                   @RequestBody CategoryDTO dto) {
        // 检查权限（只有管理员可以创建分类）
        Integer role = (Integer) request.getAttribute("role");
        if (role == null || role < User.ROLE_ADMIN) {
            return Result.forbidden();
        }

        try {
            Category category = categoryService.create(dto);
            return Result.success("创建成功", category);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 更新分类
     * PUT /api/categories/{id}
     */
    @PutMapping("/categories/{id}")
    public Result<Category> update(HttpServletRequest request,
                                   @PathVariable Long id,
                                   @RequestBody CategoryDTO dto) {
        // 检查权限
        Integer role = (Integer) request.getAttribute("role");
        if (role == null || role < User.ROLE_ADMIN) {
            return Result.forbidden();
        }

        try {
            Category category = categoryService.update(id, dto);
            return Result.success("更新成功", category);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 删除分类
     * DELETE /api/categories/{id}
     */
    @DeleteMapping("/categories/{id}")
    public Result<Void> delete(HttpServletRequest request,
                               @PathVariable Long id) {
        // 检查权限
        Integer role = (Integer) request.getAttribute("role");
        if (role == null || role < User.ROLE_ADMIN) {
            return Result.forbidden();
        }

        try {
            categoryService.delete(id);
            return Result.success("删除成功", null);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取单个分类详情
     * GET /api/categories/{id}
     */
    @GetMapping("/categories/{id}")
    public Result<Category> getById(@PathVariable Long id) {
        Category category = categoryService.getById(id);
        if (category == null) {
            return Result.notFound("分类不存在");
        }
        return Result.success(category);
    }
}
