package com.dlbyy.blog.config;

import com.dlbyy.blog.entity.User;
import com.dlbyy.blog.mapper.SchemaMapper;
import com.dlbyy.blog.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * 数据初始化器：
 * 1) 每次启动幂等补全 sys_user 新增字段（fail_count / lock_until），解决
 *    MySQL initdb 目录仅在首次建库执行、docker 重新部署后旧库字段未更新的问题；
 * 2) 幂等确保后台操作日志表 sys_operation_log 存在（旧库自动补建）；
 * 3) 确保内置管理员账号存在且密码以 BCrypt 哈希存储。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private static final String DEFAULT_ADMIN_USERNAME = "admin";
    private static final String DEFAULT_ADMIN_PASSWORD = "admin123";

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final SchemaMapper schemaMapper;

    @Override
    public void run(String... args) {
        // 幂等补全表字段，确保 docker 重新部署后 sys_user 表结构是最新的
        ensureUserColumns();
        // 幂等确保后台操作日志表存在，供操作日志审计切面写入
        ensureOperationLogTable();

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

    /**
     * 幂等补全 sys_user 表新增字段。仅当列不存在时才执行 ALTER，
     * 因此首次部署与已有旧库重复部署均可安全执行。
     */
    private void ensureUserColumns() {
        try {
            if (schemaMapper.countFailCountColumn() == 0) {
                schemaMapper.addFailCountColumn();
                log.info("[DataInitializer] 已为 sys_user 表新增字段 fail_count");
            }
            if (schemaMapper.countLockUntilColumn() == 0) {
                schemaMapper.addLockUntilColumn();
                log.info("[DataInitializer] 已为 sys_user 表新增字段 lock_until");
            }
        } catch (Exception e) {
            // 表尚未创建（极少数冷启动竞态）时忽略，下次启动重试
            log.warn("[DataInitializer] 补全 sys_user 字段时出错（可忽略，下次启动重试）：{}", e.getMessage());
        }
    }

    /**
     * 幂等确保后台操作日志表 sys_operation_log 存在。仅当表不存在时才执行 CREATE，
     * 首次部署与已有旧库重复部署均可安全执行。
     */
    private void ensureOperationLogTable() {
        try {
            if (schemaMapper.countOperationLogTable() == 0) {
                schemaMapper.createOperationLogTable();
                log.info("[DataInitializer] 已创建后台操作日志表 sys_operation_log");
            }
        } catch (Exception e) {
            // 建库失败时忽略，下次启动重试
            log.warn("[DataInitializer] 创建 sys_operation_log 表时出错（可忽略，下次启动重试）：{}", e.getMessage());
        }
    }
}
