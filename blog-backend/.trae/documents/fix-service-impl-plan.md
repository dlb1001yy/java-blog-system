# 修复 service/impl 目录下类的报错

## 问题分析
检查 service/impl 目录下所有类，发现以下编译错误：

| 文件 | 问题 |
|------|------|
| ArticleServiceImpl.java | 错误导入 `com.javalog.entity.*` 和 `com.javalog.mapper.*`，应为 `com.dlbyy.blog.entity.*` 和 `com.dlbyy.blog.mapper.*` |
| CategoryServiceImpl.java | 缺少 `CategoryMapper` 和 `Category` 的导入 |
| CommentServiceImpl.java | 缺少 `CommentMapper` 和 `Comment` 的导入 |
| MessageServiceImpl.java | 缺少 `MessageMapper` 和 `Message` 的导入 |
| TagServiceImpl.java | 缺少 `TagMapper` 的导入 |

## 变更计划

### 1. 修复 ArticleServiceImpl.java
将错误的包名 `com.javalog` 改为 `com.dlbyy.blog`

### 2. 修复 CategoryServiceImpl.java
添加缺失的导入：
- `com.dlbyy.blog.entity.Category`
- `com.dlbyy.blog.mapper.CategoryMapper`

### 3. 修复 CommentServiceImpl.java
添加缺失的导入：
- `com.dlbyy.blog.entity.Comment`
- `com.dlbyy.blog.mapper.CommentMapper`

### 4. 修复 MessageServiceImpl.java
添加缺失的导入：
- `com.dlbyy.blog.entity.Message`
- `com.dlbyy.blog.mapper.MessageMapper`

### 5. 修复 TagServiceImpl.java
添加缺失的导入：
- `com.dlbyy.blog.mapper.TagMapper`

## 验证步骤
1. 确认所有文件导入正确
2. 确认编译通过
