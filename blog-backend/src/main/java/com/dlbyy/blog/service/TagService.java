package com.dlbyy.blog.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.dlbyy.blog.entity.Tag;

public interface TagService extends IService<Tag> {

    /**
     * 按名称查询标签，不存在则新建
     *
     * @param name 标签名称
     * @return 标签实体，name 为空白时返回 null
     */
    Tag getOrCreateByName(String name);
}
