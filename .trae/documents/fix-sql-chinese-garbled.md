# 修复 create_sql.sql 中文乱码问题

## Summary

Docker MySQL 容器执行 `blog-backend/sql/create_sql.sql` 初始化数据库时，INSERT 语句中的中文（如 `'管理员'`、`'Java基础'`、`'Spring框架'` 等）全部乱码。根因是 SQL 文件缺少 `SET NAMES utf8mb4;` 声明，且 docker-compose.yml 未配置 MySQL 服务端默认字符集，导致 init 脚本执行时客户端连接字符集为 latin1，中文被错误编码后写入数据库。

## Current State Analysis

**已确认的问题：**

1. `create_sql.sql` 文件开头（第 1-3 行）只有 `CREATE DATABASE` 和 `USE`，**没有 `SET NAMES utf8mb4;`**
2. `docker-compose.yml` MySQL 服务（第 10-31 行）**没有 `command` 参数配置字符集**
3. SQL 文件中有中文内容的 INSERT 语句：
   - 第 147 行：`'管理员'`（用户昵称）
   - 第 151 行：`'Java基础'`、`'Spring框架'`、`'数据库'`、`'前端技术'`、`'面试总结'`（分类名称）
4. JDBC URL（application-docker.yaml 第 11 行）已有 `characterEncoding=utf-8`，应用连接没问题
5. 表定义已有 `CHARSET=utf8mb4`，表级字符集没问题

**乱码原因链：**
MySQL Docker 初始化脚本执行 → 客户端默认 charset=latin1 → INSERT 中文按 latin1 编码 → 存入 utf8mb4 表 → 读取时乱码

## Proposed Changes

### 1. 修改 `blog-backend/sql/create_sql.sql`

在文件最顶部（第 1 行之前）添加字符集声明：

```sql
-- 设置客户端连接字符集，防止中文乱码
SET NAMES utf8mb4;
```

位置：文件第 1 行（在 `CREATE DATABASE` 之前）

### 2. 修改 `docker-compose.yml`

在 MySQL 服务添加 `command` 参数，设置服务端默认字符集：

```yaml
  mysql:
    image: mysql:8.0
    container_name: blog-mysql
    restart: unless-stopped
    command: --character-set-server=utf8mb4 --collation-server=utf8mb4_unicode_ci
    environment:
      ...
```

### 3. 清理已有乱码数据并重新初始化

由于 MySQL 数据卷中已存在乱码数据，仅修改文件不够，必须删除数据卷重新初始化。在文档中提供清理命令：

```bash
# 停止全部服务并删除数据卷
docker compose down -v
# 重新构建启动
docker compose up -d --build
```

## Assumptions & Decisions

- **假设**：用户已有运行中的 MySQL 容器，数据卷中有乱码数据，需要清除重建
- **决策**：`SET NAMES utf8mb4;` 放在 SQL 文件最顶部（CREATE DATABASE 之前），确保整个脚本执行期间客户端字符集正确
- **决策**：docker-compose.yml 添加 `command` 参数设置服务端字符集，双重保障
- **决策**：不修改 application-docker.yaml（JDBC URL 已有 characterEncoding=utf-8，无需改动）

## Verification Steps

1. 确认 `create_sql.sql` 第一行为 `SET NAMES utf8mb4;`
2. 确认 `docker-compose.yml` MySQL 服务有 `command: --character-set-server=utf8mb4 --collation-server=utf8mb4_unicode_ci`
3. 执行 `docker compose down -v` 清理旧数据卷
4. 执行 `docker compose up -d --build` 重新初始化
5. 验证中文数据：
   ```bash
   docker exec blog-mysql mysql -uroot -p123456 -e "SELECT nickname FROM dlbyy_zp_blog.sys_user WHERE username='admin';"
   # 预期输出：管理员（非乱码）

   docker exec blog-mysql mysql -uroot -p123456 -e "SELECT name FROM dlbyy_zp_blog.blog_category;"
   # 预期输出：Java基础、Spring框架、数据库、前端技术、DevOps、面试总结
   ```
6. 验证 MySQL 字符集配置：
   ```bash
   docker exec blog-mysql mysql -uroot -p123456 -e "SHOW VARIABLES LIKE 'character_set%';"
   # 预期 character_set_server 为 utf8mb4
   ```
