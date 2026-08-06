# 挂载整个 sql/ 目录实现 SQL 脚本自动执行

## 目标

将 `docker-compose.yml` 从「逐文件挂载」改为「整个目录挂载」，使得今后在 `blog-backend/sql/` 下新增任何 `.sql` 文件都会被 MySQL 容器自动执行，无需修改 docker-compose.yml。

## 问题分析

### 当前状态

[docker-compose.yml#L24](file:///d:/my-project/java-blog-system/docker-compose.yml#L24) 仅挂载了单个文件：

```yaml
- ./blog-backend/sql/create_sql.sql:/docker-entrypoint-initdb.d/create_sql.sql:ro
```

导致 `article_evolution.sql` 不会被执行，`blog_article` 表缺少 `is_deleted`、`tags`、`status` 三列，MyBatis-Plus 操作会报错。

### 执行顺序陷阱

MySQL `/docker-entrypoint-initdb.d/` 中的脚本按**文件名字母序**执行。当前两份文件：

| 文件名 | 字母序 | 内容 | 正确执行序 |
|--------|--------|------|-----------|
| `article_evolution.sql` | 1（先） | `ALTER TABLE blog_article ADD COLUMN ...` | **必须第 2** |
| `create_sql.sql` | 2（后） | `CREATE TABLE blog_article ...` | **必须第 1** |

如果直接挂载目录不改名，`article_evolution.sql` 会先执行，此时 `blog_article` 表还不存在，`ALTER TABLE` 会报错。

## 修复方案

### 步骤 1：重命名 SQL 文件（加数字前缀）

```
blog-backend/sql/
  create_sql.sql          →  01-create_sql.sql
  article_evolution.sql   →  02-article_evolution.sql
```

数字前缀确保 `create_sql.sql`（建表）先于 `article_evolution.sql`（ALTER 加列）执行。

**今后新增 SQL 文件约定**：`NN-描述.sql`（如 `03-add-comment-table.sql`），NN 为两位数字，确保执行顺序。

### 步骤 2：修改 docker-compose.yml

将单文件挂载改为目录挂载：

```yaml
# 修改前
volumes:
  - mysql_data:/var/lib/mysql
  - ./blog-backend/sql/create_sql.sql:/docker-entrypoint-initdb.d/create_sql.sql:ro

# 修改后
volumes:
  - mysql_data:/var/lib/mysql
  # 挂载整个 sql 目录，新增 .sql 文件自动执行（按文件名排序）
  - ./blog-backend/sql:/docker-entrypoint-initdb.d/:ro
```

### 步骤 3：更新文档中的路径引用

以下文件引用了 `create_sql.sql` 路径，需要同步更新：

| 文件 | 行号 | 当前内容 | 修改为 |
|------|------|---------|--------|
| [README.md](file:///d:/my-project/java-blog-system/README.md#L102) | 102 | `sql/create_sql.sql` | `sql/01-create_sql.sql` |
| [README.md](file:///d:/my-project/java-blog-system/README.md#L238) | 238 | `blog-backend/sql/create_sql.sql` | `blog-backend/sql/01-create_sql.sql` |
| [部署操作手册.md](file:///d:/my-project/java-blog-system/部署操作手册.md#L63) | 63 | `blog-backend/sql/create_sql.sql` | `blog-backend/sql/01-create_sql.sql` |
| [部署操作手册.md](file:///d:/my-project/java-blog-system/部署操作手册.md#L497) | 497 | `blog-backend/sql/create_sql.sql` | `blog-backend/sql/01-create_sql.sql` |
| [wiki/部署操作手册.md](file:///d:/my-project/java-blog-system/wiki/部署操作手册.md#L63) | 63 | `blog-backend/sql/create_sql.sql` | `blog-backend/sql/01-create_sql.sql` |
| [wiki/部署操作手册.md](file:///d:/my-project/java-blog-system/wiki/部署操作手册.md#L497) | 497 | `blog-backend/sql/create_sql.sql` | `blog-backend/sql/01-create_sql.sql` |
| [blog-backend/README.md](file:///d:/my-project/java-blog-system/blog-backend/README.md#L34) | 34 | `sql/create_sql.sql` | `sql/01-create_sql.sql` |
| [blog-backend/README.md](file:///d:/my-project/java-blog-system/blog-backend/README.md#L178) | 178 | `sql/create_sql.sql` | `sql/01-create_sql.sql` |

## 注意事项

### 已有数据卷的场景

`/docker-entrypoint-initdb.d/` 脚本**仅在数据卷首次创建时执行**。如果 `blog_mysql_data` 卷已存在，修改后重启不会重新执行。

```bash
# 全新部署（无历史数据）
docker compose down -v
docker compose up -d --build

# 已有数据（需保留）——手动执行演进脚本
docker exec -i blog-mysql mysql -uroot -p123456 dlbyy_zp_blog < blog-backend/sql/02-article_evolution.sql
```

### 幂等性

`article_evolution.sql` 使用 `ALTER TABLE ADD COLUMN`，列已存在时会报错。首次初始化无影响；手动重复执行需注意。

## 验证步骤

1. 执行 `docker compose down -v && docker compose up -d --build`
2. 验证字段：
   ```bash
   docker exec blog-mysql mysql -uroot -p123456 -e "DESCRIBE dlbyy_zp_blog.blog_article;"
   ```
   应看到 `is_deleted`、`tags`、`status` 三列
3. 验证脚本执行日志：
   ```bash
   docker logs blog-mysql 2>&1 | grep -i "docker-entrypoint-initdb"
   ```
   应看到 `01-create_sql.sql` 和 `02-article_evolution.sql` 被执行
4. 启动后端，调用 `GET /api/admin/articles/page`，确认无 SQL 异常
