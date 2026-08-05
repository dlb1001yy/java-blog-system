package com.dlbyy.blog.utils;



import com.dlbyy.blog.common.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Component
public class FileUtils {

    @Value("${file.upload-path}")
    private String uploadPath;

    @Value("${file.allowed-types}")
    private String allowedTypes;

    public String upload(MultipartFile file) {
        if (file.isEmpty()) {
            throw new BusinessException("上传文件不能为空");
        }

        String originalFilename = file.getOriginalFilename();
        String suffix = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            suffix = originalFilename.substring(originalFilename.lastIndexOf(".") + 1);
        }

        List<String> allowedTypeList = Arrays.asList(allowedTypes.split(","));
        if (!allowedTypeList.contains(suffix.toLowerCase())) {
            throw new BusinessException("不支持的文件类型: " + suffix);
        }

        File destFile = ensureDestFile(suffix);
        try {
            // 使用绝对路径进行写入
            file.transferTo(destFile.getAbsoluteFile());
            log.info("文件上传成功: {}", destFile.getAbsolutePath());
            return "/api/uploads/" + destFile.getName();
        } catch (IOException e) {
            log.error("文件上传失败", e);
            throw new BusinessException("文件上传失败，请稍后再试");
        }
    }

    /**
     * 生成 时间戳_随机数.suffix 文件名，确保 uploadPath 目录存在，返回目标 File（不创建文件）
     */
    private File ensureDestFile(String suffix) {
        String fileName = System.currentTimeMillis() + "_" + (int)(Math.random() * 10000) + "." + suffix;
        // 【关键修复】：将相对路径转换为绝对路径，避免被 Tomcat 解析到临时目录
        File destDir = new File(uploadPath).getAbsoluteFile();
        if (!destDir.exists()) {
            destDir.mkdirs();
        }
        return new File(destDir, fileName);
    }

    /**
     * 将字节数组保存到 uploadPath 目录，返回 /api/uploads/xxx.suffix 访问路径
     */
    public String saveBytes(byte[] data, String suffix) {
        File destFile = ensureDestFile(suffix);
        try {
            Files.write(destFile.toPath(), data);
            log.info("字节数据保存成功: {}", destFile.getAbsolutePath());
            return "/api/uploads/" + destFile.getName();
        } catch (IOException e) {
            log.error("字节数据保存失败", e);
            throw new BusinessException("文件保存失败，请稍后再试");
        }
    }

}
