package com.news.service;

import java.util.Map;

/**
 * 新闻点赞服务接口
 */
public interface NewsLikeService {

    /**
     * 点赞/取消点赞 (toggle)
     * @param newsId 新闻ID
     * @param userId 用户ID
     * @return 操作后的状态 {liked: boolean, likeCount: int}
     */
    Map<String, Object> toggleLike(Long newsId, Long userId);

    /**
     * 检查用户是否已点赞
     */
    boolean isLiked(Long newsId, Long userId);

    /**
     * 获取点赞状态和数量
     */
    Map<String, Object> getLikeStatus(Long newsId, Long userId);
}
