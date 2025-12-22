package com.news.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.news.dao.NewsMapper;
import com.news.dao.NewsSearchRepository;
import com.news.document.NewsDocument;
import com.news.entity.News;
import com.news.vo.NewsListVO;
import com.news.vo.PageVO;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 新闻 ES 搜索服务
 * 支持 ES 可选，ES 不可用时降级到 MySQL
 */
@Service
public class NewsSearchService {

    private static final Logger log = LoggerFactory.getLogger(NewsSearchService.class);

    @Autowired(required = false)
    private NewsSearchRepository newsSearchRepository;

    @Autowired(required = false)
    private ElasticsearchRestTemplate elasticsearchRestTemplate;

    @Autowired
    private NewsMapper newsMapper;

    @Value("${spring.elasticsearch.enabled:false}")
    private boolean esEnabled;

    /**
     * 检查 ES 是否可用
     */
    private boolean isEsAvailable() {
        return esEnabled && newsSearchRepository != null && elasticsearchRestTemplate != null;
    }

    /**
     * 搜索新闻
     */
    public PageVO<NewsListVO> search(String keyword, Long categoryId, Integer pageNum,
                                      Integer pageSize, String sortBy, String sortOrder) {
        // 如果 ES 不可用，降级到 MySQL
        if (!isEsAvailable()) {
            log.debug("ES 未启用，使用 MySQL 降级搜索");
            return searchByMysql(keyword, categoryId, pageNum, pageSize, sortBy, sortOrder);
        }

        try {
            return searchByEs(keyword, categoryId, pageNum, pageSize, sortBy, sortOrder);
        } catch (Exception e) {
            log.warn("ES 搜索失败，降级到 MySQL: {}", e.getMessage());
            return searchByMysql(keyword, categoryId, pageNum, pageSize, sortBy, sortOrder);
        }
    }

    /**
     * ES 搜索
     */
    private PageVO<NewsListVO> searchByEs(String keyword, Long categoryId, Integer pageNum,
                                           Integer pageSize, String sortBy, String sortOrder) {
        // 构建查询
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();

        // 关键词搜索（标题、摘要、内容）
        if (keyword != null && !keyword.trim().isEmpty()) {
            boolQuery.must(QueryBuilders.multiMatchQuery(keyword, "title", "summary", "content")
                    .analyzer("standard"));
        }

        // 分类过滤
        if (categoryId != null) {
            boolQuery.filter(QueryBuilders.termQuery("categoryId", categoryId));
        }

        // 构建排序
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder()
                .withQuery(boolQuery)
                .withPageable(PageRequest.of(pageNum - 1, pageSize));

        // 排序
        if ("viewCount".equals(sortBy)) {
            queryBuilder.withSort(SortBuilders.fieldSort("viewCount")
                    .order("asc".equals(sortOrder) ? SortOrder.ASC : SortOrder.DESC));
        } else if ("publishTime".equals(sortBy)) {
            queryBuilder.withSort(SortBuilders.fieldSort("publishTime")
                    .order("asc".equals(sortOrder) ? SortOrder.ASC : SortOrder.DESC));
        } else {
            queryBuilder.withSort(SortBuilders.fieldSort("publishTime").order(SortOrder.DESC));
        }

        NativeSearchQuery searchQuery = queryBuilder.build();

        // 执行搜索
        SearchHits<NewsDocument> searchHits = elasticsearchRestTemplate.search(searchQuery, NewsDocument.class);

        // 转换结果
        List<NewsListVO> voList = searchHits.getSearchHits().stream()
                .map(SearchHit::getContent)
                .map(this::convertToVO)
                .collect(Collectors.toList());

        long total = searchHits.getTotalHits();
        long pages = (total + pageSize - 1) / pageSize;

        return PageVO.of(voList, total, pages, (long) pageNum, (long) pageSize);
    }

