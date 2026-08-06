package com.dlbyy.blog.storage;

import org.springframework.web.multipart.MultipartFile;

/**
 * 文件存储策略抽象接口
 * <p>
 * 通过 {@link com.dlbyy.blog.storage.StorageAutoConfiguration} 根据
 * {@code storage.type} 动态装配具体实现：
 * <ul>
 *     <li>{@code local} — {@link com.dlbyy.blog.storage.impl.LocalFileStorageServiceImpl}</li>
 *     <li>{@code oss}   — {@link com.dlbyy.blog.storage.impl.AliyunOssStorageServiceImpl}</li>
 * </ul>
 */
public interface FileStorageService {

    /**
     * 上传文件
     *
     * @param file 待上传的 MultipartFile
     * @param path 存储子路径（如 "images/article"），可为 null 或空
     * @return 上传结果
     * @throws com.dlbyy.blog.common.exception.BusinessException 文件为空或上传失败时抛出
     */
    FileUploadResult upload(MultipartFile file, String path);
}
