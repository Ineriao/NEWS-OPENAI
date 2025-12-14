package com.news.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.news.entity.News;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

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
            "ORDER BY n.create_time DESC" +
            "</script>")
    IPage<News> selectPageWithDetails(Page<News> page,
                                       @Param("status") Integer status,
                                       @Param("categoryId") Long categoryId,
                                       @Param("authorId") Long authorId,
                                       @Param("keyword") String keyword);

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
}
