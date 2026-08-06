package com.dlbyy.blog.es;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

/**
 * Elasticsearch 配置类
 * <p>
 * 仅当 {@code blog.search.enabled=true} 时生效，显式启用 ES Repository 扫描。
 * <p>
 * 配合 application.yaml 中 {@code spring.data.elasticsearch.repositories.enabled=false}
 * 实现：ES 未启动时不创建 ArticleRepository 等 Bean，避免启动时连接失败。
 *
 * @see ArticleRepository
 * @see com.dlbyy.blog.listener.ArticleEsSyncListener
 * @see ElasticsearchIndexInitializer
 */
@Configuration
@EnableElasticsearchRepositories(basePackages = "com.dlbyy.blog.es")
@ConditionalOnProperty(name = "blog.search.enabled", havingValue = "true")
public class ElasticsearchConfig {
}
