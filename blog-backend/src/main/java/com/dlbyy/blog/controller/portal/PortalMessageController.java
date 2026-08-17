package com.dlbyy.blog.controller.portal;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dlbyy.blog.annotation.RateLimit;
import com.dlbyy.blog.common.Result;
import com.dlbyy.blog.dto.MessageCreateDTO;
import com.dlbyy.blog.entity.Message;
import com.dlbyy.blog.service.MessageService;
import com.dlbyy.blog.utils.JsoupXssUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/portal/messages")
@RequiredArgsConstructor
@Tag(name = "前台留言接口")
public class PortalMessageController {

    private final MessageService messageService;

    @GetMapping
    @Operation(summary = "获取已审核留言列表")
    public Result<List<Message>> list() {
        LambdaQueryWrapper<Message> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Message::getStatus, 1)
               .orderByDesc(Message::getCreateTime);
        return Result.success(messageService.list(wrapper));
    }

    @PostMapping
    @RateLimit(key = "portal-message", time = 60, count = 5)
    public Result<?> create(@Valid @RequestBody MessageCreateDTO dto) {
        Message message = new Message();
        message.setNickname(JsoupXssUtil.cleanText(dto.getNickname()));
        message.setEmail(dto.getEmail());
        message.setContent(JsoupXssUtil.cleanHtml(dto.getContent()));
        message.setStatus(0); // 默认待审核，服务端设置，不信任客户端
        messageService.save(message);
        return Result.success("留言成功，待审核", null);
    }
}
