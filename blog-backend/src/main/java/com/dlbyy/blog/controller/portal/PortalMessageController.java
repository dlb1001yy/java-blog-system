package com.dlbyy.blog.controller.portal;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dlbyy.blog.common.Result;
import com.dlbyy.blog.entity.Message;
import com.dlbyy.blog.service.MessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
    public Result<?> create(@RequestBody Message message) {
        message.setStatus(0); // 默认待审核
        messageService.save(message);
        return Result.success("留言成功，待审核", null);
    }
}