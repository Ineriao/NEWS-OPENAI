package com.news.service.impl;

import com.news.dao.CommentMapper;
import com.news.dao.NewsMapper;
import com.news.dto.CommentDTO;
import com.news.entity.Comment;
import com.news.entity.News;
import com.news.entity.User;
import com.news.service.CommentService;
import com.news.vo.CommentVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 评论服务实现类
 */
@Service
public class CommentServiceImpl implements CommentService {

    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private NewsMapper newsMapper;

    @Override
    public List<CommentVO> getCommentsByNewsId(Long newsId) {
        // 1. 获取一级评论
        List<Comment> topComments = commentMapper.selectTopCommentsByNewsId(newsId);

        // 2. 为每个一级评论获取回复
        for (Comment comment : topComments) {
            List<Comment> replies = commentMapper.selectRepliesByParentId(comment.getId());
            comment.setReplies(replies);
        }

        // 3. 转换为 VO
        return topComments.stream()
                .map(CommentVO::fromComment)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public Comment createComment(CommentDTO dto, Long userId) {
        // 1. 参数校验
        if (dto.getNewsId() == null) {
            throw new RuntimeException("新闻ID不能为空");
        }
        if (dto.getContent() == null || dto.getContent().trim().isEmpty()) {
            throw new RuntimeException("评论内容不能为空");
        }
        if (dto.getContent().length() > 500) {
            throw new RuntimeException("评论内容不能超过500字");
        }

        // 2. 检查新闻是否存在且已发布
        News news = newsMapper.selectById(dto.getNewsId());
        if (news == null) {
            throw new RuntimeException("新闻不存在");
        }
        if (news.getStatus() != News.STATUS_PUBLISHED) {
            throw new RuntimeException("该新闻暂不支持评论");
        }

        // 3. 如果是回复，检查父评论是否存在
        if (dto.getParentId() != null) {
            Comment parent = commentMapper.selectById(dto.getParentId());
            if (parent == null) {
                throw new RuntimeException("被回复的评论不存在");
            }
            // 确保回复的是同一篇新闻的评论
            if (!parent.getNewsId().equals(dto.getNewsId())) {
                throw new RuntimeException("无效的回复");
            }
        }

        // 4. 创建评论
        Comment comment = new Comment();
        comment.setNewsId(dto.getNewsId());
        comment.setUserId(userId);
        comment.setContent(dto.getContent().trim());
        comment.setParentId(dto.getParentId());
        comment.setStatus(Comment.STATUS_VISIBLE);

        commentMapper.insert(comment);
        return comment;
    }

    @Override
    @Transactional
    public void deleteComment(Long commentId, Long operatorId, Integer operatorRole) {
        Comment comment = commentMapper.selectById(commentId);
        if (comment == null) {
            throw new RuntimeException("评论不存在");
        }

        // 只有评论作者或管理员可以删除
        if (!comment.getUserId().equals(operatorId) && operatorRole < User.ROLE_ADMIN) {
            throw new RuntimeException("无权删除此评论");
        }

        // 软删除（隐藏）
        comment.setStatus(Comment.STATUS_HIDDEN);
        commentMapper.updateById(comment);
    }

    @Override
    public Long countByNewsId(Long newsId) {
        return commentMapper.countByNewsId(newsId);
    }
}
