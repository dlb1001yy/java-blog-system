package com.dlbyy.blog.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 实体基类
 * <p>
 * 统一主键、时间字段与逻辑删除字段，供业务实体继承。
 * <ul>
 *     <li>{@link #createTime} — INSERT 时自动填充</li>
 *     <li>{@link #updateTime} — INSERT / UPDATE 时自动填充</li>
 *     <li>{@link #isDeleted} — 逻辑删除字段（0 正常 / 1 已删），INSERT 时自动填充为 0</li>
 * </ul>
 */
@Data
public class BaseEntity implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    @TableField(fill = FieldFill.INSERT)
    private Integer isDeleted;
}
