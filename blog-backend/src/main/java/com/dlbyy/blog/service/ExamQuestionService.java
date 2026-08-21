package com.dlbyy.blog.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.dlbyy.blog.common.PageResult;
import com.dlbyy.blog.entity.ExamQuestion;

import java.util.List;
import java.util.Map;

/**
 * 考试题目服务
 */
public interface ExamQuestionService extends IService<ExamQuestion> {

    /**
     * 管理端分页查询（type/category/difficulty/keyword 筛选）
     */
    PageResult<ExamQuestion> adminPage(int page, int size, Integer type, String category,
                                       String difficulty, String keyword, Integer status);

    /**
     * 管理端保存（id 为空新增，否则更新）
     *
     * @return 题目ID
     */
    Long adminSave(ExamQuestion question);

    /**
     * 管理端删除（逻辑删除，被试卷引用的题目允许删除但解除引用由试卷侧维护）
     */
    void adminDelete(Long id);

    /**
     * 题型统计：6 种题型（1单选/2多选/3判断/4填空/5简答/6编程）的题目计数
     *
     * @return key=题型 type，value=数量
     */
    Map<Integer, Long> countByType();

    /**
     * Excel 批量导入题目（xlsx，先全量校验再批量插入）
     *
     * @return ImportResult（count=导入条数，errors=错误明细；有错误时整体不落库，count=0）
     */
    ImportResult importFromExcel(org.springframework.web.multipart.MultipartFile file);

    /**
     * Excel 导入结果
     */
    record ImportResult(int count, List<String> errors) {
    }
}
