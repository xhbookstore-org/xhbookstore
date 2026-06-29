package com.xhbookstore.api.service;

import com.xhbookstore.api.domain.ApiLog;

/**
 * API日志服务
 */
public interface IApiLogService {
    void save(ApiLog log);
}
