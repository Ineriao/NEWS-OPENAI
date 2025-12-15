package com.news.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.news.entity.News;
import com.news.entity.NewsCollection;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 新闻收藏数据访问层
 */
@Mapper
public interface NewsCollectionMapper extends BaseMapper<NewsCollection> {

    /**
     * 检查用户是否已收藏
     */
    @Select("SELECT COUNT(*) FROM news_collection WHERE news_id = #{newsId} AND user_id = #{userId}")
    int checkCollected(@Param("newsId") Long newsId, @Param("userId") Long userId);

    /**
     * 统计新闻收藏数
     */
    @Select("SELECT COUNT(*) FROM news_collection WHERE news_id = #{newsId}")
    int countByNewsId(@Param("newsId") Long newsId);

    /**
     * 获取用户收藏的新闻列表（分页）
     */
    @Select("SELECT n.*, c.name as category_name, u.username as author_name " +
            "FROM news_collection nc " +
            "JOIN news n ON nc.news_id = n.id " +
            "LEFT JOIN category c ON n.category_id = c.id " +
            "LEFT JOIN user u ON n.author_id = u.id " +
            "WHERE nc.user_id = #{userId} AND n.status = 2 " +
            "ORDER BY nc.create_time DESC")
    IPage<News> selectUserCollections(Page<News> page, @Param("userId") Long userId);
}
