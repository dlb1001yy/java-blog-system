package com.dlbyy.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dlbyy.blog.entity.Tag;
import com.dlbyy.blog.mapper.TagMapper;
import com.dlbyy.blog.service.TagService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class TagServiceImpl extends ServiceImpl<TagMapper, Tag> implements TagService {

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
}
