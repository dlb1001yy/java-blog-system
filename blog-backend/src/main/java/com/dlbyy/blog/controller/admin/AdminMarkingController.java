package com.dlbyy.blog.controller.admin;

import com.dlbyy.blog.annotation.Admin;
import com.dlbyy.blog.annotation.RateLimit;
import com.dlbyy.blog.common.PageResult;
import com.dlbyy.blog.common.Result;
import com.dlbyy.blog.dto.ExamMarkingDTO;
import com.dlbyy.blog.dto.ExamRecordDetailDTO;
import com.dlbyy.blog.entity.ExamRecord;
import com.dlbyy.blog.service.ExamService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 后台阅卷管理
 */
@RestController
@RequestMapping("/admin/marking")
@RequiredArgsConstructor
@Tag(name = "后台阅卷管理")
public class AdminMarkingController {

    private final ExamService examService;

    @GetMapping("/pending")
    @Operation(summary = "待批改答卷分页")
    public Result<PageResult<ExamRecord>> pending(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword) {
        return Result.success(examService.pendingPage(page, size, keyword));
    }

    @GetMapping("/records/{recordId}")
    @Operation(summary = "批改详情（题目、考生答案、参考答案、评分草稿）")
    public Result<ExamRecordDetailDTO> detail(@PathVariable Long recordId) {
        return Result.success(examService.markingDetail(recordId));
    }

    @PostMapping("/records/{recordId}/save")
    @Admin("保存阅卷结果")
    @RateLimit(key = "exam-marking", time = 60, count = 100)
    @Operation(summary = "保存批改（submit=false 存草稿，true 确认并发布成绩）")
    public Result<?> save(@PathVariable Long recordId, @RequestBody SaveMarkingRequest request) {
        examService.saveMarking(recordId, request.getItems(), Boolean.TRUE.equals(request.getSubmit()));
        return Result.success(Boolean.TRUE.equals(request.getSubmit()) ? "批改完成，成绩已发布" : "草稿已保存", null);
    }

    @Data
    public static class SaveMarkingRequest {
        private List<ExamMarkingDTO> items;
        private Boolean submit;
    }
}
