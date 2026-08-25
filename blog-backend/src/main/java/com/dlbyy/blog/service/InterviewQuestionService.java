package com.dlbyy.blog.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.dlbyy.blog.common.PageResult;
import com.dlbyy.blog.entity.Category;
import com.dlbyy.blog.entity.InterviewQuestion;

import java.util.List;

/**
 * 面试题服务
 */
public interface InterviewQuestionService extends IService<InterviewQuestion> {

    /**
     * 分页查询（支持 categoryId/difficulty/keyword 筛选，仅启用状态）
     */
    PageResult<InterviewQuestion> pageQuery(int page, int size, List<Long> categoryIds, String difficulty, String keyword);

    /**
     * 题目详情（含答案，填充 categoryName 与标签名称）
     */
    InterviewQuestion getDetail(Long id);

    /**
     * 管理端分页查询（含停用题目）
     */
    PageResult<InterviewQuestion> adminPage(int page, int size, Long categoryId, String difficulty, String keyword, Integer status);

    /**
     * 管理端保存（id 为空新增，否则更新）
     * <p>按 categoryId 校验分类存在，按 tagIds 校验标签存在，并维护 interview_question_tag 关联表（先删后插）。
     *
     * @return 题目ID
     */
    Long adminSave(InterviewQuestion question);

    /**
     * 管理端删除（逻辑删除，并同步删除标签关联记录）
     */
    void adminDelete(Long id);

    /**
     * 分类列表（blog_category 全量，带 id+name）
     */
    List<Category> listCategories();
}
