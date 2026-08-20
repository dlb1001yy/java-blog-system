package com.dlbyy.blog.dto;

import lombok.Data;

import java.util.List;

/**
 * 交卷请求
 */
@Data
public class ExamSubmitDTO {

    private Long paperId;

    /** 考生答案列表 */
    private List<ExamAnswerDTO> answers;

    /** 切屏次数 */
    private Integer switchCount;

    /** 实际用时（秒） */
    private Integer durationSeconds;
}
