package com.xhbookstore.api.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import com.xhbookstore.api.domain.ApiLog;
import com.xhbookstore.api.mapper.ApiLogMapper;
import com.xhbookstore.api.service.IApiLogService;

/**
 * API日志服务实现 - 异步写入，不阻塞主线程
 */
@Service
public class ApiLogServiceImpl implements IApiLogService {

    @Autowired
    private ApiLogMapper apiLogMapper;

    @Override
    @Async
    public void save(ApiLog log) {
        apiLogMapper.insertApiLog(log);
    }
}
