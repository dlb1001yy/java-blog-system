package com.dlbyy.blog.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dlbyy.blog.entity.ResumeInfo;
import com.dlbyy.blog.mapper.ResumeInfoMapper;
import com.dlbyy.blog.service.ResumeInfoService;
import org.springframework.stereotype.Service;

@Service
public class ResumeInfoServiceImpl extends ServiceImpl<ResumeInfoMapper, ResumeInfo> implements ResumeInfoService {
}
