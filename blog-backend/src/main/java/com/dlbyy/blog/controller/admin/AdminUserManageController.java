package com.dlbyy.blog.controller.admin;

import com.dlbyy.blog.annotation.Admin;
import com.dlbyy.blog.common.PageResult;
import com.dlbyy.blog.common.Result;
import com.dlbyy.blog.entity.User;
import com.dlbyy.blog.service.UserService;
import com.dlbyy.blog.utils.PasswordStrengthValidator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

/**
 * 后台用户管理（列表/统计/编辑/重置密码/启停用）
 * <p>
 * 与 {@link AdminUserController}（/admin/user 前缀）互补，使用 /admin/users 前缀避免路由冲突。
 */
@RestController
@RequestMapping("/admin/users")
@RequiredArgsConstructor
@Tag(name = "后台用户列表管理")
public class AdminUserManageController {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    @GetMapping("/list")
    @Operation(summary = "用户分页（keyword 匹配用户名/昵称/邮箱，role/status 精确筛选）")
    public Result<PageResult<User>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) Integer status) {
        PageResult<User> pr = userService.adminUserList(page, size, keyword, role, status);
        // 防止密码哈希泄露
        pr.getRecords().forEach(u -> u.setPassword(null));
        return Result.success(pr);
    }

    @GetMapping("/stats")
    @Operation(summary = "用户统计（总数/活跃/角色分布/本月新增）")
    public Result<Map<String, Object>> stats() {
        return Result.success(userService.userStats());
    }

    @PutMapping("/{id}")
    @Admin("编辑用户")
    @Operation(summary = "编辑用户信息")
    public Result<?> update(@PathVariable Long id, @RequestBody UserUpdateRequest request) {
        User user = userService.getById(id);
        if (user == null) {
            return Result.error(404, "用户不存在");
        }
        if (request.getNickname() != null) {
            user.setNickname(request.getNickname());
        }
        if (request.getAvatar() != null) {
            user.setAvatar(request.getAvatar());
        }
        if (request.getEmail() != null) {
            user.setEmail(request.getEmail());
        }
        if (request.getRole() != null) {
            user.setRole(request.getRole());
        }
        if (request.getStatus() != null) {
            user.setStatus(request.getStatus());
        }
        userService.updateById(user);
        return Result.success("更新成功", null);
    }

    @PostMapping("/{id}/reset-password")
    @Admin("重置用户密码")
    @Operation(summary = "重置密码（body.newPassword 为空时生成随机密码并返回）")
    public Result<String> resetPassword(@PathVariable Long id, @RequestBody ResetPasswordRequest request) {
        User user = userService.getById(id);
        if (user == null) {
            return Result.error(404, "用户不存在");
        }
        String newPassword = request != null && request.getNewPassword() != null
                && !request.getNewPassword().isBlank()
                ? request.getNewPassword() : UUID.randomUUID().toString().replace("-", "").substring(0, 12) + "aA1";
        PasswordStrengthValidator.ValidationResult result =
                PasswordStrengthValidator.validate(newPassword);
        if (!result.isValid()) {
            return Result.error(400, result.getMessage());
        }
        userService.resetPassword(id, passwordEncoder.encode(newPassword));
        return Result.success("密码重置成功", newPassword);
    }

    @PostMapping("/{id}/enable")
    @Admin("启用/禁用用户")
    @Operation(summary = "启用/禁用用户（enable=true 启用）")
    public Result<?> enable(@PathVariable Long id, @RequestParam(defaultValue = "true") boolean enable) {
        userService.enable(id, enable);
        return Result.success(enable ? "已启用" : "已禁用", null);
    }

    @Data
    public static class UserUpdateRequest {
        private String nickname;
        private String avatar;
        private String email;
        private String role;
        private Integer status;
    }

    @Data
    public static class ResetPasswordRequest {
        private String newPassword;
    }
}
