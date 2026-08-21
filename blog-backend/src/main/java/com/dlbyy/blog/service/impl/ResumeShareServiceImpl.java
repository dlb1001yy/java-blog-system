package com.dlbyy.blog.service.impl;

import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dlbyy.blog.common.exception.BusinessException;
import com.dlbyy.blog.entity.ResumeInfo;
import com.dlbyy.blog.entity.ResumeShare;
import com.dlbyy.blog.mapper.ResumeShareMapper;
import com.dlbyy.blog.service.ResumeInfoService;
import com.dlbyy.blog.service.ResumeShareService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ResumeShareServiceImpl extends ServiceImpl<ResumeShareMapper, ResumeShare> implements ResumeShareService {

    private final ResumeInfoService resumeInfoService;

    @Override
    public ResumeShare create(Long userId, Long expireMinutes) {
        ResumeInfo resume = resumeInfoService.getByUserId(userId);
        if (resume == null || resume.getStatus() == null || resume.getStatus() != 1) {
            throw new BusinessException("简历未审核通过，无法分享");
        }
        ResumeShare share = new ResumeShare();
        share.setResumeId(resume.getId());
        share.setUserId(userId);
        share.setShareToken(RandomUtil.randomString(32));
        share.setExpireTime(expireMinutes == null ? null : LocalDateTime.now().plusMinutes(expireMinutes));
        save(share);
        return share;
    }

    @Override
    public List<ResumeShare> listMine(Long userId) {
        return lambdaQuery().eq(ResumeShare::getUserId, userId)
                .orderByDesc(ResumeShare::getCreateTime)
                .list();
    }

    @Override
    public void revoke(Long id, Long userId) {
        ResumeShare share = getById(id);
        if (share == null || !share.getUserId().equals(userId)) {
            throw new BusinessException(404, "分享链接不存在");
        }
        removeById(id);
    }

    @Override
    public ResumeInfo viewByToken(String token) {
        ResumeShare share = lambdaQuery().eq(ResumeShare::getShareToken, token).one();
        if (share == null
                || (share.getExpireTime() != null && share.getExpireTime().isBefore(LocalDateTime.now()))) {
            throw new BusinessException(404, "链接已失效或不存在");
        }
        ResumeInfo resume = resumeInfoService.getById(share.getResumeId());
        if (resume == null || resume.getStatus() == null || resume.getStatus() != 1) {
            throw new BusinessException(404, "链接已失效或不存在");
        }
        return resume;
    }
}
