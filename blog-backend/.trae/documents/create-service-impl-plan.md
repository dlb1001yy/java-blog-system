# 创建 Service 实现类（UserServiceImpl、ResumeInfoServiceImpl、LinkServiceImpl、ConfigServiceImpl）

## 总结
参考 ArticleServiceImpl.java 的模式，为 4 个 Service 接口创建对应的实现类。

## 当前状态分析
- service/impl 目录下已有：ArticleServiceImpl.java、CategoryServiceImpl.java、CommentServiceImpl.java、MessageServiceImpl.java、TagServiceImpl.java
- 基础实现类模式（CategoryServiceImpl、TagServiceImpl）：
  ```java
  @Service
  public class XxxServiceImpl extends ServiceImpl<XxxMapper, Xxx> implements XxxService {
  }
  ```
- ArticleServiceImpl 包含额外业务逻辑，但基础 Impl 只需继承 ServiceImpl 并实现对应 Service 接口

## 变更计划

在 `src/main/java/com/dlbyy/blog/service/impl/` 目录下创建以下 4 个文件：

| 文件 | 对应 Service | 对应 Mapper | 对应实体 |
|------|-------------|------------|---------|
| UserServiceImpl.java | UserService | UserMapper | User |
| ResumeInfoServiceImpl.java | ResumeInfoService | ResumeInfoMapper | ResumeInfo |
| LinkServiceImpl.java | LinkService | LinkMapper | Link |
| ConfigServiceImpl.java | ConfigServiceImpl | ConfigMapper | Config |

每个文件内容结构：
```java
package com.dlbyy.blog.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dlbyy.blog.entity.Xxx;
import com.dlbyy.blog.mapper.XxxMapper;
import com.dlbyy.blog.service.XxxService;
import org.springframework.stereotype.Service;

@Service
public class XxxServiceImpl extends ServiceImpl<XxxMapper, Xxx> implements XxxService {
}
```

## 验证步骤
1. 确认 4 个文件已创建在 service/impl 目录下
2. 每个类都有 `@Service` 注解
3. 每个类都继承 `ServiceImpl<XxxMapper, Xxx>` 并实现 `XxxService`
