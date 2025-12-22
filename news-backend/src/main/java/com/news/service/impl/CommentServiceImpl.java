package com.news.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.news.common.CacheConstants;
import com.news.dao.CommentLikeMapper;
import com.news.dao.CommentMapper;
import com.news.dao.NewsMapper;
import com.news.dto.CommentDTO;
import com.news.entity.Comment;
import com.news.entity.CommentLike;
import com.news.entity.News;
import com.news.entity.User;
import com.news.service.CommentService;
import com.news.vo.CommentVO;
import com.news.vo.PageVO;
import com.news.vo.UserCommentVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
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

    @Autowired
    private CommentLikeMapper commentLikeMapper;

    @Override
    public List<CommentVO> getCommentsByNewsId(Long newsId, Long currentUserId) {
        // 一次性获取所有评论（优化N+1查询）
        List<Comment> allComments = commentMapper.selectAllByNewsId(newsId);

        if (allComments.isEmpty()) {
            return new ArrayList<>();
        }

        // 获取当前用户点赞的评论ID集合
        Set<Long> likedCommentIds = new HashSet<>();
        if (currentUserId != null) {
            List<Long> commentIds = allComments.stream()
                    .map(Comment::getId)
                    .collect(Collectors.toList());
            // 查询用户点赞的评论
            LambdaQueryWrapper<CommentLike> likeQuery = new LambdaQueryWrapper<>();
            likeQuery.eq(CommentLike::getUserId, currentUserId)
                    .in(CommentLike::getCommentId, commentIds);
            List<CommentLike> likes = commentLikeMapper.selectList(likeQuery);
            likedCommentIds = likes.stream()
                    .map(CommentLike::getCommentId)
                    .collect(Collectors.toSet());
        }

        // 构建评论ID -> 评论的映射
        Map<Long, Comment> commentMap = new HashMap<>();
        for (Comment c : allComments) {
            commentMap.put(c.getId(), c);
        }

        // 分离一级评论和回复，构建树形结构
        List<Comment> topComments = new ArrayList<>();
        for (Comment c : allComments) {
            if (c.getParentId() == null) {
                // 一级评论
                c.setReplies(new ArrayList<>());
                topComments.add(c);
            } else {
                // 回复：找到父评论并添加到其回复列表
                Comment parent = commentMap.get(c.getParentId());
                if (parent != null) {
                    // 设置被回复人用户名
                    c.setReplyToUsername(parent.getUsername());
                    if (parent.getReplies() == null) {
                        parent.setReplies(new ArrayList<>());
                    }
                    parent.getReplies().add(c);
                }
            }
        }

        // 转换为 VO 并设置点赞状态
        Set<Long> finalLikedCommentIds = likedCommentIds;
        return topComments.stream()
                .map(comment -> {
                    CommentVO vo = CommentVO.fromComment(comment);
                    vo.setLiked(finalLikedCommentIds.contains(comment.getId()));
                    // 设置回复的点赞状态
                    if (vo.getReplies() != null) {
                        for (CommentVO.ReplyVO reply : vo.getReplies()) {
                            reply.setLiked(finalLikedCommentIds.contains(reply.getId()));
                        }
                    }
                    return vo;
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    @CacheEvict(value = CacheConstants.CACHE_COMMENT_COUNT, key = "#dto.newsId")
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
    @CacheEvict(value = CacheConstants.CACHE_COMMENT_COUNT, allEntries = true)
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
    @Cacheable(value = CacheConstants.CACHE_COMMENT_COUNT, key = "#newsId")
    public Long countByNewsId(Long newsId) {
        return commentMapper.countByNewsId(newsId);
    }

    @Override
    public PageVO<UserCommentVO> getUserComments(Long userId, int pageNum, int pageSize) {
        Page<Comment> page = new Page<>(pageNum, pageSize);
        IPage<Comment> result = commentMapper.selectUserComments(page, userId);

        List<UserCommentVO> voList = result.getRecords().stream()
                .map(UserCommentVO::fromComment)
                .collect(Collectors.toList());

        return PageVO.of(voList, result.getTotal(), result.getPages(),
                result.getCurrent(), result.getSize());
    }

    @Override
    @Transactional
    public boolean toggleLike(Long commentId, Long userId) {
        // 检查评论是否存在
        Comment comment = commentMapper.selectById(commentId);
        if (comment == null) {
            throw new RuntimeException("评论不存在");
        }

        // 检查是否已点赞
        LambdaQueryWrapper<CommentLike> query = new LambdaQueryWrapper<>();
        query.eq(CommentLike::getCommentId, commentId)
                .eq(CommentLike::getUserId, userId);
        CommentLike existingLike = commentLikeMapper.selectOne(query);

        if (existingLike != null) {
            // 已点赞，取消点赞
            commentLikeMapper.deleteById(existingLike.getId());
            // 更新评论点赞数
            comment.setLikeCount(Math.max(0, (comment.getLikeCount() != null ? comment.getLikeCount() : 0) - 1));
            commentMapper.updateById(comment);
            return false;
        } else {
            // 未点赞，添加点赞
            CommentLike like = new CommentLike();
            like.setCommentId(commentId);
            like.setUserId(userId);
            like.setCreateTime(LocalDateTime.now());
            commentLikeMapper.insert(like);
            // 更新评论点赞数
            comment.setLikeCount((comment.getLikeCount() != null ? comment.getLikeCount() : 0) + 1);
            commentMapper.updateById(comment);
            return true;
        }
    }

    @Override
    public boolean checkLiked(Long commentId, Long userId) {
        return commentLikeMapper.checkLiked(commentId, userId) > 0;
    }

    @Override
    public PageVO<UserCommentVO> getAllComments(int pageNum, int pageSize) {
        Page<Comment> page = new Page<>(pageNum, pageSize);
        // 查询所有评论（按时间倒序）
        LambdaQueryWrapper<Comment> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(Comment::getCreateTime);
        IPage<Comment> result = commentMapper.selectPage(page, wrapper);

        List<UserCommentVO> voList = result.getRecords().stream()
                .map(UserCommentVO::fromComment)
                .collect(Collectors.toList());

        return PageVO.of(voList, result.getTotal(), result.getPages(),
                result.getCurrent(), result.getSize());
    }
}
