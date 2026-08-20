package com.dlbyy.blog.storage;

import org.springframework.web.multipart.MultipartFile;

/**
 * 文件存储策略抽象接口
 * <p>
 * 通过 {@link com.dlbyy.blog.storage.StorageAutoConfiguration} 根据
 * {@code storage.type} 动态装配具体实现：
 * <ul>
 *     <li>{@code local} — {@link com.dlbyy.blog.storage.impl.LocalFileStorageServiceImpl}</li>
 *     <li>{@code minio} — {@link com.dlbyy.blog.storage.impl.MinioStorageServiceImpl}</li>
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

    /**
     * 保存字节数组（封面图生成、外链图片转存等无 MultipartFile 场景）
     *
     * @param data            文件内容
     * @param suffix          小写扩展名（如 "png"），不含点
     * @param contentType     MIME 类型，可为 null（实现按 suffix 推断）
     * @param originalFilename 原始文件名，可为 null
     * @return 保存结果
     * @throws com.dlbyy.blog.common.exception.BusinessException 内容为空或保存失败时抛出
     */
    FileUploadResult saveBytes(byte[] data, String suffix, String contentType, String originalFilename);
}
