package com.dlbyy.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dlbyy.blog.common.PageResult;
import com.dlbyy.blog.entity.User;
import com.dlbyy.blog.mapper.UserMapper;
import com.dlbyy.blog.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
    @Override
    public User getByUsername(String username) {
        return this.getOne(new LambdaQueryWrapper<User>().eq(User::getUsername, username));
    }

    @Override
    public PageResult<User> adminUserList(int page, int size, String keyword, String role, Integer status) {
        Page<User> p = new Page<>(page, size);
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.and(StringUtils.hasText(keyword), w -> w
                        .like(User::getUsername, keyword)
                        .or().like(User::getNickname, keyword)
                        .or().like(User::getEmail, keyword))
                .eq(StringUtils.hasText(role), User::getRole, role)
                .eq(status != null, User::getStatus, status)
                .orderByDesc(User::getCreateTime);
        this.page(p, wrapper);
        return new PageResult<>(p.getTotal(), p.getRecords());
    }

    @Override
    public Map<String, Object> userStats() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime thirtyDaysAgo = now.minusDays(30);
        LocalDateTime monthStart = YearMonth.from(now).atDay(1).atStartOfDay();

        Map<String, Object> stats = new HashMap<>();
        stats.put("total", this.count());
        // 活跃口径：最近 30 天内有更新（update_time 由 MyBatis-Plus 自动填充，登录/资料变更都会触发）
        stats.put("active", this.count(new LambdaQueryWrapper<User>()
                .ge(User::getUpdateTime, thirtyDaysAgo)));
        // 角色分布
        stats.put("roles", baseMapper.countByRole().stream()
                .collect(Collectors.toMap(m -> (String) m.get("role"),
                        m -> ((Number) m.get("cnt")).longValue())));
        // 本月新增
        stats.put("monthNew", this.count(new LambdaQueryWrapper<User>()
                .ge(User::getCreateTime, monthStart)));
        return stats;
    }

    @Override
    public void resetPassword(Long userId, String newPwdEncode) {
        User user = new User();
        user.setId(userId);
        user.setPassword(newPwdEncode);
        user.setFailCount(0);
        user.setLockUntil(null);
        this.updateById(user);
    }

    @Override
    public void enable(Long userId, boolean enable) {
        User user = new User();
        user.setId(userId);
        user.setStatus(enable ? 1 : 0);
        this.updateById(user);
    }
}
