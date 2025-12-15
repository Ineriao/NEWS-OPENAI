package com.news.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.news.entity.News;

import java.util.Map;

/**
 * 新闻收藏服务接口
 */
public interface NewsCollectionService {

    /**
     * 收藏/取消收藏 (toggle)
     * @param newsId 新闻ID
     * @param userId 用户ID
     * @return 操作后的状态 {collected: boolean, collectionCount: int}
     */
    Map<String, Object> toggleCollection(Long newsId, Long userId);

    /**
     * 检查用户是否已收藏
     */
    boolean isCollected(Long newsId, Long userId);

    /**
     * 获取收藏状态和数量
     */
    Map<String, Object> getCollectionStatus(Long newsId, Long userId);

    /**
     * 获取用户收藏列表（分页）
     */
    IPage<News> getUserCollections(Long userId, int page, int size);
}
