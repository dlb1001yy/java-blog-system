package com.dlbyy.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dlbyy.blog.common.PageResult;
import com.dlbyy.blog.entity.ExamQuestion;
import com.dlbyy.blog.mapper.ExamQuestionMapper;
import com.dlbyy.blog.service.ExamQuestionService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 考试题目服务实现
 */
@Service
@RequiredArgsConstructor
public class ExamQuestionServiceImpl extends ServiceImpl<ExamQuestionMapper, ExamQuestion> implements ExamQuestionService {

    private final ObjectMapper objectMapper;

    private static final Set<String> VALID_DIFFICULTIES = Set.of("简单", "中等", "困难");

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ImportResult importFromExcel(MultipartFile file) {
        List<ExamQuestion> questions = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        DataFormatter formatter = new DataFormatter();
        try (InputStream in = file.getInputStream(); XSSFWorkbook workbook = new XSSFWorkbook(in)) {
            var sheet = workbook.getSheetAt(0);
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) {
                    continue;
                }
                String stem = cellText(row, 0, formatter);
                String typeText = cellText(row, 1, formatter);
                String category = cellText(row, 2, formatter);
                String difficulty = cellText(row, 3, formatter);
                String options = cellText(row, 4, formatter);
                String correct = cellText(row, 5, formatter);
                String referenceAnswer = cellText(row, 6, formatter);
                String scoreText = cellText(row, 7, formatter);

                boolean blank = !StringUtils.hasText(stem) && !StringUtils.hasText(typeText)
                        && !StringUtils.hasText(scoreText) && !StringUtils.hasText(options);
                if (blank) {
                    continue; // 跳过整行空白
                }

                String error = validateRow(stem, typeText, difficulty, options, correct, scoreText, objectMapper);
                if (error != null) {
                    errors.add("第" + (i + 1) + "行: " + error);
                    continue;
                }

                ExamQuestion q = new ExamQuestion();
                q.setStem(stem);
                q.setType(Integer.parseInt(typeText.trim()));
                q.setCategory(category);
                q.setDifficulty(StringUtils.hasText(difficulty) ? difficulty : "中等");
                q.setOptions(StringUtils.hasText(options) ? options.trim() : null);
                q.setCorrect(StringUtils.hasText(correct) ? correct.trim() : null);
                q.setReferenceAnswer(referenceAnswer);
                q.setScore(new BigDecimal(scoreText.trim()));
                q.setStatus(1);
                questions.add(q);
            }
        } catch (IOException e) {
            throw new RuntimeException("解析 Excel 文件失败", e);
        }
        if (!errors.isEmpty()) {
            return new ImportResult(0, errors);
        }
        if (!questions.isEmpty()) {
            this.saveBatch(questions);
        }
        return new ImportResult(questions.size(), errors);
    }

    /**
     * 校验单行数据，返回错误原因；返回 null 表示合法
     */
    private String validateRow(String stem, String typeText, String difficulty, String options,
                               String correct, String scoreText, ObjectMapper mapper) {
        if (!StringUtils.hasText(stem)) {
            return "题干不能为空";
        }
        int type;
        try {
            type = Integer.parseInt(typeText.trim());
        } catch (Exception e) {
            return "题型必须为整数（1单选/2多选/3判断/4填空/5简答/6编程）";
        }
        if (type < 1 || type > 6) {
            return "题型必须为 1-6（1单选/2多选/3判断/4填空/5简答/6编程）";
        }
        if (StringUtils.hasText(difficulty) && !VALID_DIFFICULTIES.contains(difficulty.trim())) {
            return "难度必须为：简单/中等/困难";
        }
        if (!StringUtils.hasText(scoreText)) {
            return "分值不能为空";
        }
        try {
            if (new BigDecimal(scoreText.trim()).compareTo(BigDecimal.ZERO) <= 0) {
                return "分值必须大于 0";
            }
        } catch (NumberFormatException e) {
            return "分值必须为数字";
        }
        boolean objective = type <= 4;
        if (objective && !StringUtils.hasText(correct)) {
            return "客观题（题型1-4）必须填写正确答案";
        }
        if (StringUtils.hasText(options) && !isValidJson(mapper, options.trim(), true)) {
            return "选项必须是合法的 JSON 数组";
        }
        if (StringUtils.hasText(correct) && !isValidJson(mapper, correct.trim(), false)) {
            return "正确答案必须是合法的 JSON";
        }
        return null;
    }

    private boolean isValidJson(ObjectMapper mapper, String text, boolean requireArray) {
        try {
            JsonNode node = mapper.readTree(text);
            if (node == null || node.isMissingNode() || (requireArray && !node.isArray())) {
                return false;
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private String cellText(Row row, int col, DataFormatter formatter) {
        Cell cell = row.getCell(col);
        if (cell == null) {
            return null;
        }
        if (cell.getCellType() == CellType.FORMULA) {
            cell.setCellType(CellType.STRING);
        }
        return formatter.formatCellValue(cell).trim();
    }

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
