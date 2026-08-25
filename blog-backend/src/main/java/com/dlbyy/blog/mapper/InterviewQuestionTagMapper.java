package com.dlbyy.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dlbyy.blog.entity.InterviewQuestionTag;
import org.apache.ibatis.annotations.Mapper;

/**
 * 面试题-标签关联 Mapper
 */
@Mapper
public interface InterviewQuestionTagMapper extends BaseMapper<InterviewQuestionTag> {
}
