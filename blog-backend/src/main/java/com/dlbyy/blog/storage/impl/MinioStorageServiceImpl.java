package com.dlbyy.blog.storage.impl;

import com.dlbyy.blog.common.exception.BusinessException;
import com.dlbyy.blog.storage.FileStorageService;
import com.dlbyy.blog.storage.FileUploadResult;
import com.dlbyy.blog.storage.StorageProperties;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.SetBucketPolicyArgs;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * MinIO 文件存储实现
 */
@Slf4j
@RequiredArgsConstructor
public class MinioStorageServiceImpl implements FileStorageService {

    private static final DateTimeFormatter DATE_DIR = DateTimeFormatter.ofPattern("yyyy/MM/dd");

    private final StorageProperties.MinioConfig config;
    private final MinioClient minioClient;

    /** bucket 就绪标记（已存在或已创建并设置匿名只读策略），避免每次上传重复检查 */
    private volatile boolean bucketReady = false;

    @Override
    public FileUploadResult upload(MultipartFile file, String path) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("上传文件不能为空");
        }

        String originalFilename = file.getOriginalFilename();
        String suffix = extractSuffix(originalFilename);
        String dateSubDir = LocalDate.now().format(DATE_DIR);
        String subDir = (path != null && !path.isBlank()) ? path : dateSubDir;

        // 生成 MinIO object key
        String objectKey = subDir + "/" + System.currentTimeMillis() + "_"
                + (int) (Math.random() * 10000) + "." + suffix;

        try {
            ensureBucketExists();
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(config.getBucketName())
                    .object(objectKey)
                    .stream(file.getInputStream(), file.getSize(), -1)
                    .contentType(file.getContentType())
                    .build());
            log.info("MinIO 文件上传成功: bucket={}, key={}", config.getBucketName(), objectKey);
        } catch (IOException e) {
            log.error("MinIO 文件上传 IO 异常", e);
            throw new BusinessException("文件上传失败，请稍后再试");
        } catch (Exception e) {
            log.error("MinIO 文件上传失败", e);
            throw new BusinessException("文件上传失败，请稍后再试: " + e.getMessage());
        }

        String url = config.getUrlPrefix() + objectKey;

        return FileUploadResult.builder()
                .url(url)
                .originalFilename(originalFilename)
                .filename(objectKey)
                .size(file.getSize())
                .storageType("minio")
                .path(objectKey)
                .build();
    }

    /**
     * 幂等确保 bucket 存在：不存在则创建，并设置匿名只读下载策略（仅新建时设置一次）；
     * 检查失败不标记就绪（异常直接抛出，不执行置位语句），下次上传会重新检查；
     * 双重检查锁：避免并发首次上传时多线程同时创建 bucket（makeBucket 会因已存在而抛错）
     */
    private void ensureBucketExists() throws Exception {
        if (bucketReady) {
            return;
        }
        synchronized (this) {
            if (bucketReady) {
                return;
            }
            String bucketName = config.getBucketName();
            boolean exists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
            if (!exists) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
                String policy = "{\"Version\":\"2012-10-17\",\"Statement\":[{\"Effect\":\"Allow\","
                        + "\"Principal\":{\"AWS\":[\"*\"]},\"Action\":[\"s3:GetObject\"],"
                        + "\"Resource\":[\"arn:aws:s3:::" + bucketName + "/*\"]}]}";
                minioClient.setBucketPolicy(SetBucketPolicyArgs.builder()
                        .bucket(bucketName)
                        .config(policy)
                        .build());
                log.info("MinIO bucket 不存在，已创建并设置匿名只读下载策略: {}", bucketName);
            }
            // 全部成功后才置位：任一步骤抛异常时不会执行到此，下次上传重新检查
            bucketReady = true;
        }
    }

    private String extractSuffix(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf(".") + 1);
    }
}
