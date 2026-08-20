package com.dlbyy.blog.controller.admin;

import com.dlbyy.blog.annotation.Admin;
import com.dlbyy.blog.common.PageResult;
import com.dlbyy.blog.common.Result;
import com.dlbyy.blog.entity.InterviewQuestion;
import com.dlbyy.blog.service.InterviewQuestionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

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
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String difficulty,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status) {
        return Result.success(interviewQuestionService.adminPage(page, size, category, difficulty, keyword, status));
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

    @DeleteMapping("/{id}")
    @Admin("删除面试题")
    @Operation(summary = "删除面试题")
    public Result<?> delete(@PathVariable Long id) {
        interviewQuestionService.adminDelete(id);
        return Result.success("删除成功", null);
    }
}
