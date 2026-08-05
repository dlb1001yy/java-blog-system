package com.dlbyy.blog.controller.portal;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dlbyy.blog.common.Result;
import com.dlbyy.blog.entity.Comment;
import com.dlbyy.blog.service.CommentService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/portal/comments")
@RequiredArgsConstructor
@Tag(name = "前台评论接口")
public class PortalCommentController {

    private final CommentService commentService;

    @GetMapping("/{articleId}")
    public Result<List<Comment>> list(@PathVariable Long articleId) {
        LambdaQueryWrapper<Comment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Comment::getArticleId, articleId)
               .eq(Comment::getStatus, 1) // 只查询审核通过的
               .orderByDesc(Comment::getCreateTime);
        return Result.success(commentService.list(wrapper));
    }

    @PostMapping
    public Result<?> create(@RequestBody Comment comment) {
        comment.setStatus(0); // 默认待审核
        commentService.save(comment);
        return Result.success("评论成功，待审核", null);
    }
}