package com.dlbyy.blog.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 定时任务实体（sys_scheduled_task）。
 * <p>
 * 不继承 {@link BaseEntity}：任务表无逻辑删除字段，且保留
 * create_time / update_time 由数据库默认值维护的语义。
 */
@Data
@TableName("sys_scheduled_task")
public class ScheduledTask {

    @TableId(type = IdType.AUTO)
    private Long id;
    /** 任务唯一标识（对应 Spring 容器中 ScheduledTaskSpi Bean 的 taskKey） */
    private String taskKey;
    /** 任务名称 */
    private String taskName;
    /** 任务描述 */
    private String description;
    /** cron 表达式（Spring CronExpression 格式） */
    private String cronExpression;
    /** 状态 1:启用 0:暂停 */
    private Integer status;
    /** 最近一次执行时间 */
    private LocalDateTime lastExecuteTime;
    /** 最近一次执行状态 success/failed */
    private String lastExecuteStatus;
    /** 最近一次执行错误信息 */
    private String lastExecuteError;
    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
