package com.news.config;

import com.news.dao.NewsSearchRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

/**
 * Elasticsearch 条件配置
 * 只有当 spring.elasticsearch.enabled=true 时才启用 ES
 */
@Configuration
@ConditionalOnProperty(name = "spring.elasticsearch.enabled", havingValue = "true")
@EnableElasticsearchRepositories(basePackageClasses = NewsSearchRepository.class)
public class ElasticsearchConfig {
    // ES 启用时自动配置 Repository
}
