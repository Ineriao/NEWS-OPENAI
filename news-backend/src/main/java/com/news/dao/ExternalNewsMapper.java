package com.news.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.news.entity.ExternalNews;
import org.apache.ibatis.annotations.Mapper;

/**
 * 外部新闻 Mapper
 */
@Mapper
public interface ExternalNewsMapper extends BaseMapper<ExternalNews> {
}
