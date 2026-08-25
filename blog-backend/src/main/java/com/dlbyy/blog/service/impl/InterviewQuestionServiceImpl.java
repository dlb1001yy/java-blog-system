package com.dlbyy.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dlbyy.blog.common.PageResult;
import com.dlbyy.blog.common.exception.BusinessException;
import com.dlbyy.blog.entity.Category;
import com.dlbyy.blog.entity.InterviewQuestion;
import com.dlbyy.blog.entity.InterviewQuestionTag;
import com.dlbyy.blog.entity.Tag;
import com.dlbyy.blog.mapper.InterviewQuestionMapper;
import com.dlbyy.blog.mapper.InterviewQuestionTagMapper;
import com.dlbyy.blog.service.CategoryService;
import com.dlbyy.blog.service.InterviewQuestionService;
import com.dlbyy.blog.service.TagService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
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
    private final InterviewQuestionTagMapper interviewQuestionTagMapper;

    @Override
    public PageResult<InterviewQuestion> pageQuery(int page, int size, List<Long> categoryIds, String difficulty, String keyword) {
        Page<InterviewQuestion> p = new Page<>(page, size);
        LambdaQueryWrapper<InterviewQuestion> wrapper = buildWrapper(categoryIds, difficulty, keyword);
        wrapper.eq(InterviewQuestion::getStatus, 1);
        this.page(p, wrapper.orderByDesc(InterviewQuestion::getUpdateTime));
        fillExtra(p.getRecords());
        return new PageResult<>(p.getTotal(), p.getRecords());
    }

    @Override
    public InterviewQuestion getDetail(Long id) {
        InterviewQuestion question = this.getById(id);
        if (question != null) {
            fillExtra(Collections.singletonList(question));
        }
        return question;
    }

    @Override
    public PageResult<InterviewQuestion> adminPage(int page, int size, Long categoryId, String difficulty,
                                                    String keyword, Integer status) {
        Page<InterviewQuestion> p = new Page<>(page, size);
        LambdaQueryWrapper<InterviewQuestion> wrapper = buildWrapper(
                categoryId == null ? null : List.of(categoryId), difficulty, keyword);
        if (status != null) {
            wrapper.eq(InterviewQuestion::getStatus, status);
        }
        this.page(p, wrapper.orderByDesc(InterviewQuestion::getUpdateTime));
        fillExtra(p.getRecords());
        return new PageResult<>(p.getTotal(), p.getRecords());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long adminSave(InterviewQuestion question) {
        // 导入路径：categoryId 为空但传了分类名称，按名称创建/获取后转 id
        if (question.getCategoryId() == null && StringUtils.hasText(question.getCategoryName())) {
            Category category = categoryService.getOrCreateByName(question.getCategoryName().trim());
            if (category != null) {
                question.setCategoryId(category.getId());
            }
        }
        if (question.getCategoryId() == null) {
            throw new BusinessException("分类不能为空");
        }
        if (categoryService.getById(question.getCategoryId()) == null) {
            throw new BusinessException("分类不存在: " + question.getCategoryId());
        }

        List<Long> tagIds = question.getTagIds();
        // 导入路径：tagIds 为空但传了标签名称，按名称创建/获取后转 id
        if (CollectionUtils.isEmpty(tagIds) && !CollectionUtils.isEmpty(question.getTagNameList())) {
            LinkedHashSet<Long> ids = new LinkedHashSet<>();
            for (String name : question.getTagNameList()) {
                if (!StringUtils.hasText(name)) {
                    continue;
                }
                Tag tag = tagService.getOrCreateByName(name.trim());
                if (tag != null) {
                    ids.add(tag.getId());
                }
            }
            tagIds = List.copyOf(ids);
        }
        if (!CollectionUtils.isEmpty(tagIds)) {
            List<Long> missing = tagIds.stream()
                    .filter(id -> tagService.getById(id) == null)
                    .toList();
            if (!missing.isEmpty()) {
                throw new BusinessException("标签不存在: " + missing);
            }
        }

        if (question.getId() == null) {
            this.save(question);
        } else {
            this.updateById(question);
        }

        // 维护标签关联表（先删后插）
        interviewQuestionTagMapper.delete(new LambdaQueryWrapper<InterviewQuestionTag>()
                .eq(InterviewQuestionTag::getQuestionId, question.getId()));
        if (!CollectionUtils.isEmpty(tagIds)) {
            for (Long tagId : new LinkedHashSet<>(tagIds)) {
                InterviewQuestionTag rel = new InterviewQuestionTag();
                rel.setQuestionId(question.getId());
                rel.setTagId(tagId);
                interviewQuestionTagMapper.insert(rel);
            }
        }
        return question.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void adminDelete(Long id) {
        this.removeById(id);
        interviewQuestionTagMapper.delete(new LambdaQueryWrapper<InterviewQuestionTag>()
                .eq(InterviewQuestionTag::getQuestionId, id));
    }

    @Override
    public List<Category> listCategories() {
        return categoryService.list(new LambdaQueryWrapper<Category>()
                .orderByAsc(Category::getSort)
                .orderByAsc(Category::getId));
    }

    /** 构造通用筛选条件 */
    private LambdaQueryWrapper<InterviewQuestion> buildWrapper(List<Long> categoryIds, String difficulty, String keyword) {
        LambdaQueryWrapper<InterviewQuestion> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(!CollectionUtils.isEmpty(categoryIds), InterviewQuestion::getCategoryId, categoryIds)
                .eq(StringUtils.hasText(difficulty), InterviewQuestion::getDifficulty, difficulty)
                .like(StringUtils.hasText(keyword), InterviewQuestion::getTitle, keyword);
        return wrapper;
    }

    /** 批量填充 categoryName 与 tagNameList */
    private void fillExtra(List<InterviewQuestion> questions) {
        if (CollectionUtils.isEmpty(questions)) {
            return;
        }
        // 分类名称
        Set<Long> categoryIds = questions.stream()
                .map(InterviewQuestion::getCategoryId)
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toSet());
        if (!categoryIds.isEmpty()) {
            Map<Long, String> nameMap = categoryService.listByIds(categoryIds).stream()
                    .collect(Collectors.toMap(Category::getId, Category::getName, (a, b) -> a));
            questions.forEach(q -> q.setCategoryName(nameMap.get(q.getCategoryId())));
        }
        // 标签名称
        List<Long> questionIds = questions.stream()
                .map(InterviewQuestion::getId)
                .collect(Collectors.toList());
        List<InterviewQuestionTag> relations = interviewQuestionTagMapper.selectList(
                new LambdaQueryWrapper<InterviewQuestionTag>()
                        .in(InterviewQuestionTag::getQuestionId, questionIds));
        if (relations.isEmpty()) {
            return;
        }
        Map<Long, Tag> tagMap = tagService.listByIds(relations.stream()
                        .map(InterviewQuestionTag::getTagId).collect(Collectors.toSet())).stream()
                .collect(Collectors.toMap(Tag::getId, Function.identity(), (a, b) -> a));
        Map<Long, List<InterviewQuestionTag>> relByQuestion = relations.stream()
                .collect(Collectors.groupingBy(InterviewQuestionTag::getQuestionId));
        questions.forEach(q -> {
            List<InterviewQuestionTag> rels = relByQuestion.get(q.getId());
            if (rels == null) {
                return;
            }
            q.setTagIds(rels.stream().map(InterviewQuestionTag::getTagId).distinct().collect(Collectors.toList()));
            q.setTagNameList(rels.stream()
                    .map(InterviewQuestionTag::getTagId)
                    .map(tagMap::get)
                    .filter(java.util.Objects::nonNull)
                    .map(Tag::getName)
                    .distinct()
                    .collect(Collectors.toList()));
        });
    }
}
