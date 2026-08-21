package com.dlbyy.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dlbyy.blog.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface UserMapper extends BaseMapper<User> {

    /**
     * 按角色分组统计用户数
     */
    @Select("SELECT role, COUNT(*) AS cnt FROM sys_user GROUP BY role")
    List<Map<String, Object>> countByRole();
}