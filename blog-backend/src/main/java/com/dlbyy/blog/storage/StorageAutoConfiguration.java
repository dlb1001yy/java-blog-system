package com.dlbyy.blog.storage;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.dlbyy.blog.storage.impl.AliyunOssStorageServiceImpl;
import com.dlbyy.blog.storage.impl.LocalFileStorageServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 文件存储自动装配配置类
 * <p>
 * 根据 {@code storage.type} 动态装配对应的 {@link FileStorageService} 实现：
 * <ul>
 *     <li>{@code storage.type=local}（默认）— 装配 {@link LocalFileStorageServiceImpl}</li>
 *     <li>{@code storage.type=oss}     — 装配 {@link AliyunOssStorageServiceImpl} 及 OSS 客户端</li>
 * </ul>
 */
@Slf4j
@Configuration
@EnableConfigurationProperties(StorageProperties.class)
public class StorageAutoConfiguration {

    /**
     * 本地存储策略（默认）
     */
    @Bean
    @ConditionalOnMissingBean(FileStorageService.class)
    @ConditionalOnProperty(name = "storage.type", havingValue = "local", matchIfMissing = true)
    public FileStorageService localFileStorageService(StorageProperties properties) {
        log.info("启用本地文件存储策略，上传路径: {}", properties.getLocal().getUploadPath());
        return new LocalFileStorageServiceImpl(properties.getLocal());
    }

    /**
     * OSS 客户端（仅 storage.type=oss 时创建）
     */
    @Bean(destroyMethod = "shutdown")
    @ConditionalOnProperty(name = "storage.type", havingValue = "oss")
    @ConditionalOnMissingBean(OSS.class)
    public OSS ossClient(StorageProperties properties) {
        StorageProperties.OssConfig oss = properties.getOss();
        log.info("创建阿里云 OSS 客户端: endpoint={}, bucket={}", oss.getEndpoint(), oss.getBucketName());
        return new OSSClientBuilder().build(oss.getEndpoint(), oss.getAccessKeyId(), oss.getAccessKeySecret());
    }

    /**
     * OSS 存储策略
     */
    @Bean
    @ConditionalOnProperty(name = "storage.type", havingValue = "oss")
    public FileStorageService ossFileStorageService(StorageProperties properties, OSS ossClient) {
        log.info("启用阿里云 OSS 文件存储策略，bucket: {}", properties.getOss().getBucketName());
        return new AliyunOssStorageServiceImpl(properties.getOss(), ossClient);
    }
}
