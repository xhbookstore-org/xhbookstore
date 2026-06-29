package com.xhbookstore.system.service.member.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.xhbookstore.system.domain.member.CardType;
import com.xhbookstore.system.mapper.member.CardTypeMapper;
import com.xhbookstore.system.service.member.ICardTypeService;

@Service
public class CardTypeServiceImpl implements ICardTypeService {

    @Autowired private CardTypeMapper mapper;

    @Override
    public List<CardType> selectAll() {
        return mapper.selectAll();
    }

    @Override
    public CardType selectById(Integer id) {
        return mapper.selectById(id);
    }
}