package com.news.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.news.dao.NewsCollectionMapper;
import com.news.dao.NewsMapper;
import com.news.entity.News;
import com.news.entity.NewsCollection;
import com.news.service.NewsCollectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

/**
 * 新闻收藏服务实现类
 */
@Service
public class NewsCollectionServiceImpl implements NewsCollectionService {

    @Autowired
    private NewsCollectionMapper newsCollectionMapper;

    @Autowired
    private NewsMapper newsMapper;

    @Override
    @Transactional
    public Map<String, Object> toggleCollection(Long newsId, Long userId) {
        Map<String, Object> result = new HashMap<>();

        // 检查是否已收藏
        LambdaQueryWrapper<NewsCollection> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(NewsCollection::getNewsId, newsId)
               .eq(NewsCollection::getUserId, userId);
        NewsCollection existing = newsCollectionMapper.selectOne(wrapper);

        if (existing != null) {
            // 已收藏，取消收藏
            newsCollectionMapper.deleteById(existing.getId());
            newsMapper.decrementCollectionCount(newsId);
            result.put("collected", false);
        } else {
            // 未收藏，添加收藏
            NewsCollection collection = new NewsCollection();
            collection.setNewsId(newsId);
            collection.setUserId(userId);
            newsCollectionMapper.insert(collection);
            newsMapper.incrementCollectionCount(newsId);
            result.put("collected", true);
        }

        // 返回最新收藏数
        int collectionCount = newsCollectionMapper.countByNewsId(newsId);
        result.put("collectionCount", collectionCount);

        return result;
    }

    @Override
    public boolean isCollected(Long newsId, Long userId) {
        if (userId == null) {
            return false;
        }
        return newsCollectionMapper.checkCollected(newsId, userId) > 0;
    }

    @Override
    public Map<String, Object> getCollectionStatus(Long newsId, Long userId) {
        Map<String, Object> result = new HashMap<>();
        result.put("collected", isCollected(newsId, userId));
        result.put("collectionCount", newsCollectionMapper.countByNewsId(newsId));
        return result;
    }

    @Override
    public IPage<News> getUserCollections(Long userId, int page, int size) {
        Page<News> pageParam = new Page<>(page, size);
        return newsCollectionMapper.selectUserCollections(pageParam, userId);
    }
}
