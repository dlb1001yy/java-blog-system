package com.dlbyy.blog.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("resume_info")
public class ResumeInfo {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String jobTitle;
    private Integer gender;
    private LocalDate birthDate;
    private String phone;
    private String email;
    private String address;
    private String avatar;
    private String summary;
    private String skills;
    private String workExperience;
    private String education;
    private String projects;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
