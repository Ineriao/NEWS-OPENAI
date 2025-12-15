package com.news.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.news.entity.Comment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 评论数据访问层
 */
@Mapper
public interface CommentMapper extends BaseMapper<Comment> {

    /**
     * 查询新闻的评论（带用户信息）
     * 只查询一级评论（parent_id IS NULL）
     */
    @Select("SELECT c.*, u.username, u.avatar as user_avatar " +
            "FROM comment c " +
            "LEFT JOIN user u ON c.user_id = u.id " +
            "WHERE c.news_id = #{newsId} AND c.parent_id IS NULL AND c.status = 1 " +
            "ORDER BY c.create_time DESC")
    List<Comment> selectTopCommentsByNewsId(@Param("newsId") Long newsId);

    /**
     * 查询评论的回复（带用户信息）
     */
    @Select("SELECT c.*, u.username, u.avatar as user_avatar, " +
            "u2.username as reply_to_username " +
            "FROM comment c " +
            "LEFT JOIN user u ON c.user_id = u.id " +
            "LEFT JOIN comment pc ON c.parent_id = pc.id " +
            "LEFT JOIN user u2 ON pc.user_id = u2.id " +
            "WHERE c.parent_id = #{parentId} AND c.status = 1 " +
            "ORDER BY c.create_time ASC")
    List<Comment> selectRepliesByParentId(@Param("parentId") Long parentId);

    /**
     * 统计新闻的评论数
     */
    @Select("SELECT COUNT(*) FROM comment WHERE news_id = #{newsId} AND status = 1")
    Long countByNewsId(@Param("newsId") Long newsId);

    /**
     * 一次性查询新闻的所有评论（优化N+1问题）
     * 包含一级评论和回复，带用户信息
     */
    @Select("SELECT c.*, u.username, u.avatar as user_avatar " +
            "FROM comment c " +
            "LEFT JOIN user u ON c.user_id = u.id " +
            "WHERE c.news_id = #{newsId} AND c.status = 1 " +
            "ORDER BY CASE WHEN c.parent_id IS NULL THEN 0 ELSE 1 END, c.create_time ASC")
    List<Comment> selectAllByNewsId(@Param("newsId") Long newsId);

    /**
     * 分页查询用户的评论历史（带新闻标题）
     */
    @Select("SELECT c.id, c.news_id, c.content, c.create_time, n.title as news_title " +
            "FROM comment c " +
            "LEFT JOIN news n ON c.news_id = n.id " +
            "WHERE c.user_id = #{userId} AND c.status = 1 " +
            "ORDER BY c.create_time DESC")
    IPage<Comment> selectUserComments(Page<?> page, @Param("userId") Long userId);
}
