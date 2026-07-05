package com.xhbookstore.system.service.member.impl;

import java.security.SecureRandom;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.xhbookstore.common.core.redis.RedisCache;
import com.xhbookstore.system.domain.member.Member;
import com.xhbookstore.system.domain.member.MemberCodeTokenInfo;
import com.xhbookstore.system.mapper.member.MemberMapper;
import com.xhbookstore.system.service.member.IMemberCodeTokenService;

@Service
public class MemberCodeTokenServiceImpl implements IMemberCodeTokenService {
    private static final String PREFIX = "member:code:";
    private static final SecureRandom RANDOM = new SecureRandom();

    @Autowired private RedisCache redisCache;
    @Autowired private MemberMapper memberMapper;

    @Override
    public MemberCodeTokenInfo createToken(Integer memberId, String scene, int ttlSeconds) {
        if (memberId == null) throw new IllegalArgumentException("memberId is required");
        Member member = memberMapper.selectMemberById(memberId);
        if (member == null) throw new IllegalArgumentException("Member not found");
        long now = System.currentTimeMillis();
        String token = UUID.randomUUID().toString().replace("-", "") + Long.toHexString(RANDOM.nextLong());
        MemberCodeTokenInfo info = new MemberCodeTokenInfo();
        info.setToken(token);
        info.setMemberId(memberId);
        info.setMemberNo(member.getCardNo());
        info.setScene(scene);
        info.setIssuedAt(now);
        info.setExpiresAt(now + ttlSeconds * 1000L);
        redisCache.setCacheObject(key(token), info, ttlSeconds, TimeUnit.SECONDS);
        return info;
    }

    @Override
    public Member verifyToken(String tokenOrContent, String scene) {
        MemberCodeTokenInfo info = getInfo(tokenOrContent);
        validate(info, scene);
        Member member = memberMapper.selectMemberById(info.getMemberId());
        if (member == null) throw new IllegalArgumentException("Member not found");
        return member;
    }

    @Override
    public Member consumeToken(String tokenOrContent, String scene) {
        String token = normalize(tokenOrContent);
        if (token == null || token.isEmpty()) throw new IllegalArgumentException("memberCodeToken is required");
        @SuppressWarnings("unchecked")
        MemberCodeTokenInfo info = (MemberCodeTokenInfo) redisCache.redisTemplate.opsForValue().getAndDelete(key(token));
        validate(info, scene);
        Member member = memberMapper.selectMemberById(info.getMemberId());
        if (member == null) throw new IllegalArgumentException("Member not found");
        return member;
    }

    private MemberCodeTokenInfo getInfo(String tokenOrContent) {
        String token = normalize(tokenOrContent);
        if (token == null || token.isEmpty()) throw new IllegalArgumentException("memberCodeToken is required");
        return redisCache.getCacheObject(key(token));
    }

    private void validate(MemberCodeTokenInfo info, String scene) {
        if (info == null) throw new IllegalArgumentException("Member code expired or used");
        if (scene != null && info.getScene() != null && !scene.equals(info.getScene())) {
            throw new IllegalArgumentException("Invalid member code scene");
        }
        if (info.getExpiresAt() != null && info.getExpiresAt() < System.currentTimeMillis()) {
            throw new IllegalArgumentException("Member code expired");
        }
    }

    private String normalize(String tokenOrContent) {
        if (tokenOrContent == null) return null;
        String value = tokenOrContent.trim();
        if (value.startsWith("MCODE:")) return value.substring("MCODE:".length()).trim();
        return value;
    }

    private String key(String token) {
        return PREFIX + token;
    }
}
