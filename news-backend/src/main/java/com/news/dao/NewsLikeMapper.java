package com.news.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.news.entity.NewsLike;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 新闻点赞数据访问层
 */
@Mapper
public interface NewsLikeMapper extends BaseMapper<NewsLike> {

    /**
     * 检查用户是否已点赞
     */
    @Select("SELECT COUNT(*) FROM news_like WHERE news_id = #{newsId} AND user_id = #{userId}")
    int checkLiked(@Param("newsId") Long newsId, @Param("userId") Long userId);

    /**
     * 统计新闻点赞数
     */
    @Select("SELECT COUNT(*) FROM news_like WHERE news_id = #{newsId}")
    int countByNewsId(@Param("newsId") Long newsId);
}
