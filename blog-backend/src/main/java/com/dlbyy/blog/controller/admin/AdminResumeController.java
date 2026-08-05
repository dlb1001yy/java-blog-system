package com.dlbyy.blog.controller.admin;

import com.dlbyy.blog.common.Result;
import com.dlbyy.blog.entity.ResumeInfo;
import com.dlbyy.blog.service.ResumeInfoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/admin/resume")
@RequiredArgsConstructor
@Tag(name = "后台简历管理")
public class AdminResumeController {

    private final ResumeInfoService resumeInfoService;

    @GetMapping
    @Operation(summary = "获取简历信息")
    public Result<ResumeInfo> get() {
        return Result.success(resumeInfoService.getOne(null));
    }

    @PostMapping
    @Operation(summary = "保存简历信息")
    public Result<?> save(@RequestBody ResumeInfo resumeInfo) {
        ResumeInfo existing = resumeInfoService.getOne(null);
        LocalDateTime now = LocalDateTime.now();
        if (existing != null) {
            resumeInfo.setId(existing.getId());
            resumeInfo.setUpdateTime(now);
            resumeInfoService.updateById(resumeInfo);
        } else {
            resumeInfo.setCreateTime(now);
            resumeInfo.setUpdateTime(now);
            resumeInfoService.save(resumeInfo);
        }
        return Result.success("保存成功", null);
    }

    @PutMapping
    @Operation(summary = "更新简历信息")
    public Result<?> update(@RequestBody ResumeInfo resumeInfo) {
        resumeInfo.setUpdateTime(LocalDateTime.now());
        resumeInfoService.updateById(resumeInfo);
        return Result.success("更新成功", null);
    }
}
