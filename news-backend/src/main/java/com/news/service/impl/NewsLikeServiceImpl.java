package com.news.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.news.dao.NewsLikeMapper;
import com.news.dao.NewsMapper;
import com.news.entity.NewsLike;
import com.news.service.NewsLikeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

/**
 * 新闻点赞服务实现类
 */
@Service
public class NewsLikeServiceImpl implements NewsLikeService {

    @Autowired
    private NewsLikeMapper newsLikeMapper;

    @Autowired
    private NewsMapper newsMapper;

    @Override
    @Transactional
    public Map<String, Object> toggleLike(Long newsId, Long userId) {
        Map<String, Object> result = new HashMap<>();

        // 检查是否已点赞
        LambdaQueryWrapper<NewsLike> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(NewsLike::getNewsId, newsId)
               .eq(NewsLike::getUserId, userId);
        NewsLike existing = newsLikeMapper.selectOne(wrapper);

        if (existing != null) {
            // 已点赞，取消点赞
            newsLikeMapper.deleteById(existing.getId());
            newsMapper.decrementLikeCount(newsId);
            result.put("liked", false);
        } else {
            // 未点赞，添加点赞
            NewsLike like = new NewsLike();
            like.setNewsId(newsId);
            like.setUserId(userId);
            newsLikeMapper.insert(like);
            newsMapper.incrementLikeCount(newsId);
            result.put("liked", true);
        }

        // 返回最新点赞数
        int likeCount = newsLikeMapper.countByNewsId(newsId);
        result.put("likeCount", likeCount);

        return result;
    }

    @Override
    public boolean isLiked(Long newsId, Long userId) {
        if (userId == null) {
            return false;
        }
        return newsLikeMapper.checkLiked(newsId, userId) > 0;
    }

    @Override
    public Map<String, Object> getLikeStatus(Long newsId, Long userId) {
        Map<String, Object> result = new HashMap<>();
        result.put("liked", isLiked(newsId, userId));
        result.put("likeCount", newsLikeMapper.countByNewsId(newsId));
        return result;
    }
}
