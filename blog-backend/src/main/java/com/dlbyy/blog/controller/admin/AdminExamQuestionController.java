package com.dlbyy.blog.controller.admin;

import com.dlbyy.blog.annotation.Admin;
import com.dlbyy.blog.common.PageResult;
import com.dlbyy.blog.common.Result;
import com.dlbyy.blog.dto.BatchIds;
import com.dlbyy.blog.entity.Category;
import com.dlbyy.blog.entity.ExamQuestion;
import com.dlbyy.blog.service.CategoryService;
import com.dlbyy.blog.service.ExamQuestionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.xssf.usermodel.XSSFDataValidation;
import org.apache.poi.xssf.usermodel.XSSFDataValidationHelper;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 后台考试题库管理
 */
@RestController
@RequestMapping("/admin/exam-questions")
@RequiredArgsConstructor
@Tag(name = "后台考试题库管理")
public class AdminExamQuestionController {

    private final ExamQuestionService examQuestionService;
    private final CategoryService categoryService;

    @GetMapping
    @Operation(summary = "分页查询题目（支持题型/分类/难度/关键词/状态筛选）")
    public Result<PageResult<ExamQuestion>> page(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Integer type,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String difficulty,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status) {
        return Result.success(examQuestionService.adminPage(page, size, type, category, difficulty, keyword, status));
    }

    @GetMapping("/stats")
    @Operation(summary = "题型统计（6 种题型题目计数）")
    public Result<Map<Integer, Long>> stats() {
        return Result.success(examQuestionService.countByType());
    }

    @GetMapping("/{id}")
    @Operation(summary = "题目详情")
    public Result<ExamQuestion> detail(@PathVariable Long id) {
        return Result.success(examQuestionService.getById(id));
    }

    @PostMapping
    @Admin("保存考试题目")
    @Operation(summary = "新增/更新题目")
    public Result<Long> save(@RequestBody ExamQuestion question) {
        return Result.success("保存成功", examQuestionService.adminSave(question));
    }

    @DeleteMapping("/{id}")
    @Admin("删除考试题目")
    @Operation(summary = "删除题目")
    public Result<?> delete(@PathVariable Long id) {
        examQuestionService.adminDelete(id);
        return Result.success("删除成功", null);
    }

    @DeleteMapping("/batch")
    @Admin("批量删除考试题目")
    @Operation(summary = "批量删除题目")
    public Result<?> batchDelete(@RequestBody BatchIds batchIds) {
        for (Long id : batchIds.getIds()) {
            examQuestionService.adminDelete(id);
        }
        return Result.success("批量删除成功", null);
    }

    @PostMapping("/import")
    @Admin("Excel批量导入题目")
    @Operation(summary = "Excel 批量导入题目（xlsx，全量校验通过才落库）")
    public Result<Map<String, Object>> importQuestions(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return Result.error("请选择要上传的 Excel 文件");
        }
        String filename = file.getOriginalFilename();
        if (!StringUtils.hasText(filename) || !filename.toLowerCase().endsWith(".xlsx")) {
            return Result.error("仅支持 .xlsx 格式的 Excel 文件");
        }
        ExamQuestionService.ImportResult result = examQuestionService.importFromExcel(file);
        Map<String, Object> data = new HashMap<>();
        data.put("count", result.count());
        data.put("errors", result.errors());
        return result.errors().isEmpty()
                ? Result.success("导入成功", data)
                : Result.success("导入失败，共 " + result.errors().size() + " 条错误", data);
    }

    @GetMapping("/template")
    @Admin("下载题目导入模板")
    @Operation(summary = "下载题目导入 Excel 模板（表头 + 2 行示例）")
    public void template(HttpServletResponse response) throws IOException {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            XSSFSheet sheet = workbook.createSheet("题目导入模板");
            List<String> categoryNames = categoryService.list().stream()
                    .map(Category::getName)
                    .filter(StringUtils::hasText)
                    .toList();
            String sampleCategory = categoryNames.isEmpty() ? "Java基础" : categoryNames.get(0);
            String[][] rows = {
                    {"题干(stem)", "题型(单选题/多选题/判断题/填空题/简答题/编程题)", "分类(category)", "难度(简单/中等/困难)",
                            "选项(仅单选题/多选题填写，多个选项用|分隔)", "正确答案(单选填A，多选填AB，判断题填对或错，填空题多个空用|分隔，简答/编程题留空)", "参考答案/解析(reference_answer)", "分值(score)"},
                    {"Java 中用于定义常量的关键字是？", "单选题", sampleCategory, "简单",
                            "final|finally|finalize|const", "A", "final 修饰的变量不可重新赋值", "2"},
                    {"简述 JVM 的垃圾回收机制", "简答题",
                            categoryNames.isEmpty() ? "JVM" : categoryNames.get(0), "中等",
                            "", "", "可从可达性分析、常见回收器等角度作答", "10"}
            };
            for (int i = 0; i < rows.length; i++) {
                Row row = sheet.createRow(i);
                for (int j = 0; j < rows[i].length; j++) {
                    row.createCell(j).setCellValue(rows[i][j]);
                }
            }
            XSSFDataValidationHelper helper = new XSSFDataValidationHelper(sheet);
            var typeConstraint = helper.createExplicitListConstraint(new String[]{"单选题", "多选题", "判断题", "填空题", "简答题", "编程题"});
            XSSFDataValidation typeValidation = (XSSFDataValidation) helper.createValidation(typeConstraint, new CellRangeAddressList(1, 500, 1, 1));
            sheet.addValidationData(typeValidation);
            if (!categoryNames.isEmpty()) {
                var constraint = helper.createExplicitListConstraint(categoryNames.toArray(new String[0]));
                XSSFDataValidation validation = (XSSFDataValidation) helper.createValidation(constraint, new CellRangeAddressList(1, 500, 2, 2));
                sheet.addValidationData(validation);
            }
            var difficultyConstraint = helper.createExplicitListConstraint(new String[]{"简单", "中等", "困难"});
            XSSFDataValidation difficultyValidation = (XSSFDataValidation) helper.createValidation(difficultyConstraint, new CellRangeAddressList(1, 500, 3, 3));
            sheet.addValidationData(difficultyValidation);
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            String fileName = URLEncoder.encode("题目导入模板.xlsx", StandardCharsets.UTF_8).replace("+", "%20");
            response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + fileName);
            workbook.write(response.getOutputStream());
        }
    }
}
