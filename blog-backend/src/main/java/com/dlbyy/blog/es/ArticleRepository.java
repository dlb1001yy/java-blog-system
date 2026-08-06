package com.dlbyy.blog.es;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

/**
 * 文章 Elasticsearch Repository
 * <p>
 * 继承 {@link ElasticsearchRepository} 自动获得 CRUD 能力，
 * 可按需扩展自定义查询方法（如按标题/内容检索）。
 */
@Repository
public interface ArticleRepository extends ElasticsearchRepository<ArticleDocument, Long> {

}
