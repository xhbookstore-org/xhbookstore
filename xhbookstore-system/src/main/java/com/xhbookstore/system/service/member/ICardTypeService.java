package com.xhbookstore.system.service.member;

import java.util.List;
import com.xhbookstore.system.domain.member.CardType;

public interface ICardTypeService {
    List<CardType> selectAll();
    CardType selectById(Integer id);
}