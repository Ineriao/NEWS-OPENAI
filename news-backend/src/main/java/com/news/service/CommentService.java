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
     */
    List<CommentVO> getCommentsByNewsId(Long newsId);

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
}
