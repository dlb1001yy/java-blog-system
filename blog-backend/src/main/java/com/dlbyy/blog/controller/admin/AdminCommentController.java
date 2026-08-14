package com.dlbyy.blog.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dlbyy.blog.common.Result;
import com.dlbyy.blog.entity.Comment;
import com.dlbyy.blog.service.CommentService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/admin/comments")
@RequiredArgsConstructor
@Tag(name = "后台评论管理")
public class AdminCommentController {

    private final CommentService commentService;

    @GetMapping("/page")
    public Result<Page<Comment>> page(@RequestParam(defaultValue = "1") Integer current,
                                      @RequestParam(defaultValue = "10") Integer size,
                                      @RequestParam(required = false) Integer status) {
        Page<Comment> page = new Page<>(current, size);
        LambdaQueryWrapper<Comment> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(Comment::getStatus, status);
        }
        wrapper.orderByDesc(Comment::getCreateTime);
        return Result.success(commentService.page(page, wrapper));
    }

    @PutMapping("/{id}/approve")
    public Result<?> approve(@PathVariable Long id,
                             @RequestParam(required = false, defaultValue = "1") Integer status) {
        Comment comment = new Comment();
        comment.setId(id);
        comment.setStatus(status);
        commentService.updateById(comment);
        return Result.success("审核成功", null);
    }

    @PutMapping("/{id}/reject")
    public Result<?> reject(@PathVariable Long id) {
        Comment comment = new Comment();
        comment.setId(id);
        comment.setStatus(2);
        commentService.updateById(comment);
        return Result.success("拒绝成功", null);
    }

    @PutMapping("/batch-approve")
    public Result<?> batchApprove(@RequestBody List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Result.success("批量审核成功", null);
        }
        List<Comment> comments = ids.stream()
                .map(id -> {
                    Comment comment = new Comment();
                    comment.setId(id);
                    comment.setStatus(1);
                    return comment;
                })
                .collect(Collectors.toList());
        commentService.updateBatchById(comments);
        return Result.success("批量审核成功", null);
    }

    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        commentService.removeById(id);
        return Result.success("删除成功", null);
    }
}