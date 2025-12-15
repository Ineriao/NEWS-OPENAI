package com.news.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.news.entity.News;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Map;

/**
 * 新闻数据访问层
 */
@Mapper
public interface NewsMapper extends BaseMapper<News> {

    /**
     * 分页查询新闻（带关联信息）
     * 使用 MyBatis-Plus 分页 + 自定义 SQL
     */
    @Select("<script>" +
            "SELECT n.*, " +
            "c.name as category_name, " +
            "u1.username as author_name, " +
            "u2.username as reviewer_name " +
            "FROM news n " +
            "LEFT JOIN category c ON n.category_id = c.id " +
            "LEFT JOIN user u1 ON n.author_id = u1.id " +
            "LEFT JOIN user u2 ON n.reviewer_id = u2.id " +
            "<where>" +
            "  <if test='status != null'> AND n.status = #{status}</if>" +
            "  <if test='categoryId != null'> AND n.category_id = #{categoryId}</if>" +
            "  <if test='authorId != null'> AND n.author_id = #{authorId}</if>" +
            "  <if test='keyword != null and keyword != \"\"'>" +
            "    AND (n.title LIKE CONCAT('%',#{keyword},'%') OR n.summary LIKE CONCAT('%',#{keyword},'%'))" +
            "  </if>" +
            "</where>" +
            "<choose>" +
            "  <when test='sortBy == \"viewCount\"'>ORDER BY n.view_count</when>" +
            "  <when test='sortBy == \"publishTime\"'>ORDER BY n.publish_time</when>" +
            "  <otherwise>ORDER BY n.create_time</otherwise>" +
            "</choose>" +
            "<if test='sortOrder == \"asc\"'> ASC</if>" +
            "<if test='sortOrder != \"asc\"'> DESC</if>" +
            "</script>")
    IPage<News> selectPageWithDetails(Page<News> page,
                                       @Param("status") Integer status,
                                       @Param("categoryId") Long categoryId,
                                       @Param("authorId") Long authorId,
                                       @Param("keyword") String keyword,
                                       @Param("sortBy") String sortBy,
                                       @Param("sortOrder") String sortOrder);

    /**
     * 根据ID查询新闻详情（带关联信息）
     */
    @Select("SELECT n.*, " +
            "c.name as category_name, " +
            "u1.username as author_name, " +
            "u2.username as reviewer_name " +
            "FROM news n " +
            "LEFT JOIN category c ON n.category_id = c.id " +
            "LEFT JOIN user u1 ON n.author_id = u1.id " +
            "LEFT JOIN user u2 ON n.reviewer_id = u2.id " +
            "WHERE n.id = #{id}")
    News selectByIdWithDetails(@Param("id") Long id);

    /**
     * 增加浏览次数
     */
    @Update("UPDATE news SET view_count = view_count + 1 WHERE id = #{id}")
    void incrementViewCount(@Param("id") Long id);

    /**
     * 增加点赞数
     */
    @Update("UPDATE news SET like_count = like_count + 1 WHERE id = #{id}")
    void incrementLikeCount(@Param("id") Long id);

    /**
     * 减少点赞数
     */
    @Update("UPDATE news SET like_count = GREATEST(like_count - 1, 0) WHERE id = #{id}")
    void decrementLikeCount(@Param("id") Long id);

    /**
     * 增加收藏数
     */
    @Update("UPDATE news SET collection_count = collection_count + 1 WHERE id = #{id}")
    void incrementCollectionCount(@Param("id") Long id);

    /**
     * 减少收藏数
     */
    @Update("UPDATE news SET collection_count = GREATEST(collection_count - 1, 0) WHERE id = #{id}")
    void decrementCollectionCount(@Param("id") Long id);

    /**
     * 统计各分类的新闻数量
     */
    @Select("SELECT c.name as name, COUNT(n.id) as count " +
            "FROM category c " +
            "LEFT JOIN news n ON c.id = n.category_id AND n.status = 2 " +
            "GROUP BY c.id, c.name " +
            "ORDER BY count DESC")
    List<Map<String, Object>> countByCategory();

    /**
     * 统计近7天每天发布的新闻数量
     */
    @Select("SELECT DATE(publish_time) as date, COUNT(*) as count " +
            "FROM news " +
            "WHERE status = 2 AND publish_time >= DATE_SUB(CURDATE(), INTERVAL 6 DAY) " +
            "GROUP BY DATE(publish_time) " +
            "ORDER BY date ASC")
    List<Map<String, Object>> countByDay();
}
