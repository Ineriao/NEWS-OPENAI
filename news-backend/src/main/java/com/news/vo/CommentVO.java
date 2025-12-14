package com.news.vo;

import com.news.entity.Comment;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 评论 VO（带回复）
 */
@Data
public class CommentVO {

    private Long id;
    private Long newsId;
    private Long userId;
    private String username;
    private String userAvatar;
    private String content;
    private LocalDateTime createTime;

    /** 回复列表 */
    private List<ReplyVO> replies;

    /**
     * 从 Comment 实体转换
     */
    public static CommentVO fromComment(Comment comment) {
        if (comment == null) return null;

        CommentVO vo = new CommentVO();
        vo.setId(comment.getId());
        vo.setNewsId(comment.getNewsId());
        vo.setUserId(comment.getUserId());
        vo.setUsername(comment.getUsername());
        vo.setUserAvatar(comment.getUserAvatar());
        vo.setContent(comment.getContent());
        vo.setCreateTime(comment.getCreateTime());

        // 转换回复
        if (comment.getReplies() != null) {
            vo.setReplies(comment.getReplies().stream()
                    .map(ReplyVO::fromComment)
                    .collect(Collectors.toList()));
        }

        return vo;
    }

    /**
     * 回复 VO（二级评论）
     */
    @Data
    public static class ReplyVO {
        private Long id;
        private Long userId;
        private String username;
        private String userAvatar;
        private String content;
        private String replyToUsername;  // 回复的目标用户名
        private LocalDateTime createTime;

        public static ReplyVO fromComment(Comment comment) {
            if (comment == null) return null;

            ReplyVO vo = new ReplyVO();
            vo.setId(comment.getId());
            vo.setUserId(comment.getUserId());
            vo.setUsername(comment.getUsername());
            vo.setUserAvatar(comment.getUserAvatar());
            vo.setContent(comment.getContent());
            vo.setReplyToUsername(comment.getReplyToUsername());
            vo.setCreateTime(comment.getCreateTime());
            return vo;
        }
    }
}
