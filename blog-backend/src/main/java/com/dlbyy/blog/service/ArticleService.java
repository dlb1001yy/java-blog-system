package com.dlbyy.blog.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.dlbyy.blog.entity.Article;

import java.util.List;
import java.util.Map;

public interface ArticleService extends IService<Article> {
    
    void fillArticleInfo(Article article);
    
    void incrementViewCount(Long id);
    
    void incrementLikeCount(Long id);
    
    void saveArticleTags(Long articleId, List<Long> tagIds);

    void updateArticleTags(Long articleId, List<Long> tagIds);

    /**
     * 事务化保存文章及其标签关联（新增）
     * tagIds 元素可为数字 id 字符串或新建标签名称（allow-create 场景），
     * 非数字项通过 TagService.getOrCreateByName 解析为 id 后再写关联
     * @return 文章ID
     */
    Long saveArticleWithTags(Article article, List<String> tagIds);

    /**
     * 事务化更新文章及其标签关联，tagIds 解析规则同 saveArticleWithTags
     */
    void updateArticleWithTags(Article article, List<String> tagIds);
    
    List<Map<String, Object>> getArchives();
}