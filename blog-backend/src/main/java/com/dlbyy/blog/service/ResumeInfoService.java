package com.dlbyy.blog.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
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

    /** 管理端分页：查所有用户简历（userId 非空），keyword 匹配姓名，status 过滤，附带 userName */
    IPage<ResumeInfo> pageAll(int page, int size, String keyword, Integer status);

    /** 审核：更新 status 与 auditRemark */
    void audit(Long id, Integer status, String remark);
}
