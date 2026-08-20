package com.dlbyy.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.dlbyy.blog.entity.ExamRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 考试答卷 Mapper
 */
@Mapper
public interface ExamRecordMapper extends BaseMapper<ExamRecord> {

    /**
     * 待批改答卷分页（联表：考生姓名、试卷名、待批主观题数）
     */
    IPage<ExamRecord> selectPendingPage(IPage<ExamRecord> page,
                                        @Param("keyword") String keyword,
                                        @Param("status") Integer status);
}
