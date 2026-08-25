package com.dlbyy.blog.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dlbyy.blog.annotation.Admin;
import com.dlbyy.blog.common.PageResult;
import com.dlbyy.blog.common.Result;
import com.dlbyy.blog.dto.BatchIds;
import com.dlbyy.blog.entity.ExamPaperQuestion;
import com.dlbyy.blog.entity.ExamPaper;
import com.dlbyy.blog.mapper.ExamPaperQuestionMapper;
import com.dlbyy.blog.service.ExamPaperService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 后台试卷管理
 */
@RestController
@RequestMapping("/admin/exam-papers")
@RequiredArgsConstructor
@Tag(name = "后台试卷管理")
public class AdminExamPaperController {

    private final ExamPaperService examPaperService;
    private final ExamPaperQuestionMapper examPaperQuestionMapper;

    @GetMapping
    @Operation(summary = "分页查询试卷")
    public Result<PageResult<ExamPaper>> page(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status) {
        return Result.success(examPaperService.adminPage(page, size, keyword, status));
    }

    @GetMapping("/{id}")
    @Operation(summary = "试卷详情（含题目ID有序列表）")
    public Result<Map<String, Object>> detail(@PathVariable Long id) {
        ExamPaper paper = examPaperService.adminDetail(id);
        if (paper == null) {
            return Result.error(404, "试卷不存在");
        }
        List<Long> questionIds = examPaperQuestionMapper.selectList(
                        new LambdaQueryWrapper<ExamPaperQuestion>()
                                .eq(ExamPaperQuestion::getPaperId, id)
                                .orderByAsc(ExamPaperQuestion::getSortOrder))
                .stream().map(ExamPaperQuestion::getQuestionId).toList();
        Map<String, Object> data = new HashMap<>();
        data.put("paper", paper);
        data.put("questionIds", questionIds);
        return Result.success(data);
    }

    @PostMapping
    @Admin("保存试卷")
    @Operation(summary = "新增/更新试卷")
    public Result<Long> save(@RequestBody ExamPaper paper) {
        return Result.success("保存成功", examPaperService.adminSave(paper));
    }

    @PostMapping("/{id}/compose")
    @Admin("试卷组卷")
    @Operation(summary = "组卷（全量替换试卷题目）")
    public Result<?> compose(@PathVariable Long id, @RequestBody ComposeRequest request) {
        examPaperService.compose(id, request.getQuestionIds());
        return Result.success("组卷成功", null);
    }

    @PostMapping("/{id}/publish")
    @Admin("发布/停用试卷")
    @Operation(summary = "发布试卷（enable=true 发布，false 停用）")
    public Result<?> publish(@PathVariable Long id, @RequestParam(defaultValue = "true") boolean enable) {
        examPaperService.publish(id, enable);
        return Result.success(enable ? "已发布" : "已停用", null);
    }

    @DeleteMapping("/{id}")
    @Admin("删除试卷")
    @Operation(summary = "删除试卷")
    public Result<?> delete(@PathVariable Long id) {
        examPaperService.adminDelete(id);
        return Result.success("删除成功", null);
    }

    @DeleteMapping("/batch")
    @Admin("批量删除试卷")
    @Operation(summary = "批量删除试卷")
    public Result<?> batchDelete(@RequestBody BatchIds batchIds) {
        for (Long id : batchIds.getIds()) {
            examPaperService.adminDelete(id);
        }
        return Result.success("批量删除成功", null);
    }

    @Data
    public static class ComposeRequest {
        private List<Long> questionIds;
    }
}
