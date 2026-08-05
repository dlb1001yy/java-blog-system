# 创建 Mapper 接口（CategoryMapper、TagMapper、CommentMapper、MessageMapper、ResumeInfoMapper、LinkMapper、ConfigMapper）

## 总结
参考 ArticleMapper.java 的模式，为 7 个实体类创建对应的 Mapper 接口。

## 当前状态分析
- mapper 目录下已有：ArticleMapper.java、ArticleTagMapper.java、UserMapper.java
- 统一使用 MyBatis-Plus 的 `BaseMapper<T>` 接口
- 代码模式：
  ```java
  @Mapper
  public interface XxxMapper extends BaseMapper<Xxx> {
  }
  ```
- 每个 Mapper 只需继承 BaseMapper，无需额外方法

## 变更计划

在 `src/main/java/com/dlbyy/blog/` 目录下创建以下 7 个文件：

| 文件 | 对应实体 |
|------|---------|
| CategoryMapper.java | Category |
| TagMapper.java | Tag |
| CommentMapper.java | Comment |
| MessageMapper.java | Message |
| ResumeInfoMapper.java | ResumeInfo |
| LinkMapper.java | Link |
| ConfigMapper.java | Config |

每个文件内容结构：
```java
package com.dlbyy.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dlbyy.blog.entity.Xxx;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface XxxMapper extends BaseMapper<Xxx> {
}
```

## 验证步骤
1. 确认 7 个文件已创建在 mapper 目录下
2. 每个接口都有 `@Mapper` 注解
3. 每个接口都继承 `BaseMapper<对应实体>`
