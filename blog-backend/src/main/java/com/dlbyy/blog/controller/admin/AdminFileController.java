package com.dlbyy.blog.controller.admin;


import com.dlbyy.blog.annotation.Admin;
import com.dlbyy.blog.common.Result;
import com.dlbyy.blog.utils.FileUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@Tag(name = "后台文件管理")
public class AdminFileController {
    private final FileUtils fileUtils;

    /**
     * 通用文件上传接口
     * 前端 el-upload 组件默认使用 "file" 作为参数名
     */
    @PostMapping("/upload")
    @Admin("上传文件")
    @Operation(summary = "文件上传")
    public Result<String> upload(@RequestParam("file") MultipartFile file) {
        try {
            // 调用 FileUtils 执行上传，返回可访问的文件 URL 路径
            String url = fileUtils.upload(file);
            return Result.success("上传成功", url);
        } catch (Exception e) {
            return Result.error("文件上传失败: " + e.getMessage());
        }
    }
}