package com.dlbyy.blog.controller.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.dlbyy.blog.annotation.Admin;
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
    @Admin
    @Operation(summary = "获取简历信息")
    public Result<ResumeInfo> get() {
        return Result.success(resumeInfoService.getOne(null));
    }

    @GetMapping("/page")
    @Admin("查看简历列表")
    @Operation(summary = "用户简历分页")
    public Result<IPage<ResumeInfo>> page(@RequestParam(defaultValue = "1") int page,
                                          @RequestParam(defaultValue = "10") int size,
                                          @RequestParam(required = false) String keyword,
                                          @RequestParam(required = false) Integer status) {
        return Result.success(resumeInfoService.pageAll(page, size, keyword, status));
    }

    @GetMapping("/detail/{id}")
    @Admin("查看简历详情")
    @Operation(summary = "简历详情")
    public Result<ResumeInfo> detail(@PathVariable Long id) {
        return Result.success(resumeInfoService.getById(id));
    }

    @PutMapping("/audit/{id}")
    @Admin("审核简历")
    @Operation(summary = "审核简历")
    public Result<?> audit(@PathVariable Long id,
                           @RequestParam Integer status,
                           @RequestParam(required = false) String remark) {
        resumeInfoService.audit(id, status, remark);
        return Result.success("审核成功", null);
    }

    @PostMapping
    @Admin("保存简历信息")
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
    @Admin("更新简历信息")
    @Operation(summary = "更新简历信息")
    public Result<?> update(@RequestBody ResumeInfo resumeInfo) {
        resumeInfo.setUpdateTime(LocalDateTime.now());
        resumeInfoService.updateById(resumeInfo);
        return Result.success("更新成功", null);
    }
}
