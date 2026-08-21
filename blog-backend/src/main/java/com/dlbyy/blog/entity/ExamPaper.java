package com.dlbyy.blog.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 试卷实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("exam_paper")
public class ExamPaper extends BaseEntity {

    /** 试卷名称 */
    private String title;

    /** 试卷说明 */
    private String description;

    /** 总分 */
    private BigDecimal totalScore;

    /** 及格线 */
    private BigDecimal passScore;

    /** 考试时长（分钟） */
    private Integer duration;

    /** 状态 0:草稿 1:已发布 2:已停用 */
    private Integer status;

    // ---- 非数据库字段 ----

    /** 题目数量（查询填充） */
    @TableField(exist = false)
    private Integer questionCount;
}
