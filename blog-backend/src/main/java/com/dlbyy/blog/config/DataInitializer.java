package com.dlbyy.blog.config;

import com.dlbyy.blog.entity.User;
import com.dlbyy.blog.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * 数据初始化器：确保内置管理员账号存在，且密码始终以 BCrypt 哈希存储。
 * 解决 SQL 脚本中密码哈希可能因环境差异失效的问题，并统一密码加密入口。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private static final String DEFAULT_ADMIN_USERNAME = "admin";
    private static final String DEFAULT_ADMIN_PASSWORD = "admin123";

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        User admin = userService.getByUsername(DEFAULT_ADMIN_USERNAME);
        if (admin == null) {
            User user = new User();
            user.setUsername(DEFAULT_ADMIN_USERNAME);
            user.setPassword(passwordEncoder.encode(DEFAULT_ADMIN_PASSWORD));
            user.setNickname("管理员");
            user.setRole("admin");
            user.setStatus(1);
            userService.save(user);
            log.info("[DataInitializer] 已创建内置管理员账号: {}", DEFAULT_ADMIN_USERNAME);
            return;
        }
        // 若已存在但密码非 BCrypt 格式（明文存储），不再强制覆盖为硬编码默认值，
        // 以免错误覆盖用户自定义的强密码。仅告警提示，由运维/管理员手动处理。
        String pwd = admin.getPassword();
        if (pwd == null || (!pwd.startsWith("$2a$") && !pwd.startsWith("$2b$"))) {
            log.warn("[安全告警] 管理员账号 '{}' 的密码未以 BCrypt 格式存储（疑似明文），"
                    + "出于安全考虑未自动重置。请管理员尽快通过改密接口手动重置密码。", DEFAULT_ADMIN_USERNAME);
        }
    }
}
