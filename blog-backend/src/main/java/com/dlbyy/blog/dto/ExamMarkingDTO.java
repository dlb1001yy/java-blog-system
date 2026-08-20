package com.dlbyy.blog.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 主观题评分项
 */
@Data
public class ExamMarkingDTO {

    private Long questionId;

    /** 评分 */
    private BigDecimal score;

    /** 评语 */
    private String comment;
}
