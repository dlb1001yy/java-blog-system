package com.dlbyy.blog.controller.portal;

import com.dlbyy.blog.common.Result;
import com.dlbyy.blog.entity.ResumeInfo;
import com.dlbyy.blog.service.ResumeInfoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/portal/resume")
@RequiredArgsConstructor
@Tag(name = "前台简历接口")
public class PortalResumeController {

    private final ResumeInfoService resumeInfoService;

    @GetMapping
    @Operation(summary = "获取站长简历")
    public Result<ResumeInfo> get() {
        return Result.success(resumeInfoService.getOne(null));
    }

    @GetMapping("/{userId}")
    @Operation(summary = "获取指定用户简历")
    public Result<ResumeInfo> getByUserId(@PathVariable Long userId) {
        return Result.success(resumeInfoService.lambdaQuery()
                .eq(ResumeInfo::getUserId, userId)
                .one());
    }

    @GetMapping("/mine")
    @Operation(summary = "获取我的简历")
    public Result<ResumeInfo> getMine() {
        return Result.success(resumeInfoService.getByUserId(currentUserId()));
    }

    @PostMapping("/mine")
    @Operation(summary = "保存我的简历（upsert）")
    public Result<?> saveMine(@RequestBody ResumeInfo resumeInfo) {
        resumeInfoService.mySave(currentUserId(), resumeInfo);
        return Result.success("保存成功", null);
    }

    @PutMapping("/mine")
    @Operation(summary = "保存我的简历（upsert）")
    public Result<?> updateMine(@RequestBody ResumeInfo resumeInfo) {
        resumeInfoService.mySave(currentUserId(), resumeInfo);
        return Result.success("保存成功", null);
    }

    /**
     * 获取当前登录用户ID（写法与 PortalInterviewController 一致）
     */
    private Long currentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new com.dlbyy.blog.common.exception.BusinessException(401, "未登录");
        }
        com.dlbyy.blog.entity.User user = resumeInfoService.getUserByUsername(authentication.getName());
        if (user == null) {
            throw new com.dlbyy.blog.common.exception.BusinessException(401, "用户不存在");
        }
        return user.getId();
    }
}
