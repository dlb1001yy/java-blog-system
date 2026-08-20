package com.dlbyy.blog.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.dlbyy.blog.common.PageResult;
import com.dlbyy.blog.dto.ExamPortalQuestionDTO;
import com.dlbyy.blog.entity.ExamPaper;

import java.util.List;

/**
 * 试卷服务
 */
public interface ExamPaperService extends IService<ExamPaper> {

    /**
     * 管理端分页查询
     */
    PageResult<ExamPaper> adminPage(int page, int size, String keyword, Integer status);

    /**
     * 管理端试卷详情（含题目ID有序列表）
     */
    ExamPaper adminDetail(Long paperId);

    /**
     * 管理端保存试卷基本信息（id 为空新增）
     *
     * @return 试卷ID
     */
    Long adminSave(ExamPaper paper);

    /**
     * 组卷：保存题目ID有序列表到 exam_paper_question（全量替换），并同步更新题目 usage_count
     */
    void compose(Long paperId, List<Long> questionIds);

    /**
     * 管理端删除试卷（同时删除试卷-题目关联）
     */
    void adminDelete(Long paperId);

    /**
     * 发布（status=1）/ 停用（status=2）
     *
     * @param publish true 发布；false 停用
     */
    void publish(Long paperId, boolean publish);

    /**
     * 门户：已发布试卷分页列表（含题目数）
     */
    PageResult<ExamPaper> portalPage(int page, int size, String keyword);

    /**
     * 门户：试卷详情（题目列表不含 correct / reference_answer）
     */
    List<ExamPortalQuestionDTO> portalDetail(Long paperId);
}
