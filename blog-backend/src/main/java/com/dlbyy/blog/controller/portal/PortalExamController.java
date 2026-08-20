package com.dlbyy.blog.controller.portal;

import com.dlbyy.blog.common.PageResult;
import com.dlbyy.blog.common.Result;
import com.dlbyy.blog.common.exception.BusinessException;
import com.dlbyy.blog.dto.ExamPortalQuestionDTO;
import com.dlbyy.blog.dto.ExamRecordDetailDTO;
import com.dlbyy.blog.dto.ExamSubmitDTO;
import com.dlbyy.blog.entity.ExamPaper;
import com.dlbyy.blog.entity.ExamRecord;
import com.dlbyy.blog.entity.User;
import com.dlbyy.blog.service.ExamPaperService;
import com.dlbyy.blog.service.ExamService;
import com.dlbyy.blog.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 门户在线考试接口
 */
@RestController
@RequestMapping("/portal/exam")
@RequiredArgsConstructor
@Tag(name = "前台在线考试接口")
public class PortalExamController {

    private final ExamPaperService examPaperService;
    private final ExamService examService;
    private final UserService userService;

    @GetMapping("/papers")
    @Operation(summary = "已发布试卷分页列表")
    public Result<PageResult<ExamPaper>> papers(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword) {
        return Result.success(examPaperService.portalPage(page, size, keyword));
    }

    @GetMapping("/papers/{id}")
    @Operation(summary = "试卷详情（题目脱敏，不含正确答案）")
    public Result<List<ExamPortalQuestionDTO>> paperDetail(@PathVariable Long id) {
        return Result.success(examPaperService.portalDetail(id));
    }

    @PostMapping("/papers/{id}/submit")
    @Operation(summary = "交卷（需登录）")
    public Result<Long> submit(@PathVariable Long id, @RequestBody ExamSubmitDTO dto) {
        dto.setPaperId(id);
        return Result.success("交卷成功", examService.submitPaper(currentUserId(), dto));
    }

    @GetMapping("/records")
    @Operation(summary = "我的考试记录分页")
    public Result<PageResult<ExamRecord>> records(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        return Result.success(examService.myRecords(currentUserId(), page, size));
    }

    @GetMapping("/records/{id}")
    @Operation(summary = "我的成绩详情（仅本人）")
    public Result<ExamRecordDetailDTO> recordDetail(@PathVariable Long id) {
        User user = currentUser();
        return Result.success(examService.recordDetail(id, user.getId(), user.getRole()));
    }

    private User currentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new BusinessException(401, "未登录");
        }
        User user = userService.getByUsername(authentication.getName());
        if (user == null) {
            throw new BusinessException(401, "用户不存在");
        }
        return user;
    }

    private Long currentUserId() {
        return currentUser().getId();
    }
}
