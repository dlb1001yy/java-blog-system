package com.dlbyy.blog.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.dlbyy.blog.entity.ResumeInfo;
import com.dlbyy.blog.entity.User;

public interface ResumeInfoService extends IService<ResumeInfo> {

    /** 按用户查询简历 */
    ResumeInfo getByUserId(Long userId);

    /** 保存（upsert）指定用户的简历，userId 强制取当前登录人 */
    void mySave(Long userId, ResumeInfo resumeInfo);

    /** 按用户名查用户（供 Portal 控制器取当前登录人） */
    User getUserByUsername(String username);
}
