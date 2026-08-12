package com.dlbyy.blog.listener;

import com.dlbyy.blog.entity.Article;
import com.dlbyy.blog.es.ArticleDocument;
import com.dlbyy.blog.es.ArticleDocumentConverter;
import com.dlbyy.blog.es.ArticleRepository;
import com.dlbyy.blog.event.ArticlePublishedEvent;
import com.dlbyy.blog.service.ArticleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * 文章 ES 同步事件监听器
 * <p>
 * 监听 {@link ArticlePublishedEvent}，通过 {@link Async} 异步执行，
 * 将文章数据同步至 Elasticsearch 索引 blog_article。
 * <p>
 * 同步策略：
 * <ul>
 *     <li>CREATED / UPDATED / PUBLISHED：重新查询文章 + 关联信息后写入 ES</li>
 *     <li>DELETED：从 ES 中删除对应文档</li>
 * </ul>
 * 失败不影响主流程，仅记录日志（可后续接入 MQ 重试）。
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "blog.search.enabled", havingValue = "true")
@RequiredArgsConstructor
public class ArticleEsSyncListener {

    private final ArticleRepository articleRepository;
    private final ArticleService articleService;

    @Async("esSyncExecutor")
    @EventListener
    public void onArticlePublished(ArticlePublishedEvent event) {
        Long articleId = event.getArticleId();
        if (articleId == null) {
            log.warn("ES 同步事件缺少 articleId，已跳过 | type={}", event.getEventType());
            return;
        }

        ArticlePublishedEvent.EventType type = event.getEventType();
        try {
            switch (type) {
                case CREATED, UPDATED, PUBLISHED -> handleUpsert(articleId, type);
                case DELETED -> handleDelete(articleId);
            }
        } catch (Exception e) {
            log.error("ES 同步失败 | articleId={} | type={}", articleId, type, e);
        }
    }

    /**
     * 新增/更新：重新查询文章（含分类、标签、作者）后写入 ES
     */
    private void handleUpsert(Long articleId, ArticlePublishedEvent.EventType type) {
        Article article = articleService.getById(articleId);
        if (article == null) {
            // 文章已被逻辑删除，兜底删除 ES 中的文档
            articleRepository.deleteById(articleId);
            log.warn("文章在 DB 中不存在，已尝试删除 ES 文档 | articleId={}", articleId);
            return;
        }
        articleService.fillArticleInfo(article);
        ArticleDocument doc = ArticleDocumentConverter.convert(article);
        articleRepository.save(doc);
        log.info("ES 同步成功 | articleId={} | type={} | title={}", articleId, type, doc.getTitle());
    }

    /**
     * 删除：从 ES 中移除文档
     */
    private void handleDelete(Long articleId) {
        articleRepository.deleteById(articleId);
        log.info("ES 删除成功 | articleId={}", articleId);
    }
}
