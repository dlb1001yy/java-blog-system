package com.dlbyy.blog.service;

import com.dlbyy.blog.common.exception.BusinessException;
import com.dlbyy.blog.utils.CoverImageGenerator;
import com.dlbyy.blog.utils.FileUtils;
import cn.hutool.http.HttpUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
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
    private static final int SUMMARY_MAX_LENGTH = 200;

    private final FileUtils fileUtils;
    private final CoverImageGenerator coverImageGenerator;

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

        Map<String, String> result = new LinkedHashMap<>();
        result.put("title", title);
        result.put("summary", summary);
        result.put("content", content);
        result.put("coverImage", coverImage);
        return result;
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
                return fileUtils.saveBytes(bytes, suffix);
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
