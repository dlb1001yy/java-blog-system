package com.dlbyy.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dlbyy.blog.common.PageResult;
import com.dlbyy.blog.entity.InterviewQuestion;
import com.dlbyy.blog.mapper.InterviewQuestionMapper;
import com.dlbyy.blog.service.CategoryService;
import com.dlbyy.blog.service.InterviewQuestionService;
import com.dlbyy.blog.service.TagService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.stream.Collectors;

/**
 * 面试题服务实现
 */
@Service
@RequiredArgsConstructor
public class InterviewQuestionServiceImpl extends ServiceImpl<InterviewQuestionMapper, InterviewQuestion>
        implements InterviewQuestionService {

    private final CategoryService categoryService;
    private final TagService tagService;

    @Override
    public PageResult<InterviewQuestion> pageQuery(int page, int size, String category, String difficulty, String keyword) {
        Page<InterviewQuestion> p = new Page<>(page, size);
        LambdaQueryWrapper<InterviewQuestion> wrapper = buildWrapper(category, difficulty, keyword);
        wrapper.eq(InterviewQuestion::getStatus, 1);
        this.page(p, wrapper.orderByDesc(InterviewQuestion::getUpdateTime));
        return new PageResult<>(p.getTotal(), p.getRecords());
    }

    @Override
    public InterviewQuestion getDetail(Long id) {
        return this.getById(id);
    }

    @Override
    public PageResult<InterviewQuestion> adminPage(int page, int size, String category, String difficulty,
                                                    String keyword, Integer status) {
        Page<InterviewQuestion> p = new Page<>(page, size);
        LambdaQueryWrapper<InterviewQuestion> wrapper = buildWrapper(category, difficulty, keyword);
        if (status != null) {
            wrapper.eq(InterviewQuestion::getStatus, status);
        }
        this.page(p, wrapper.orderByDesc(InterviewQuestion::getUpdateTime));
        return new PageResult<>(p.getTotal(), p.getRecords());
    }

    @Override
    public Long adminSave(InterviewQuestion question) {
        if (StringUtils.hasText(question.getCategory())) {
            categoryService.getOrCreateByName(question.getCategory().trim());
        }
        if (StringUtils.hasText(question.getTags())) {
            LinkedHashSet<String> tagSet = Arrays.stream(question.getTags().split("[,，]"))
                    .map(String::trim)
                    .filter(StringUtils::hasText)
                    .collect(Collectors.toCollection(LinkedHashSet::new));
            tagSet.forEach(tagService::getOrCreateByName);
            question.setTags(String.join(",", tagSet));
        }
        if (question.getId() == null) {
            this.save(question);
        } else {
            this.updateById(question);
        }
        return question.getId();
    }

    @Override
    public void adminDelete(Long id) {
        this.removeById(id);
    }

    @Override
    public java.util.List<String> listEnabledCategories() {
        return this.list(new LambdaQueryWrapper<InterviewQuestion>()
                        .select(InterviewQuestion::getCategory)
                        .eq(InterviewQuestion::getStatus, 1)
                        .isNotNull(InterviewQuestion::getCategory))
                .stream()
                .map(InterviewQuestion::getCategory)
                .filter(StringUtils::hasText)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    /** 构造通用筛选条件 */
    private LambdaQueryWrapper<InterviewQuestion> buildWrapper(String category, String difficulty, String keyword) {
        LambdaQueryWrapper<InterviewQuestion> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StringUtils.hasText(category), InterviewQuestion::getCategory, category)
                .eq(StringUtils.hasText(difficulty), InterviewQuestion::getDifficulty, difficulty)
                .and(StringUtils.hasText(keyword), w -> w
                        .like(InterviewQuestion::getTitle, keyword)
                        .or().like(InterviewQuestion::getTags, keyword));
        return wrapper;
    }
}
