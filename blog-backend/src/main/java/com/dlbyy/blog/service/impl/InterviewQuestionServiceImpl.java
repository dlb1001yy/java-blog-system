package com.dlbyy.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dlbyy.blog.common.PageResult;
import com.dlbyy.blog.entity.InterviewQuestion;
import com.dlbyy.blog.mapper.InterviewQuestionMapper;
import com.dlbyy.blog.service.InterviewQuestionService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * 面试题服务实现
 */
@Service
public class InterviewQuestionServiceImpl extends ServiceImpl<InterviewQuestionMapper, InterviewQuestion>
        implements InterviewQuestionService {

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
