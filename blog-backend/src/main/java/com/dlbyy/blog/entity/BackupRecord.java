package com.dlbyy.blog.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 数据库备份记录实体（sys_backup_record）。
 * <p>
 * 不继承 {@link BaseEntity}：备份记录只增不改，无 update_time 与逻辑删除字段。
 */
@Data
@TableName("sys_backup_record")
public class BackupRecord {

    @TableId(type = IdType.AUTO)
    private Long id;
    /** 备份文件名 */
    private String fileName;
    /** 文件大小（字节） */
    private Long fileSize;
    /** 备份类型 auto:定时备份 manual:手动备份 */
    private String type;
    /** 备份状态 success:成功 failed:失败 */
    private String status;
    /** 失败原因 */
    private String errorMsg;
    /** 备份时间 */
    private LocalDateTime backupTime;
    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
