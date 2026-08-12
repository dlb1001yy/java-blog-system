package com.dlbyy.blog.es;

import com.dlbyy.blog.entity.Article;
import com.dlbyy.blog.entity.Tag;

import java.util.stream.Collectors;

/**
 * Article 实体与 Elasticsearch 文档 {@link ArticleDocument} 的共享转换器。
 * <p>
 * 供 {@link ElasticsearchIndexInitializer}（全量同步）与
 * {@link ArticleEsSyncListener}（增量同步）复用，避免转换逻辑重复。
 */
public final class ArticleDocumentConverter {

    private ArticleDocumentConverter() {
    }

    /**
     * 将 Article 实体转换为 ES 文档。
     * <p>
     * 注意：调用方需先通过 {@code articleService.fillArticleInfo(article)}
     * 补齐分类名、标签列表与作者名，否则文档中相应字段为空。
     */
    public static ArticleDocument convert(Article article) {
        ArticleDocument doc = new ArticleDocument();
        doc.setId(article.getId());
        doc.setUserId(article.getUserId());
        doc.setCategoryId(article.getCategoryId());
        doc.setTitle(article.getTitle());
        doc.setSummary(article.getSummary());
        doc.setContent(article.getContent());

        // 优先使用冗余字段；为空时由 tagList 拼接
        String tags = article.getTags();
        if ((tags == null || tags.isEmpty()) && article.getTagList() != null) {
            tags = article.getTagList().stream()
                    .map(Tag::getName)
                    .collect(Collectors.joining(","));
        }
        doc.setTags(tags);

        doc.setStatus(article.getStatus());
        doc.setType(article.getType());
        doc.setCoverImage(article.getCoverImage());
        doc.setSourceUrl(article.getSourceUrl());
        doc.setSourceName(article.getSourceName());
        doc.setViewCount(article.getViewCount());
        doc.setLikeCount(article.getLikeCount());
        doc.setCommentCount(article.getCommentCount());
        doc.setIsTop(article.getIsTop());
        doc.setIsPublish(article.getIsPublish());
        doc.setCreateTime(article.getCreateTime());
        doc.setUpdateTime(article.getUpdateTime());
        doc.setCategoryName(article.getCategoryName());
        doc.setAuthorName(article.getAuthorName());
        return doc;
    }
}
