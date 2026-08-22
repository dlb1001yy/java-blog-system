package com.dlbyy.blog.storage.impl;

import com.aliyun.oss.OSS;
import com.aliyun.oss.model.ObjectMetadata;
import com.dlbyy.blog.common.exception.BusinessException;
import com.dlbyy.blog.storage.FileStorageService;
import com.dlbyy.blog.storage.FileUploadResult;
import com.dlbyy.blog.storage.StorageProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * 阿里云 OSS 文件存储实现
 * <p>
 * 通过 {@code storage.type=oss} 启用，与 MinIO/本地存储通过配置切换。
 */
@Slf4j
@RequiredArgsConstructor
public class AliyunOssStorageServiceImpl implements FileStorageService {

    private static final DateTimeFormatter DATE_DIR = DateTimeFormatter.ofPattern("yyyy/MM/dd");

    private final StorageProperties.OssConfig config;
    private final OSS ossClient;

    @Override
    public FileUploadResult upload(MultipartFile file, String path) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("上传文件不能为空");
        }

        String originalFilename = file.getOriginalFilename();
        String suffix = extractSuffix(originalFilename);
        String dateSubDir = LocalDate.now().format(DATE_DIR);
        String subDir = (path != null && !path.isBlank()) ? path : dateSubDir;

        // 生成 OSS object key
        String objectKey = subDir + "/" + System.currentTimeMillis() + "_"
                + (int) (Math.random() * 10000) + "." + suffix;

        try (InputStream inputStream = file.getInputStream()) {
            ossClient.putObject(config.getBucketName(), objectKey, inputStream);
            log.info("OSS 文件上传成功: bucket={}, key={}", config.getBucketName(), objectKey);
        } catch (IOException e) {
            log.error("OSS 文件上传 IO 异常", e);
            throw new BusinessException("文件上传失败，请稍后再试");
        } catch (Exception e) {
            log.error("OSS 文件上传失败", e);
            throw new BusinessException("OSS 上传失败: " + e.getMessage());
        }

        String url = config.getUrlPrefix() + objectKey;

        return FileUploadResult.builder()
                .url(url)
                .originalFilename(originalFilename)
                .filename(objectKey)
                .size(file.getSize())
                .storageType("oss")
                .path(objectKey)
                .build();
    }

    @Override
    public FileUploadResult saveBytes(byte[] data, String suffix, String contentType, String originalFilename) {
        if (data == null || data.length == 0) {
            throw new BusinessException("保存内容不能为空");
        }
        String safeSuffix = (suffix == null || suffix.isBlank()) ? "" : suffix.toLowerCase();
        String dateSubDir = LocalDate.now().format(DATE_DIR);
        String objectKey = dateSubDir + "/" + System.currentTimeMillis() + "_"
                + (int) (Math.random() * 10000) + "." + safeSuffix;

        try {
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(data.length);
            metadata.setContentType(contentType != null ? contentType : "application/octet-stream");
            ossClient.putObject(config.getBucketName(), objectKey,
                    new ByteArrayInputStream(data), metadata);
            log.info("OSS 字节数据保存成功: bucket={}, key={}", config.getBucketName(), objectKey);
        } catch (Exception e) {
            log.error("OSS 字节数据保存失败", e);
            throw new BusinessException("OSS 上传失败: " + e.getMessage());
        }

        String url = config.getUrlPrefix() + objectKey;
        return FileUploadResult.builder()
                .url(url)
                .originalFilename(originalFilename)
                .filename(objectKey)
                .size(data.length)
                .storageType("oss")
                .path(objectKey)
                .build();
    }

    @Override
    public void delete(String url) {
        if (url == null || url.isBlank()) {
            return;
        }
        String urlPrefix = config.getUrlPrefix();
        if (urlPrefix == null || !url.startsWith(urlPrefix)) {
            // 非本存储的文件（外链等），跳过
            log.info("URL 不属于 OSS 存储，跳过删除: {}", url);
            return;
        }
        String objectKey = url.substring(urlPrefix.length());
        try {
            ossClient.deleteObject(config.getBucketName(), objectKey);
            log.info("OSS 文件删除成功: bucket={}, key={}", config.getBucketName(), objectKey);
        } catch (Exception e) {
            log.warn("OSS 文件删除失败（仅记录，不影响业务）: key={}, 原因: {}", objectKey, e.getMessage());
        }
    }

    private String extractSuffix(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf(".") + 1);
    }
}
