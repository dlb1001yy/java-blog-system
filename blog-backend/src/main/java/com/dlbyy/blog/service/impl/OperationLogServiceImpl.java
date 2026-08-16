package com.dlbyy.blog.service.impl;

import com.dlbyy.blog.entity.OperationLog;
import com.dlbyy.blog.mapper.OperationLogMapper;
import com.dlbyy.blog.service.OperationLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * 后台操作日志服务实现：通过 opLogExecutor 线程池异步落库，
 * 落库失败仅记录告警，绝不影响主业务流程。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OperationLogServiceImpl implements OperationLogService {

    private final OperationLogMapper operationLogMapper;

    @Async("opLogExecutor")
    @Override
    public void asyncSave(OperationLog operationLog) {
        try {
            operationLogMapper.insert(operationLog);
        } catch (Exception e) {
            log.warn("操作日志落库失败", e);
        }
    }
}
