package com.dlbyy.blog.controller.portal;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dlbyy.blog.common.Result;
import com.dlbyy.blog.entity.Article;
import com.dlbyy.blog.es.ArticleDocument;
import com.dlbyy.blog.service.ArticleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.IndexCoordinates;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/portal/articles")
@RequiredArgsConstructor
@Tag(name = "前台文章接口")
public class PortalArticleController {

    private final ArticleService articleService;

    /** Elasticsearch 检索开关（blog.search.enabled），默认关闭 */
    @Value("${blog.search.enabled:false}")
    private boolean searchEnabled;

    /** ES 操作对象，仅当 blog.search.enabled=true 时注入，未启用时为 null */
    @Autowired(required = false)
    private ElasticsearchOperations elasticsearchOperations;

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

        // 启用 ES 全文检索时优先走 Elasticsearch（IK 中文分词，匹配标题/摘要/正文）
        if (searchEnabled && elasticsearchOperations != null) {
            try {
                return Result.success(searchFromEs(keyword, current, size));
            } catch (Exception e) {
                // ES 连接失败/索引不存在等，降级到数据库查询，避免单点故障影响搜索
                log.warn("Elasticsearch 搜索失败，已回退数据库查询 | keyword={}", keyword, e);
            }
        }

        // 回退：数据库模糊查询
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

    /**
     * 基于 Elasticsearch 全文检索（索引 blog_article，title/summary/content 使用 IK 分词）
     * 命中后按 ES 文档 id 反查 Article 实体，返回结构与数据库路径完全一致。
     */
    private Page<Article> searchFromEs(String keyword, Integer current, Integer size) {
        Criteria criteria = new Criteria("title").matches(keyword)
                .or(new Criteria("summary").matches(keyword))
                .or(new Criteria("content").matches(keyword));
        CriteriaQuery query = new CriteriaQuery(criteria);
        query.setPageable(PageRequest.of(Math.max(current - 1, 0), size));

        SearchHits<ArticleDocument> hits = elasticsearchOperations.search(
                query, ArticleDocument.class, IndexCoordinates.of("blog_article"));

        List<Long> ids = hits.getSearchHits().stream()
                .map(SearchHit::getContent)
                .map(ArticleDocument::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        Page<Article> result = new Page<>(current, size, hits.getTotalHits());
        if (ids.isEmpty()) {
            result.setRecords(Collections.emptyList());
            return result;
        }

        // 按 ES 命中 id 顺序反查 Article，避免 listByIds 破坏相关度排序
        List<Article> records = articleService.listByIds(ids);
        Map<Long, Article> recordMap = records.stream()
                .collect(Collectors.toMap(Article::getId, a -> a));
        List<Article> ordered = ids.stream()
                .map(recordMap::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        ordered.forEach(articleService::fillArticleInfo);
        result.setRecords(ordered);
        return result;
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