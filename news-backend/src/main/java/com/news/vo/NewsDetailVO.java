package com.news.vo;

import com.news.entity.News;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 新闻详情 VO (包含内容)
 */
@Data
public class NewsDetailVO {

    private Long id;
    private String title;
    private String summary;
    private String content;  // 包含富文本内容
    private String coverImage;
    private Long categoryId;
    private String categoryName;
    private Long authorId;
    private String authorName;
    private Long reviewerId;
    private String reviewerName;
    private Integer status;
    private String statusName;
    private Integer viewCount;
    private LocalDateTime publishTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    /**
     * 从 News 实体转换
     */
    public static NewsDetailVO fromNews(News news) {
        if (news == null) return null;

        NewsDetailVO vo = new NewsDetailVO();
        vo.setId(news.getId());
        vo.setTitle(news.getTitle());
        vo.setSummary(news.getSummary());
        vo.setContent(news.getContent());
        vo.setCoverImage(news.getCoverImage());
        vo.setCategoryId(news.getCategoryId());
        vo.setCategoryName(news.getCategoryName());
        vo.setAuthorId(news.getAuthorId());
        vo.setAuthorName(news.getAuthorName());
        vo.setReviewerId(news.getReviewerId());
        vo.setReviewerName(news.getReviewerName());
        vo.setStatus(news.getStatus());
        vo.setStatusName(news.getStatusName());
        vo.setViewCount(news.getViewCount());
        vo.setPublishTime(news.getPublishTime());
        vo.setCreateTime(news.getCreateTime());
        vo.setUpdateTime(news.getUpdateTime());
        return vo;
    }
}
