package com.dlbyy.blog.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
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
    private Integer maritalStatus;
    private Integer workYears;
    private String expectedSalary;
    private String highestEducation;
    private Integer jobSearchStatus;
    private String hukou;
    private LocalDate birthDate;
    private String phone;
    private String email;
    private String address;
    private String avatar;
    private String summary;
    private String selfEvaluation;
    private String skills;
    private String workExperience;
    private String education;
    private String projects;
    private String certificates;
    private String interests;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
