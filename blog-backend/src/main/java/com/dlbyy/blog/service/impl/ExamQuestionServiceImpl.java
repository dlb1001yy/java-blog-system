package com.dlbyy.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dlbyy.blog.common.PageResult;
import com.dlbyy.blog.entity.ExamQuestion;
import com.dlbyy.blog.mapper.ExamQuestionMapper;
import com.dlbyy.blog.service.ExamQuestionService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 考试题目服务实现
 */
@Service
public class ExamQuestionServiceImpl extends ServiceImpl<ExamQuestionMapper, ExamQuestion> implements ExamQuestionService {

    @Override
    public PageResult<ExamQuestion> adminPage(int page, int size, Integer type, String category,
                                               String difficulty, String keyword, Integer status) {
        Page<ExamQuestion> p = new Page<>(page, size);
        LambdaQueryWrapper<ExamQuestion> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(type != null, ExamQuestion::getType, type)
                .eq(StringUtils.hasText(category), ExamQuestion::getCategory, category)
                .eq(StringUtils.hasText(difficulty), ExamQuestion::getDifficulty, difficulty)
                .eq(status != null, ExamQuestion::getStatus, status)
                .like(StringUtils.hasText(keyword), ExamQuestion::getStem, keyword)
                .orderByDesc(ExamQuestion::getUpdateTime);
        this.page(p, wrapper);
        return new PageResult<>(p.getTotal(), p.getRecords());
    }

    @Override
    public Long adminSave(ExamQuestion question) {
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
    public Map<Integer, Long> countByType() {
        // 仅统计未删除题目，6 种题型缺失的补 0
        List<ExamQuestion> questions = this.list(new LambdaQueryWrapper<ExamQuestion>()
                .select(ExamQuestion::getType));
        Map<Integer, Long> countMap = questions.stream()
                .collect(Collectors.groupingBy(ExamQuestion::getType, Collectors.counting()));
        Map<Integer, Long> result = new HashMap<>();
        for (int type = 1; type <= 6; type++) {
            result.put(type, countMap.getOrDefault(type, 0L));
        }
        return result;
    }
}
