package com.dlbyy.blog.service;

import com.dlbyy.blog.common.exception.BusinessException;
import com.dlbyy.blog.entity.Category;
import com.dlbyy.blog.entity.Tag;
import com.dlbyy.blog.service.CategoryService;
import com.dlbyy.blog.service.TagService;
import com.dlbyy.blog.storage.FileStorageService;
import com.dlbyy.blog.utils.CoverImageGenerator;
import cn.hutool.http.HttpUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Markdown 文件导入服务：解析 .md 文件为文章字段
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MarkdownImportService {

    private static final Pattern H1_PATTERN = Pattern.compile("^#\\s+(.+)$", Pattern.MULTILINE);
    private static final Pattern IMAGE_PATTERN = Pattern.compile("!\\[.*?\\]\\(([^)]+)\\)");
    private static final Pattern FRONT_MATTER_PATTERN = Pattern.compile("\\A---\\s*\\r?\\n(.*?)\\r?\\n---\\s*\\r?\\n", Pattern.DOTALL);
    private static final Pattern FM_CATEGORY_PATTERN = Pattern.compile("^(?:category|categories)\\s*:\\s*(.+)$", Pattern.MULTILINE | Pattern.CASE_INSENSITIVE);
    private static final Pattern FM_TAGS_PATTERN = Pattern.compile("^tags\\s*:\\s*(.+)$", Pattern.MULTILINE | Pattern.CASE_INSENSITIVE);
    private static final int SUMMARY_MAX_LENGTH = 200;

    private final FileStorageService fileStorageService;
    private final CoverImageGenerator coverImageGenerator;
    private final CategoryService categoryService;
    private final TagService tagService;

    /**
     * 导入 Markdown 文件，解析为文章字段
     */
    public Map<String, String> importMarkdown(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null
                || (!originalFilename.toLowerCase().endsWith(".md")
                    && !originalFilename.toLowerCase().endsWith(".markdown"))) {
            throw new BusinessException("仅支持 .md / .markdown 文件");
        }

        String content;
        try {
            content = new String(file.getBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.error("读取 Markdown 文件失败", e);
            throw new BusinessException("读取 Markdown 文件失败");
        }

        String title = extractTitle(content, originalFilename);
        String summary = extractSummary(content);
        String imageUrl = extractFirstImage(content);
        String coverImage = resolveCoverImage(imageUrl, title);

        // front-matter（--- 包裹的 YAML 头）中的分类/标签解析为 id，无 front-matter 时不返回
        Map<String, String> frontMatter = extractFrontMatter(content);
        Map<String, String> result = new LinkedHashMap<>();
        result.put("title", title);
        result.put("summary", summary);
        result.put("content", content);
        result.put("coverImage", coverImage);
        applyFrontMatterCategory(frontMatter, result);
        applyFrontMatterTags(frontMatter, result);
        return result;
    }

    /**
     * 提取文件头部的 front-matter（--- 包裹的 YAML 头）键值，无则返回空 Map
     */
    private Map<String, String> extractFrontMatter(String content) {
        Map<String, String> frontMatter = new LinkedHashMap<>();
        Matcher matcher = FRONT_MATTER_PATTERN.matcher(content);
        if (!matcher.find()) {
            return frontMatter;
        }
        Matcher categoryMatcher = FM_CATEGORY_PATTERN.matcher(matcher.group(1));
        if (categoryMatcher.find()) {
            frontMatter.put("category", categoryMatcher.group(1).trim());
        }
        Matcher tagsMatcher = FM_TAGS_PATTERN.matcher(matcher.group(1));
        if (tagsMatcher.find()) {
            frontMatter.put("tags", tagsMatcher.group(1).trim());
        }
        return frontMatter;
    }

    /**
     * front-matter 分类（category/categories，取首个，支持 [a] 数组写法）：getOrCreateByName 后放入 categoryId
     */
    private void applyFrontMatterCategory(Map<String, String> frontMatter, Map<String, String> result) {
        String raw = frontMatter.get("category");
        if (raw == null) {
            return;
        }
        String name = firstListItem(raw);
        Category category = categoryService.getOrCreateByName(name);
        if (category != null) {
            result.put("categoryId", String.valueOf(category.getId()));
        }
    }

    /**
     * front-matter 标签（tags，支持 [a, b] 或逗号分隔）：逐个 getOrCreateByName，tagIds 放入名称列表
     */
    private void applyFrontMatterTags(Map<String, String> frontMatter, Map<String, String> result) {
        String raw = frontMatter.get("tags");
        if (raw == null) {
            return;
        }
        List<String> tagNames = splitList(raw);
        if (tagNames.isEmpty()) {
            return;
        }
        // 预创建标签，确保 getOrCreateByName 已落库；tagIds 返回名称，
        // 由前端回传后经 saveArticleWithTags 解析为 id 并写 blog_article_tag 关联
        for (String name : tagNames) {
            tagService.getOrCreateByName(name);
        }
        result.put("tagIds", String.join(",", tagNames));
    }

    /**
     * 解析 [a, b] 或 a, b 形式，取首个非空项
     */
    private String firstListItem(String raw) {
        List<String> items = splitList(raw);
        return items.isEmpty() ? raw.trim() : items.get(0);
    }

    /**
     * 解析 [a, b] 或 a, b 形式为列表，去引号、去空项
     */
    private List<String> splitList(String raw) {
        String body = raw.trim();
        if (body.startsWith("[") && body.endsWith("]")) {
            body = body.substring(1, body.length() - 1);
        }
        List<String> items = new ArrayList<>();
        for (String item : body.split(",")) {
            String name = item.trim().replaceAll("^[\"']|[\"']$", "");
            if (!name.isEmpty()) {
                items.add(name);
            }
        }
        return items;
    }

    /**
     * 提取标题：首个 H1，无则用文件名（去扩展名）
     */
    private String extractTitle(String content, String fileName) {
        Matcher matcher = H1_PATTERN.matcher(content);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        if (fileName.contains(".")) {
            return fileName.substring(0, fileName.lastIndexOf("."));
        }
        return fileName;
    }

    /**
     * 提取摘要：跳过标题行、代码块、图片行、空行，取第一段纯文本
     */
    private String extractSummary(String content) {
        String[] lines = content.split("\\r?\\n");
        boolean inCodeBlock = false;
        for (String rawLine : lines) {
            String line = rawLine.trim();
            // 代码块开关
            if (line.startsWith("```")) {
                inCodeBlock = !inCodeBlock;
                continue;
            }
            if (inCodeBlock) {
                continue;
            }
            // 跳过标题行
            if (line.startsWith("#")) {
                continue;
            }
            // 跳过图片行
            if (IMAGE_PATTERN.matcher(line).find()) {
                continue;
            }
            // 跳过空行
            if (line.isEmpty()) {
                continue;
            }
            // 去除 markdown 标记
            String text = line
                    .replaceAll("\\*\\*", "")
                    .replaceAll("\\*", "")
                    .replaceAll("`", "")
                    .replaceAll("^>+\\s*", "")
                    .trim();
            if (text.isEmpty()) {
                continue;
            }
            if (text.length() > SUMMARY_MAX_LENGTH) {
                return text.substring(0, SUMMARY_MAX_LENGTH) + "…";
            }
            return text;
        }
        return "";
    }

    /**
     * 提取首图 URL，无则返回 null
     */
    private String extractFirstImage(String content) {
        Matcher matcher = IMAGE_PATTERN.matcher(content);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    /**
     * 解析封面图：外链下载失败或相对路径则生成封面图
     */
    private String resolveCoverImage(String imageUrl, String title) {
        if (imageUrl == null || imageUrl.isBlank()) {
            return coverImageGenerator.generate(title);
        }
        if (imageUrl.startsWith("http://") || imageUrl.startsWith("https://")) {
            try {
                byte[] bytes = HttpUtil.downloadBytes(imageUrl);
                String suffix = extractSuffixFromUrl(imageUrl);
                // 转存走存储策略（storage.type 切换 local/minio/oss）
                return fileStorageService.saveBytes(bytes, suffix, null, null).getUrl();
            } catch (Exception e) {
                log.warn("下载外链图片失败: {}, 改为生成封面图", imageUrl, e);
                return coverImageGenerator.generate(title);
            }
        }
        log.warn("首图为相对路径, 跳过下载: {}", imageUrl);
        return coverImageGenerator.generate(title);
    }

    /**
     * 从 URL 提取图片后缀，默认 png
     */
    private String extractSuffixFromUrl(String url) {
        String noQuery = url.split("[?]")[0];
        if (noQuery.contains(".")) {
            String suffix = noQuery.substring(noQuery.lastIndexOf(".") + 1);
            if (suffix.matches("[a-zA-Z0-9]{2,5}")) {
                return suffix.toLowerCase();
            }
        }
        return "png";
    }
}
