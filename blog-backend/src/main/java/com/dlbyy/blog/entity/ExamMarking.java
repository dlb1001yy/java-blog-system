package com.dlbyy.blog.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 主观题批改实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("exam_marking")
public class ExamMarking extends BaseEntity {

    /** 答卷ID */
    private Long recordId;

    /** 题目ID */
    private Long questionId;

    /** 评分 */
    private BigDecimal score;

    /** 评语 */
    private String comment;

    /** 状态 0:草稿 1:已确认 */
    private Integer status;
}
