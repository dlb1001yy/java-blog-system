package com.dlbyy.blog.storage;

import com.dlbyy.blog.common.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件存储接口
 * <p>
 * 实际访问路径为 {@code /api/v1/storage/upload}（context-path 为 /api）。
 */
@RestController
@RequestMapping("/v1/storage")
@RequiredArgsConstructor
@Tag(name = "文件存储")
public class StorageController {

    private final FileStorageService fileStorageService;

    @PostMapping("/upload")
    @Operation(summary = "上传文件")
    public Result<FileUploadResult> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "path", required = false) String path) {
        FileUploadResult result = fileStorageService.upload(file, path);
        return Result.success("上传成功", result);
    }
}
