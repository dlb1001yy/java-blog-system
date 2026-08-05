package com.dlbyy.blog.controller.portal;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dlbyy.blog.common.Result;
import com.dlbyy.blog.entity.Article;
import com.dlbyy.blog.service.ArticleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/portal/articles")
@RequiredArgsConstructor
@Tag(name = "前台文章接口")
public class PortalArticleController {

    private final ArticleService articleService;

    @GetMapping("/page")
    @Operation(summary = "分页查询文章列表")
    public Result<Page<Article>> page(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long tagId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer type) {
        
        Page<Article> page = new Page<>(current, size);
        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Article::getIsPublish, 1);
        
        if (categoryId != null) {
            wrapper.eq(Article::getCategoryId, categoryId);
        }
        if (type != null) {
            wrapper.eq(Article::getType, type);
        }
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like(Article::getTitle, keyword)
                          .or().like(Article::getSummary, keyword));
        }
        if (tagId != null) {
            wrapper.apply("id IN (SELECT article_id FROM blog_article_tag WHERE tag_id = {0})", tagId);
        }
        
        wrapper.orderByDesc(Article::getIsTop);
        wrapper.orderByDesc(Article::getCreateTime);
        
        Page<Article> result = articleService.page(page, wrapper);
        
        // 填充分类名和标签
        result.getRecords().forEach(articleService::fillArticleInfo);
        
        return Result.success(result);
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取文章详情")
    public Result<Article> detail(@PathVariable Long id) {
        Article article = articleService.getById(id);
        if (article == null || article.getIsPublish() == 0) {
            return Result.error(404, "文章不存在");
        }
        articleService.fillArticleInfo(article);
        articleService.incrementViewCount(id);
        return Result.success(article);
    }

    @GetMapping("/hot")
    @Operation(summary = "获取热门文章")
    public Result<?> hot() {
        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Article::getIsPublish, 1)
               .orderByDesc(Article::getViewCount)
               .last("LIMIT 5");
        return Result.success(articleService.list(wrapper));
    }

    @GetMapping("/latest")
    @Operation(summary = "获取最新文章")
    public Result<?> latest() {
        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Article::getIsPublish, 1)
               .orderByDesc(Article::getCreateTime)
               .last("LIMIT 5");
        return Result.success(articleService.list(wrapper));
    }

    @GetMapping("/archives")
    @Operation(summary = "文章归档")
    public Result<?> archives() {
        return Result.success(articleService.getArchives());
    }

    @PostMapping("/{id}/like")
    @Operation(summary = "点赞文章")
    public Result<?> like(@PathVariable Long id) {
        articleService.incrementLikeCount(id);
        return Result.success();
    }

    @GetMapping("/search")
    @Operation(summary = "搜索文章")
    public Result<Page<Article>> search(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size) {

        Page<Article> page = new Page<>(current, size);
        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Article::getIsPublish, 1)
               .and(w -> w.like(Article::getTitle, keyword)
                         .or().like(Article::getSummary, keyword))
               .orderByDesc(Article::getCreateTime);

        Page<Article> result = articleService.page(page, wrapper);
        // 填充分类名和标签
        result.getRecords().forEach(articleService::fillArticleInfo);

        return Result.success(result);
    }

    @GetMapping("/{id}/related")
    @Operation(summary = "获取相关文章")
    public Result<List<Article>> related(@PathVariable Long id) {
        Article article = articleService.getById(id);
        if (article == null) {
            return Result.error(404, "文章不存在");
        }

        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Article::getCategoryId, article.getCategoryId())
               .ne(Article::getId, id)
               .eq(Article::getIsPublish, 1)
               .orderByDesc(Article::getViewCount)
               .last("LIMIT 3");

        List<Article> list = articleService.list(wrapper);
        list.forEach(articleService::fillArticleInfo);

        return Result.success(list);
    }
}