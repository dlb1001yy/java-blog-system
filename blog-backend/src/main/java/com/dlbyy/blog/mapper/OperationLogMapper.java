package com.dlbyy.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dlbyy.blog.entity.OperationLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 后台操作日志 Mapper
 */
@Mapper
public interface OperationLogMapper extends BaseMapper<OperationLog> {
}
