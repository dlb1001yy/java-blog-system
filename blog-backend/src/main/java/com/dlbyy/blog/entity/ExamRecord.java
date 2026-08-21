package com.dlbyy.blog.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 考试答卷记录实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("exam_record")
public class ExamRecord extends BaseEntity {

    /** 试卷ID */
    private Long paperId;

    /** 考生用户ID */
    private Long userId;

    /** 考生答案 JSON: [{questionId, answer, marked}] */
    private String answers;

    /** 客观题得分（自动判分） */
    private BigDecimal objectiveScore;

    /** 主观题得分（人工批改） */
    private BigDecimal subjectiveScore;

    /** 最终得分（阅卷提交后汇总） */
    private BigDecimal finalScore;

    /** 切屏次数 */
    private Integer switchCount;

    /** 作弊标记 0:正常 1:切屏超限 */
    private Integer cheatFlag;

    /** 实际用时（秒） */
    private Integer durationSeconds;

    /** 状态 0:待批改 1:已发布 */
    private Integer status;

    /** 交卷时间 */
    private LocalDateTime submitTime;

    // ---- 非数据库字段 ----

    /** 考生姓名（联表查询填充） */
    @TableField(exist = false)
    private String userName;

    /** 试卷名称（联表查询填充） */
    @TableField(exist = false)
    private String paperTitle;

    /** 待批主观题数（查询填充） */
    @TableField(exist = false)
    private Integer subjectiveCount;
}
