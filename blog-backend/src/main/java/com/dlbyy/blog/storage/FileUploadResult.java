package com.dlbyy.blog.storage;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 文件上传结果
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileUploadResult implements Serializable {

    /** 访问 URL */
    private String url;

    /** 原始文件名 */
    private String originalFilename;

    /** 存储文件名（含路径） */
    private String filename;

    /** 文件大小（字节） */
    private long size;

    /** 存储类型：local / oss */
    private String storageType;

    /** 存储路径（相对路径或 OSS object key） */
    private String path;
}
