package com.news.vo;

import com.news.entity.News;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 新闻列表项 VO (不包含内容)
 */
@Data
public class NewsListVO {

    private Long id;
    private String title;
    private String summary;
    private String coverImage;
    private Long categoryId;
    private String categoryName;
    private Long authorId;
    private String authorName;
    private Integer status;
    private String statusName;
    private Integer viewCount;
    private LocalDateTime publishTime;
    private LocalDateTime createTime;

    /**
     * 从 News 实体转换
     */
    public static NewsListVO fromNews(News news) {
        if (news == null) return null;

        NewsListVO vo = new NewsListVO();
        vo.setId(news.getId());
        vo.setTitle(news.getTitle());
        vo.setSummary(news.getSummary());
        vo.setCoverImage(news.getCoverImage());
        vo.setCategoryId(news.getCategoryId());
        vo.setCategoryName(news.getCategoryName());
        vo.setAuthorId(news.getAuthorId());
        vo.setAuthorName(news.getAuthorName());
        vo.setStatus(news.getStatus());
        vo.setStatusName(news.getStatusName());
        vo.setViewCount(news.getViewCount());
        vo.setPublishTime(news.getPublishTime());
        vo.setCreateTime(news.getCreateTime());
        return vo;
    }
}
