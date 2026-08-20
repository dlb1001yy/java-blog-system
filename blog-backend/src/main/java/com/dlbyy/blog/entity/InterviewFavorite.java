package com.dlbyy.blog.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 面试题收藏/错题本实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("interview_favorite")
public class InterviewFavorite extends BaseEntity {

    private Long userId;
    private Long questionId;

    /** 类型 0:收藏 1:错题 */
    private Integer type;

    // ---- 非数据库字段 ----

    /** 收藏的题目对象（联表/二次查询填充） */
    @TableField(exist = false)
    private InterviewQuestion question;
}
