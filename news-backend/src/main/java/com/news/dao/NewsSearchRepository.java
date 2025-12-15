package com.news.dao;

import com.news.document.NewsDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * 新闻 ES 仓库
 */
public interface NewsSearchRepository extends ElasticsearchRepository<NewsDocument, Long> {
}
