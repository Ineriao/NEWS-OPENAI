package com.news.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.news.common.CacheConstants;
import com.news.dao.NewsMapper;
import com.news.dto.NewsDTO;
import com.news.dto.NewsQueryDTO;
import com.news.entity.News;
import com.news.entity.User;
import com.news.service.CategoryService;
import com.news.service.NewsService;
import com.news.service.ViewCountService;
import com.news.vo.NewsDetailVO;
import com.news.vo.NewsListVO;
import com.news.vo.PageVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 新闻服务实现类
 */
@Service
public class NewsServiceImpl implements NewsService {

    @Autowired
    private NewsMapper newsMapper;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private ViewCountService viewCountService;

    // ==================== 查询方法 ====================

    @Override
    public PageVO<NewsListVO> getPage(NewsQueryDTO query) {
        Page<News> page = new Page<>(query.getPageNum(), query.getPageSize());

        IPage<News> result = newsMapper.selectPageWithDetails(
                page,
                query.getStatus(),
                query.getCategoryId(),
                query.getAuthorId(),
                query.getKeyword(),
                query.getSortBy(),
                query.getSortOrder()
        );

        List<NewsListVO> voList = result.getRecords().stream()
                .map(NewsListVO::fromNews)
                .collect(Collectors.toList());

        return PageVO.of(voList, result.getTotal(), result.getPages(),
                result.getCurrent(), result.getSize());
    }

    @Override
    @Cacheable(value = CacheConstants.CACHE_NEWS, key = "'detail:' + #id")
    public NewsDetailVO getDetail(Long id) {
        News news = newsMapper.selectByIdWithDetails(id);
        return NewsDetailVO.fromNews(news);
    }

    @Override
    public NewsDetailVO getDetailAndView(Long id) {
        // 异步累积浏览量到 Redis
        viewCountService.incrementViewCount(id);
        return getDetail(id);
    }

    @Override
    @Cacheable(value = CacheConstants.CACHE_NEWS_LIST,
            key = "#categoryId + ':' + #pageNum + ':' + #pageSize + ':' + #sortBy + ':' + #sortOrder")
    public PageVO<NewsListVO> getPublishedPage(Long categoryId, Integer pageNum,
                                                Integer pageSize, String sortBy, String sortOrder) {
        NewsQueryDTO query = new NewsQueryDTO();
        query.setStatus(News.STATUS_PUBLISHED);
        query.setCategoryId(categoryId);
        query.setPageNum(pageNum);
        query.setPageSize(pageSize);
        query.setSortBy(sortBy);
        query.setSortOrder(sortOrder);
        return getPage(query);
    }

    // ==================== 编辑操作 ====================

    @Override
    @Transactional
    public News create(NewsDTO dto, Long authorId) {
        // 1. 参数校验
        validateNewsDTO(dto);

        // 2. 检查分类是否存在
        if (!categoryService.existsById(dto.getCategoryId())) {
            throw new RuntimeException("分类不存在");
        }

        // 3. 创建新闻
        News news = new News();
        news.setTitle(dto.getTitle().trim());
        news.setSummary(dto.getSummary());
        news.setContent(dto.getContent());
        news.setCoverImage(dto.getCoverImage());
        news.setCategoryId(dto.getCategoryId());
        news.setAuthorId(authorId);
        news.setStatus(News.STATUS_DRAFT);  // 默认草稿状态
        news.setViewCount(0);

        newsMapper.insert(news);
        return news;
    }

    @Override
    @Transactional
    @CacheEvict(value = {CacheConstants.CACHE_NEWS, CacheConstants.CACHE_NEWS_LIST}, allEntries = true)
    public News update(Long id, NewsDTO dto, Long operatorId, Integer operatorRole) {
        // 1. 检查新闻是否存在
        News news = newsMapper.selectById(id);
        if (news == null) {
            throw new RuntimeException("新闻不存在");
        }

        // 2. 权限检查
        checkEditPermission(news, operatorId, operatorRole);

        // 3. 状态检查（只有草稿或被退回的新闻可以编辑）
        if (news.getStatus() != News.STATUS_DRAFT) {
            throw new RuntimeException("只有草稿状态的新闻可以编辑");
        }

        // 4. 参数校验
        validateNewsDTO(dto);

        // 5. 检查分类是否存在
        if (!categoryService.existsById(dto.getCategoryId())) {
            throw new RuntimeException("分类不存在");
        }

        // 6. 更新新闻
        news.setTitle(dto.getTitle().trim());
        news.setSummary(dto.getSummary());
        news.setContent(dto.getContent());
        news.setCoverImage(dto.getCoverImage());
        news.setCategoryId(dto.getCategoryId());

        newsMapper.updateById(news);
        return news;
    }

