package com.dlbyy.blog.es;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dlbyy.blog.entity.Article;
import com.dlbyy.blog.service.ArticleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Elasticsearch 索引初始化器
 * <p>
 * 应用启动时（仅当 {@code blog.search.enabled=true}）执行：
 * <ul>
 *     <li>检测 blog_article 索引是否存在，不存在则依据 {@link ArticleDocument} 注解创建索引与映射（含 IK 分词器）</li>
 *     <li>将数据库中所有已发布的历史文章一次性全量同步到 ES，补齐存量数据（增量同步由 {@link ArticleEsSyncListener} 负责）</li>
 * </ul>
 * 若 ES 未就绪或同步失败，仅记录警告并跳过，不阻断应用启动。
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "blog.search.enabled", havingValue = "true")
@RequiredArgsConstructor
public class ElasticsearchIndexInitializer implements ApplicationRunner {

    /** 全量同步每批写入 ES 的文档数量，避免大批量导致 ES/GC 压力 */
    private static final int BATCH_SIZE = 200;

    private final ElasticsearchOperations elasticsearchOperations;
    private final ArticleRepository articleRepository;
    private final ArticleService articleService;

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

            fullSyncPublishedArticles();
        } catch (Exception e) {
            log.warn("ES 索引初始化/全量同步失败（ES 可能未就绪），将在首次写入时由 ES 自动创建 | msg={}", e.getMessage());
        }
    }

    /**
     * 全量同步数据库中所有已发布文章到 ES。
     * 分批读取与写入，降低内存与 ES 压力。
     */
    private void fullSyncPublishedArticles() {
        long total = articleService.count(new LambdaQueryWrapper<Article>().eq(Article::getIsPublish, 1));
        if (total == 0) {
            log.info("ES 全量同步：数据库无已发布文章，跳过");
            return;
        }

        long synced = 0L;
        long pages = (total + BATCH_SIZE - 1) / BATCH_SIZE;
        for (long page = 1; page <= pages; page++) {
            List<Article> batch = articleService.page(
                            new com.baomidou.mybatisplus.extension.plugins.pagination.Page<Article>(page, BATCH_SIZE),
                            new LambdaQueryWrapper<Article>().eq(Article::getIsPublish, 1))
                    .getRecords();
            if (batch.isEmpty()) {
                break;
            }
            List<ArticleDocument> docs = new ArrayList<>(batch.size());
            for (Article article : batch) {
                articleService.fillArticleInfo(article);
                docs.add(ArticleDocumentConverter.convert(article));
            }
            articleRepository.saveAll(docs);
            synced += docs.size();
        }
        log.info("ES 全量同步完成：已发布文章共 {} 篇，成功同步 {} 篇", total, synced);
    }
}
