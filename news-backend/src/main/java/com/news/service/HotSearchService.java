package com.news.service;

import com.news.vo.HotSearchVO;
import java.util.List;

/**
 * 热搜服务接口
 */
public interface HotSearchService {

    /**
     * 获取微博热搜
     */
    List<HotSearchVO> getWeiboHot();

    /**
     * 获取抖音热搜
     */
    List<HotSearchVO> getDouyinHot();

    /**
     * 获取知乎热搜
     */
    List<HotSearchVO> getZhihuHot();

    /**
     * 获取百度热搜
     */
    List<HotSearchVO> getBaiduHot();

    /**
     * 刷新并保存所有平台热搜到数据库
     */
    void refreshAndSaveAll();

    /**
     * 从数据库获取指定平台的热搜
     */
    List<HotSearchVO> getFromDb(String source);

    /**
     * 获取历史热搜记录（支持分页）
     */
    List<HotSearchVO> getHistory(String source, int page, int size);
}
