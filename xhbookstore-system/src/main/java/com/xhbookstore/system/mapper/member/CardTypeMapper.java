package com.xhbookstore.system.mapper.member;

import java.util.List;
import com.xhbookstore.system.domain.member.CardType;

public interface CardTypeMapper {
    List<CardType> selectAll();
    CardType selectById(Integer id);
    int insert(CardType cardType);
    int update(CardType cardType);
    int deleteById(Integer id);
}
