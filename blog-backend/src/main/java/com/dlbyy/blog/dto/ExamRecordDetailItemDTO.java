package com.dlbyy.blog.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 成绩详情单题得分明细（客观题正误 + 主观题评分，供前端图表）
 */
@Data
public class ExamRecordDetailItemDTO {

    private Long questionId;

    /** 题干 */
    private String stem;

    /** 题型 1:单选 2:多选 3:判断 4:填空 5:简答 6:编程 */
    private Integer type;

    /** 分类ID */
    private Long categoryId;

    /** 分类名称 */
    private String categoryName;

    /** 选项 JSON */
    private String options;

    /** 我的答案（字符串或列表序列化后的字符串） */
    private String myAnswer;

    /** 标准答案（客观题 correct JSON） */
    private String correctAnswer;

    /** 解析/参考答案 */
    private String referenceAnswer;

    /** 本卷该题分值 */
    private BigDecimal score;

    /** 该题得分 */
    private BigDecimal gotScore;

    /** 是否正确（客观题：true/false；主观题：null） */
    private Boolean correct;

    /** 主观题评语 */
    private String comment;
}
