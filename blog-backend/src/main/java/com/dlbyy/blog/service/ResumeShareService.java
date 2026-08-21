package com.dlbyy.blog.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.dlbyy.blog.entity.ResumeInfo;
import com.dlbyy.blog.entity.ResumeShare;

import java.util.List;

public interface ResumeShareService extends IService<ResumeShare> {

    /** 创建分享链接（需简历已审核通过），expireMinutes 为 null 表示永久 */
    ResumeShare create(Long userId, Long expireMinutes);

    /** 我的分享列表 */
    List<ResumeShare> listMine(Long userId);

    /** 撤销分享（校验归属） */
    void revoke(Long id, Long userId);

    /** 通过 token 匿名查看简历（校验存在、未过期、简历已通过审核） */
    ResumeInfo viewByToken(String token);
}
