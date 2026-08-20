package com.dlbyy.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dlbyy.blog.common.PageResult;
import com.dlbyy.blog.entity.InterviewFavorite;
import com.dlbyy.blog.entity.InterviewQuestion;
import com.dlbyy.blog.mapper.InterviewFavoriteMapper;
import com.dlbyy.blog.mapper.InterviewQuestionMapper;
import com.dlbyy.blog.service.InterviewFavoriteService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 面试题收藏/错题本服务实现
 */
@Service
@RequiredArgsConstructor
public class InterviewFavoriteServiceImpl extends ServiceImpl<InterviewFavoriteMapper, InterviewFavorite>
        implements InterviewFavoriteService {

    private final InterviewQuestionMapper interviewQuestionMapper;

    @Override
    public boolean toggle(Long userId, Long questionId, Integer type) {
        InterviewFavorite existing = this.getOne(new LambdaQueryWrapper<InterviewFavorite>()
                .eq(InterviewFavorite::getUserId, userId)
                .eq(InterviewFavorite::getQuestionId, questionId)
                .eq(InterviewFavorite::getType, type));
        if (existing != null) {
            this.removeById(existing.getId());
            return false;
        }
        InterviewFavorite favorite = new InterviewFavorite();
        favorite.setUserId(userId);
        favorite.setQuestionId(questionId);
        favorite.setType(type);
        this.save(favorite);
        return true;
    }

    @Override
    public PageResult<InterviewFavorite> pageByUser(int page, int size, Long userId, Integer type) {
        Page<InterviewFavorite> p = new Page<>(page, size);
        this.page(p, new LambdaQueryWrapper<InterviewFavorite>()
                .eq(InterviewFavorite::getUserId, userId)
                .eq(InterviewFavorite::getType, type)
                .orderByDesc(InterviewFavorite::getCreateTime));

        // 批量填充题目对象
        List<InterviewFavorite> records = p.getRecords();
        if (!records.isEmpty()) {
            List<Long> questionIds = records.stream()
                    .map(InterviewFavorite::getQuestionId).distinct().collect(Collectors.toList());
            Map<Long, InterviewQuestion> questionMap = interviewQuestionMapper.selectBatchIds(questionIds).stream()
                    .collect(Collectors.toMap(InterviewQuestion::getId, q -> q));
            records.forEach(f -> f.setQuestion(questionMap.get(f.getQuestionId())));
        }
        return new PageResult<>(p.getTotal(), records);
    }
}
