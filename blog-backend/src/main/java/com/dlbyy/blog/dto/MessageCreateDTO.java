package com.dlbyy.blog.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 前台留言创建 DTO（仅暴露白名单字段，status 等由服务端设置）
 */
@Data
public class MessageCreateDTO {

    @NotBlank(message = "昵称不能为空")
    @Size(max = 50, message = "昵称不能超过50个字符")
    private String nickname;

    @Email(message = "邮箱格式不正确")
    @Size(max = 100, message = "邮箱不能超过100个字符")
    private String email;

    @NotBlank(message = "留言内容不能为空")
    @Size(max = 1000, message = "留言内容不能超过1000个字符")
    private String content;
}
