package com.dlbyy.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dlbyy.blog.entity.ExamMarking;
import org.apache.ibatis.annotations.Mapper;

/**
 * 主观题批改 Mapper
 */
@Mapper
public interface ExamMarkingMapper extends BaseMapper<ExamMarking> {
}
