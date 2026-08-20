package com.dlbyy.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dlbyy.blog.entity.ExamPaperQuestion;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * 试卷-题目关联 Mapper
 */
@Mapper
public interface ExamPaperQuestionMapper extends BaseMapper<ExamPaperQuestion> {

    /**
     * 题目使用次数 +delta（组卷/移除时调用）
     */
    @Update("UPDATE exam_question SET usage_count = usage_count + #{delta} WHERE id = #{questionId}")
    int addUsageCount(@Param("questionId") Long questionId, @Param("delta") int delta);
}
