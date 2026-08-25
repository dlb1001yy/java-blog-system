package com.dlbyy.blog.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 面试题实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("interview_question")
public class InterviewQuestion extends BaseEntity {

    /** 分类ID（关联 blog_category） */
    private Long categoryId;

    /** 难度：简单/中等/困难 */
    private String difficulty;

    /** 题目标题/题干 */
    private String title;

    /** 参考答案（支持 Markdown/代码块） */
    private String answer;

    /** 解题思路/拓展（支持 Markdown） */
    private String tips;

    /** 状态 0:停用 1:启用 */
    private Integer status;

    // ---- 非数据库字段 ----

    /** 标签ID列表（维护 interview_question_tag 关联表） */
    @TableField(exist = false)
    private List<Long> tagIds;

    /** 标签名称列表（从关联表查询填充） */
    @TableField(exist = false)
    private List<String> tagNameList;

    /** 分类名称（从 blog_category 查询填充；导入时可作为分类名称入参） */
    @TableField(exist = false)
    private String categoryName;
}
