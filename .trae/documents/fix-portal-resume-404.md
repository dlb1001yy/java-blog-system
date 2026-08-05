# 修复 /api/portal/resume 404 问题

## 问题分析

**根因**: 前端调用 `GET /portal/resume` 获取简历信息，但后端缺少对应的 Controller 接口。

**证据链**:
1. 前端 API 定义：`blog-admin/src/api/article.js` 第62-64行调用 `request.get('/portal/resume')`
2. 后端已有 `ResumeInfo` 实体、`ResumeInfoMapper`、`ResumeInfoService`，但**没有 Controller** 暴露该接口
3. 后端 context-path 为 `/api`（application.yaml 第4行），所以完整路径是 `/api/portal/resume`

## 修复方案

**新建文件**: `blog-backend/src/main/java/com/dlbyy/blog/controller/portal/PortalResumeController.java`

**实现内容**:
- 创建 `PortalResumeController`，映射路径 `/portal/resume`
- 使用 `ResumeInfoService` 获取简历信息
- 返回第一条记录（博客主人的简历，通常只有一条）
- 遵循现有 Controller 风格（参考 `PortalCategoryController`）

```java
package com.dlbyy.blog.controller.portal;

import com.dlbyy.blog.common.Result;
import com.dlbyy.blog.entity.ResumeInfo;
import com.dlbyy.blog.service.ResumeInfoService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/portal/resume")
@RequiredArgsConstructor
@Tag(name = "前台简历接口")
public class PortalResumeController {

    private final ResumeInfoService resumeInfoService;

    @GetMapping
    public Result<ResumeInfo> get() {
        return Result.success(resumeInfoService.getOne(null));
    }
}
```

## 验证步骤

1. 启动后端服务
2. 访问 `http://localhost:8080/api/portal/resume` 确认返回 200
3. 前端简历页面能正常展示数据
