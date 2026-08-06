package com.dlbyy.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.dlbyy.blog.entity.*;
import com.dlbyy.blog.mapper.*;
import com.dlbyy.blog.service.ArticleService;
import com.dlbyy.blog.utils.RedisUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ArticleServiceImpl extends ServiceImpl<ArticleMapper, Article> implements ArticleService {

    private final CategoryMapper categoryMapper;
    private final TagMapper tagMapper;
    private final ArticleTagMapper articleTagMapper;
    private final UserMapper userMapper;
//    private final RedisTemplate<String, Object> redisTemplate;
    private final RedisUtils redisUtils;

    @Override
    public void fillArticleInfo(Article article) {
        if (article == null) return;
        
        // 填充分类名
        if (article.getCategoryId() != null) {
            Category category = categoryMapper.selectById(article.getCategoryId());
            if (category != null) {
                article.setCategoryName(category.getName());
            }
        }
        
        // 填充标签
        LambdaQueryWrapper<ArticleTag> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ArticleTag::getArticleId, article.getId());
        List<ArticleTag> articleTags = articleTagMapper.selectList(wrapper);
        if (!articleTags.isEmpty()) {
            List<Long> tagIds = articleTags.stream()
                    .map(ArticleTag::getTagId)
                    .collect(Collectors.toList());
            List<Tag> tags = tagMapper.selectBatchIds(tagIds);
            article.setTagList(tags);
        }
        
        // 填充作者名
        if (article.getUserId() != null) {
            User user = userMapper.selectById(article.getUserId());
            if (user != null) {
                article.setAuthorName(user.getNickname());
            }
        }
    }

    @Override
    public void incrementViewCount(Long id) {
        // 使用Redis缓存浏览量，定时同步到数据库
        String key = "article:view:" + id;
//        redisTemplate.opsForValue().increment(key);
        // 替换后 (使用上一问生成的 RedisUtils)
        redisUtils.increment(key);
        // 简化方案：直接更新数据库
        Article article = this.getById(id);
        if (article != null) {
            article.setViewCount(article.getViewCount() + 1);
            this.updateById(article);
        }
    }

    @Override
    public void incrementLikeCount(Long id) {
        Article article = this.getById(id);
        if (article != null) {
            article.setLikeCount(article.getLikeCount() + 1);
            this.updateById(article);
        }
    }

    @Override
    @Transactional
    public void saveArticleTags(Long articleId, List<Long> tagIds) {
        for (Long tagId : tagIds) {
            ArticleTag articleTag = new ArticleTag();
            articleTag.setArticleId(articleId);
            articleTag.setTagId(tagId);
            articleTagMapper.insert(articleTag);
        }
    }

    @Override
    @Transactional
    public void updateArticleTags(Long articleId, List<Long> tagIds) {
        // 先删除旧关联
        articleTagMapper.delete(new LambdaQueryWrapper<ArticleTag>()
                .eq(ArticleTag::getArticleId, articleId));
        // 再添加新关联
        saveArticleTags(articleId, tagIds);
    }

    @Override
    public List<Map<String, Object>> getArchives() {
        List<Article> articles = this.list(new LambdaQueryWrapper<Article>()
                .eq(Article::getIsPublish, 1)
                .select(Article::getId, Article::getTitle, Article::getCreateTime)
                .orderByDesc(Article::getCreateTime));
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM");
        Map<String, List<Map<String, Object>>> grouped = new TreeMap<>(Collections.reverseOrder());
        
        for (Article article : articles) {
            String month = article.getCreateTime().format(formatter);
            grouped.computeIfAbsent(month, k -> new ArrayList<>());
            Map<String, Object> item = new HashMap<>();
            item.put("id", article.getId());
            item.put("title", article.getTitle());
            item.put("date", article.getCreateTime());
            grouped.get(month).add(item);
        }
        
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map.Entry<String, List<Map<String, Object>>> entry : grouped.entrySet()) {
            Map<String, Object> group = new HashMap<>();
            group.put("month", entry.getKey());
            group.put("count", entry.getValue().size());
            group.put("articles", entry.getValue());
            result.add(group);
        }
        
        return result;
    }
}