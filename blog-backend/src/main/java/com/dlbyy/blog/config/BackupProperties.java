package com.dlbyy.blog.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 数据库备份配置属性。
 * <p>
 * 对应 application.yaml 中 {@code blog.backup.*} 配置：
 *
 * <pre>
 * blog:
 *   backup:
 *     dir: ./backups          # 备份文件存放目录
 *     retention-days: 30      # 备份保留天数（超出自动清理）
 * </pre>
 */
@Data
@Component
@ConfigurationProperties(prefix = "blog.backup")
public class BackupProperties {

    /** 备份文件存放目录 */
    private String dir = "./backups";

    /** 备份保留天数（超期的备份文件与记录自动清理） */
    private int retentionDays = 30;
}
