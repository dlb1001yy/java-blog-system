package com.dlbyy.blog.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dlbyy.blog.common.exception.BusinessException;
import com.dlbyy.blog.config.BackupProperties;
import com.dlbyy.blog.entity.BackupRecord;
import com.dlbyy.blog.mapper.BackupRecordMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * 数据库备份服务。
 * <p>
 * 不依赖 mysqldump（容器内通常未安装），通过 JDBC 元数据遍历全库：
 * 逐表导出 SHOW CREATE TABLE 建表语句与全量 INSERT 数据，
 * GZIP 压缩落盘为 {@code backup_yyyyMMdd_HHmmss.sql.gz}，支持还原、删除与过期清理。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BackupService {

    /** 备份/还原互斥标记：同一时刻仅允许一个备份或还原在执行，防止交错执行导致数据损坏 */
    private final AtomicBoolean backingUp = new AtomicBoolean(false);
    /** 全量 SELECT 的 fetch size：MySQL 驱动仅 Integer.MIN_VALUE（逐行流式）生效，正值会被忽略（全量缓冲） */
    private static final int FETCH_SIZE = Integer.MIN_VALUE;
    private static final DateTimeFormatter FILE_NAME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
    /** 单条错误信息写入记录表时的最大长度 */
    private static final int ERROR_MAX_LENGTH = 1000;

    private final DataSource dataSource;
    private final BackupRecordMapper backupRecordMapper;
    private final BackupProperties backupProperties;

    /**
     * 执行一次全库备份（导出 + 写记录 + 清理过期）
     *
     * @param type 备份类型 auto:定时 manual:手动
     * @return 备份记录（成功时含文件名与大小）
     */
    public BackupRecord backup(String type) {
        if (!backingUp.compareAndSet(false, true)) {
            throw new BusinessException("已有备份正在执行");
        }
        LocalDateTime backupTime = LocalDateTime.now();
        String fileName = "backup_" + backupTime.format(FILE_NAME_FORMATTER) + ".sql.gz";
        Path dir = Paths.get(backupProperties.getDir());
        Path file = dir.resolve(fileName);
        try {
            // 目录不存在则创建（含父目录）
            Files.createDirectories(dir);
            try (Connection conn = dataSource.getConnection();
                 GZIPOutputStream gzip = new GZIPOutputStream(Files.newOutputStream(file));
                 BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(gzip, StandardCharsets.UTF_8))) {
                writeHeader(writer, backupTime);
                dumpAllTables(conn, writer);
                writer.write("SET FOREIGN_KEY_CHECKS=1;\n");
            }
            BackupRecord record = new BackupRecord();
            record.setFileName(fileName);
            record.setFileSize(Files.size(file));
            record.setType(type);
            record.setStatus("success");
            record.setBackupTime(backupTime);
            // 备份记录在 dump 完成后插入，导出内容不含本次记录，避免循环自引用
            backupRecordMapper.insert(record);
            log.info("[BackupService] 数据库备份完成: {} ({} 字节)", fileName, record.getFileSize());
            return record;
        } catch (Exception e) {
            log.error("[BackupService] 数据库备份失败", e);
            // 清理残留的不完整备份文件（尽力而为，删除失败不影响记录）
            try {
                Files.deleteIfExists(file);
            } catch (IOException ignore) {
                // 忽略：残留文件下次过期清理或手动删除
            }
            BackupRecord record = new BackupRecord();
            record.setFileName(fileName);
            record.setType(type);
            record.setStatus("failed");
            record.setErrorMsg(briefError(e));
            record.setBackupTime(backupTime);
            backupRecordMapper.insert(record);
            throw new BusinessException("数据库备份失败: " + e.getMessage());
        } finally {
            backingUp.set(false);
            // 无论成功失败，最后都尝试清理过期备份；清理失败仅记日志，
            // 不能让它从 finally 抛出而掩盖真正的备份失败原因（或把成功备份误报为失败）
            try {
                deleteExpired();
            } catch (Exception e) {
                log.warn("[BackupService] 清理过期备份时出错（不影响本次备份结果）：{}", e.getMessage());
            }
        }
    }

    /**
     * 写出备份文件头部注释与会话设置
     */
    private void writeHeader(BufferedWriter writer, LocalDateTime backupTime) throws IOException {
        writer.write("-- Java Blog Database Backup\n");
        writer.write("-- time: " + backupTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) + "\n");
        writer.write("SET NAMES utf8mb4;\n");
        writer.write("SET FOREIGN_KEY_CHECKS=0;\n");
    }

    /**
     * 遍历当前库全部用户表并导出（建表语句 + 全量 INSERT）
     */
    private void dumpAllTables(Connection conn, BufferedWriter writer) throws SQLException, IOException {
        DatabaseMetaData meta = conn.getMetaData();
        List<String> tables = new ArrayList<>();
        try (ResultSet rs = meta.getTables(conn.getCatalog(), null, "%", new String[]{"TABLE"})) {
            while (rs.next()) {
                String tableName = rs.getString("TABLE_NAME");
                // getTables 已按当前库 catalog 过滤，不会混入 information_schema 等系统表
                if (tableName == null) {
                    continue;
                }
                tables.add(tableName);
            }
        }
        for (String table : tables) {
            dumpTable(conn, writer, table);
        }
    }

    /**
     * 导出单表：DROP + 建表语句 + 全量 INSERT
     */
    private void dumpTable(Connection conn, BufferedWriter writer, String table) throws SQLException, IOException {
        // 建表语句
        writer.write("DROP TABLE IF EXISTS `" + table + "`;\n");
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SHOW CREATE TABLE `" + table + "`")) {
            if (rs.next()) {
                writer.write(rs.getString(2) + ";\n");
            }
        }
        // 全量数据（逐行拼 INSERT，fetch size 控制内存占用）
        try (Statement stmt = conn.createStatement()) {
            stmt.setFetchSize(FETCH_SIZE);
            try (ResultSet rs = stmt.executeQuery("SELECT * FROM `" + table + "`")) {
                ResultSetMetaData rsMeta = rs.getMetaData();
                int columnCount = rsMeta.getColumnCount();
                while (rs.next()) {
                    StringBuilder sb = new StringBuilder("INSERT INTO `").append(table).append("` VALUES (");
                    for (int i = 1; i <= columnCount; i++) {
                        if (i > 1) {
                            sb.append(", ");
                        }
                        sb.append(escapeValue(rs.getObject(i)));
                    }
                    sb.append(");\n");
                    writer.write(sb.toString());
                }
            }
        }
    }

    /**
     * SQL 字面量转义：
     * null→NULL；数字按 toString；byte[]→0xHEX；
     * 时间类型 ISO 格式加引号；其余（含字符串）加单引号并转义 \ ' \n \r
     */
    private String escapeValue(Object value) {
        if (value == null) {
            return "NULL";
        }
        if (value instanceof byte[] bytes) {
            StringBuilder hex = new StringBuilder("0x");
            for (byte b : bytes) {
                hex.append(String.format("%02X", b));
            }
            return hex.toString();
        }
        if (value instanceof Number || value instanceof Boolean) {
            return value.toString();
        }
        // 时间类型：JDBC 旧三件（Timestamp/Date/Time）与 JSR-310（LocalDateTime 等）toString 均为
        // 'yyyy-MM-dd[ HH:mm:ss...]' 形式，MySQL 可直接解析，统一加引号
        if (value instanceof java.util.Date
                || value instanceof LocalDateTime || value instanceof LocalDate || value instanceof LocalTime
                || value instanceof OffsetDateTime || value instanceof OffsetTime) {
            return "'" + value.toString() + "'";
        }
        // 字符串与其他类型：统一按字符串转义
        // \0(NUL) 与 \Z(0x1A) 按 MySQL 官方转义规则同样必须处理：
        // NUL 字节裸写在语句文本中会截断解析，0x1A 在部分平台被视为 EOF
        String text = value.toString();
        StringBuilder sb = new StringBuilder("'");
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            switch (c) {
                case '\\' -> sb.append("\\\\");
                case '\'' -> sb.append("\\'");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\0' -> sb.append("\\0");
                case '\u001A' -> sb.append("\\Z");
                default -> sb.append(c);
            }
        }
        sb.append("'");
        return sb.toString();
    }

    /**
     * 还原指定备份：读 gzip 全文 → SQL 切分器逐句执行
     *
     * @param id 备份记录 ID
     */
    public void restore(Long id) {
        // 与备份共用互斥标记：还原进行中拒绝再次还原与备份（反之亦然），
        // 防止交错执行 DROP/CREATE/INSERT 损坏数据、或把还原中间态 dump 成 success 备份
        if (!backingUp.compareAndSet(false, true)) {
            throw new BusinessException("已有备份或还原正在执行");
        }
        try {
            doRestore(id);
        } finally {
            backingUp.set(false);
        }
    }

    /**
     * 还原的实际执行逻辑（由 {@link #restore(Long)} 持有互斥标记后调用）
     */
    private void doRestore(Long id) {
        BackupRecord record = backupRecordMapper.selectById(id);
        if (record == null || !"success".equals(record.getStatus())) {
            throw new BusinessException("备份记录不存在或状态不可还原");
        }
        Path file = Paths.get(backupProperties.getDir()).resolve(record.getFileName());
        if (!Files.exists(file)) {
            throw new BusinessException("备份文件不存在: " + record.getFileName());
        }
        // 先读出全文再执行，避免执行过程中文件被改动
        String content = readGzipFile(file);
        List<String> statements = splitSqlStatements(content);

        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            int index = 0;
            for (String sql : statements) {
                index++;
                try {
                    stmt.execute(sql);
                } catch (SQLException e) {
                    throw new BusinessException("还原失败：第 " + index + " 条语句执行出错 - " + e.getMessage());
                }
            }
            log.info("[BackupService] 数据库还原完成: {} 共执行 {} 条语句", record.getFileName(), index);
        } catch (SQLException e) {
            throw new BusinessException("还原失败: " + e.getMessage());
        }
    }

    /**
     * 读取 gzip 备份文件全文（UTF-8）
     */
    private String readGzipFile(Path file) {
        StringBuilder sb = new StringBuilder();
        try (GZIPInputStream gzip = new GZIPInputStream(Files.newInputStream(file));
             BufferedReader reader = new BufferedReader(new InputStreamReader(gzip, StandardCharsets.UTF_8))) {
            char[] buffer = new char[8192];
            int n;
            while ((n = reader.read(buffer)) != -1) {
                sb.append(buffer, 0, n);
            }
        } catch (IOException e) {
            throw new BusinessException("读取备份文件失败: " + e.getMessage());
        }
        return sb.toString();
    }

    /**
     * SQL 语句切分器：按分号切分，正确处理以下场景中的分号（不视为语句结尾）：
     * <ul>
     *     <li>单引号字符串 'abc'（含反斜杠转义 \' 与 \\）</li>
     *     <li>双引号字符串 "abc"（MySQL 默认模式下同为字符串字面量，同样处理反斜杠转义）</li>
     *     <li>反引号标识符 {@code `table`}（标识符内不再处理转义，直接找下一个反引号）</li>
     *     <li>{@code --} 行注释（到行尾）与 {@code #} 行注释（到行尾）</li>
     *     <li>{@code /* *}{@code /} 块注释（跨行，整体保留在语句中不影响切分）</li>
     * </ul>
     */
    List<String> splitSqlStatements(String sql) {
        List<String> statements = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        int len = sql.length();
        int i = 0;
        while (i < len) {
            char c = sql.charAt(i);
            // 行注释 -- 与 #：整段连同行尾读入当前语句，不在注释内部识别分号
            if (c == '-' && i + 1 < len && sql.charAt(i + 1) == '-') {
                while (i < len && sql.charAt(i) != '\n') {
                    current.append(sql.charAt(i));
                    i++;
                }
                continue;
            }
            if (c == '#') {
                while (i < len && sql.charAt(i) != '\n') {
                    current.append(sql.charAt(i));
                    i++;
                }
                continue;
            }
            // 块注释：整体读入（含其中的分号与换行）
            if (c == '/' && i + 1 < len && sql.charAt(i + 1) == '*') {
                current.append("/*");
                i += 2;
                while (i + 1 < len && !(sql.charAt(i) == '*' && sql.charAt(i + 1) == '/')) {
                    current.append(sql.charAt(i));
                    i++;
                }
                if (i + 1 < len) {
                    current.append("*/");
                    i += 2;
                }
                continue;
            }
            // 单/双引号字符串：整体读入，处理反斜杠转义（\' \" \\ 等跳过下一字符）
            if (c == '\'' || c == '"') {
                char quote = c;
                current.append(quote);
                i++;
                while (i < len) {
                    char sc = sql.charAt(i);
                    current.append(sc);
                    if (sc == '\\' && i + 1 < len) {
                        // 反斜杠转义：连同被转义字符一起读入
                        current.append(sql.charAt(i + 1));
                        i += 2;
                        continue;
                    }
                    i++;
                    if (sc == quote) {
                        break;
                    }
                }
                continue;
            }
            // 反引号标识符：整体读入，标识符内不处理转义
            if (c == '`') {
                current.append(c);
                i++;
                while (i < len) {
                    char sc = sql.charAt(i);
                    current.append(sc);
                    i++;
                    if (sc == '`') {
                        break;
                    }
                }
                continue;
            }
            // 语句结束符
            if (c == ';') {
                addStatement(statements, current);
                current.setLength(0);
                i++;
                continue;
            }
            current.append(c);
            i++;
        }
        // 收尾：无分号结尾的最后一段
        addStatement(statements, current);
        return statements;
    }

    /**
     * 去除首尾空白后为空的语句不入结果
     */
    private void addStatement(List<String> statements, StringBuilder sb) {
        String s = sb.toString().trim();
        if (!s.isEmpty()) {
            statements.add(s);
        }
    }

    /**
     * 删除指定备份：删文件（不存在仅提示）+ 删记录
     */
    public void delete(Long id) {
        BackupRecord record = backupRecordMapper.selectById(id);
        if (record == null) {
            throw new BusinessException("备份记录不存在");
        }
        Path file = Paths.get(backupProperties.getDir()).resolve(record.getFileName());
        try {
            if (record.getFileName() != null && Files.exists(file)) {
                Files.delete(file);
            } else {
                log.warn("[BackupService] 备份文件不存在，仅删除记录: {}", record.getFileName());
            }
        } catch (IOException e) {
            log.warn("[BackupService] 删除备份文件失败: {}", file, e);
        }
        backupRecordMapper.deleteById(id);
        log.info("[BackupService] 已删除备份记录: {} ({})", id, record.getFileName());
    }

    /**
     * 清理过期备份：备份时间早于 now - retentionDays 的记录删文件（文件缺失忽略）+ 删记录
     */
    public void deleteExpired() {
        LocalDateTime expireBefore = LocalDateTime.now().minusDays(backupProperties.getRetentionDays());
        List<BackupRecord> expired = backupRecordMapper.selectList(new LambdaQueryWrapper<BackupRecord>()
                .lt(BackupRecord::getBackupTime, expireBefore));
        if (expired.isEmpty()) {
            return;
        }
        Path dir = Paths.get(backupProperties.getDir());
        for (BackupRecord record : expired) {
            if (record.getFileName() != null) {
                try {
                    // 文件缺失时忽略（Files.deleteIfExists 静默处理）
                    Files.deleteIfExists(dir.resolve(record.getFileName()));
                } catch (IOException e) {
                    // 文件被占用等场景：跳过文件，仅清理记录，日志提示人工介入
                    log.warn("[BackupService] 清理过期备份文件失败: {}", record.getFileName());
                }
            }
            backupRecordMapper.deleteById(record.getId());
        }
        log.info("[BackupService] 已清理过期备份 {} 条（保留 {} 天）", expired.size(), backupProperties.getRetentionDays());
    }

    /**
     * 分页查询备份记录
     *
     * @param current 页码
     * @param size    每页条数
     * @param type    备份类型过滤（可选：auto/manual）
     */
    public Page<BackupRecord> page(Integer current, Integer size, String type) {
        Page<BackupRecord> page = new Page<>(current, size);
        LambdaQueryWrapper<BackupRecord> wrapper = new LambdaQueryWrapper<>();
        if (type != null && !type.isEmpty()) {
            wrapper.eq(BackupRecord::getType, type);
        }
        wrapper.orderByDesc(BackupRecord::getBackupTime);
        return backupRecordMapper.selectPage(page, wrapper);
    }

    /**
     * 生成错误摘要（类名 + 消息 + 有限堆栈，截断）
     */
    private String briefError(Exception e) {
        StringBuilder sb = new StringBuilder(e.getClass().getName());
        if (e.getMessage() != null) {
            sb.append(": ").append(e.getMessage());
        }
        StackTraceElement[] stack = e.getStackTrace();
        for (int i = 0; i < Math.min(stack.length, 5); i++) {
            sb.append("\n    at ").append(stack[i]);
        }
        String text = sb.toString();
        return text.length() > ERROR_MAX_LENGTH ? text.substring(0, ERROR_MAX_LENGTH) : text;
    }
}
