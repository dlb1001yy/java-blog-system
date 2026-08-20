package com.dlbyy.blog.utils;


import com.dlbyy.blog.common.exception.BusinessException;
import com.dlbyy.blog.storage.FileStorageService;
import com.dlbyy.blog.storage.FileUploadResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * 上传校验工具：负责类型/大小/真实格式校验与图片重编码，
 * 落盘统一委托 {@link FileStorageService}（随 storage.type 切换 local/minio/oss）
 */
@Slf4j
@Component
public class FileUtils {

    private final FileStorageService fileStorageService;

    @Value("${file.allowed-types}")
    private String allowedTypes;

    @Value("${file.max-size:5242880}")
    private long maxSize;

    public FileUtils(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
    }

    /** 图片扩展名（可被 ImageIO 重编码消除恶意载荷） */
    private static final Set<String> IMAGE_SUFFIXES = Set.of("jpg", "jpeg", "png", "gif");

    /** 扩展名 -> 期望的 Content-Type 前缀/集合 */
    private static final Map<String, String> IMAGE_CONTENT_TYPES = Map.of(
            "jpg", "image/jpeg",
            "jpeg", "image/jpeg",
            "png", "image/png",
            "gif", "image/gif",
            "webp", "image/webp"
    );

    public String upload(MultipartFile file) {
        if (file.isEmpty()) {
            throw new BusinessException("上传文件不能为空");
        }

        String originalFilename = file.getOriginalFilename();
        String suffix = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            suffix = originalFilename.substring(originalFilename.lastIndexOf(".") + 1);
        }
        suffix = suffix.toLowerCase(Locale.ROOT);

        List<String> allowedTypeList = Arrays.asList(allowedTypes.split(","));
        if (!allowedTypeList.contains(suffix)) {
            throw new BusinessException("不支持的文件类型: " + suffix);
        }

        // 1. 文件大小上限校验
        if (file.getSize() > maxSize) {
            throw new BusinessException("上传文件过大，最大允许 " + (maxSize / 1024 / 1024) + "MB");
        }

        byte[] data;
        try {
            data = file.getBytes();
        } catch (IOException e) {
            log.error("读取上传文件失败", e);
            throw new BusinessException("文件上传失败，请稍后再试");
        }

        boolean isImage = IMAGE_CONTENT_TYPES.containsKey(suffix);
        if (isImage) {
            // 2. 真实类型校验：magic bytes + Content-Type 交叉校验
            String detected = detectImageType(data);
            if (detected == null) {
                throw new BusinessException("文件内容不是有效的图片，禁止上传伪装为图片的文件");
            }
            String normalizedSuffix = "jpg".equals(suffix) ? "jpeg" : suffix;
            if (!detected.equals(normalizedSuffix)) {
                throw new BusinessException("文件真实类型(" + detected + ")与扩展名(" + suffix + ")不一致，禁止上传");
            }
            String contentType = file.getContentType();
            String expected = IMAGE_CONTENT_TYPES.get(suffix);
            if (contentType == null || !contentType.toLowerCase(Locale.ROOT).startsWith(expected)) {
                throw new BusinessException("文件Content-Type与文件类型不一致，禁止上传");
            }
            // 3. 图片重编码：消除嵌入的恶意载荷（webp 无原生 ImageIO 编码器，跳过重编码）
            if (IMAGE_SUFFIXES.contains(suffix)) {
                data = reencodeImage(data, normalizedSuffix);
            }
        }

        // 4. 落盘：委托存储策略（storage.type 切换 local/minio/oss）
        FileUploadResult result = fileStorageService.saveBytes(data, suffix, file.getContentType(), originalFilename);
        log.info("文件上传成功: storageType={}, path={}", result.getStorageType(), result.getPath());
        return result.getUrl();
    }

    /**
     * 基于文件头 magic bytes 判断图片真实类型
     * @return jpeg/png/gif/webp，非白名单图片返回 null
     */
    private String detectImageType(byte[] data) {
        if (data.length < 12) {
            return null;
        }
        // JPEG: FF D8 FF
        if ((data[0] & 0xFF) == 0xFF && (data[1] & 0xFF) == 0xD8 && (data[2] & 0xFF) == 0xFF) {
            return "jpeg";
        }
        // PNG: 89 50 4E 47 0D 0A 1A 0A
        if ((data[0] & 0xFF) == 0x89 && data[1] == 'P' && data[2] == 'N' && data[3] == 'G'
                && data[4] == 0x0D && data[5] == 0x0A && data[6] == 0x1A && data[7] == 0x0A) {
            return "png";
        }
        // GIF: GIF87a / GIF89a
        if (data[0] == 'G' && data[1] == 'I' && data[2] == 'F' && data[3] == '8'
                && (data[4] == '7' || data[4] == '9') && data[5] == 'a') {
            return "gif";
        }
        // WebP: RIFF....WEBP
        if (data[0] == 'R' && data[1] == 'I' && data[2] == 'F' && data[3] == 'F'
                && data[8] == 'W' && data[9] == 'E' && data[10] == 'B' && data[11] == 'P') {
            return "webp";
        }
        return null;
    }

    /**
     * 用 ImageIO 读入再写出，重编码图片以消除嵌入的恶意载荷（如图片马中的 PHP/脚本代码）
     */
    private byte[] reencodeImage(byte[] data, String format) {
        try {
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(data));
            if (image == null) {
                throw new BusinessException("文件内容不是有效的图片，禁止上传伪装为图片的文件");
            }
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            if (!ImageIO.write(image, format, out) || out.size() == 0) {
                throw new BusinessException("图片重编码失败，禁止上传");
            }
            return out.toByteArray();
        } catch (BusinessException e) {
            throw e;
        } catch (IOException e) {
            log.error("图片重编码失败", e);
            throw new BusinessException("图片处理失败，请稍后再试");
        }
    }

}
