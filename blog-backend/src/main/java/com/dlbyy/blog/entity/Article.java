package com.dlbyy.blog.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 文章实体
 * <p>
 * 继承 {@link BaseEntity}，自动拥有 id / createTime / updateTime / isDeleted 字段。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("blog_article")
public class Article extends BaseEntity {

    private Long userId;
    private Long categoryId;

    /** 标签名（逗号分隔，冗余存储便于列表展示） */
    private String tags;

    /** 文章状态：0 草稿 / 1 已发布 / 2 下线 */
    private Integer status;

    private String title;
    private String summary;
    private String content;
    private String coverImage;
    private Integer type;          // 0:原创 1:转载 2:翻译
    private String sourceUrl;
    private String sourceName;
    private Integer viewCount;
    private Integer likeCount;
    private Integer commentCount;
    private Integer isTop;
    private Integer isPublish;

    // ---- 非数据库字段 ----

    @TableField(exist = false)
    private String categoryName;

    /** 关联标签对象列表（从 blog_article_tag 关联表查询填充） */
    @TableField(exist = false)
    private List<Tag> tagList;

    @TableField(exist = false)
    private String authorName;
}
