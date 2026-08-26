package com.dlbyy.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dlbyy.blog.common.exception.BusinessException;
import com.dlbyy.blog.entity.Article;
import com.dlbyy.blog.entity.ArticleTag;
import com.dlbyy.blog.entity.Tag;
import com.dlbyy.blog.mapper.ArticleMapper;
import com.dlbyy.blog.mapper.ArticleTagMapper;
import com.dlbyy.blog.mapper.TagMapper;
import com.dlbyy.blog.service.TagService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class TagServiceImpl extends ServiceImpl<TagMapper, Tag> implements TagService {

    private final ArticleTagMapper articleTagMapper;
    private final ArticleMapper articleMapper;

    public TagServiceImpl(ArticleTagMapper articleTagMapper, ArticleMapper articleMapper) {
        this.articleTagMapper = articleTagMapper;
        this.articleMapper = articleMapper;
    }


    @Override
    public Tag getOrCreateByName(String name) {
        if (!StringUtils.hasText(name)) {
            return null;
        }
        Tag tag = getOne(new LambdaQueryWrapper<Tag>().eq(Tag::getName, name));
        if (tag == null) {
            tag = new Tag();
            tag.setName(name);
            save(tag);
        }
        return tag;
    }

    @Override
    public void checkBeforeDelete(List<Long> tagIds) {
        if (tagIds == null || tagIds.isEmpty()) {
            return;
        }
        List<ArticleTag> relations = articleTagMapper.selectList(
                new LambdaQueryWrapper<ArticleTag>().in(ArticleTag::getTagId, tagIds)
        );
        if (relations.isEmpty()) {
            return;
        }
        // 过滤已被逻辑删除的文章（关联记录未同步清理的孤儿数据），并顺手清理
        Set<Long> articleIds = relations.stream().map(ArticleTag::getArticleId).collect(Collectors.toSet());
        List<Long> aliveArticleIds = articleMapper.selectBatchIds(articleIds).stream()
                .map(Article::getId)
                .collect(Collectors.toList());
        Set<Long> aliveSet = aliveArticleIds.stream().collect(Collectors.toSet());
        Set<Long> orphanArticleIds = articleIds.stream()
                .filter(id -> !aliveSet.contains(id))
                .collect(Collectors.toSet());
        if (!orphanArticleIds.isEmpty()) {
            articleTagMapper.delete(new LambdaQueryWrapper<ArticleTag>()
                    .in(ArticleTag::getArticleId, orphanArticleIds));
            relations = relations.stream()
                    .filter(r -> !orphanArticleIds.contains(r.getArticleId()))
                    .collect(Collectors.toList());
            if (relations.isEmpty()) {
                return;
            }
        }
        Map<Long, Long> countByTagId = relations.stream()
                .collect(Collectors.groupingBy(ArticleTag::getTagId, Collectors.counting()));
        Map<Long, String> nameById = listByIds(countByTagId.keySet()).stream()
                .collect(Collectors.toMap(Tag::getId, Tag::getName));
        String detail = countByTagId.entrySet().stream()
                .map(e -> "标签[" + nameById.getOrDefault(e.getKey(), String.valueOf(e.getKey()))
                        + "]已被" + e.getValue() + "篇文章使用，无法删除")
                .collect(Collectors.joining("；"));
        throw new BusinessException(detail);
    }

    @Override
    public void removeRelationsByArticleIds(List<Long> articleIds) {
        if (articleIds == null || articleIds.isEmpty()) {
            return;
        }
        articleTagMapper.delete(new LambdaQueryWrapper<ArticleTag>()
                .in(ArticleTag::getArticleId, articleIds));
    }
}
