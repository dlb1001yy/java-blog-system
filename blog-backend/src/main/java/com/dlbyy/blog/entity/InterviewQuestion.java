package com.dlbyy.blog.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 面试题实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("interview_question")
public class InterviewQuestion extends BaseEntity {

    /** 技术方向：后端/前端/数据库/DevOps/算法 */
    private String category;

    /** 难度：简单/中等/困难 */
    private String difficulty;

    /** 题目标题/题干 */
    private String title;

    /** 标签（逗号分隔） */
    private String tags;

    /** 参考答案（支持 Markdown/代码块） */
    private String answer;

    /** 状态 0:停用 1:启用 */
    private Integer status;
}
