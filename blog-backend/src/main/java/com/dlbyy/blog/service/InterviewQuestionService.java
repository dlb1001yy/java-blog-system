package com.dlbyy.blog.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.dlbyy.blog.common.PageResult;
import com.dlbyy.blog.entity.InterviewQuestion;

/**
 * 面试题服务
 */
public interface InterviewQuestionService extends IService<InterviewQuestion> {

    /**
     * 分页查询（支持 category/difficulty/keyword 筛选，仅启用状态）
     */
    PageResult<InterviewQuestion> pageQuery(int page, int size, String category, String difficulty, String keyword);

    /**
     * 题目详情（含答案）
     */
    InterviewQuestion getDetail(Long id);

    /**
     * 管理端分页查询（含停用题目）
     */
    PageResult<InterviewQuestion> adminPage(int page, int size, String category, String difficulty, String keyword, Integer status);

    /**
     * 管理端保存（id 为空新增，否则更新）
     *
     * @return 题目ID
     */
    Long adminSave(InterviewQuestion question);

    /**
     * 管理端删除（逻辑删除）
     */
    void adminDelete(Long id);

    /**
     * 启用题目去重方向列表（按名称排序）
     */
    java.util.List<String> listEnabledCategories();
}
