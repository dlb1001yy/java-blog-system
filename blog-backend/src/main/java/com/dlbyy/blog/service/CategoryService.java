package com.dlbyy.blog.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.dlbyy.blog.entity.Category;

public interface CategoryService extends IService<Category> {

    /**
     * 按名称查询分类，不存在则新建
     *
     * @param name 分类名称
     * @return 分类实体，name 为空白时返回 null
     */
    Category getOrCreateByName(String name);
}
