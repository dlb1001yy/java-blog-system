package com.dlbyy.blog.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dlbyy.blog.annotation.Admin;
import com.dlbyy.blog.annotation.RateLimit;
import com.dlbyy.blog.common.Result;
import com.dlbyy.blog.common.exception.BusinessException;
import com.dlbyy.blog.entity.Article;
import com.dlbyy.blog.event.ArticlePublishedEvent;
import com.dlbyy.blog.service.ArticleService;
import com.dlbyy.blog.service.MarkdownImportService;
import com.dlbyy.blog.service.TagService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/articles")
@RequiredArgsConstructor
@Tag(name = "后台文章管理")
public class AdminArticleController {

    private final ArticleService articleService;
    private final TagService tagService;
    private final MarkdownImportService markdownImportService;
    private final ApplicationEventPublisher eventPublisher;

    @GetMapping("/page")
    @Operation(summary = "分页查询文章")
    public Result<Page<Article>> page(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) Integer type,
            @RequestParam(required = false) Integer isPublish) {
        
        Page<Article> page = new Page<>(current, size);
        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<>();
        
        if (title != null && !title.isEmpty()) {
            wrapper.like(Article::getTitle, title);
        }
        if (type != null) {
            wrapper.eq(Article::getType, type);
        }
        if (isPublish != null) {
            wrapper.eq(Article::getIsPublish, isPublish);
        }
        wrapper.orderByDesc(Article::getCreateTime);
        
        return Result.success(articleService.page(page, wrapper));
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取文章详情")
    public Result<Article> detail(@PathVariable Long id) {
        Article article = articleService.getById(id);
        if (article != null) {
            articleService.fillArticleInfo(article);
        }
        return Result.success(article);
    }

    @PostMapping
    @Admin("新增文章")
    @Operation(summary = "新增文章")
    public Result<?> create(@RequestBody ArticleDTO dto) {
        Article article = new Article();
        article.setUserId(1L); // TODO: 从当前登录用户获取
        article.setCategoryId(dto.getCategoryId());
        article.setTitle(dto.getTitle());
        article.setSummary(dto.getSummary());
        article.setContent(dto.getContent());
        article.setCoverImage(dto.getCoverImage());
        article.setType(dto.getType());
        article.setSourceUrl(dto.getSourceUrl());
        article.setSourceName(dto.getSourceName());
        article.setIsTop(dto.getIsTop() != null ? dto.getIsTop() : 0);
        article.setIsPublish(dto.getIsPublish() != null ? dto.getIsPublish() : 0);
        article.setViewCount(0);
        article.setLikeCount(0);
        article.setCommentCount(0);
        article.setCreateTime(LocalDateTime.now());
        article.setUpdateTime(LocalDateTime.now());

        articleService.save(article);

        // 保存标签关联
        if (dto.getTagIds() != null && !dto.getTagIds().isEmpty()) {
            articleService.saveArticleTags(article.getId(), dto.getTagIds());
        }

        // 发布文章创建事件，异步同步至 Elasticsearch
        eventPublisher.publishEvent(new ArticlePublishedEvent(this, article.getId(), ArticlePublishedEvent.EventType.CREATED));

        return Result.success("创建成功", article.getId());
    }

    @PutMapping
    @Admin("更新文章")
    @Operation(summary = "更新文章")
    public Result<?> update(@RequestBody ArticleDTO dto) {
        Article article = articleService.getById(dto.getId());
        if (article == null) {
            return Result.error("文章不存在");
        }

        article.setCategoryId(dto.getCategoryId());
        article.setTitle(dto.getTitle());
        article.setSummary(dto.getSummary());
        article.setContent(dto.getContent());
        article.setCoverImage(dto.getCoverImage());
        article.setType(dto.getType());
        article.setSourceUrl(dto.getSourceUrl());
        article.setSourceName(dto.getSourceName());
        article.setIsTop(dto.getIsTop());
        article.setIsPublish(dto.getIsPublish());
        article.setUpdateTime(LocalDateTime.now());

        articleService.updateById(article);

        // 更新标签关联
        if (dto.getTagIds() != null) {
            articleService.updateArticleTags(article.getId(), dto.getTagIds());
        }

        // 发布文章更新事件，异步同步至 Elasticsearch
        eventPublisher.publishEvent(new ArticlePublishedEvent(this, article.getId(), ArticlePublishedEvent.EventType.UPDATED));

        return Result.success("更新成功", null);
    }

    @DeleteMapping("/{id}")
    @Admin("删除文章")
    @Operation(summary = "删除文章")
    public Result<?> delete(@PathVariable Long id) {
        articleService.removeById(id);

        // 发布文章删除事件，异步删除 Elasticsearch 中的对应文档
        eventPublisher.publishEvent(new ArticlePublishedEvent(this, id, ArticlePublishedEvent.EventType.DELETED));

        return Result.success("删除成功", null);
    }

    @PutMapping("/{id}/publish")
    @Admin("发布/取消发布文章")
    @Operation(summary = "发布/取消发布文章")
    public Result<?> togglePublish(@PathVariable Long id) {
        Article article = articleService.getById(id);
        if (article == null) {
            return Result.error("文章不存在");
        }
        article.setIsPublish(article.getIsPublish() == 1 ? 0 : 1);
        article.setUpdateTime(LocalDateTime.now());
        articleService.updateById(article);

        // 发布状态切换事件，异步同步至 Elasticsearch
        eventPublisher.publishEvent(new ArticlePublishedEvent(this, id, ArticlePublishedEvent.EventType.PUBLISHED));

        return Result.success("操作成功", null);
    }

    @PutMapping("/{id}/top")
    @Admin("置顶/取消置顶文章")
    @Operation(summary = "置顶/取消置顶文章")
    public Result<?> toggleTop(@PathVariable Long id) {
        Article article = articleService.getById(id);
        if (article == null) {
            return Result.error("文章不存在");
        }
        article.setIsTop(article.getIsTop() == 1 ? 0 : 1);
        article.setUpdateTime(LocalDateTime.now());
        articleService.updateById(article);
        return Result.success("操作成功", null);
    }

    @PostMapping("/import-markdown")
    @Admin("导入Markdown文件")
    @Operation(summary = "导入 Markdown 文件解析为文章字段")
    @RateLimit(key = "import-markdown", time = 60, count = 10)
    public Result<?> importMarkdown(@RequestParam("file") MultipartFile file) {
        try {
            Map<String, String> result = markdownImportService.importMarkdown(file);
            return Result.success("导入成功", result);
        } catch (BusinessException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            return Result.error("导入失败");
        }
    }

    @lombok.Data
    public static class ArticleDTO {
        private Long id;
        private Long categoryId;
        private String title;
        private String summary;
        private String content;
        private String coverImage;
        private Integer type;
        private String sourceUrl;
        private String sourceName;
        private Integer isTop;
        private Integer isPublish;
        private List<Long> tagIds;
    }
}