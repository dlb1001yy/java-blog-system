package com.dlbyy.blog.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("blog_article_tag")
public class ArticleTag {
    private Long articleId;
    private Long tagId;
}