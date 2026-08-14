package com.dlbyy.blog.controller.admin;

import com.dlbyy.blog.common.Result;
import com.dlbyy.blog.service.ConfigService;
import com.dlbyy.blog.storage.StorageProperties;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 后台系统配置管理接口
 * <p>网站配置与文件上传配置均以键值形式持久化到 sys_config 表。</p>
 */
@RestController
@RequestMapping("/admin/config")
public class AdminConfigController {

    private static final String KEY_SITE_NAME = "site.blogName";
    private static final String KEY_SITE_DESC = "site.blogDescription";
    private static final String KEY_SITE_DOMAIN = "site.blogDomain";

    private static final String KEY_UPLOAD_PATH = "upload.uploadPath";
    private static final String KEY_UPLOAD_TYPES = "upload.allowedTypes";
    private static final String KEY_UPLOAD_MAXSIZE = "upload.maxSize";

    private final ConfigService configService;
    private final StorageProperties storageProperties;

    @Autowired
    public AdminConfigController(ConfigService configService, StorageProperties storageProperties) {
        this.configService = configService;
        this.storageProperties = storageProperties;
    }

    @PutMapping("/site")
    public Result<Void> saveSite(@RequestBody SiteConfigDTO dto) {
        configService.setByKey(KEY_SITE_NAME, str(dto.getBlogName()), "网站名称");
        configService.setByKey(KEY_SITE_DESC, str(dto.getBlogDescription()), "网站描述");
        configService.setByKey(KEY_SITE_DOMAIN, str(dto.getBlogDomain()), "网站域名");
        return Result.success();
    }

    @GetMapping("/site")
    public Result<SiteConfigDTO> getSite() {
        SiteConfigDTO dto = new SiteConfigDTO();
        dto.setBlogName(configService.getByKey(KEY_SITE_NAME));
        dto.setBlogDescription(configService.getByKey(KEY_SITE_DESC));
        dto.setBlogDomain(configService.getByKey(KEY_SITE_DOMAIN));
        return Result.success(dto);
    }

    @PutMapping("/upload")
    public Result<Void> saveUpload(@RequestBody UploadConfigDTO dto) {
        configService.setByKey(KEY_UPLOAD_PATH, str(dto.getUploadPath()), "上传存储路径");
        configService.setByKey(KEY_UPLOAD_TYPES, str(dto.getAllowedTypes()), "允许上传的文件类型");

        long maxSizeBytes = MB_TO_BYTES * (dto.getMaxSize() == null ? 0 : dto.getMaxSize());
        configService.setByKey(KEY_UPLOAD_MAXSIZE, String.valueOf(maxSizeBytes), "上传文件最大大小(字节)");

        // 动态改写运行时存储参数，使上传限制立即生效（仅 local 类型生效）
        if (storageProperties.getLocal() != null) {
            StorageProperties.LocalConfig local = storageProperties.getLocal();
            if (dto.getUploadPath() != null) {
                local.setUploadPath(dto.getUploadPath());
            }
            if (dto.getAllowedTypes() != null) {
                local.setAllowedTypes(dto.getAllowedTypes());
            }
            local.setMaxSize(maxSizeBytes);
        }
        return Result.success();
    }

    @GetMapping("/upload")
    public Result<UploadConfigDTO> getUpload() {
        UploadConfigDTO dto = new UploadConfigDTO();
        dto.setUploadPath(configService.getByKey(KEY_UPLOAD_PATH));
        dto.setAllowedTypes(configService.getByKey(KEY_UPLOAD_TYPES));
        String maxSize = configService.getByKey(KEY_UPLOAD_MAXSIZE);
        if (maxSize != null && !maxSize.isEmpty()) {
            dto.setMaxSize((int) (Long.parseLong(maxSize) / MB_TO_BYTES));
        }
        return Result.success(dto);
    }

    private static String str(String value) {
        return value == null ? "" : value;
    }

    private static final long MB_TO_BYTES = 1024L * 1024L;

    @Data
    public static class SiteConfigDTO {
        private String blogName;
        private String blogDescription;
        private String blogDomain;
    }

    @Data
    public static class UploadConfigDTO {
        private String uploadPath;
        private String allowedTypes;
        private Integer maxSize;
    }
}
