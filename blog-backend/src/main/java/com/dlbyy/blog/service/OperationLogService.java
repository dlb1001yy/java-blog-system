package com.dlbyy.blog.service;

import com.dlbyy.blog.entity.OperationLog;

/**
 * 后台操作日志服务
 */
public interface OperationLogService {

    /**
     * 异步保存操作日志（失败仅告警，不影响主流程）
     *
     * @param operationLog 操作日志实体
     */
    void asyncSave(OperationLog operationLog);
}
