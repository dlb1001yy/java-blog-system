package com.dlbyy.blog.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 试卷-题目关联实体
 * <p>
 * 该表无逻辑删除与更新时间字段，不继承 {@link BaseEntity}。
 */
@Data
@TableName("exam_paper_question")
public class ExamPaperQuestion implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 试卷ID */
    private Long paperId;

    /** 题目ID */
    private Long questionId;

    /** 题目顺序 */
    private Integer sortOrder;

    /** 本卷该题分值 */
    private BigDecimal score;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
