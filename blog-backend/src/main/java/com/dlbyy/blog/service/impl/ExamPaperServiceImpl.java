package com.dlbyy.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dlbyy.blog.common.PageResult;
import com.dlbyy.blog.dto.ExamPortalQuestionDTO;
import com.dlbyy.blog.entity.ExamPaper;
import com.dlbyy.blog.entity.ExamPaperQuestion;
import com.dlbyy.blog.entity.ExamQuestion;
import com.dlbyy.blog.mapper.ExamPaperMapper;
import com.dlbyy.blog.mapper.ExamPaperQuestionMapper;
import com.dlbyy.blog.mapper.ExamQuestionMapper;
import com.dlbyy.blog.service.ExamPaperService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 试卷服务实现
 */
@Service
@RequiredArgsConstructor
public class ExamPaperServiceImpl extends ServiceImpl<ExamPaperMapper, ExamPaper> implements ExamPaperService {

    private final ExamPaperQuestionMapper examPaperQuestionMapper;
    private final ExamQuestionMapper examQuestionMapper;

    @Override
    public PageResult<ExamPaper> adminPage(int page, int size, String keyword, Integer status) {
        Page<ExamPaper> p = new Page<>(page, size);
        LambdaQueryWrapper<ExamPaper> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(keyword), ExamPaper::getTitle, keyword)
                .eq(status != null, ExamPaper::getStatus, status)
                .orderByDesc(ExamPaper::getUpdateTime);
        this.page(p, wrapper);
        fillQuestionCount(p.getRecords());
        return new PageResult<>(p.getTotal(), p.getRecords());
    }

    @Override
    public ExamPaper adminDetail(Long paperId) {
        ExamPaper paper = this.getById(paperId);
        if (paper != null) {
            Long count = examPaperQuestionMapper.selectCount(new LambdaQueryWrapper<ExamPaperQuestion>()
                    .eq(ExamPaperQuestion::getPaperId, paperId));
            paper.setQuestionCount(count == null ? 0 : count.intValue());
        }
        return paper;
    }

    @Override
    public Long adminSave(ExamPaper paper) {
        if (paper.getId() == null) {
            this.save(paper);
        } else {
            this.updateById(paper);
        }
        return paper.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void compose(Long paperId, List<Long> questionIds) {
        // 1. 旧关联题目 usage_count -1
        List<ExamPaperQuestion> oldList = examPaperQuestionMapper.selectList(
                new LambdaQueryWrapper<ExamPaperQuestion>().eq(ExamPaperQuestion::getPaperId, paperId));
        oldList.forEach(rel -> examPaperQuestionMapper.addUsageCount(rel.getQuestionId(), -1));

        // 2. 删除旧关联
        examPaperQuestionMapper.delete(new LambdaQueryWrapper<ExamPaperQuestion>()
                .eq(ExamPaperQuestion::getPaperId, paperId));

        // 3. 写入新关联（有序），usage_count +1
        if (questionIds != null && !questionIds.isEmpty()) {
            Set<Long> distinct = new HashSet<>(questionIds); // 去重，防唯一键冲突
            Map<Long, ExamQuestion> questionMap = examQuestionMapper.selectBatchIds(distinct).stream()
                    .collect(Collectors.toMap(ExamQuestion::getId, Function.identity()));
            int sort = 0;
            for (Long questionId : questionIds) {
                if (!distinct.contains(questionId)) {
                    continue; // 跳过重复项
                }
                distinct.remove(questionId);
                ExamQuestion question = questionMap.get(questionId);
                ExamPaperQuestion rel = new ExamPaperQuestion();
                rel.setPaperId(paperId);
                rel.setQuestionId(questionId);
                rel.setSortOrder(sort++);
                rel.setScore(question != null ? question.getScore() : null);
                examPaperQuestionMapper.insert(rel);
                examPaperQuestionMapper.addUsageCount(questionId, 1);
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void adminDelete(Long paperId) {
        this.removeById(paperId);
        List<ExamPaperQuestion> rels = examPaperQuestionMapper.selectList(
                new LambdaQueryWrapper<ExamPaperQuestion>().eq(ExamPaperQuestion::getPaperId, paperId));
        rels.forEach(rel -> examPaperQuestionMapper.addUsageCount(rel.getQuestionId(), -1));
        examPaperQuestionMapper.delete(new LambdaQueryWrapper<ExamPaperQuestion>()
                .eq(ExamPaperQuestion::getPaperId, paperId));
    }

    @Override
    public void publish(Long paperId, boolean publish) {
        ExamPaper paper = new ExamPaper();
        paper.setId(paperId);
        paper.setStatus(publish ? 1 : 2);
        this.updateById(paper);
    }

    @Override
    public PageResult<ExamPaper> portalPage(int page, int size, String keyword) {
        Page<ExamPaper> p = new Page<>(page, size);
        LambdaQueryWrapper<ExamPaper> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ExamPaper::getStatus, 1)
                .like(StringUtils.hasText(keyword), ExamPaper::getTitle, keyword)
                .orderByDesc(ExamPaper::getCreateTime);
        this.page(p, wrapper);
        fillQuestionCount(p.getRecords());
        return new PageResult<>(p.getTotal(), p.getRecords());
    }

    @Override
    public List<ExamPortalQuestionDTO> portalDetail(Long paperId) {
        List<ExamPaperQuestion> rels = examPaperQuestionMapper.selectList(
                new LambdaQueryWrapper<ExamPaperQuestion>()
                        .eq(ExamPaperQuestion::getPaperId, paperId)
                        .orderByAsc(ExamPaperQuestion::getSortOrder));
        if (rels.isEmpty()) {
            return new ArrayList<>();
        }
        Map<Long, ExamQuestion> questionMap = examQuestionMapper.selectBatchIds(
                        rels.stream().map(ExamPaperQuestion::getQuestionId).collect(Collectors.toList()))
                .stream().collect(Collectors.toMap(ExamQuestion::getId, Function.identity()));
        return rels.stream()
                .filter(rel -> questionMap.containsKey(rel.getQuestionId()))
                .map(rel -> {
                    ExamQuestion q = questionMap.get(rel.getQuestionId());
                    // 门户侧不返回 correct / reference_answer
                    return ExamPortalQuestionDTO.of(q.getId(), q.getStem(), q.getType(), q.getCategory(),
                            q.getDifficulty(), q.getOptions(), rel.getScore(), rel.getSortOrder());
                })
                .collect(Collectors.toList());
    }

    /** 填充题目数量 */
    private void fillQuestionCount(List<ExamPaper> papers) {
        for (ExamPaper paper : papers) {
            Long count = examPaperQuestionMapper.selectCount(new LambdaQueryWrapper<ExamPaperQuestion>()
                    .eq(ExamPaperQuestion::getPaperId, paper.getId()));
            paper.setQuestionCount(count == null ? 0 : count.intValue());
        }
    }
}
