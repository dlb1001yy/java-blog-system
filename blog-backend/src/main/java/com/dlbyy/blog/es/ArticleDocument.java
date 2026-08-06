package com.dlbyy.blog.es;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.LocalDateTime;

/**
 * 文章 Elasticsearch 文档实体
 * <p>
 * 对应索引 blog_article，用于全文检索与高亮展示。
 * <p>
 * 注意：title / summary / content 使用 ik_max_word 分词器，
 * 需在 Elasticsearch 中安装 IK 分词器插件：
 * <pre>
 * ./bin/elasticsearch-plugin install https://github.com/medcl/elasticsearch-analysis-ik/releases/download/v8.x/elasticsearch-analysis-ik-8.x.zip
 * </pre>
 */
@Data
@Document(indexName = "blog_article")
public class ArticleDocument {

    @Id
    @Field(type = FieldType.Long)
    private Long id;

    @Field(type = FieldType.Long)
    private Long userId;

    @Field(type = FieldType.Long)
    private Long categoryId;

    /** 文章标题（中文分词，便于全文检索） */
    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
    private String title;

    /** 文章摘要 */
    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
    private String summary;

    /** 文章正文（Markdown 原文，可检索） */
    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
    private String content;

    /** 标签名（逗号分隔，关键词精确匹配） */
    @Field(type = FieldType.Keyword)
    private String tags;

    /** 文章状态：0 草稿 / 1 已发布 / 2 下线 */
    @Field(type = FieldType.Integer)
    private Integer status;

    /** 文章类型：0 原创 / 1 转载 / 2 翻译 */
    @Field(type = FieldType.Integer)
    private Integer type;

    @Field(type = FieldType.Keyword)
    private String coverImage;

    @Field(type = FieldType.Keyword)
    private String sourceUrl;

    @Field(type = FieldType.Keyword)
    private String sourceName;

    @Field(type = FieldType.Integer)
    private Integer viewCount;

    @Field(type = FieldType.Integer)
    private Integer likeCount;

    @Field(type = FieldType.Integer)
    private Integer commentCount;

    /** 是否置顶：0 否 / 1 是 */
    @Field(type = FieldType.Integer)
    private Integer isTop;

    /** 是否发布：0 否 / 1 是 */
    @Field(type = FieldType.Integer)
    private Integer isPublish;

    @Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second_millis)
    private LocalDateTime createTime;

    @Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second_millis)
    private LocalDateTime updateTime;

    @Field(type = FieldType.Keyword)
    private String categoryName;

    @Field(type = FieldType.Keyword)
    private String authorName;
}
