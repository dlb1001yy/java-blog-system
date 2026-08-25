package com.dlbyy.blog.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 面试题-标签关联实体
 */
@Data
@TableName("interview_question_tag")
public class InterviewQuestionTag {

    private Long questionId;
    private Long tagId;
}
