package com.dlbyy.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dlbyy.blog.common.PageResult;
import com.dlbyy.blog.common.exception.BusinessException;
import com.dlbyy.blog.dto.ExamAnswerDTO;
import com.dlbyy.blog.dto.ExamMarkingDTO;
import com.dlbyy.blog.dto.ExamRecordDetailDTO;
import com.dlbyy.blog.dto.ExamRecordDetailItemDTO;
import com.dlbyy.blog.dto.ExamSubmitDTO;
import com.dlbyy.blog.entity.ExamMarking;
import com.dlbyy.blog.entity.ExamPaper;
import com.dlbyy.blog.entity.ExamPaperQuestion;
import com.dlbyy.blog.entity.ExamQuestion;
import com.dlbyy.blog.entity.ExamRecord;
import com.dlbyy.blog.mapper.ExamMarkingMapper;
import com.dlbyy.blog.mapper.ExamPaperMapper;
import com.dlbyy.blog.mapper.ExamPaperQuestionMapper;
import com.dlbyy.blog.mapper.ExamQuestionMapper;
import com.dlbyy.blog.mapper.ExamRecordMapper;
import com.dlbyy.blog.service.ExamService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 考试核心业务实现：交卷自动判分、主观题批改、成绩详情
 */
@Service
@RequiredArgsConstructor
public class ExamServiceImpl implements ExamService {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final ExamRecordMapper examRecordMapper;
    private final ExamPaperMapper examPaperMapper;
    private final ExamPaperQuestionMapper examPaperQuestionMapper;
    private final ExamQuestionMapper examQuestionMapper;
    private final ExamMarkingMapper examMarkingMapper;

    // ==================== 交卷 ====================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long submitPaper(Long userId, ExamSubmitDTO dto) {
        Long paperId = dto.getPaperId();
        if (paperId == null) {
            throw new BusinessException("试卷ID不能为空");
        }
        ExamPaper paper = examPaperMapper.selectById(paperId);
        if (paper == null || paper.getStatus() == null || paper.getStatus() != 1) {
            throw new BusinessException("试卷不存在或未发布");
        }

        // 本卷题目（含分值与顺序）
        List<ExamPaperQuestion> rels = examPaperQuestionMapper.selectList(
                new LambdaQueryWrapper<ExamPaperQuestion>()
                        .eq(ExamPaperQuestion::getPaperId, paperId)
                        .orderByAsc(ExamPaperQuestion::getSortOrder));
        Map<Long, ExamQuestion> questionMap = examQuestionMapper.selectBatchIds(
                        rels.stream().map(ExamPaperQuestion::getQuestionId).collect(Collectors.toList()))
                .stream().collect(Collectors.toMap(ExamQuestion::getId, Function.identity()));

        // 考生答案索引
        Map<Long, Object> answerMap = dto.getAnswers() == null ? Map.of() :
                dto.getAnswers().stream()
                        .filter(a -> a.getQuestionId() != null)
                        .collect(Collectors.toMap(ExamAnswerDTO::getQuestionId, ExamAnswerDTO::getAnswer,
                                (a, b) -> b));

        BigDecimal objectiveScore = BigDecimal.ZERO;
        List<Map<String, Object>> storedAnswers = new ArrayList<>();

        for (ExamPaperQuestion rel : rels) {
            ExamQuestion question = questionMap.get(rel.getQuestionId());
            if (question == null) {
                continue;
            }
            Object myAnswer = answerMap.get(rel.getQuestionId());
            storedAnswers.add(Map.of("questionId", rel.getQuestionId(), "answer", myAnswer == null ? "" : myAnswer));

            int type = question.getType() == null ? 0 : question.getType();
            if (type >= 1 && type <= 4) {
                // 客观题自动判分
                if (judgeObjective(type, question.getCorrect(), myAnswer)) {
                    objectiveScore = objectiveScore.add(rel.getScore() == null ? BigDecimal.ZERO : rel.getScore());
                }
            } else {
                // 主观题（5 简答 / 6 编程）：保存记录后统一生成批改草稿
            }
        }

        // 保存答卷记录
        ExamRecord record = new ExamRecord();
        record.setPaperId(paperId);
        record.setUserId(userId);
        record.setAnswers(toJson(storedAnswers));
        record.setObjectiveScore(objectiveScore);
        record.setSwitchCount(dto.getSwitchCount() == null ? 0 : dto.getSwitchCount());
        record.setDurationSeconds(dto.getDurationSeconds());
        record.setStatus(0); // 待批改
        record.setSubmitTime(LocalDateTime.now());
        examRecordMapper.insert(record);

