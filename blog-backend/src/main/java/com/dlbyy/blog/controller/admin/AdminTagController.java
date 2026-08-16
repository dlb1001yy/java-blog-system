package com.dlbyy.blog.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dlbyy.blog.annotation.Admin;
import com.dlbyy.blog.common.Result;
import com.dlbyy.blog.entity.Tag;
import com.dlbyy.blog.service.TagService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/admin/tags")
@RequiredArgsConstructor
@io.swagger.v3.oas.annotations.tags.Tag(name = "后台标签管理")
public class AdminTagController {

    private final TagService tagService;

    @GetMapping
    @Operation(summary = "获取所有标签")
    public Result<List<Tag>> list() {
        return Result.success(tagService.list(
                new LambdaQueryWrapper<Tag>().orderByDesc(Tag::getCreateTime)
        ));
    }

    @GetMapping("/page")
    @Operation(summary = "分页查询标签")
    public Result<Page<Tag>> page(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String name) {

        Page<Tag> page = new Page<>(current, size);
        LambdaQueryWrapper<Tag> wrapper = new LambdaQueryWrapper<>();
        if (name != null && !name.isEmpty()) {
            wrapper.like(Tag::getName, name);
        }
        wrapper.orderByDesc(Tag::getCreateTime);
        return Result.success(tagService.page(page, wrapper));
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取标签详情")
    public Result<Tag> detail(@PathVariable Long id) {
        return Result.success(tagService.getById(id));
    }

    @PostMapping
    @Admin("新增标签")
    @Operation(summary = "新增标签")
    public Result<?> create(@RequestBody Tag tag) {
        tag.setCreateTime(LocalDateTime.now());
        tagService.save(tag);
        return Result.success("创建成功", null);
    }

    @PutMapping
    @Admin("更新标签")
    @Operation(summary = "更新标签")
    public Result<?> update(@RequestBody Tag tag) {
        tagService.updateById(tag);
        return Result.success("更新成功", null);
    }

    @DeleteMapping("/{id}")
    @Admin("删除标签")
    @Operation(summary = "删除标签")
    public Result<?> delete(@PathVariable Long id) {
        tagService.removeById(id);
        return Result.success("删除成功", null);
    }
}
