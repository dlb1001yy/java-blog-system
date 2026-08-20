package com.dlbyy.blog.dto;

import lombok.Data;

import java.util.List;

/**
 * 考生交卷答案项
 */
@Data
public class ExamAnswerDTO {

    /** 题目ID */
    private Long questionId;

    /**
     * 考生答案：
     * 客观题——选项索引列表或字符串（填空为字符串/字符串列表）；
     * 主观题——文本
     */
    private Object answer;
}
