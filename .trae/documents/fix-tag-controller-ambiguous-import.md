# 修复 PortalTagController 编译报错

## 问题分析

`PortalTagController.java` 同时导入了两个简名为 `Tag` 的类：
- `com.dlbyy.blog.entity.Tag`（实体类，用于 `List<Tag>` 返回值）
- `io.swagger.v3.oas.annotations.tags.Tag`（Swagger 注解，用于 `@Tag(name = "...")`）

Java 编译器无法区分二者，导致"对Tag的引用不明确"编译错误。

## 修复方案

**文件**: `blog-backend/src/main/java/com/dlbyy/blog/controller/portal/PortalTagController.java`

**修改内容**:
1. 删除 `import io.swagger.v3.oas.annotations.tags.Tag;`（第6行）
2. 将类上的 `@Tag(name = "前台标签接口")` 改为使用全限定名 `@io.swagger.v3.oas.annotations.tags.Tag(name = "前台标签接口")`

**原因**: 实体类 `Tag` 在方法签名中使用（`List<Tag>`），保留 import 更简洁；Swagger 的 `@Tag` 注解仅在类上使用一次，使用全限定名更清晰且避免歧义。

## 验证步骤

1. 在 IDE 中确认文件无红色错误提示
2. 执行 `mvn compile` 确认编译通过
