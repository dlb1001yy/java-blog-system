package com.dlbyy.blog.controller.admin;

import com.dlbyy.blog.annotation.Admin;
import com.dlbyy.blog.common.PageResult;
import com.dlbyy.blog.common.Result;
import com.dlbyy.blog.dto.BatchIds;
import com.dlbyy.blog.entity.InterviewQuestion;
import com.dlbyy.blog.service.InterviewQuestionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 后台面试题管理
 */
@RestController
@RequestMapping("/admin/interview-questions")
@RequiredArgsConstructor
@Tag(name = "后台面试题管理")
public class AdminInterviewQuestionController {

    private final InterviewQuestionService interviewQuestionService;

    @GetMapping
    @Operation(summary = "分页查询面试题")
    public Result<PageResult<InterviewQuestion>> page(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String difficulty,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status) {
        return Result.success(interviewQuestionService.adminPage(page, size, categoryId, difficulty, keyword, status));
    }

    @GetMapping("/{id}")
    @Operation(summary = "面试题详情")
    public Result<InterviewQuestion> detail(@PathVariable Long id) {
        return Result.success(interviewQuestionService.getDetail(id));
    }

    @PostMapping
    @Admin("保存面试题")
    @Operation(summary = "新增/更新面试题")
    public Result<Long> save(@RequestBody InterviewQuestion question) {
        return Result.success("保存成功", interviewQuestionService.adminSave(question));
    }

    @PostMapping("/import")
    @Admin("批量导入面试题")
    @Operation(summary = "JSON 批量导入面试题（全量校验通过才落库）")
    public Result<Map<String, Object>> importQuestions(@RequestBody List<InterviewQuestion> questions) {
        if (questions == null || questions.isEmpty()) {
            return Result.error("导入列表不能为空");
        }
        List<String> errors = new ArrayList<>();
        for (int i = 0; i < questions.size(); i++) {
            InterviewQuestion question = questions.get(i);
            List<String> missing = new ArrayList<>();
            if (!StringUtils.hasText(question.getTitle())) {
                missing.add("title");
            }
            if (question.getCategoryId() == null && !StringUtils.hasText(question.getCategoryName())) {
                missing.add("category");
            }
            if (!StringUtils.hasText(question.getDifficulty())) {
                missing.add("difficulty");
            }
            if (!missing.isEmpty()) {
                errors.add("第" + (i + 1) + "题：缺少" + String.join("、", missing));
            }
        }
        Map<String, Object> data = new HashMap<>();
        data.put("count", errors.isEmpty() ? questions.size() : 0);
        data.put("errors", errors);
        if (!errors.isEmpty()) {
            return Result.success("导入失败，共 " + errors.size() + " 条错误", data);
        }
        for (InterviewQuestion question : questions) {
            interviewQuestionService.adminSave(question);
        }
        return Result.success("导入成功", data);
    }

    @DeleteMapping("/{id}")
    @Admin("删除面试题")
    @Operation(summary = "删除面试题")
    public Result<?> delete(@PathVariable Long id) {
        interviewQuestionService.adminDelete(id);
        return Result.success("删除成功", null);
    }

    @DeleteMapping("/batch")
    @Admin("批量删除面试题")
    @Operation(summary = "批量删除面试题")
    public Result<?> batchDelete(@RequestBody BatchIds batchIds) {
        for (Long id : batchIds.getIds()) {
            interviewQuestionService.adminDelete(id);
        }
        return Result.success("批量删除成功", null);
    }
}
