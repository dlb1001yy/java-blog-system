package com.dlbyy.blog.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 答卷成绩详情（含汇总与单题明细）
 */
@Data
public class ExamRecordDetailDTO {

    private Long recordId;

    private Long paperId;

    private String paperTitle;

    /** 客观题得分 */
    private BigDecimal objectiveScore;

    /** 主观题得分 */
    private BigDecimal subjectiveScore;

    /** 最终得分 */
    private BigDecimal finalScore;

    /** 状态 0:待批改 1:已发布 */
    private Integer status;

    private Integer switchCount;

    private Integer durationSeconds;

    private java.time.LocalDateTime submitTime;

    /** 单题明细 */
    private List<ExamRecordDetailItemDTO> items;
}