        // 主观题生成批改草稿（score 空）
        for (ExamPaperQuestion rel : rels) {
            ExamQuestion question = questionMap.get(rel.getQuestionId());
            if (question == null) continue;
            int type = question.getType() == null ? 0 : question.getType();
            if (type == 5 || type == 6) {
                ExamMarking marking = new ExamMarking();
                marking.setRecordId(record.getId());
                marking.setQuestionId(rel.getQuestionId());
                marking.setStatus(0);
                examMarkingMapper.insert(marking);
            }
        }
        return record.getId();
    }

    /**
     * 客观题判分：correct 为 JSON
     * 1 单选/3 判断：[index]，答案可为数字索引或单元素列表；
     * 2 多选：[i,j,...] 需与考生答案集合完全一致（顺序无关）；
     * 4 填空：["答案文本", ...]，忽略首尾空格、大小写不敏感，逐空比对全部一致。
     */
    private boolean judgeObjective(int type, String correctJson, Object myAnswer) {
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

    private String toJson(Object obj) {
        try {
            return OBJECT_MAPPER.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new BusinessException("答案序列化失败");
        }
    }

    // ==================== 批改 ====================

    @Override
    public PageResult<ExamRecord> pendingPage(int page, int size, String keyword) {
        IPage<ExamRecord> p = examRecordMapper.selectPendingPage(new Page<>(page, size), keyword, 0);
        return new PageResult<>(p.getTotal(), p.getRecords());
    }

    @Override
    public long countPendingMarking() {
        return examRecordMapper.selectCount(
                new LambdaQueryWrapper<ExamRecord>().eq(ExamRecord::getStatus, 0));
    }

    @Override
    public ExamRecordDetailDTO markingDetail(Long recordId) {
        return buildDetail(recordId, true);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveMarking(Long recordId, List<ExamMarkingDTO> markings, boolean submit) {
        if (markings == null || markings.isEmpty()) {
            throw new BusinessException("批改数据不能为空");
        }
        ExamRecord record = examRecordMapper.selectById(recordId);
        if (record == null) {
            throw new BusinessException("答卷不存在");
        }
        if (record.getStatus() != null && record.getStatus() == 1) {
            throw new BusinessException("该答卷已发布，不能重复批改");
        }

        BigDecimal subjectiveScore = BigDecimal.ZERO;
        for (ExamMarkingDTO dto : markings) {
            ExamMarking marking = examMarkingMapper.selectOne(new LambdaQueryWrapper<ExamMarking>()
                    .eq(ExamMarking::getRecordId, recordId)
                    .eq(ExamMarking::getQuestionId, dto.getQuestionId()));
            if (marking == null) {
                continue;
            }
            marking.setScore(dto.getScore());
            marking.setComment(dto.getComment());
            marking.setStatus(submit ? 1 : 0);
            examMarkingMapper.updateById(marking);
        }

        if (submit) {
            // 汇总所有主观题得分
            List<ExamMarking> all = examMarkingMapper.selectList(new LambdaQueryWrapper<ExamMarking>()
                    .eq(ExamMarking::getRecordId, recordId));
            for (ExamMarking m : all) {
                if (m.getScore() != null) {
                    subjectiveScore = subjectiveScore.add(m.getScore());
                }
            }
            record.setSubjectiveScore(subjectiveScore);
            record.setFinalScore(subjectiveScore.add(record.getObjectiveScore() == null
                    ? BigDecimal.ZERO : record.getObjectiveScore()));
            record.setStatus(1); // 已发布
            examRecordMapper.updateById(record);
        }
    }

    // ==================== 我的成绩 ====================

    @Override
    public PageResult<ExamRecord> myRecords(Long userId, int page, int size) {
        Page<ExamRecord> p = new Page<>(page, size);
        examRecordMapper.selectPage(p, new LambdaQueryWrapper<ExamRecord>()
                .eq(ExamRecord::getUserId, userId)
                .orderByDesc(ExamRecord::getSubmitTime));
        // 填充试卷名
        if (!p.getRecords().isEmpty()) {
            Set<Long> paperIds = p.getRecords().stream().map(ExamRecord::getPaperId).collect(Collectors.toSet());
            Map<Long, String> titleMap = examPaperMapper.selectBatchIds(paperIds).stream()
                    .collect(Collectors.toMap(ExamPaper::getId, ExamPaper::getTitle));
            p.getRecords().forEach(r -> r.setPaperTitle(titleMap.get(r.getPaperId())));
        }
        return new PageResult<>(p.getTotal(), p.getRecords());
    }

    @Override
    public ExamRecordDetailDTO recordDetail(Long recordId, Long currentUserId, String currentUserRole) {
        ExamRecord record = examRecordMapper.selectById(recordId);
        if (record == null) {
            throw new BusinessException("答卷不存在");
        }
        boolean isAdmin = "admin".equalsIgnoreCase(currentUserRole) || "ROLE_ADMIN".equalsIgnoreCase(currentUserRole);
        if (!isAdmin && !record.getUserId().equals(currentUserId)) {
            throw new BusinessException("无权查看该答卷");
        }
        return buildDetail(recordId, false);
    }

    // ==================== 详情组装 ====================

    /**
     * 组装答卷详情
     *
     * @param forMarking true 批改视角（含参考答案与评分草稿）；false 考生视角（已发布后含解析）
     */
    private ExamRecordDetailDTO buildDetail(Long recordId, boolean forMarking) {
        ExamRecord record = examRecordMapper.selectById(recordId);
        if (record == null) {
            throw new BusinessException("答卷不存在");
        }
        ExamPaper paper = examPaperMapper.selectById(record.getPaperId());

        ExamRecordDetailDTO dto = new ExamRecordDetailDTO();
        dto.setRecordId(record.getId());
        dto.setPaperId(record.getPaperId());
        dto.setPaperTitle(paper == null ? null : paper.getTitle());
        dto.setObjectiveScore(record.getObjectiveScore());
        dto.setSubjectiveScore(record.getSubjectiveScore());
        dto.setFinalScore(record.getFinalScore());
        dto.setStatus(record.getStatus());
        dto.setSwitchCount(record.getSwitchCount());
        dto.setDurationSeconds(record.getDurationSeconds());
        dto.setSubmitTime(record.getSubmitTime());

        // 题目与顺序
        List<ExamPaperQuestion> rels = examPaperQuestionMapper.selectList(
                new LambdaQueryWrapper<ExamPaperQuestion>()
                        .eq(ExamPaperQuestion::getPaperId, record.getPaperId())
                        .orderByAsc(ExamPaperQuestion::getSortOrder));
        Map<Long, ExamQuestion> questionMap = rels.isEmpty() ? Map.of() :
                examQuestionMapper.selectBatchIds(rels.stream()
                                .map(ExamPaperQuestion::getQuestionId).collect(Collectors.toList())).stream()
                        .collect(Collectors.toMap(ExamQuestion::getId, Function.identity()));

        // 我的答案
        Map<Long, Object> myAnswerMap = parseAnswers(record.getAnswers());

        // 批改草稿
        Map<Long, ExamMarking> markingMap = examMarkingMapper.selectList(
                        new LambdaQueryWrapper<ExamMarking>().eq(ExamMarking::getRecordId, recordId)).stream()
                .collect(Collectors.toMap(ExamMarking::getQuestionId, Function.identity()));

        List<ExamRecordDetailItemDTO> items = new ArrayList<>();
        for (ExamPaperQuestion rel : rels) {
            ExamQuestion question = questionMap.get(rel.getQuestionId());
            if (question == null) continue;
            ExamRecordDetailItemDTO item = new ExamRecordDetailItemDTO();
            item.setQuestionId(question.getId());
            item.setStem(question.getStem());
            item.setType(question.getType());
            item.setCategory(question.getCategory());
            item.setOptions(question.getOptions());
            item.setMyAnswer(toJsonSafe(myAnswerMap.get(question.getId())));
            item.setScore(rel.getScore());
            int type = question.getType() == null ? 0 : question.getType();
            if (type >= 1 && type <= 4) {
                item.setCorrectAnswer(question.getCorrect());
                item.setCorrect(judgeObjective(type, question.getCorrect(), myAnswerMap.get(question.getId())));
                item.setGotScore(Boolean.TRUE.equals(item.getCorrect()) ? rel.getScore() : BigDecimal.ZERO);
            } else {
                ExamMarking marking = markingMap.get(question.getId());
                if (marking != null) {
                    item.setGotScore(marking.getScore());
                    item.setComment(marking.getComment());
                }
                // 参考答案：批改视角始终可见；考生视角仅在成绩发布后可见
                if (forMarking || (record.getStatus() != null && record.getStatus() == 1)) {
                    item.setReferenceAnswer(question.getReferenceAnswer());
                }
            }
            items.add(item);
        }
        dto.setItems(items);
        return dto;
    }

    /** 解析答卷 answers JSON: [{questionId, answer}] */
    private Map<Long, Object> parseAnswers(String answersJson) {
        if (answersJson == null || answersJson.isEmpty()) {
            return Map.of();
        }
        try {
            List<Map<String, Object>> list = OBJECT_MAPPER.readValue(answersJson,
                    new TypeReference<List<Map<String, Object>>>() {});
            return list.stream().collect(Collectors.toMap(
                    m -> Long.valueOf(String.valueOf(m.get("questionId"))),
                    m -> m.get("answer")));
        } catch (JsonProcessingException e) {
            return Map.of();
        }
    }

    private String toJsonSafe(Object obj) {
        try {
            return OBJECT_MAPPER.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            return String.valueOf(obj);
        }
    }
}
