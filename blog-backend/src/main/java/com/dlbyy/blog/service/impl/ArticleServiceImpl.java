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
        // Redis 仅做防刷去重：同一浏览器 1 小时内访问同一文章不重复计数
        String dedupKey = "article:view:dedup:" + id;
        if (!redisUtils.setIfAbsent(dedupKey, 1, 3600)) {
            return;
        }
        // DB 原子自增，避免并发读-改-写丢失更新。
        // 取舍说明：若此处写 DB 失败，仅丢失一次计数，可接受（简化方案，不做 Redis delta 补偿回写）
        baseMapper.addViewCount(id, 1);
    }

    @Override
    public void incrementLikeCount(Long id) {
        // DB 原子自增，避免并发丢更新
        baseMapper.addLikeCount(id, 1);
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
    @Transactional(rollbackFor = Exception.class)
    public Long saveArticleWithTags(Article article, List<Long> tagIds) {
        this.save(article);
        if (tagIds != null && !tagIds.isEmpty()) {
            saveArticleTags(article.getId(), tagIds);
        }
        return article.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateArticleWithTags(Article article, List<Long> tagIds) {
        this.updateById(article);
        if (tagIds != null) {
            updateArticleTags(article.getId(), tagIds);
        }
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