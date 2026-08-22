package com.dlbyy.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.toolkit.Db;
import com.dlbyy.blog.entity.ExamPaperQuestion;
import com.dlbyy.blog.entity.ExamQuestion;
import com.dlbyy.blog.entity.ExamMarking;
import com.dlbyy.blog.entity.ExamRecord;
import com.dlbyy.blog.mapper.ExamPaperQuestionMapper;
import com.dlbyy.blog.mapper.ExamQuestionMapper;
import com.dlbyy.blog.mapper.ExamRecordMapper;
import com.dlbyy.blog.utils.JsoupXssUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 考试判分异步服务：交卷后异步完成客观题判分与主观题批改草稿生成。
 * <p>
 * 独立于 {@link ExamServiceImpl}，避免 {@code @Async} 自调用失效；
 * 异常全量捕获记录日志，不影响交卷主流程。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ExamJudgeAsyncService {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final ExamRecordMapper examRecordMapper;
    private final ExamPaperQuestionMapper examPaperQuestionMapper;
    private final ExamQuestionMapper examQuestionMapper;

    /**
     * 异步判分：读取试卷题目与考生原始答案，XSS 清洗后客观题判分，
     * 更新 objectiveScore 并批量生成主观题批改草稿。
     */
    @Async("examJudgeExecutor")
    @Transactional(rollbackFor = Exception.class)
    public void judge(Long recordId, Long paperId, Map<Long, Object> answerMap) {
        try {
            ExamRecord record = examRecordMapper.selectById(recordId);
            if (record == null) {
                log.error("异步判分失败：答卷不存在 | recordId={}", recordId);
                return;
            }
            List<ExamPaperQuestion> rels = examPaperQuestionMapper.selectList(
                    new LambdaQueryWrapper<ExamPaperQuestion>()
                            .eq(ExamPaperQuestion::getPaperId, paperId)
                            .orderByAsc(ExamPaperQuestion::getSortOrder));
            Map<Long, ExamQuestion> questionMap = rels.isEmpty() ? Map.of() :
                    examQuestionMapper.selectBatchIds(rels.stream()
                                    .map(ExamPaperQuestion::getQuestionId).collect(Collectors.toList())).stream()
                            .collect(Collectors.toMap(ExamQuestion::getId, Function.identity()));

            BigDecimal objectiveScore = BigDecimal.ZERO;
            List<ExamMarking> markings = new ArrayList<>();
            for (ExamPaperQuestion rel : rels) {
                ExamQuestion question = questionMap.get(rel.getQuestionId());
                if (question == null) {
                    continue;
                }
                Object myAnswer = answerMap.get(rel.getQuestionId());
                int type = question.getType() == null ? 0 : question.getType();
                if (type >= 1 && type <= 4) {
                    // 客观题自动判分
                    if (judgeObjective(type, question.getCorrect(), myAnswer)) {
                        objectiveScore = objectiveScore.add(rel.getScore() == null ? BigDecimal.ZERO : rel.getScore());
                    }
                } else if (type == 5 || type == 6) {
                    // 主观题：生成批改草稿（score 空），批量插入
                    ExamMarking marking = new ExamMarking();
                    marking.setRecordId(recordId);
                    marking.setQuestionId(rel.getQuestionId());
                    marking.setStatus(0);
                    markings.add(marking);
                }
            }
            record.setObjectiveScore(objectiveScore);
            examRecordMapper.updateById(record);
            if (!markings.isEmpty()) {
                Db.saveBatch(markings);
            }
        } catch (Exception e) {
            log.error("异步判分异常 | recordId={} | paperId={}", recordId, paperId, e);
        }
    }

    /**
     * 客观题判分：correct 为 JSON
     * 1 单选/3 判断：[index]，答案可为数字索引或单元素列表；
     * 2 多选：[i,j,...] 需与考生答案集合完全一致（顺序无关）；
     * 4 填空：["答案文本", ...]，忽略首尾空格、大小写不敏感，逐空比对全部一致。
     */
    boolean judgeObjective(int type, String correctJson, Object myAnswer) {
        if (correctJson == null || myAnswer == null) {
            return false;
        }
        try {
            List<Object> correct = OBJECT_MAPPER.readValue(correctJson, new TypeReference<List<Object>>() {});
            switch (type) {
                case 1:
                case 3: {
                    Integer idx = toIndex(myAnswer);
                    return idx != null && correct.size() == 1
                            && idx.equals(Integer.valueOf(String.valueOf(correct.get(0))));
                }
                case 2: {
                    Set<Integer> correctSet = correct.stream()
                            .map(c -> Integer.valueOf(String.valueOf(c))).collect(Collectors.toSet());
                    Set<Integer> mySet = toIndexSet(myAnswer);
                    return !mySet.isEmpty() && mySet.equals(correctSet);
                }
                case 4: {
                    List<String> myTexts = toStringList(myAnswer);
                    if (myTexts.size() != correct.size()) {
                        return false;
                    }
                    for (int i = 0; i < correct.size(); i++) {
                        String expect = String.valueOf(correct.get(i)).trim().toLowerCase();
                        String actual = myTexts.get(i) == null ? "" : myTexts.get(i).trim().toLowerCase();
                        if (!expect.equals(actual)) {
                            return false;
                        }
                    }
                    return true;
                }
                default:
                    return false;
            }
        } catch (JsonProcessingException e) {
            return false;
        }
    }

    /** 考生答案 → 单个选项索引 */
    private Integer toIndex(Object answer) {
        if (answer instanceof Number) {
            return ((Number) answer).intValue();
        }
        if (answer instanceof List<?> && !((List<?>) answer).isEmpty()) {
            return toIndex(((List<?>) answer).get(0));
        }
        try {
            return Integer.valueOf(String.valueOf(answer).trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /** 考生答案 → 选项索引集合 */
    private Set<Integer> toIndexSet(Object answer) {
        Set<Integer> set = new TreeSet<>();
        if (answer instanceof List<?>) {
            ((List<?>) answer).forEach(a -> {
                Integer idx = toIndex(a);
                if (idx != null) set.add(idx);
            });
        } else {
            Integer idx = toIndex(answer);
            if (idx != null) set.add(idx);
        }
        return set;
    }

    /** 考生答案 → 字符串列表（填空） */
    private List<String> toStringList(Object answer) {
        List<String> list = new ArrayList<>();
        if (answer instanceof List<?>) {
            ((List<?>) answer).forEach(a -> list.add(a == null ? "" : String.valueOf(a)));
        } else {
            list.add(String.valueOf(answer));
        }
        return list;
    }
}
