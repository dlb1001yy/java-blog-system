package com.dlbyy.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dlbyy.blog.entity.Category;
import com.dlbyy.blog.mapper.CategoryMapper;
import com.dlbyy.blog.service.CategoryService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {

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
}