    @Override
    @Transactional
    @CacheEvict(value = {CacheConstants.CACHE_NEWS, CacheConstants.CACHE_NEWS_LIST}, allEntries = true)
    public void delete(Long id, Long operatorId, Integer operatorRole) {
        News news = newsMapper.selectById(id);
        if (news == null) {
            throw new RuntimeException("新闻不存在");
        }

        // 权限检查
        checkEditPermission(news, operatorId, operatorRole);

        // 只有草稿状态可以删除，已发布的需要先存档
        if (news.getStatus() == News.STATUS_PUBLISHED) {
            throw new RuntimeException("已发布的新闻不能直接删除，请先存档");
        }

        newsMapper.deleteById(id);
    }

    // ==================== 状态流转 ====================

    @Override
    @Transactional
    public void submitForReview(Long id, Long operatorId) {
        News news = newsMapper.selectById(id);
        if (news == null) {
            throw new RuntimeException("新闻不存在");
        }

        // 只有作者可以提交审核
        if (!news.getAuthorId().equals(operatorId)) {
            throw new RuntimeException("只有作者可以提交审核");
        }

        // 只有草稿状态可以提交审核
        if (news.getStatus() != News.STATUS_DRAFT) {
            throw new RuntimeException("只有草稿状态可以提交审核");
        }

        news.setStatus(News.STATUS_PENDING);
        newsMapper.updateById(news);
    }

    @Override
    @Transactional
    @CacheEvict(value = {CacheConstants.CACHE_NEWS, CacheConstants.CACHE_NEWS_LIST}, allEntries = true)
    public void approve(Long id, Long reviewerId) {
        News news = newsMapper.selectById(id);
        if (news == null) {
            throw new RuntimeException("新闻不存在");
        }

        // 只有待审核状态可以审核
        if (news.getStatus() != News.STATUS_PENDING) {
            throw new RuntimeException("只有待审核状态可以审核");
        }

        news.setStatus(News.STATUS_PUBLISHED);
        news.setReviewerId(reviewerId);
        news.setPublishTime(LocalDateTime.now());
        newsMapper.updateById(news);
    }

    @Override
    @Transactional
    public void reject(Long id, Long reviewerId) {
        News news = newsMapper.selectById(id);
        if (news == null) {
            throw new RuntimeException("新闻不存在");
        }

        // 只有待审核状态可以退回
        if (news.getStatus() != News.STATUS_PENDING) {
            throw new RuntimeException("只有待审核状态可以退回");
        }

        news.setStatus(News.STATUS_DRAFT);  // 退回到草稿
        news.setReviewerId(reviewerId);
        newsMapper.updateById(news);
    }

    @Override
    @Transactional
    @CacheEvict(value = {CacheConstants.CACHE_NEWS, CacheConstants.CACHE_NEWS_LIST}, allEntries = true)
    public void archive(Long id, Long operatorId, Integer operatorRole) {
        News news = newsMapper.selectById(id);
        if (news == null) {
            throw new RuntimeException("新闻不存在");
        }

        // 权限检查（总编或管理员）
        if (operatorRole < User.ROLE_CHIEF) {
            throw new RuntimeException("权限不足");
        }

        // 只有已发布状态可以存档
        if (news.getStatus() != News.STATUS_PUBLISHED) {
            throw new RuntimeException("只有已发布的新闻可以存档");
        }

        news.setStatus(News.STATUS_ARCHIVED);
        newsMapper.updateById(news);
    }

    // ==================== 辅助方法 ====================

    @Override
    public News getById(Long id) {
        return newsMapper.selectById(id);
    }

    /**
     * 校验新闻 DTO
     */
    private void validateNewsDTO(NewsDTO dto) {
        if (dto.getTitle() == null || dto.getTitle().trim().isEmpty()) {
            throw new RuntimeException("标题不能为空");
        }
        if (dto.getContent() == null || dto.getContent().trim().isEmpty()) {
            throw new RuntimeException("内容不能为空");
        }
        if (dto.getCategoryId() == null) {
            throw new RuntimeException("请选择分类");
        }
    }

    /**
     * 检查编辑权限
     * 编辑只能操作自己的新闻，管理员可以操作所有新闻
     */
    private void checkEditPermission(News news, Long operatorId, Integer operatorRole) {
        // 管理员有所有权限
        if (operatorRole >= User.ROLE_ADMIN) {
            return;
        }
        // 非管理员只能操作自己的新闻
        if (!news.getAuthorId().equals(operatorId)) {
            throw new RuntimeException("无权操作此新闻");
        }
    }
}
