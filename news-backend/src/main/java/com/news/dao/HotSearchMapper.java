package com.news.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.news.entity.HotSearch;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 热搜 Mapper
 */
@Mapper
public interface HotSearchMapper extends BaseMapper<HotSearch> {

    /**
     * 删除指定来源和时间之前的热搜数据
     */
    int deleteBySourceAndTimeBefore(@Param("source") String source, @Param("time") LocalDateTime time);
}
