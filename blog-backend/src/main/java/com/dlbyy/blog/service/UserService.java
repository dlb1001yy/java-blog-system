package com.dlbyy.blog.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.dlbyy.blog.common.PageResult;
import com.dlbyy.blog.entity.User;

import java.util.Map;

public interface UserService extends IService<User> {
    User getByUsername(String username);

    /**
     * 管理端用户分页（keyword 匹配用户名/昵称/邮箱，role/status 精确筛选）
     */
    PageResult<User> adminUserList(int page, int size, String keyword, String role, Integer status);

    /**
     * 用户统计：total 总数 / active 活跃（近30天有更新）/ roles 角色分布 / monthNew 本月新增
     */
    Map<String, Object> userStats();

    /**
     * 重置用户密码
     *
     * @param newPwdEncode 已加密（BCrypt）的新密码
     */
    void resetPassword(Long userId, String newPwdEncode);

    /**
     * 启用 / 禁用用户（status 1 启用 0 禁用）
     */
    void enable(Long userId, boolean enable);
}
