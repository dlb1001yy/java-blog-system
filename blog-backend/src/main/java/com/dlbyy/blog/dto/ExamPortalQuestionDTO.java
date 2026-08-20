package com.dlbyy.blog.dto;

import lombok.Data;

/**
 * 试卷题目 DTO（门户使用，隐藏正确答案与解析）
 */
@Data
public class ExamPortalQuestionDTO {

    private Long id;

    /** 题干 */
    private String stem;

    /** 题型 1:单选 2:多选 3:判断 4:填空 5:简答 6:编程 */
    private Integer type;

    private String category;

    private String difficulty;

    /** 选项 JSON 数组（客观题） */
    private String options;

    /** 本卷该题分值 */
    private java.math.BigDecimal score;

    /** 顺序 */
    private Integer sortOrder;

    /**
     * 门户接口不含 correct / reference_answer 字段（服务端置空，不返回）
     */
    public static ExamPortalQuestionDTO of(Long id, String stem, Integer type, String category,
                                           String difficulty, String options,
                                           java.math.BigDecimal score, Integer sortOrder) {
        ExamPortalQuestionDTO dto = new ExamPortalQuestionDTO();
        dto.setId(id);
        dto.setStem(stem);
        dto.setType(type);
        dto.setCategory(category);
        dto.setDifficulty(difficulty);
        dto.setOptions(options);
        dto.setScore(score);
        dto.setSortOrder(sortOrder);
        return dto;
    }

}
