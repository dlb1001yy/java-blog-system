package com.dlbyy.blog.storage.impl;

import com.dlbyy.blog.common.exception.BusinessException;
import com.dlbyy.blog.storage.FileStorageService;
import com.dlbyy.blog.storage.FileUploadResult;
import com.dlbyy.blog.storage.StorageProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

/**
 * 本地文件存储实现
 */
@Slf4j
@RequiredArgsConstructor
public class LocalFileStorageServiceImpl implements FileStorageService {

    private static final DateTimeFormatter DATE_DIR = DateTimeFormatter.ofPattern("yyyy/MM/dd");

    private final StorageProperties.LocalConfig config;

    @Override
    public FileUploadResult upload(MultipartFile file, String path) {
        validate(file);

        String originalFilename = file.getOriginalFilename();
        String suffix = extractSuffix(originalFilename);
        String dateSubDir = LocalDate.now().format(DATE_DIR);
        String subDir = (path != null && !path.isBlank()) ? path : dateSubDir;

        // 生成唯一文件名
        String storedFilename = System.currentTimeMillis() + "_"
                + (int) (Math.random() * 10000) + "." + suffix;

        // 构建目标目录
        File destDir = new File(config.getUploadPath(), subDir).getAbsoluteFile();
        if (!destDir.exists() && !destDir.mkdirs()) {
            throw new BusinessException("创建存储目录失败: " + destDir.getAbsolutePath());
        }

        File destFile = new File(destDir, storedFilename);
        try {
            file.transferTo(destFile.getAbsoluteFile());
            log.info("本地文件上传成功: {}", destFile.getAbsolutePath());
        } catch (IOException e) {
            log.error("本地文件上传失败", e);
            throw new BusinessException("文件上传失败，请稍后再试");
        }

        // 拼接访问 URL
        String relativePath = subDir + "/" + storedFilename;
        String url = config.getUrlPrefix() + relativePath;

        return FileUploadResult.builder()
                .url(url)
                .originalFilename(originalFilename)
                .filename(storedFilename)
                .size(file.getSize())
                .storageType("local")
                .path(relativePath)
                .build();
    }

    private void validate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("上传文件不能为空");
        }
        if (file.getSize() > config.getMaxSize()) {
            throw new BusinessException("文件大小超过限制: " + config.getMaxSize() + " 字节");
        }
        String suffix = extractSuffix(file.getOriginalFilename());
        List<String> allowed = Arrays.asList(config.getAllowedTypes().split(","));
        if (!allowed.contains(suffix.toLowerCase())) {
            throw new BusinessException("不支持的文件类型: " + suffix);
        }
    }

    private String extractSuffix(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf(".") + 1);
    }
}
