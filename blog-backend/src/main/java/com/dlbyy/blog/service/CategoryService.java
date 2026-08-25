package com.dlbyy.blog.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.dlbyy.blog.entity.Category;

import java.util.List;

public interface CategoryService extends IService<Category> {

    /**
     * 按名称查询分类，不存在则新建
     *
     * @param name 分类名称
     * @return 分类实体，name 为空白时返回 null
     */
    Category getOrCreateByName(String name);

    /**
     * 删除分类前校验文章引用，存在引用则抛出 BusinessException
     *
     * @param id 分类 ID
     */
    void deleteWithCheck(Long id);

    /**
     * 批量删除分类前校验文章引用，任一分类被引用则整批拒绝
     *
     * @param ids 分类 ID 列表
     */
    void batchDeleteWithCheck(List<Long> ids);
}
