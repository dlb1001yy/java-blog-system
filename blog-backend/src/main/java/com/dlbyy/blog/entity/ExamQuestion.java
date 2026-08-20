package com.dlbyy.blog.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 考试题目实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("exam_question")
public class ExamQuestion extends BaseEntity {

    /** 题干（支持 Markdown/代码块） */
    private String stem;

    /** 题型 1:单选 2:多选 3:判断 4:填空 5:简答 6:编程 */
    private Integer type;

    /** 分类（知识领域） */
    private String category;

    /** 难度：简单/中等/困难 */
    private String difficulty;

    /** 选项 JSON 数组（客观题） */
    private String options;

    /** 正确答案 JSON（索引/布尔/字符串数组） */
    private String correct;

    /** 参考答案/解析 */
    private String referenceAnswer;

    /** 题目分值 */
    private BigDecimal score;

    /** 使用次数（被试卷引用次数） */
    private Integer usageCount;

    /** 状态 0:停用 1:启用 2:待审核 */
    private Integer status;
}
