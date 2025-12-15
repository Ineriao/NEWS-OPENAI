package com.news.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.news.dao.CommentMapper;
import com.news.dao.NewsMapper;
import com.news.dao.UserMapper;
import com.news.entity.Comment;
import com.news.entity.News;
import com.news.service.StatsService;
import com.news.vo.CategoryStatsVO;
import com.news.vo.DashboardStatsVO;
import com.news.vo.TrendStatsVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 统计服务实现类
 */
@Service
public class StatsServiceImpl implements StatsService {

    @Autowired
    private NewsMapper newsMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private CommentMapper commentMapper;

    @Override
    public DashboardStatsVO getDashboardStats() {
        DashboardStatsVO stats = new DashboardStatsVO();

        // 新闻总数（已发布）
        Long publishedCount = newsMapper.selectCount(
                new LambdaQueryWrapper<News>().eq(News::getStatus, News.STATUS_PUBLISHED)
        );
        stats.setPublishedCount(publishedCount);
        stats.setNewsCount(publishedCount);

        // 用户总数
        Long userCount = userMapper.selectCount(null);
        stats.setUserCount(userCount);

        // 评论总数
        Long commentCount = commentMapper.selectCount(
                new LambdaQueryWrapper<Comment>().eq(Comment::getStatus, Comment.STATUS_VISIBLE)
        );
        stats.setCommentCount(commentCount);

        // 待审核新闻数
        Long pendingCount = newsMapper.selectCount(
                new LambdaQueryWrapper<News>().eq(News::getStatus, News.STATUS_PENDING)
        );
        stats.setPendingCount(pendingCount);

        return stats;
    }

    @Override
    public List<CategoryStatsVO> getCategoryStats() {
        List<Map<String, Object>> rawStats = newsMapper.countByCategory();
        List<CategoryStatsVO> result = new ArrayList<>();

        for (Map<String, Object> row : rawStats) {
            CategoryStatsVO vo = new CategoryStatsVO();
            vo.setName((String) row.get("name"));
            Object countObj = row.get("count");
            vo.setCount(countObj instanceof Long ? (Long) countObj : ((Number) countObj).longValue());
            result.add(vo);
        }

        return result;
    }

    @Override
    public List<TrendStatsVO> getTrendStats() {
        List<Map<String, Object>> rawStats = newsMapper.countByDay();
        List<TrendStatsVO> result = new ArrayList<>();

        // 构建近7天的日期映射
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate today = LocalDate.now();

        // 初始化近7天，默认数量为0
        for (int i = 6; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            TrendStatsVO vo = new TrendStatsVO();
            vo.setDate(date.format(formatter));
            vo.setCount(0L);
            result.add(vo);
        }

        // 填充实际数据
        for (Map<String, Object> row : rawStats) {
            String dateStr = row.get("date").toString();
            Object countObj = row.get("count");
            Long count = countObj instanceof Long ? (Long) countObj : ((Number) countObj).longValue();

            // 找到对应日期并更新
            for (TrendStatsVO vo : result) {
                if (vo.getDate().equals(dateStr)) {
                    vo.setCount(count);
                    break;
                }
            }
        }

        return result;
    }
}
