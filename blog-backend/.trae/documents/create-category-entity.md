# 创建 Category 实体类

## 总结

根据 `blog_category` 表的 SQL 定义，在 entity 目录下创建对应的 Category 实体类。

## 当前状态分析

* entity 目录下已有 `User.java`、`Article.java`、`Tag.java` 等实体类

* 项目使用 **MyBatis-Plus** + **Lombok** 技术栈

* 现有实体类遵循以下约定：

  * `@Data` 注解（Lombok）

  * `@TableName("表名")` 注解映射数据库表

  * `@TableId(type = IdType.AUTO)` 标注自增主键

  * 类型映射：`bigint` → `Long`，`varchar` → `String`，`int` → `Integer`，`datetime` → `LocalDateTime`

## 表结构（blog\_category）

```sql
CREATE TABLE `blog_category` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `name` varchar(50) NOT NULL COMMENT '分类名称',
    `sort` int DEFAULT 0 COMMENT '排序',
    `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文章分类表';
```

## 变更计划

**新建文件**: `src/main/java/com/dlbyy/blog/entity/Category.java`

字段映射：

| 数据库字段        | Java 类型       | Java 字段名   |
| ------------ | ------------- | ---------- |
| id           | Long          | id         |
| name         | String        | name       |
| sort         | Integer       | sort       |
| create\_time | LocalDateTime | createTime |

## 验证步骤

1. 确认文件已创建在正确路径
2. 确认类上有 `@Data`、`@TableName("blog_category")` 注解
3. 确认主键字段有 `@TableId(type = IdType.AUTO)` 注解

