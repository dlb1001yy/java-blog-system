# 批量创建实体类（Comment、Message、ResumeInfo、Link、Config）

## 总结
参考 Category 类的生成方式，根据 SQL 脚本中的表定义，创建 5 个实体类。

## 当前状态分析
- entity 目录下已有：Article.java、ArticleTag.java、Category.java、Tag.java、User.java
- 使用 MyBatis-Plus + Lombok 技术栈
- 代码约定：
  - `@Data`（Lombok）
  - `@TableName("表名")` 映射数据库表
  - `@TableId(type = IdType.AUTO)` 标注自增主键
  - 类型映射：`bigint` → `Long`，`varchar` → `String`，`int` → `Integer`，`tinyint` → `Integer`，`datetime` → `LocalDateTime`，`text` → `String`，`date` → `LocalDate`

## 变更计划

### 1. Comment.java → blog_comment（评论表）
**路径**: `src/main/java/com/dlbyy/blog/entity/Comment.java`

| 数据库字段 | Java 类型 | Java 字段名 |
|-----------|----------|------------|
| id | Long | id |
| article_id | Long | articleId |
| user_id | Long | userId |
| nickname | String | nickname |
| email | String | email |
| content | String | content |
| parent_id | Long | parentId |
| reply_to | String | replyTo |
| status | Integer | status |
| ip | String | ip |
| create_time | LocalDateTime | createTime |

### 2. Message.java → blog_message（留言表）
**路径**: `src/main/java/com/dlbyy/blog/entity/Message.java`

| 数据库字段 | Java 类型 | Java 字段名 |
|-----------|----------|------------|
| id | Long | id |
| nickname | String | nickname |
| email | String | email |
| content | String | content |
| status | Integer | status |
| create_time | LocalDateTime | createTime |

### 3. ResumeInfo.java → resume_info（简历信息表）
**路径**: `src/main/java/com/dlbyy/blog/entity/ResumeInfo.java`

| 数据库字段 | Java 类型 | Java 字段名 |
|-----------|----------|------------|
| id | Long | id |
| user_id | Long | userId |
| name | String | name |
| job_title | String | jobTitle |
| gender | Integer | gender |
| birth_date | LocalDate | birthDate |
| phone | String | phone |
| email | String | email |
| address | String | address |
| avatar | String | avatar |
| summary | String | summary |
| skills | String | skills |
| work_experience | String | workExperience |
| education | String | education |
| projects | String | projects |
| create_time | LocalDateTime | createTime |
| update_time | LocalDateTime | updateTime |

### 4. Link.java → blog_link（友情链接表）
**路径**: `src/main/java/com/dlbyy/blog/entity/Link.java`

| 数据库字段 | Java 类型 | Java 字段名 |
|-----------|----------|------------|
| id | Long | id |
| name | String | name |
| url | String | url |
| description | String | description |
| logo | String | logo |
| sort | Integer | sort |
| status | Integer | status |
| create_time | LocalDateTime | createTime |

### 5. Config.java → sys_config（系统配置表）
**路径**: `src/main/java/com/dlbyy/blog/entity/Config.java`

| 数据库字段 | Java 类型 | Java 字段名 |
|-----------|----------|------------|
| id | Long | id |
| config_key | String | configKey |
| config_value | String | configValue |
| description | String | description |
| update_time | LocalDateTime | updateTime |

## 验证步骤
1. 确认 5 个文件已创建在 entity 目录下
2. 每个类都有 `@Data`、`@TableName` 注解
3. 主键字段有 `@TableId(type = IdType.AUTO)` 注解
4. 字段类型映射正确
