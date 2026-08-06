package com.dlbyy.blog.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * 文章发布/更新事件
 * <p>
 * 在文章新增、更新、发布状态切换、删除时由 Controller 发布。
 * 监听器 {@code ArticleEsSyncListener} 异步消费此事件，
 * 将文章数据同步至 Elasticsearch 索引 blog_article。
 */
@Getter
public class ArticlePublishedEvent extends ApplicationEvent {

    private final Long articleId;
    private final EventType eventType;

    public ArticlePublishedEvent(Object source, Long articleId, EventType eventType) {
        super(source);
        this.articleId = articleId;
        this.eventType = eventType;
    }

    /**
     * 事件类型
     */
    public enum EventType {
        /** 新增 */
        CREATED,
        /** 更新 */
        UPDATED,
        /** 发布状态切换 */
        PUBLISHED,
        /** 删除 */
        DELETED
    }
}
