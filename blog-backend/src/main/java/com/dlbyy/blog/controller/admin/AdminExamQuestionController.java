package com.dlbyy.blog.controller.admin;

import com.dlbyy.blog.annotation.Admin;
import com.dlbyy.blog.common.PageResult;
import com.dlbyy.blog.common.Result;
import com.dlbyy.blog.entity.ExamQuestion;
import com.dlbyy.blog.service.ExamQuestionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

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
}
