package com.dlbyy.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dlbyy.blog.common.PageResult;
import com.dlbyy.blog.entity.Category;
import com.dlbyy.blog.entity.ExamQuestion;
import com.dlbyy.blog.mapper.ExamQuestionMapper;
import com.dlbyy.blog.service.CategoryService;
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
    private final CategoryService categoryService;

    private static final Set<String> VALID_DIFFICULTIES = Set.of("简单", "中等", "困难");

    private static final Map<String, Integer> TYPE_NAME_MAP = Map.ofEntries(
            Map.entry("单选题", 1), Map.entry("单选", 1),
            Map.entry("多选题", 2), Map.entry("多选", 2),
            Map.entry("判断题", 3), Map.entry("判断", 3),
            Map.entry("填空题", 4), Map.entry("填空", 4),
            Map.entry("简答题", 5), Map.entry("简答", 5),
            Map.entry("编程题", 6), Map.entry("编程", 6));

    /**
     * 解析题型：支持 1-6 数字或题型名称（忽略大小写），无效返回 null
     */
    private static Integer resolveType(String typeText) {
        if (!StringUtils.hasText(typeText)) {
            return null;
        }
        String text = typeText.trim();
        if (text.matches("[1-6]")) {
            return Integer.valueOf(text);
        }
        return TYPE_NAME_MAP.get(text.toLowerCase());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ImportResult importFromExcel(MultipartFile file) {
        List<ExamQuestion> questions = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        DataFormatter formatter = new DataFormatter();
        Set<String> categoryNames = categoryService.list().stream()
                .map(Category::getName)
                .filter(StringUtils::hasText)
                .collect(Collectors.toSet());
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

                Integer type = resolveType(typeText);
                String error = validateRow(stem, type, difficulty, scoreText);
                String parsedOptions = null;
                String parsedCorrect = null;
                if (error == null) {
                    ParsedValue optionsResult = parseOptions(type, options, objectMapper);
                    error = optionsResult.error();
                    parsedOptions = optionsResult.value();
                }
                if (error == null) {
                    ParsedValue correctResult = parseCorrect(type, options, correct, objectMapper);
                    error = correctResult.error();
                    parsedCorrect = correctResult.value();
                }
                if (error == null && !StringUtils.hasText(category)) {
                    error = "分类必须为分类管理中已有的分类";
                }
                if (error == null && !categoryNames.contains(category.trim())) {
                    error = "分类必须为分类管理中已有的分类";
                }
                if (error != null) {
                    errors.add("第" + (i + 1) + "行: " + error);
                    continue;
                }

                ExamQuestion q = new ExamQuestion();
                q.setStem(stem);
                q.setType(type);
                q.setCategory(category);
                q.setDifficulty(StringUtils.hasText(difficulty) ? difficulty : "中等");
                q.setOptions(parsedOptions);
                q.setCorrect(parsedCorrect);
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
    private String validateRow(String stem, Integer type, String difficulty, String scoreText) {
        if (!StringUtils.hasText(stem)) {
            return "题干不能为空";
        }
        if (type == null) {
            return "题型必须为：单选题/多选题/判断题/填空题/简答题/编程题（或 1-6）";
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
        return null;
    }

    /** 解析结果：value 与 error 二选一 */
    private record ParsedValue(String value, String error) {
        static ParsedValue ok(String value) {
            return new ParsedValue(value, null);
        }

        static ParsedValue fail(String error) {
            return new ParsedValue(null, error);
        }
    }

    /**
     * 解析选项：题型1/2 支持 | 或换行分隔的纯文本，也兼容 JSON 数组；题型3-6 不允许填写
     */
    private ParsedValue parseOptions(Integer type, String options, ObjectMapper mapper) {
        boolean hasOptions = StringUtils.hasText(options);
        if (type != null && (type == 1 || type == 2)) {
            if (!hasOptions) {
                return ParsedValue.fail((type == 1 ? "单选题" : "多选题") + "必须填写选项（多个选项用|分隔）");
            }
            String text = options.trim();
            if (text.startsWith("[")) {
                if (!isValidJson(mapper, text, true)) {
                    return ParsedValue.fail("选项必须是合法的 JSON 数组");
                }
                return ParsedValue.ok(text);
            }
            String[] items = text.split("[|\\n\\r]+");
            List<String> list = new ArrayList<>();
            for (String item : items) {
                if (StringUtils.hasText(item)) {
                    list.add(item.trim());
                }
            }
            if (list.isEmpty()) {
                return ParsedValue.fail((type == 1 ? "单选题" : "多选题") + "必须填写选项（多个选项用|分隔）");
            }
            try {
                return ParsedValue.ok(mapper.writeValueAsString(list));
            } catch (Exception e) {
                return ParsedValue.fail("选项解析失败");
            }
        }
        if (hasOptions) {
            return ParsedValue.fail("该题型无需填写选项");
        }
        return ParsedValue.ok(null);
    }

    /** 选项数量（用于索引越界校验），解析失败返回 -1 */
    private int optionCount(String options, ObjectMapper mapper) {
        if (!StringUtils.hasText(options)) {
            return 0;
        }
        String text = options.trim();
        if (text.startsWith("[")) {
            try {
                JsonNode node = mapper.readTree(text);
                return node.isArray() ? node.size() : -1;
            } catch (Exception e) {
                return -1;
            }
        }
        return (int) java.util.Arrays.stream(text.split("[|\\n\\r]+"))
                .filter(StringUtils::hasText)
                .count();
    }

    /**
     * 解析正确答案：按题型转换为存储格式的 JSON 字符串，兼容以 [ 开头的原 JSON
     */
    private ParsedValue parseCorrect(Integer type, String options, String correct, ObjectMapper mapper) {
        boolean hasCorrect = StringUtils.hasText(correct);
        // 兼容：以 [ 开头的按原 JSON 校验后原样存储
        if (hasCorrect && correct.trim().startsWith("[")) {
            if (!isValidJson(mapper, correct.trim(), true)) {
                return ParsedValue.fail("正确答案必须是合法的 JSON 数组");
            }
            return ParsedValue.ok(correct.trim());
        }
        int count = optionCount(options, mapper);
        switch (type) {
            case 1 -> { // 单选
                if (!hasCorrect) {
                    return ParsedValue.fail("单选题正确答案必须为单个选项字母（如 A）");
                }
                String letter = correct.trim().toLowerCase();
                if (letter.length() != 1 || letter.charAt(0) < 'a' || letter.charAt(0) > 'z'
                        || count >= 0 && (letter.charAt(0) - 'a') >= count) {
                    return ParsedValue.fail("单选题正确答案必须为单个选项字母（如 A）");
                }
                return ParsedValue.ok("[" + (letter.charAt(0) - 'a') + "]");
            }
            case 2 -> { // 多选
                if (!hasCorrect) {
                    return ParsedValue.fail("多选题正确答案必须为不重复的选项字母（如 AB）");
                }
                String letters = correct.trim().replaceAll("[,，\\s]", "").toLowerCase();
                List<Integer> indexes = new ArrayList<>();
                for (char c : letters.toCharArray()) {
                    int idx = c - 'a';
                    if (c < 'a' || c > 'z' || indexes.contains(idx)
                            || count >= 0 && idx >= count) {
                        return ParsedValue.fail("多选题正确答案必须为不重复的选项字母（如 AB）");
                    }
                    indexes.add(idx);
                }
                if (indexes.isEmpty()) {
                    return ParsedValue.fail("多选题正确答案必须为不重复的选项字母（如 AB）");
                }
                java.util.Collections.sort(indexes);
                String joined = indexes.stream().map(String::valueOf).collect(Collectors.joining(","));
                return ParsedValue.ok("[" + joined + "]");
            }
            case 3 -> { // 判断
                if (!hasCorrect) {
                    return ParsedValue.fail("判断题正确答案必须为：对/错");
                }
                String text = correct.trim().toLowerCase();
                return switch (text) {
                    case "对", "正确", "true", "√", "是" -> ParsedValue.ok("[true]");
                    case "错", "错误", "false", "×", "否" -> ParsedValue.ok("[false]");
                    default -> ParsedValue.fail("判断题正确答案必须为：对/错");
                };
            }
            case 4 -> { // 填空
                if (!hasCorrect) {
                    return ParsedValue.fail("填空题正确答案不能为空（多个空用|分隔）");
                }
                List<String> answers = new ArrayList<>();
                for (String part : correct.trim().split("\\|")) {
                    if (StringUtils.hasText(part)) {
                        answers.add(part.trim());
                    }
                }
                if (answers.isEmpty()) {
                    return ParsedValue.fail("填空题正确答案不能为空（多个空用|分隔）");
                }
                try {
                    return ParsedValue.ok(mapper.writeValueAsString(answers));
                } catch (Exception e) {
                    return ParsedValue.fail("正确答案解析失败");
                }
            }
            default -> { // 简答/编程
                if (hasCorrect) {
                    return ParsedValue.fail("简答/编程题无需填写正确答案，请填参考答案列");
                }
                return ParsedValue.ok(null);
            }
        }
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
