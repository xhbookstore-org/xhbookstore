package com.xhbookstore.system.mapper.member;

import java.util.List;
import java.util.Map;

public interface CardTypeLogMapper {
    int insert(Map<String, Object> log);
    List<Map<String, Object>> selectByCardTypeId(Integer cardTypeId);
}
