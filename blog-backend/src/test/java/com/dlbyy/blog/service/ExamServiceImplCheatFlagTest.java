package com.dlbyy.blog.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dlbyy.blog.dto.ExamAnswerDTO;
import com.dlbyy.blog.dto.ExamSubmitDTO;
import com.dlbyy.blog.entity.ExamPaper;
import com.dlbyy.blog.entity.ExamPaperQuestion;
import com.dlbyy.blog.entity.ExamQuestion;
import com.dlbyy.blog.entity.ExamRecord;
import com.dlbyy.blog.mapper.ExamPaperMapper;
import com.dlbyy.blog.mapper.ExamPaperQuestionMapper;
import com.dlbyy.blog.mapper.ExamQuestionMapper;
import com.dlbyy.blog.mapper.ExamRecordMapper;
import com.dlbyy.blog.service.impl.ExamJudgeAsyncService;
import com.dlbyy.blog.service.impl.ExamServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link ExamServiceImpl#submitPaper} 中 cheatFlag 作弊判定单元测试。
 * 使用 Mockito 模拟 mapper 与异步判分服务。
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ExamServiceImpl 交卷作弊判定测试")
class ExamServiceImplCheatFlagTest {

    private static final Long USER_ID = 1L;
    private static final Long PAPER_ID = 100L;

    @Mock
    private ExamRecordMapper examRecordMapper;
    @Mock
    private ExamPaperMapper examPaperMapper;
    @Mock
    private ExamPaperQuestionMapper examPaperQuestionMapper;
    @Mock
    private ExamQuestionMapper examQuestionMapper;
    @Mock
    private com.dlbyy.blog.mapper.ExamMarkingMapper examMarkingMapper;
    @Mock
    private com.dlbyy.blog.service.CategoryService categoryService;
    @Mock
    private ExamJudgeAsyncService examJudgeAsyncService;

    private ExamServiceImpl examService;

    @BeforeEach
    void setUp() {
        examService = new ExamServiceImpl(examRecordMapper, examPaperMapper,
                examPaperQuestionMapper, examQuestionMapper, examMarkingMapper, categoryService, examJudgeAsyncService);
    }

    private void stubPaper() {
        ExamPaper paper = new ExamPaper();
        paper.setId(PAPER_ID);
        paper.setStatus(1);
        when(examPaperMapper.selectById(PAPER_ID)).thenReturn(paper);
        // 模拟 MyBatis-Plus insert 回填主键
        lenient().when(examRecordMapper.insert(any(ExamRecord.class))).thenAnswer(inv -> {
            inv.getArgument(0, ExamRecord.class).setId(999L);
            return 1;
        });
    }

    /** 构造 n 道单选题的试卷题目与题目映射 */
    private void stubQuestions(int n) {
        List<ExamPaperQuestion> rels = new ArrayList<>();
        List<ExamQuestion> questions = new ArrayList<>();
        for (long i = 1; i <= n; i++) {
            ExamPaperQuestion rel = new ExamPaperQuestion();
            rel.setPaperId(PAPER_ID);
            rel.setQuestionId(i);
            rel.setSortOrder((int) i);
            rels.add(rel);

            ExamQuestion q = new ExamQuestion();
            q.setId(i);
            q.setType(1);
            q.setCorrect("[0]");
            questions.add(q);
        }
        when(examPaperQuestionMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(rels);
        lenient().when(examQuestionMapper.selectBatchIds(anyList())).thenReturn(questions);
    }

    private ExamSubmitDTO buildDto(int questionCount, Integer switchCount, Integer durationSeconds) {
        ExamSubmitDTO dto = new ExamSubmitDTO();
        dto.setPaperId(PAPER_ID);
        dto.setSwitchCount(switchCount);
        dto.setDurationSeconds(durationSeconds);
        List<ExamAnswerDTO> answers = new ArrayList<>();
        for (long i = 1; i <= questionCount; i++) {
            ExamAnswerDTO a = new ExamAnswerDTO();
            a.setQuestionId(i);
            a.setAnswer(0);
            answers.add(a);
        }
        dto.setAnswers(answers);
        return dto;
    }

    private ExamRecord captureInsertedRecord() {
        ArgumentCaptor<ExamRecord> captor = ArgumentCaptor.forClass(ExamRecord.class);
        verify(examRecordMapper).insert(captor.capture());
        return captor.getValue();
    }

    @Test
    @DisplayName("切屏次数达 3 次触发作弊标记")
    void switchCountAtLeast3TriggersCheatFlag() {
        stubPaper();
        stubQuestions(5);
        examService.submitPaper(USER_ID, buildDto(5, 3, 600));
        assertThat(captureInsertedRecord().getCheatFlag()).isEqualTo(1);
    }

    @Test
    @DisplayName("8 题用时 50 秒（< 8*10）触发瞬答作弊标记")
    void rapidAnswerTriggersCheatFlag() {
        stubPaper();
        stubQuestions(8);
        examService.submitPaper(USER_ID, buildDto(8, 0, 50));
        assertThat(captureInsertedRecord().getCheatFlag()).isEqualTo(1);
    }

    @Test
    @DisplayName("正常作答（切屏 1 次、8 题用时 300 秒）不触发作弊标记")
    void normalSubmissionDoesNotTriggerCheatFlag() {
        stubPaper();
        stubQuestions(8);
        examService.submitPaper(USER_ID, buildDto(8, 1, 300));
        assertThat(captureInsertedRecord().getCheatFlag()).isEqualTo(0);
    }

    @Test
    @DisplayName("交卷后触发异步判分且客观分占位为 0")
    void triggersAsyncJudgeWithZeroScore() {
        stubPaper();
        stubQuestions(8);
        examService.submitPaper(USER_ID, buildDto(8, 0, 300));
        ExamRecord record = captureInsertedRecord();
        assertThat(record.getObjectiveScore()).isEqualByComparingTo(java.math.BigDecimal.ZERO);
        verify(examJudgeAsyncService).judge(anyLong(), eq(PAPER_ID), any());
    }
}
