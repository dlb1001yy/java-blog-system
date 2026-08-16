package com.dlbyy.blog.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dlbyy.blog.common.Result;
import com.dlbyy.blog.entity.OperationLog;
import com.dlbyy.blog.mapper.OperationLogMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/operation-logs")
@RequiredArgsConstructor
@Tag(name = "后台操作日志")
public class AdminOperationLogController {

    private final OperationLogMapper operationLogMapper;

    @GetMapping("/page")
    @Operation(summary = "分页查询操作日志")
    public Result<Page<OperationLog>> page(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String operation,
            @RequestParam(required = false) Integer status) {

        Page<OperationLog> page = new Page<>(current, size);
        LambdaQueryWrapper<OperationLog> wrapper = new LambdaQueryWrapper<>();
        if (username != null && !username.isEmpty()) {
            wrapper.eq(OperationLog::getUsername, username);
        }
        if (operation != null && !operation.isEmpty()) {
            wrapper.like(OperationLog::getOperation, operation);
        }
        if (status != null) {
            wrapper.eq(OperationLog::getStatus, status);
        }
        wrapper.orderByDesc(OperationLog::getCreateTime);
        return Result.success(operationLogMapper.selectPage(page, wrapper));
    }
}
