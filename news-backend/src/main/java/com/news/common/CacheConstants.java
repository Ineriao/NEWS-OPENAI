package com.news.common;

/**
 * 缓存常量定义
 */
public class CacheConstants {

    // ==================== 缓存名称 ====================

    /** 分类缓存 */
    public static final String CACHE_CATEGORY = "category";

    /** 热搜缓存 */
    public static final String CACHE_HOT_SEARCH = "hotSearch";

    /** 新闻缓存 */
    public static final String CACHE_NEWS = "news";

    /** 评论数缓存 */
    public static final String CACHE_COMMENT_COUNT = "commentCount";

    // ==================== TTL（秒） ====================

    /** 分类缓存 TTL - 1小时 */
    public static final long TTL_CATEGORY = 3600;

    /** 热搜缓存 TTL - 30分钟 */
    public static final long TTL_HOT_SEARCH = 1800;

    /** 新闻列表缓存 TTL - 10分钟 */
    public static final long TTL_NEWS_LIST = 600;

    /** 新闻详情缓存 TTL - 5分钟 */
    public static final long TTL_NEWS_DETAIL = 300;

    /** 评论数缓存 TTL - 2分钟 */
    public static final long TTL_COMMENT_COUNT = 120;

    // ==================== 缓存 Key 前缀 ====================

    /** 分类树 Key */
    public static final String KEY_CATEGORY_TREE = "tree";

    /** 新闻列表 Key 前缀 */
    public static final String KEY_NEWS_LIST = "list:";

    /** 新闻详情 Key 前缀 */
    public static final String KEY_NEWS_DETAIL = "detail:";

    private CacheConstants() {
        // 私有构造函数，防止实例化
    }
}
