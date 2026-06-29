package com.xhbookstore.api.mapper;

import com.xhbookstore.api.domain.ApiLog;

/**
 * API日志Mapper
 */
public interface ApiLogMapper {
    int insertApiLog(ApiLog log);
}
