package com.dlbyy.blog.service;

import com.dlbyy.blog.common.PageResult;
import com.dlbyy.blog.dto.ExamMarkingDTO;
import com.dlbyy.blog.dto.ExamRecordDetailDTO;
import com.dlbyy.blog.dto.ExamSubmitDTO;
import com.dlbyy.blog.entity.ExamRecord;

import java.util.List;

/**
 * 考试核心业务服务（交卷 / 判分 / 批改 / 成绩）
 */
public interface ExamService {

    /**
     * 交卷：客观题自动判分，主观题生成批改草稿，保存答卷记录
     *
     * @return 答卷记录ID
     */
    Long submitPaper(Long userId, ExamSubmitDTO dto);

    /**
     * 待批改答卷分页（含考生姓名、试卷名、客观分、待批主观题数）
     */
    PageResult<ExamRecord> pendingPage(int page, int size, String keyword);

    /**
     * 待阅卷（待批改）答卷总数，count 查询，不加载实体
     */
    long countPendingMarking();

    /**
     * 批改详情（recordId → 题目、考生答案、参考答案、已评分草稿）
     */
    ExamRecordDetailDTO markingDetail(Long recordId);

    /**
     * 保存批改：submit=false 存草稿；true 全部确认并汇总成绩、发布答卷
     */
    void saveMarking(Long recordId, List<ExamMarkingDTO> markings, boolean submit);

    /**
     * 我的考试记录分页
     */
    PageResult<ExamRecord> myRecords(Long userId, int page, int size);

    /**
     * 成绩详情（校验本人或管理员）
     *
     * @param currentUserRole 当前请求用户角色（管理员放行）
     */
    ExamRecordDetailDTO recordDetail(Long recordId, Long currentUserId, String currentUserRole);
}
