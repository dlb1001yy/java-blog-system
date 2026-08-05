package com.dlbyy.blog.controller.portal;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dlbyy.blog.common.Result;
import com.dlbyy.blog.entity.Article;
import com.dlbyy.blog.service.ArticleService;
import com.dlbyy.blog.service.CategoryService;
import com.dlbyy.blog.service.TagService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/portal/stats")
@RequiredArgsConstructor
@Tag(name = "前台统计接口")
public class PortalStatsController {

    private final ArticleService articleService;
    private final CategoryService categoryService;
    private final TagService tagService;

    @GetMapping
    @Operation(summary = "获取站点统计信息")
    public Result<Map<String, Object>> stats() {
        // 已发布文章数
        long articleCount = articleService.count(
                new LambdaQueryWrapper<Article>().eq(Article::getIsPublish, 1));

        // 已发布文章浏览量总和
        List<Article> articles = articleService.list(
                new LambdaQueryWrapper<Article>().eq(Article::getIsPublish, 1));
        int viewCount = articles.stream()
                .mapToInt(a -> a.getViewCount() == null ? 0 : a.getViewCount())
                .sum();

        // 标签数
        long tagCount = tagService.count();
        // 分类数
        long categoryCount = categoryService.count();

        Map<String, Object> data = new HashMap<>();
        data.put("articleCount", articleCount);
        data.put("viewCount", viewCount);
        data.put("tagCount", tagCount);
        data.put("categoryCount", categoryCount);

        return Result.success(data);
    }
}
