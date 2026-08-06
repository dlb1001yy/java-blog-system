package com.dlbyy.blog.es;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.stereotype.Component;

/**
 * Elasticsearch 索引初始化器
 * <p>
 * 应用启动时检测 blog_article 索引是否存在：
 * <ul>
 *     <li>不存在：依据 {@link ArticleDocument} 注解创建索引与映射（含 IK 分词器）</li>
 *     <li>已存在：跳过</li>
 * </ul>
 * 若 ES 未就绪则记录警告并跳过，索引将在首次写入文档时由 ES 自动创建。
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "blog.search.enabled", havingValue = "true")
@RequiredArgsConstructor
public class ElasticsearchIndexInitializer implements ApplicationRunner {

    private final ElasticsearchOperations elasticsearchOperations;

    @Override
    public void run(ApplicationArguments args) {
        try {
            IndexOperations indexOps = elasticsearchOperations.indexOps(ArticleDocument.class);
            if (!indexOps.exists()) {
                indexOps.createWithMapping();
                log.info("ES 索引 blog_article 创建成功（含 IK 分词映射）");
            } else {
                log.info("ES 索引 blog_article 已存在，跳过创建");
            }
        } catch (Exception e) {
            log.warn("ES 索引初始化失败（ES 可能未就绪），将在首次写入时由 ES 自动创建 | msg={}", e.getMessage());
        }
    }
}
