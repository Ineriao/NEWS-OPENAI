package com.news.service;

import com.news.common.CacheConstants;
import com.news.dao.NewsMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

/**
 * 浏览量服务
 * 使用 Redis 异步累积浏览量，定时批量同步到数据库
 */
@Service
public class ViewCountService {

    private static final Logger log = LoggerFactory.getLogger(ViewCountService.class);

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private NewsMapper newsMapper;

    /**
     * 增加浏览量（写入 Redis）
     */
    public void incrementViewCount(Long newsId) {
        String key = CacheConstants.KEY_VIEW_COUNT_DELTA + newsId;
        stringRedisTemplate.opsForValue().increment(key);
    }

    /**
     * 获取 Redis 中的浏览量增量
     */
    public Long getViewCountDelta(Long newsId) {
        String key = CacheConstants.KEY_VIEW_COUNT_DELTA + newsId;
        String value = stringRedisTemplate.opsForValue().get(key);
        return value != null ? Long.parseLong(value) : 0L;
    }

    /**
     * 批量同步浏览量到数据库
     */
    @Transactional
    public void syncViewCountsToDb() {
        Set<String> keys = stringRedisTemplate.keys(CacheConstants.KEY_VIEW_COUNT_DELTA + "*");
        if (keys == null || keys.isEmpty()) {
            log.debug("没有浏览量增量需要同步");
            return;
        }

        int syncCount = 0;
        for (String key : keys) {
            try {
                Long newsId = Long.parseLong(key.replace(CacheConstants.KEY_VIEW_COUNT_DELTA, ""));
                String value = stringRedisTemplate.opsForValue().getAndDelete(key);
                if (value != null) {
                    int delta = Integer.parseInt(value);
                    if (delta > 0) {
                        newsMapper.incrementViewCountBy(newsId, delta);
                        syncCount++;
                    }
                }
            } catch (Exception e) {
                log.error("同步浏览量失败, key={}: {}", key, e.getMessage());
            }
        }

        if (syncCount > 0) {
            log.info("浏览量同步完成，共 {} 条记录", syncCount);
        }
    }
}
