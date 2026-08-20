package com.dlbyy.blog.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dlbyy.blog.common.Result;
import com.dlbyy.blog.entity.*;
import com.dlbyy.blog.mapper.OperationLogMapper;
import com.dlbyy.blog.mapper.SchemaMapper;
import com.dlbyy.blog.service.*;
import com.dlbyy.blog.utils.RedisUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/admin/dashboard")
@RequiredArgsConstructor
public class AdminDashboardController {

    private final ArticleService articleService;
    private final CommentService commentService;
    private final MessageService messageService;
    private final CategoryService categoryService;
    private final ExamService examService;
    private final OperationLogMapper operationLogMapper;
    private final SchemaMapper schemaMapper;
    private final RedisUtils redisUtils;

    @GetMapping("/stats")
    public Result<?> stats() {
        Map<String, Object> stats = new HashMap<>();
        
        // 文章总数
        stats.put("articleCount", articleService.count());
        
        // 已发布文章数
        stats.put("publishedCount", articleService.count(
                new LambdaQueryWrapper<Article>().eq(Article::getIsPublish, 1)));
        
        // 评论总数
        stats.put("commentCount", commentService.count());
        
        // 待审核评论
        stats.put("pendingCommentCount", commentService.count(
                new LambdaQueryWrapper<Comment>().eq(Comment::getStatus, 0)));
        
        // 留言总数
        stats.put("messageCount", messageService.count());
        
        // 待审核留言
        stats.put("pendingMessageCount", messageService.count(
                new LambdaQueryWrapper<Message>().eq(Message::getStatus, 0)));
        
        // 总浏览量
        List<Article> articles = articleService.list();
        int totalViews = articles.stream().mapToInt(Article::getViewCount).sum();
        stats.put("totalViews", totalViews);
        
        // 总点赞数
        int totalLikes = articles.stream().mapToInt(Article::getLikeCount).sum();
        stats.put("totalLikes", totalLikes);
        
        // 今日新增文章
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        stats.put("todayArticleCount", articleService.count(
                new LambdaQueryWrapper<Article>().ge(Article::getCreateTime, todayStart)));
        
        return Result.success(stats);
    }

    @GetMapping("/article-trend")
    public Result<?> articleTrend() {
        // 最近7天文章发布趋势
        List<Map<String, Object>> trend = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            LocalDateTime start = date.atStartOfDay();
            LocalDateTime end = date.plusDays(1).atStartOfDay();
            
            long count = articleService.count(new LambdaQueryWrapper<Article>()
                    .ge(Article::getCreateTime, start)
                    .lt(Article::getCreateTime, end));
            
            Map<String, Object> item = new HashMap<>();
            item.put("date", date.toString());
            item.put("count", count);
            trend.add(item);
        }
        return Result.success(trend);
    }

    @GetMapping("/category-stats")
    public Result<?> categoryStats() {
        List<Category> categories = categoryService.list();
        List<Map<String, Object>> result = new ArrayList<>();
        
        for (Category category : categories) {
            long count = articleService.count(new LambdaQueryWrapper<Article>()
                    .eq(Article::getCategoryId, category.getId())
                    .eq(Article::getIsPublish, 1));
            
            Map<String, Object> item = new HashMap<>();
            item.put("name", category.getName());
            item.put("count", count);
            result.add(item);
        }
        
        return Result.success(result);
    }

    @GetMapping("/type-stats")
    public Result<?> typeStats() {
        Map<String, Object> result = new HashMap<>();
        
        result.put("original", articleService.count(new LambdaQueryWrapper<Article>()
                .eq(Article::getType, 0).eq(Article::getIsPublish, 1)));
        result.put("reproduced", articleService.count(new LambdaQueryWrapper<Article>()
                .eq(Article::getType, 1).eq(Article::getIsPublish, 1)));
        result.put("translated", articleService.count(new LambdaQueryWrapper<Article>()
                .eq(Article::getType, 2).eq(Article::getIsPublish, 1)));
        
        return Result.success(result);
    }

    @GetMapping("/todo")
    public Result<?> todo() {
        Map<String, Object> todo = new HashMap<>();
        // 待审核评论数
        todo.put("pendingCommentCount", commentService.count(
                new LambdaQueryWrapper<Comment>().eq(Comment::getStatus, 0)));
        // 待审核留言数
        todo.put("pendingMessageCount", messageService.count(
                new LambdaQueryWrapper<Message>().eq(Message::getStatus, 0)));
        // 待阅卷数
        todo.put("pendingMarkingCount", examService.countPendingMarking());
        return Result.success(todo);
    }

    @GetMapping("/activities")
    public Result<Page<OperationLog>> activities(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size) {
        return Result.success(operationLogMapper.selectPage(new Page<>(current, size),
                new LambdaQueryWrapper<OperationLog>().orderByDesc(OperationLog::getCreateTime)));
    }

    @GetMapping("/system-status")
    public Result<?> systemStatus() {
        Map<String, Object> status = new HashMap<>();

        // Redis 状态：尝试一次读写探测
        Map<String, Object> redis = new HashMap<>();
        try {
            String pingKey = "dashboard:ping:" + System.currentTimeMillis();
            redisUtils.set(pingKey, "1", 3000);
            Object value = redisUtils.get(pingKey);
            redisUtils.delete(pingKey);
            redis.put("status", "1".equals(value) ? "up" : "unknown");
        } catch (Exception e) {
            redis.put("status", "unknown");
        }
        status.put("redis", redis);

        // 数据库状态：SELECT 1 探测
        Map<String, Object> database = new HashMap<>();
        try {
            database.put("status", schemaMapper.ping() == 1 ? "up" : "unknown");
        } catch (Exception e) {
            database.put("status", "unknown");
        }
        status.put("database", database);

        // 磁盘状态
        Map<String, Object> disk = new HashMap<>();
        try {
            File root = new File(".").getAbsoluteFile();
            disk.put("usableSpace", root.getUsableSpace());
            disk.put("totalSpace", root.getTotalSpace());
            disk.put("status", "up");
        } catch (Exception e) {
            disk.put("status", "unknown");
        }
        status.put("disk", disk);

        // JVM 状态
        Map<String, Object> jvm = new HashMap<>();
        Runtime runtime = Runtime.getRuntime();
        jvm.put("maxMemory", runtime.maxMemory());
        jvm.put("totalMemory", runtime.totalMemory());
        jvm.put("freeMemory", runtime.freeMemory());
        status.put("jvm", jvm);

        return Result.success(status);
    }
}