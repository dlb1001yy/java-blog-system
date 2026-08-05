package com.dlbyy.blog.controller.portal;

import com.dlbyy.blog.common.Result;
import com.dlbyy.blog.entity.ResumeInfo;
import com.dlbyy.blog.service.ResumeInfoService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/portal/resume")
@RequiredArgsConstructor
@Tag(name = "前台简历接口")
public class PortalResumeController {

    private final ResumeInfoService resumeInfoService;

    @GetMapping
    public Result<ResumeInfo> get() {
        return Result.success(resumeInfoService.getOne(null));
    }
}
