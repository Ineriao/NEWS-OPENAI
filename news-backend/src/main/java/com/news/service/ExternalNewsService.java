package com.news.service;

import com.news.vo.ExternalNewsVO;
import java.util.List;

/**
 * 外部新闻服务接口
 */
public interface ExternalNewsService {

    /**
     * 获取36氪新闻
     */
    List<ExternalNewsVO> get36krNews();

    /**
     * 获取IT之家新闻
     */
    List<ExternalNewsVO> getIthomeNews();

    /**
     * 获取澎湃新闻
     */
    List<ExternalNewsVO> getThepaperNews();

    /**
     * 获取今日头条新闻
     */
    List<ExternalNewsVO> getToutiaoNews();

    /**
     * 刷新并保存所有新闻到数据库（每个来源保存前10条）
     */
    void refreshAndSaveAll();

    /**
     * 从数据库获取指定来源的新闻
     */
    List<ExternalNewsVO> getFromDb(String source, int limit);

    /**
     * 获取所有来源的最新新闻（混合展示）
     */
    List<ExternalNewsVO> getLatestNews(int limit);
}
