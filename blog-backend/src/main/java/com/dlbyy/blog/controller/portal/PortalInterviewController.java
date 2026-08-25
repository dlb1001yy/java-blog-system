package com.dlbyy.blog.controller.portal;

import com.dlbyy.blog.common.PageResult;
import com.dlbyy.blog.common.Result;
import com.dlbyy.blog.entity.InterviewFavorite;
import com.dlbyy.blog.entity.InterviewQuestion;
import com.dlbyy.blog.entity.User;
import com.dlbyy.blog.service.InterviewFavoriteService;
import com.dlbyy.blog.service.InterviewQuestionService;
import com.dlbyy.blog.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 门户面试题接口
 * <p>
 * 列表不含 answer，答案通过详情接口获取；
 * 收藏/错题为用户级数据，需登录。
 */
@RestController
@RequestMapping("/portal/interview")
@RequiredArgsConstructor
@Tag(name = "前台面试题接口")
public class PortalInterviewController {

    private final InterviewQuestionService interviewQuestionService;
    private final InterviewFavoriteService interviewFavoriteService;
    private final UserService userService;

    @GetMapping("/questions")
    @Operation(summary = "分页查询面试题（不含答案）")
    public Result<PageResult<Map<String, Object>>> questions(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String categoryId,
            @RequestParam(required = false) String difficulty,
            @RequestParam(required = false) String keyword) {

        // 分类支持多选，逗号分隔，如 categoryId=1,2,3；忽略空值与非法项
        List<Long> categoryIds = null;
        if (categoryId != null && !categoryId.isBlank()) {
            categoryIds = Arrays.stream(categoryId.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .map(s -> {
                        try {
                            return Long.valueOf(s);
                        } catch (NumberFormatException e) {
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .toList();
        }
        PageResult<InterviewQuestion> pr = interviewQuestionService
                .pageQuery(page, size, categoryIds, difficulty, keyword);

        // 列表脱敏：置空 answer，防止未登录直接批量拉取答案
        PageResult<Map<String, Object>> result = new PageResult<>(pr.getTotal(),
                pr.getRecords().stream().map(q -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("id", q.getId());
                    item.put("categoryId", q.getCategoryId());
                    item.put("categoryName", q.getCategoryName());
                    item.put("difficulty", q.getDifficulty());
                    item.put("title", q.getTitle());
                    item.put("tagNames", q.getTagNameList());
                    item.put("status", q.getStatus());
                    item.put("createTime", q.getCreateTime());
                    return item;
                }).toList());
        return Result.success(result);
    }

    @GetMapping("/categories")
    @Operation(summary = "题库分类列表（id+name）")
    public Result<java.util.List<Map<String, Object>>> categories() {
        return Result.success(interviewQuestionService.listCategories().stream().map(c -> {
            Map<String, Object> item = new HashMap<>();
            item.put("id", c.getId());
            item.put("name", c.getName());
            return item;
        }).toList());
    }

    @GetMapping("/questions/{id}/answer")
    @Operation(summary = "获取题目详情（含答案）")
    public Result<InterviewQuestion> answer(@PathVariable Long id) {
        return Result.success(interviewQuestionService.getDetail(id));
    }

    @PostMapping("/favorites/{questionId}")
    @Operation(summary = "收藏/取消收藏题目")
    public Result<Boolean> toggleFavorite(@PathVariable Long questionId) {
        Long userId = currentUserId();
        boolean added = interviewFavoriteService.toggle(userId, questionId, 0);
        return Result.success(added ? "已收藏" : "已取消收藏", added);
    }

    @PostMapping("/wrong/{questionId}")
    @Operation(summary = "加入/移出错题本")
    public Result<Boolean> toggleWrong(@PathVariable Long questionId) {
        Long userId = currentUserId();
        boolean added = interviewFavoriteService.toggle(userId, questionId, 1);
        return Result.success(added ? "已加入错题本" : "已移出错题本", added);
    }

    @GetMapping("/favorites")
    @Operation(summary = "我的收藏分页")
    public Result<PageResult<InterviewFavorite>> favorites(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        return Result.success(interviewFavoriteService.pageByUser(page, size, currentUserId(), 0));
    }

    @GetMapping("/wrong")
    @Operation(summary = "错题本分页")
    public Result<PageResult<InterviewFavorite>> wrong(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        return Result.success(interviewFavoriteService.pageByUser(page, size, currentUserId(), 1));
    }

    /**
     * 获取当前登录用户ID
     * <p>/portal/** 为 permitAll，需排除匿名认证（AnonymousAuthenticationToken 的 isAuthenticated() 也为 true）
     */
    private Long currentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof org.springframework.security.authentication.AnonymousAuthenticationToken) {
            throw new com.dlbyy.blog.common.exception.BusinessException(401, "未登录");
        }
        User user = userService.getByUsername(authentication.getName());
        if (user == null) {
            throw new com.dlbyy.blog.common.exception.BusinessException(401, "用户不存在");
        }
        return user.getId();
    }
}
