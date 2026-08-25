package com.dlbyy.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dlbyy.blog.common.exception.BusinessException;
import com.dlbyy.blog.entity.Article;
import com.dlbyy.blog.entity.Category;
import com.dlbyy.blog.mapper.ArticleMapper;
import com.dlbyy.blog.mapper.CategoryMapper;
import com.dlbyy.blog.service.CategoryService;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {

    private final ArticleMapper articleMapper;

    public CategoryServiceImpl(ArticleMapper articleMapper) {
        this.articleMapper = articleMapper;
    }

    @Override
    public Category getOrCreateByName(String name) {
        if (!StringUtils.hasText(name)) {
            return null;
        }
        Category category = getOne(new LambdaQueryWrapper<Category>().eq(Category::getName, name));
        if (category == null) {
            category = new Category();
            category.setName(name);
            save(category);
        }
        return category;
    }

    @Override
    public void deleteWithCheck(Long id) {
        batchDeleteWithCheck(List.of(id));
    }

    @Override
    public void batchDeleteWithCheck(List<Long> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return;
        }
        List<Category> categories = listByIds(ids);
        List<Article> articles = articleMapper.selectList(
                new LambdaQueryWrapper<Article>().in(Article::getCategoryId, ids).select(Article::getCategoryId));
        Map<Long, Long> countByCategoryId = articles.stream()
                .collect(Collectors.groupingBy(Article::getCategoryId, Collectors.counting()));
        List<String> messages = categories.stream()
                .filter(c -> countByCategoryId.getOrDefault(c.getId(), 0L) > 0)
                .map(c -> "分类[" + c.getName() + "]下存在 " + countByCategoryId.get(c.getId()) + " 篇文章，无法删除")
                .collect(Collectors.toList());
        if (!messages.isEmpty()) {
            throw new BusinessException(String.join("；", messages));
        }
        removeByIds(ids);
    }
}
