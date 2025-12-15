package com.news.service;

import com.news.dto.NewsDTO;
import com.news.dto.NewsQueryDTO;
import com.news.entity.News;
import com.news.vo.NewsDetailVO;
import com.news.vo.NewsListVO;
import com.news.vo.PageVO;

/**
 * 新闻服务接口
 */
public interface NewsService {

    // ==================== 查询方法 ====================

    /**
     * 分页查询新闻
     */
    PageVO<NewsListVO> getPage(NewsQueryDTO query);

    /**
     * 获取新闻详情
     */
    NewsDetailVO getDetail(Long id);

    /**
     * 获取新闻详情并增加浏览量
     */
    NewsDetailVO getDetailAndView(Long id);

    /**
     * 获取已发布新闻列表（带缓存）
     */
    PageVO<NewsListVO> getPublishedPage(Long categoryId, Integer pageNum,
                                         Integer pageSize, String sortBy, String sortOrder);

    // ==================== 编辑操作 ====================

    /**
     * 创建新闻（草稿）
     * @param dto 新闻内容
     * @param authorId 作者ID
     */
    News create(NewsDTO dto, Long authorId);

    /**
     * 更新新闻
     * @param id 新闻ID
     * @param dto 新闻内容
     * @param operatorId 操作者ID
     * @param operatorRole 操作者角色
     */
    News update(Long id, NewsDTO dto, Long operatorId, Integer operatorRole);

    /**
     * 删除新闻
     */
    void delete(Long id, Long operatorId, Integer operatorRole);

    // ==================== 状态流转 ====================

    /**
     * 提交审核（草稿 → 待审核）
     */
    void submitForReview(Long id, Long operatorId);

    /**
     * 审核通过（待审核 → 已发布）
     */
    void approve(Long id, Long reviewerId);

    /**
     * 审核退回（待审核 → 草稿）
     */
    void reject(Long id, Long reviewerId);

    /**
     * 存档（已发布 → 已存档）
     */
    void archive(Long id, Long operatorId, Integer operatorRole);

    // ==================== 辅助方法 ====================

    /**
     * 根据ID获取新闻
     */
    News getById(Long id);
}