    /**
     * MySQL 降级搜索
     */
    private PageVO<NewsListVO> searchByMysql(String keyword, Long categoryId, Integer pageNum,
                                              Integer pageSize, String sortBy, String sortOrder) {
        QueryWrapper<News> wrapper = new QueryWrapper<>();
        wrapper.eq("status", News.STATUS_PUBLISHED);

        // 关键词搜索
        if (keyword != null && !keyword.trim().isEmpty()) {
            wrapper.and(w -> w
                    .like("title", keyword)
                    .or().like("summary", keyword)
                    .or().like("content", keyword));
        }

        // 分类过滤
        if (categoryId != null) {
            wrapper.eq("category_id", categoryId);
        }

        // 排序
        if ("viewCount".equals(sortBy)) {
            wrapper.orderBy(true, "asc".equals(sortOrder), "view_count");
        } else if ("publishTime".equals(sortBy)) {
            wrapper.orderBy(true, "asc".equals(sortOrder), "publish_time");
        } else {
            wrapper.orderByDesc("publish_time");
        }

        // 分页查询
        Page<News> page = new Page<>(pageNum, pageSize);
        Page<News> result = newsMapper.selectPage(page, wrapper);

        List<NewsListVO> voList = result.getRecords().stream()
                .map(NewsListVO::fromNews)
                .collect(Collectors.toList());

        return PageVO.of(voList, result.getTotal(), result.getPages(),
                result.getCurrent(), result.getSize());
    }

    /**
     * 索引新闻
     */
    public void indexNews(News news) {
        if (!isEsAvailable()) {
            return;
        }

        if (news.getStatus() != News.STATUS_PUBLISHED) {
            return;
        }

        try {
            NewsDocument doc = convertToDocument(news);
            newsSearchRepository.save(doc);
            log.info("索引新闻: id={}, title={}", news.getId(), news.getTitle());
        } catch (Exception e) {
            log.warn("索引新闻失败: {}", e.getMessage());
        }
    }

    /**
     * 删除索引
     */
    public void deleteIndex(Long newsId) {
        if (!isEsAvailable()) {
            return;
        }

        try {
            newsSearchRepository.deleteById(newsId);
            log.info("删除新闻索引: id={}", newsId);
        } catch (Exception e) {
            log.warn("删除索引失败: {}", e.getMessage());
        }
    }

    /**
     * 全量同步
     */
    public void syncAll() {
        if (!isEsAvailable()) {
            log.warn("ES 未启用，跳过全量同步");
            return;
        }

        log.info("开始全量同步新闻到 ES...");

        try {
            // 查询所有已发布新闻
            List<News> newsList = newsMapper.selectList(
                    new QueryWrapper<News>().eq("status", News.STATUS_PUBLISHED)
            );

            // 批量索引
            List<NewsDocument> docs = newsList.stream()
                    .map(this::convertToDocument)
                    .collect(Collectors.toList());

            newsSearchRepository.saveAll(docs);
            log.info("全量同步完成，共 {} 条新闻", docs.size());
        } catch (Exception e) {
            log.error("全量同步失败: {}", e.getMessage());
        }
    }

    /**
     * News 转 NewsDocument
     */
    private NewsDocument convertToDocument(News news) {
        NewsDocument doc = new NewsDocument();
        doc.setId(news.getId());
        doc.setTitle(news.getTitle());
        doc.setSummary(news.getSummary());
        doc.setContent(news.getContent());
        doc.setCategoryId(news.getCategoryId());
        doc.setCategoryName(news.getCategoryName());
        doc.setAuthorId(news.getAuthorId());
        doc.setAuthorName(news.getAuthorName());
        doc.setViewCount(news.getViewCount());
        doc.setLikeCount(news.getLikeCount());
        doc.setPublishTime(news.getPublishTime());
        doc.setCreateTime(news.getCreateTime());
        doc.setCoverImage(news.getCoverImage());
        return doc;
    }

    /**
     * NewsDocument 转 NewsListVO
     */
    private NewsListVO convertToVO(NewsDocument doc) {
        NewsListVO vo = new NewsListVO();
        vo.setId(doc.getId());
        vo.setTitle(doc.getTitle());
        vo.setSummary(doc.getSummary());
        vo.setCoverImage(doc.getCoverImage());
        vo.setCategoryId(doc.getCategoryId());
        vo.setCategoryName(doc.getCategoryName());
        vo.setAuthorId(doc.getAuthorId());
        vo.setAuthorName(doc.getAuthorName());
        vo.setViewCount(doc.getViewCount());
        vo.setPublishTime(doc.getPublishTime());
        vo.setCreateTime(doc.getCreateTime());
        return vo;
    }
}
