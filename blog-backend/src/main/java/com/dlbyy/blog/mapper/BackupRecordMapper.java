package com.dlbyy.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dlbyy.blog.entity.BackupRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * 数据库备份记录 Mapper
 */
@Mapper
public interface BackupRecordMapper extends BaseMapper<BackupRecord> {
}
