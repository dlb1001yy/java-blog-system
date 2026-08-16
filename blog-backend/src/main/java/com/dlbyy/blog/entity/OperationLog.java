package com.dlbyy.blog.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 后台操作日志实体。
 * <p>
 * 不继承 {@link BaseEntity}：日志表只增不改，无 update_time 与逻辑删除字段。
 */
@Data
@TableName("sys_operation_log")
public class OperationLog {

    @TableId(type = IdType.AUTO)
    private Long id;
    /** 操作人 */
    private String username;
    /** 操作描述 */
    private String operation;
    /** HTTP 方法（GET/POST/PUT/DELETE 等） */
    private String method;
    /** 请求路径 */
    private String uri;
    /** 请求参数 */
    private String params;
    /** 客户端 IP */
    private String ip;
    /** 操作状态 1:成功 0:失败 */
    private Integer status;
    /** 异常信息 */
    @TableField("error_msg")
    private String errorMsg;
    /** 耗时（毫秒） */
    @TableField("cost_ms")
    private Long costMs;
    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
