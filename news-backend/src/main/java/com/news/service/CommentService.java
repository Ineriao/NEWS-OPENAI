package com.news.service;

import com.news.dto.CommentDTO;
import com.news.entity.Comment;
import com.news.vo.CommentVO;
import com.news.vo.PageVO;
import com.news.vo.UserCommentVO;

import java.util.List;

/**
 * 评论服务接口
 */
public interface CommentService {

    /**
     * 获取新闻的评论列表（带回复）
     * @param newsId 新闻ID
     * @param currentUserId 当前用户ID（用于判断是否已点赞），可为null
     */
    List<CommentVO> getCommentsByNewsId(Long newsId, Long currentUserId);

    /**
     * 发表评论
     * @param dto 评论内容
     * @param userId 用户ID
     */
    Comment createComment(CommentDTO dto, Long userId);

    /**
     * 删除评论
     * @param commentId 评论ID
     * @param operatorId 操作者ID
     * @param operatorRole 操作者角色
     */
    void deleteComment(Long commentId, Long operatorId, Integer operatorRole);

    /**
     * 统计新闻的评论数
     */
    Long countByNewsId(Long newsId);

    /**
     * 获取用户的评论历史（分页）
     * @param userId 用户ID
     * @param pageNum 页码
     * @param pageSize 每页数量
     */
    PageVO<UserCommentVO> getUserComments(Long userId, int pageNum, int pageSize);

    /**
     * 点赞/取消点赞评论
     * @param commentId 评论ID
     * @param userId 用户ID
     * @return true=点赞成功, false=取消点赞
     */
    boolean toggleLike(Long commentId, Long userId);

    /**
     * 检查用户是否已点赞评论
     */
    boolean checkLiked(Long commentId, Long userId);
}
