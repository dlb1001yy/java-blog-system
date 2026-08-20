package com.dlbyy.blog.controller.admin;

import com.dlbyy.blog.annotation.Admin;
import com.dlbyy.blog.common.Result;
import com.dlbyy.blog.entity.User;
import com.dlbyy.blog.service.UserService;
import com.dlbyy.blog.utils.PasswordStrengthValidator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 后台用户管理（新增用户 / 修改密码）
 * 所有密码写入路径统一经过强度校验 + BCrypt 加密，确保数据库不存明文。
 */
@RestController
@RequestMapping("/admin/user")
@RequiredArgsConstructor
@Tag(name = "后台用户管理")
public class AdminUserController {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/create")
    @Admin("新增用户")
    @Operation(summary = "新增用户（密码强度校验 + BCrypt 加密）")
    public Result<?> createUser(@RequestBody CreateUserRequest request) {
        if (request.getUsername() == null || request.getUsername().isBlank()) {
            return Result.error(400, "用户名不能为空");
        }
        if (userService.getByUsername(request.getUsername()) != null) {
            return Result.error(400, "用户名已存在");
        }
        PasswordStrengthValidator.ValidationResult result =
                PasswordStrengthValidator.validate(request.getPassword());
        if (!result.isValid()) {
            return Result.error(400, result.getMessage());
        }
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setNickname(request.getNickname() == null ? request.getUsername() : request.getNickname());
        user.setRole(request.getRole() == null ? "user" : request.getRole());
        user.setStatus(1);
        userService.save(user);
        return Result.success("用户创建成功");
    }

    @PostMapping("/change-password")
    @Admin("修改用户密码")
    @Operation(summary = "修改密码（强度校验 + BCrypt 加密）")
    public Result<?> changePassword(@RequestBody ChangePasswordRequest request) {
        User user = userService.getByUsername(request.getUsername());
        if (user == null) {
            return Result.error(404, "用户不存在");
        }
        PasswordStrengthValidator.ValidationResult result =
                PasswordStrengthValidator.validate(request.getNewPassword());
        if (!result.isValid()) {
            return Result.error(400, result.getMessage());
        }
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userService.updateById(user);
        return Result.success("密码修改成功");
    }

    // 用户列表/统计/重置密码/启停用等管理功能由 AdminUserManageController（/admin/users）统一提供

    @Data
    public static class CreateUserRequest {
        private String username;
        private String password;
        private String nickname;
        private String role;
    }

    @Data
    public static class ChangePasswordRequest {
        private String username;
        private String newPassword;
    }
}
