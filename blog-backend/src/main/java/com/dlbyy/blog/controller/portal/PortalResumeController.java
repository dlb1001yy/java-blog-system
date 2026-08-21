package com.dlbyy.blog.controller.portal;

import com.dlbyy.blog.common.Result;
import com.dlbyy.blog.entity.ResumeInfo;
import com.dlbyy.blog.entity.ResumeShare;
import com.dlbyy.blog.service.ResumeInfoService;
import com.dlbyy.blog.service.ResumeShareService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/portal/resume")
@RequiredArgsConstructor
@Tag(name = "前台简历接口")
public class PortalResumeController {

    private final ResumeInfoService resumeInfoService;
    private final ResumeShareService resumeShareService;

    @GetMapping
    @Operation(summary = "获取站长简历")
    public Result<ResumeInfo> get() {
        return Result.success(resumeInfoService.getOne(null));
    }

    @GetMapping("/share/{token}")
    @Operation(summary = "通过分享链接查看简历（匿名）")
    public Result<ResumeInfo> getByShareToken(@PathVariable String token) {
        return Result.success(resumeShareService.viewByToken(token));
    }

    @GetMapping("/mine")
    @Operation(summary = "获取我的简历")
    public Result<ResumeInfo> getMine() {
        return Result.success(resumeInfoService.getByUserId(currentUserId()));
    }

    @PostMapping("/mine/share")
    @Operation(summary = "生成我的简历分享链接")
    public Result<Map<String, Object>> createShare(@RequestBody Map<String, ?> body) {
        Object raw = body == null ? null : body.get("expireMinutes");
        Long expireMinutes = raw == null ? null : ((Number) raw).longValue();
        ResumeShare share = resumeShareService.create(currentUserId(), expireMinutes);
        Map<String, Object> data = new HashMap<>();
        data.put("token", share.getShareToken());
        data.put("expireTime", share.getExpireTime());
        return Result.success(data);
    }

    @GetMapping("/mine/shares")
    @Operation(summary = "我的分享列表")
    public Result<List<ResumeShare>> listMyShares() {
        return Result.success(resumeShareService.listMine(currentUserId()));
    }

    @DeleteMapping("/mine/share/{id}")
    @Operation(summary = "撤销分享链接")
    public Result<?> revokeShare(@PathVariable Long id) {
        resumeShareService.revoke(id, currentUserId());
        return Result.success("撤销成功", null);
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

    @GetMapping("/{userId}")
    @Operation(summary = "获取指定用户简历（仅审核通过）")
    public Result<ResumeInfo> getByUserId(@PathVariable Long userId) {
        ResumeInfo resume = resumeInfoService.getByUserId(userId);
        if (resume != null && Integer.valueOf(1).equals(resume.getStatus())) {
            return Result.success(resume);
        }
        return Result.success(null);
    }

    /**
     * 获取当前登录用户ID
     * <p>/portal/** 为 permitAll，未登录时 SecurityContext 中是 AnonymousAuthenticationToken，
     * 其 isAuthenticated() 也为 true，必须先排除匿名认证再查用户。
     */
    private Long currentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof org.springframework.security.authentication.AnonymousAuthenticationToken) {
            throw new com.dlbyy.blog.common.exception.BusinessException(401, "未登录");
        }
        com.dlbyy.blog.entity.User user = resumeInfoService.getUserByUsername(authentication.getName());
        if (user == null) {
            throw new com.dlbyy.blog.common.exception.BusinessException(401, "用户不存在");
        }
        return user.getId();
    }
}
