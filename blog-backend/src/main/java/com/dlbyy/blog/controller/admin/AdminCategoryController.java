package com.dlbyy.blog.controller.admin;

import com.dlbyy.blog.annotation.Admin;
import com.dlbyy.blog.common.Result;
import com.dlbyy.blog.entity.Category;
import com.dlbyy.blog.service.CategoryService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/categories")
@RequiredArgsConstructor
@Tag(name = "后台分类管理")
public class AdminCategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public Result<List<Category>> list() {
        return Result.success(categoryService.list());
    }

    @PostMapping
    @Admin("新增分类")
    public Result<?> create(@RequestBody Category category) {
        categoryService.save(category);
        return Result.success("创建成功", null);
    }

    @PutMapping
    @Admin("更新分类")
    public Result<?> update(@RequestBody Category category) {
        categoryService.updateById(category);
        return Result.success("更新成功", null);
    }

    @DeleteMapping("/{id}")
    @Admin("删除分类")
    public Result<?> delete(@PathVariable Long id) {
        categoryService.removeById(id);
        return Result.success("删除成功", null);
    }
}