package com.dlbyy.blog.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.dlbyy.blog.entity.Tag;

import java.util.List;

public interface TagService extends IService<Tag> {

    /**
     * 按名称查询标签，不存在则新建
     *
     * @param name 标签名称
     * @return 标签实体，name 为空白时返回 null
     */
    Tag getOrCreateByName(String name);

    /**
     * 删除前校验标签是否被文章关联，若被关联则抛出业务异常（含所有被关联标签信息）
     *
     * @param tagIds 待删除标签 id 集合
     */
    void checkBeforeDelete(List<Long> tagIds);
}
