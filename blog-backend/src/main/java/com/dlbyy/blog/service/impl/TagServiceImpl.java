package com.dlbyy.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dlbyy.blog.common.exception.BusinessException;
import com.dlbyy.blog.entity.ArticleTag;
import com.dlbyy.blog.entity.Tag;
import com.dlbyy.blog.mapper.ArticleTagMapper;
import com.dlbyy.blog.mapper.TagMapper;
import com.dlbyy.blog.service.TagService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class TagServiceImpl extends ServiceImpl<TagMapper, Tag> implements TagService {

    private final ArticleTagMapper articleTagMapper;

    public TagServiceImpl(ArticleTagMapper articleTagMapper) {
        this.articleTagMapper = articleTagMapper;
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
}
