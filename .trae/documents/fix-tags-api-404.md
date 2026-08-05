# 修复标签接口 404 问题

## 问题分析

### 现象
访问 `http://localhost:3000/api/portal/tags` 返回 404

### 根本原因
后端缺少 `PortalTagController` 控制器，没有为前台提供标签列表接口。

### 现有代码分析

**前端 API 调用** (`blog-admin/src/api/article.js`):
```javascript
getTags() {
  return request.get('/portal/tags')
}
```

**后端现有控制器**:
| 控制器 | 路径 | 存在 |
|--------|------|------|
| PortalCategoryController | `/portal/categories` | ✅ |
| PortalArticleController | `/portal/articles` | ✅ |
| PortalCommentController | `/portal/comments` | ✅ |
| PortalMessageController | `/portal/messages` | ✅ |
| PortalTagController | `/portal/tags` | ❌ 缺失 |

**已有代码**:
- `Tag` 实体类: `blog-backend/src/main/java/com/dlbyy/blog/entity/Tag.java`
- `TagService` 接口: `blog-backend/src/main/java/com/dlbyy/blog/service/TagService.java`
- `TagServiceImpl` 实现: `blog-backend/src/main/java/com/dlbyy/blog/service/impl/TagServiceImpl.java`

## 修复方案

### 新建文件
- `blog-backend/src/main/java/com/dlbyy/blog/controller/portal/PortalTagController.java`

### 参考实现
参照 `PortalCategoryController` 的模式创建标签控制器：

```java
package com.dlbyy.blog.controller.portal;

import com.dlbyy.blog.common.Result;
import com.dlbyy.blog.entity.Tag;
import com.dlbyy.blog.service.TagService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/portal/tags")
@RequiredArgsConstructor
@Tag(name = "前台标签接口")
public class PortalTagController {

    private final TagService tagService;

    @GetMapping
    public Result<List<Tag>> list() {
        return Result.success(tagService.list());
    }
}
```

## 验证步骤

1. 创建 `PortalTagController.java`
2. 重启后端服务
3. 访问 `http://localhost:3000/api/portal/tags`
4. 确认返回标签列表数据
