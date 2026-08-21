package com.dlbyy.blog.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("resume_share")
public class ResumeShare {
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 简历ID */
    private Long resumeId;
    /** 所属用户ID */
    private Long userId;
    /** 分享令牌 */
    private String shareToken;
    /** 过期时间（NULL=永久） */
    private LocalDateTime expireTime;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
