package com.dlbyy.blog.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dlbyy.blog.annotation.Admin;
import com.dlbyy.blog.common.Result;
import com.dlbyy.blog.entity.ScheduledTask;
import com.dlbyy.blog.mapper.ScheduledTaskMapper;
import com.dlbyy.blog.task.DynamicTaskManager;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 后台定时任务管理接口。
 * <p>任务数量少，分页接口直接查全量包装 Page 返回，并补充下次触发时间。</p>
 */
@RestController
@RequestMapping("/admin/tasks")
@RequiredArgsConstructor
@Tag(name = "后台定时任务管理")
public class AdminTaskController {

    private final ScheduledTaskMapper scheduledTaskMapper;
    private final DynamicTaskManager dynamicTaskManager;

    @GetMapping("/page")
    @Operation(summary = "分页查询定时任务")
    public Result<Page<TaskVO>> page(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size) {

        Page<ScheduledTask> page = new Page<>(current, size);
        LambdaQueryWrapper<ScheduledTask> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(ScheduledTask::getTaskKey);
        Page<ScheduledTask> result = scheduledTaskMapper.selectPage(page, wrapper);

        // 转换为 VO 并补充下次触发时间（暂停/非法 cron 时为 null）
        Page<TaskVO> voPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        List<TaskVO> voList = new ArrayList<>();
        for (ScheduledTask task : result.getRecords()) {
            TaskVO vo = new TaskVO();
            vo.setId(task.getId());
            vo.setTaskKey(task.getTaskKey());
            vo.setTaskName(task.getTaskName());
            vo.setDescription(task.getDescription());
            vo.setCronExpression(task.getCronExpression());
            vo.setStatus(task.getStatus());
            vo.setLastExecuteTime(task.getLastExecuteTime());
            vo.setLastExecuteStatus(task.getLastExecuteStatus());
            vo.setLastExecuteError(task.getLastExecuteError());
            vo.setNextTriggerTime(dynamicTaskManager.nextTriggerTime(task.getTaskKey()));
            voList.add(vo);
        }
        voPage.setRecords(voList);
        return Result.success(voPage);
    }

    @PutMapping("/{id}/cron")
    @Admin("修改定时任务cron")
    @Operation(summary = "修改定时任务cron表达式")
    public Result<Void> updateCron(@PathVariable Long id, @RequestBody CronUpdateDTO dto) {
        ScheduledTask task = scheduledTaskMapper.selectById(id);
        if (task == null) {
            return Result.error("任务不存在");
        }
        dynamicTaskManager.reschedule(task.getTaskKey(), dto.getCron());
        return Result.success();
    }

    @PutMapping("/{id}/pause")
    @Admin("暂停定时任务")
    @Operation(summary = "暂停定时任务")
    public Result<Void> pause(@PathVariable Long id) {
        ScheduledTask task = scheduledTaskMapper.selectById(id);
        if (task == null) {
            return Result.error("任务不存在");
        }
        dynamicTaskManager.pause(task.getTaskKey());
        return Result.success();
    }

    @PutMapping("/{id}/resume")
    @Admin("恢复定时任务")
    @Operation(summary = "恢复定时任务")
    public Result<Void> resume(@PathVariable Long id) {
        ScheduledTask task = scheduledTaskMapper.selectById(id);
        if (task == null) {
            return Result.error("任务不存在");
        }
        dynamicTaskManager.resume(task.getTaskKey());
        return Result.success();
    }

    @PostMapping("/{id}/run")
    @Admin("手动执行定时任务")
    @Operation(summary = "手动触发定时任务")
    public Result<String> run(@PathVariable Long id) {
        ScheduledTask task = scheduledTaskMapper.selectById(id);
        if (task == null) {
            return Result.error("任务不存在");
        }
        dynamicTaskManager.triggerOnce(task.getTaskKey());
        return Result.success("已触发");
    }

    @Data
    public static class CronUpdateDTO {
        /** 新 cron 表达式（Spring CronExpression 格式） */
        private String cron;
    }

    @Data
    public static class TaskVO {
        private Long id;
        /** 任务唯一标识 */
        private String taskKey;
        /** 任务名称 */
        private String taskName;
        /** 任务描述 */
        private String description;
        /** cron 表达式 */
        private String cronExpression;
        /** 状态 1:启用 0:暂停 */
        private Integer status;
        /** 最近执行时间 */
        private LocalDateTime lastExecuteTime;
        /** 最近执行状态 success/failed */
        private String lastExecuteStatus;
        /** 最近执行错误 */
        private String lastExecuteError;
        /** 下次触发时间（暂停时为 null） */
        private LocalDateTime nextTriggerTime;
    }
}
