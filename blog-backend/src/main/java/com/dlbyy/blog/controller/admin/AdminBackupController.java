package com.dlbyy.blog.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dlbyy.blog.annotation.Admin;
import com.dlbyy.blog.common.Result;
import com.dlbyy.blog.entity.BackupRecord;
import com.dlbyy.blog.service.BackupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 后台数据库备份管理接口
 */
@RestController
@RequestMapping("/admin/backups")
@RequiredArgsConstructor
@Tag(name = "后台数据库备份管理")
public class AdminBackupController {

    private final BackupService backupService;

    @GetMapping("/page")
    @Operation(summary = "分页查询备份记录")
    public Result<Page<BackupRecord>> page(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String type) {
        return Result.success(backupService.page(current, size, type));
    }

    @PostMapping
    @Admin("手动备份数据库")
    @Operation(summary = "手动执行数据库备份")
    public Result<BackupRecord> backup() {
        return Result.success(backupService.backup("manual"));
    }

    @DeleteMapping("/{id}")
    @Admin("删除备份")
    @Operation(summary = "删除备份")
    public Result<Void> delete(@PathVariable Long id) {
        backupService.delete(id);
        return Result.success();
    }

    @PostMapping("/{id}/restore")
    @Admin("还原数据库")
    @Operation(summary = "还原数据库到指定备份")
    public Result<Void> restore(@PathVariable Long id) {
        backupService.restore(id);
        return Result.success();
    }
}
