package com.dlbyy.blog.storage;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 文件存储配置属性
 * <p>
 * 绑定 application.yaml 中 {@code storage.*} 配置：
 *
 * <pre>
 * storage:
 *   type: local          # local | minio | oss
 *   local:
 *     upload-path: /data/uploads
 *     url-prefix: /uploads/
 *     allowed-types: jpg,jpeg,png,gif,webp
 *     max-size: 10485760
 *   minio:
 *     endpoint: http://localhost:9000
 *     access-key: minioadmin
 *     secret-key: minioadmin
 *     bucket-name: blog
 *     url-prefix: http://localhost:9000/blog/
 *   oss:
 *     endpoint: oss-cn-hangzhou.aliyuncs.com
 *     access-key-id: xxx
 *     access-key-secret: xxx
 *     bucket-name: my-bucket
 *     url-prefix: https://my-bucket.oss-cn-hangzhou.aliyuncs.com/
 * </pre>
 */
@Data
@ConfigurationProperties(prefix = "storage")
public class StorageProperties {

    /** 存储类型：local | minio | oss */
    private String type = "local";

    private LocalConfig local = new LocalConfig();

    private MinioConfig minio = new MinioConfig();

    private OssConfig oss = new OssConfig();

    @Data
    public static class LocalConfig {
        /** 本地存储根路径 */
        private String uploadPath = "./uploads/";

        /** URL 访问前缀 */
        private String urlPrefix = "/uploads/";

        /** 允许的文件类型（逗号分隔） */
        private String allowedTypes = "jpg,jpeg,png,gif,webp,md,markdown";

        /** 最大文件大小（字节） */
        private long maxSize = 10L * 1024 * 1024;
    }

    @Data
    public static class MinioConfig {
        /** MinIO 服务地址（如 http://localhost:9000） */
        private String endpoint;

        /** AccessKey（访问密钥） */
        private String accessKey;

        /** SecretKey（私有密钥） */
        private String secretKey;

        /** Bucket 名称 */
        private String bucketName;

        /** URL 访问前缀 */
        private String urlPrefix;
    }

    @Data
    public static class OssConfig {
        /** OSS Endpoint（如 oss-cn-hangzhou.aliyuncs.com） */
        private String endpoint;

        /** AccessKey ID */
        private String accessKeyId;

        /** AccessKey Secret */
        private String accessKeySecret;

        /** Bucket 名称 */
        private String bucketName;

        /** URL 访问前缀 */
        private String urlPrefix;
    }
}
