package com.dlbyy.blog.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dlbyy.blog.annotation.Admin;
import com.dlbyy.blog.common.Result;
import com.dlbyy.blog.entity.Message;
import com.dlbyy.blog.service.MessageService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/messages")
@RequiredArgsConstructor
@Tag(name = "后台留言管理")
public class AdminMessageController {

    private final MessageService messageService;

    @GetMapping("/page")
    public Result<Page<Message>> page(@RequestParam(defaultValue = "1") Integer current,
                                      @RequestParam(defaultValue = "10") Integer size,
                                      @RequestParam(required = false) Integer status) {
        Page<Message> page = new Page<>(current, size);
        LambdaQueryWrapper<Message> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(Message::getStatus, status);
        }
        wrapper.orderByDesc(Message::getCreateTime);
        return Result.success(messageService.page(page, wrapper));
    }

    @PutMapping("/{id}/audit")
    @Admin("审核留言")
    public Result<?> audit(@PathVariable Long id, @RequestParam Integer status) {
        Message message = new Message();
        message.setId(id);
        message.setStatus(status);
        messageService.updateById(message);
        return Result.success("审核成功", null);
    }

    @DeleteMapping("/{id}")
    @Admin("删除留言")
    public Result<?> delete(@PathVariable Long id) {
        messageService.removeById(id);
        return Result.success("删除成功", null);
    }
}