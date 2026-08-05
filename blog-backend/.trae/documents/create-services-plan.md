# 创建 Service 接口（UserService、ResumeInfoService、LinkService、ConfigService）

## 总结
参考 TagService.java 的模式，为 4 个实体类创建对应的 Service 接口。

## 当前状态分析
- service 目录下已有：ArticleService.java、CategoryService.java、CommentService.java、MessageService.java、TagService.java
- 统一使用 MyBatis-Plus 的 `IService<T>` 接口
- 代码模式：
  ```java
  public interface XxxService extends IService<Xxx> {
  }
  ```

## 变更计划

在 `src/main/java/com/dlbyy/blog/service/` 目录下创建以下 4 个文件：

| 文件 | 对应实体 |
|------|---------|
| UserService.java | User |
| ResumeInfoService.java | ResumeInfo |
| LinkService.java | Link |
| ConfigService.java | Config |

每个文件内容结构：
```java
package com.dlbyy.blog.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.dlbyy.blog.entity.Xxx;

public interface XxxService extends IService<Xxx> {
}
```

## 验证步骤
1. 确认 4 个文件已创建在 service 目录下
2. 每个接口都继承 `IService<对应实体>`
