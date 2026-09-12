package com.dlbyy.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dlbyy.blog.entity.ScheduledTask;
import org.apache.ibatis.annotations.Mapper;

/**
 * 定时任务 Mapper
 */
@Mapper
public interface ScheduledTaskMapper extends BaseMapper<ScheduledTask> {
}
