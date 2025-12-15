package com.news.vo;

import com.news.entity.Comment;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户评论历史 VO
 */
@Data
public class UserCommentVO {

    /** 评论ID */
    private Long id;

    /** 新闻ID */
    private Long newsId;

    /** 新闻标题 */
    private String newsTitle;

    /** 评论内容 */
    private String content;

    /** 评论时间 */
    private LocalDateTime createTime;

    /**
     * 从 Comment 实体转换
     */
    public static UserCommentVO fromComment(Comment comment) {
        UserCommentVO vo = new UserCommentVO();
        vo.setId(comment.getId());
        vo.setNewsId(comment.getNewsId());
        vo.setNewsTitle(comment.getNewsTitle());
        vo.setContent(comment.getContent());
        vo.setCreateTime(comment.getCreateTime());
        return vo;
    }
}
