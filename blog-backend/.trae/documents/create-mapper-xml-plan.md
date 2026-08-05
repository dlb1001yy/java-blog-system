# 关于创建 Mapper XML 文件的分析

## 当前状态

项目中存在 10 个 Mapper 接口：
- ArticleMapper、ArticleTagMapper、CategoryMapper、CommentMapper、ConfigMapper、LinkMapper、MessageMapper、ResumeInfoMapper、TagMapper、UserMapper

**所有 Mapper 接口都继承自 `BaseMapper<T>`，且没有任何自定义方法。**

## 重要说明

**Mapper XML 文件对于当前项目不是必需的。**

原因：
1. MyBatis Plus 的 `BaseMapper<T>` 已经提供了完整的 CRUD 操作实现（insert、update、delete、select 等）
2. XML 文件仅用于定义自定义 SQL 查询（如复杂的多表联查、条件查询等）
3. 当前所有 Mapper 接口都没有自定义方法，因此不需要 XML 文件

## 建议方案

如果需要创建 XML 文件作为模板供后续扩展，可以创建以下结构：

```
src/main/resources/mapper/
├── ArticleMapper.xml
├── CategoryMapper.xml
├── CommentMapper.xml
├── ConfigMapper.xml
├── LinkMapper.xml
├── MessageMapper.xml
├── ResumeInfoMapper.xml
├── TagMapper.xml
└── UserServiceImpl.xml
```

每个 XML 文件内容模板：
```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" 
    "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="com.dlbyy.blog.mapper.XxxMapper">
    <!-- 自定义 SQL 语句写在这里 -->
</mapper>
```

## 决策点

需要用户确认：
1. 是否创建空的 XML 文件模板供后续扩展使用？
2. 还是目前不需要创建（因为当前没有自定义 SQL 需求）？
