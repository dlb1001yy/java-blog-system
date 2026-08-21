package com.dlbyy.blog.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dlbyy.blog.entity.ResumeInfo;
import com.dlbyy.blog.entity.User;
import com.dlbyy.blog.mapper.ResumeInfoMapper;
import com.dlbyy.blog.service.ResumeInfoService;
import com.dlbyy.blog.service.UserService;
import com.dlbyy.blog.utils.JsoupXssUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ResumeInfoServiceImpl extends ServiceImpl<ResumeInfoMapper, ResumeInfo> implements ResumeInfoService {

    private final UserService userService;

    @Override
    public ResumeInfo getByUserId(Long userId) {
        return lambdaQuery().eq(ResumeInfo::getUserId, userId).one();
    }

    @Override
    public void mySave(Long userId, ResumeInfo resumeInfo) {
        ResumeInfo existing = getByUserId(userId);
        LocalDateTime now = LocalDateTime.now();
        resumeInfo.setUserId(userId);
        // XSS 清洗：这些为纯文本段落字段，用 cleanText 保留纯文本；JSON 数组字段（skills/workExperience 等）跳过
        resumeInfo.setSummary(JsoupXssUtil.cleanText(resumeInfo.getSummary()));
        resumeInfo.setSelfEvaluation(JsoupXssUtil.cleanText(resumeInfo.getSelfEvaluation()));
        resumeInfo.setInterests(JsoupXssUtil.cleanText(resumeInfo.getInterests()));
        if (existing != null) {
            resumeInfo.setId(existing.getId());
            resumeInfo.setUpdateTime(now);
            updateById(resumeInfo);
        } else {
            resumeInfo.setId(null);
            resumeInfo.setCreateTime(now);
            resumeInfo.setUpdateTime(now);
            save(resumeInfo);
        }
    }

    @Override
    public User getUserByUsername(String username) {
        return userService.getByUsername(username);
    }
}
