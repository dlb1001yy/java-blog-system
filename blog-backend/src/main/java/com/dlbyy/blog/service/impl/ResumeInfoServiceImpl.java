package com.dlbyy.blog.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dlbyy.blog.entity.ResumeInfo;
import com.dlbyy.blog.entity.User;
import com.dlbyy.blog.mapper.ResumeInfoMapper;
import com.dlbyy.blog.service.ResumeInfoService;
import com.dlbyy.blog.service.UserService;
import com.dlbyy.blog.utils.JsoupXssUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

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
        resumeInfo.setSummary(resumeInfo.getSummary());
        resumeInfo.setSelfEvaluation(resumeInfo.getSelfEvaluation());
        resumeInfo.setInterests(resumeInfo.getInterests());
        // 不信任客户端：保存/更新后重置为待审核
        resumeInfo.setStatus(0);
        resumeInfo.setAuditRemark(null);
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

    @Override
    public IPage<ResumeInfo> pageAll(int page, int size, String keyword, Integer status) {
        IPage<ResumeInfo> result = lambdaQuery()
                .isNotNull(ResumeInfo::getUserId)
                .eq(status != null, ResumeInfo::getStatus, status)
                .like(StringUtils.hasText(keyword), ResumeInfo::getName, keyword)
                .orderByDesc(ResumeInfo::getUpdateTime)
                .page(new Page<>(page, size));
        // 批量组装 userName
        List<Long> userIds = result.getRecords().stream()
                .map(ResumeInfo::getUserId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        if (!userIds.isEmpty()) {
            Map<Long, String> nameMap = userService.listByIds(userIds).stream()
                    .collect(Collectors.toMap(User::getId, User::getUsername, (a, b) -> a));
            result.getRecords().forEach(r -> r.setUserName(nameMap.get(r.getUserId())));
        }
        return result;
    }

    @Override
    public void audit(Long id, Integer status, String remark) {
        ResumeInfo update = new ResumeInfo();
        update.setId(id);
        update.setStatus(status);
        update.setAuditRemark(remark);
        updateById(update);
    }
}
