package com.dlbyy.blog.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dlbyy.blog.annotation.Admin;
import com.dlbyy.blog.common.Result;
import com.dlbyy.blog.entity.Link;
import com.dlbyy.blog.service.LinkService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/admin/links")
@RequiredArgsConstructor
@Tag(name = "后台友链管理")
public class AdminLinkController {

    private final LinkService linkService;

    @GetMapping
    @Operation(summary = "获取所有友链")
    public Result<List<Link>> list() {
        return Result.success(linkService.list(
                new LambdaQueryWrapper<Link>().orderByAsc(Link::getSort)
        ));
    }

    @GetMapping("/page")
    @Operation(summary = "分页查询友链")
    public Result<Page<Link>> page(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Integer status) {

        Page<Link> page = new Page<>(current, size);
        LambdaQueryWrapper<Link> wrapper = new LambdaQueryWrapper<>();
        if (name != null && !name.isEmpty()) {
            wrapper.like(Link::getName, name);
        }
        if (status != null) {
            wrapper.eq(Link::getStatus, status);
        }
        wrapper.orderByAsc(Link::getSort);
        return Result.success(linkService.page(page, wrapper));
    }

    @PostMapping
    @Admin("新增友链")
    @Operation(summary = "新增友链")
    public Result<?> create(@RequestBody Link link) {
        link.setCreateTime(LocalDateTime.now());
        linkService.save(link);
        return Result.success("创建成功", null);
    }

    @PutMapping
    @Admin("更新友链")
    @Operation(summary = "更新友链")
    public Result<?> update(@RequestBody Link link) {
        linkService.updateById(link);
        return Result.success("更新成功", null);
    }

    @DeleteMapping("/{id}")
    @Admin("删除友链")
    @Operation(summary = "删除友链")
    public Result<?> delete(@PathVariable Long id) {
        linkService.removeById(id);
        return Result.success("删除成功", null);
    }
}
