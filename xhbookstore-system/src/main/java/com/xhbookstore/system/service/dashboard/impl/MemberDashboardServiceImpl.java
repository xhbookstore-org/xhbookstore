package com.xhbookstore.system.service.dashboard.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.xhbookstore.common.constant.Constants;
import com.xhbookstore.common.core.domain.entity.SysDept;
import com.xhbookstore.common.core.domain.entity.SysRole;
import com.xhbookstore.common.core.domain.entity.SysUser;
import com.xhbookstore.common.core.redis.RedisCache;
import com.xhbookstore.common.utils.SecurityUtils;
import com.xhbookstore.common.utils.StringUtils;
import com.xhbookstore.common.exception.ServiceException;
import com.xhbookstore.system.domain.dashboard.MemberDashboardDeptStats;
import com.xhbookstore.system.domain.dashboard.MemberDashboardLoginStats;
import com.xhbookstore.system.domain.dashboard.MemberDashboardOverview;
import com.xhbookstore.system.mapper.SysDeptMapper;
import com.xhbookstore.system.mapper.dashboard.MemberDashboardMapper;
import com.xhbookstore.system.service.dashboard.IMemberDashboardService;

@Service
public class MemberDashboardServiceImpl implements IMemberDashboardService {
    private static final String DEPT_KEY_PREFIX = "dashboard:member:dept:";
    private static final String DEPT_IDS_KEY = "dashboard:member:dept_ids";
    private static final String LAST_REFRESH_AT_KEY = "dashboard:member:last_refresh_at";
    private static final String LAST_REFRESH_STATUS_KEY = "dashboard:member:last_refresh_status";
    private static final String LOCK_KEY = "lock:dashboard:member-stats:refresh";

    @Autowired
    private MemberDashboardMapper dashboardMapper;

    @Autowired
    private SysDeptMapper deptMapper;

    @Autowired
    private RedisCache redisCache;

    @Override
    public MemberDashboardOverview getOverview(Long selectedDeptId) {
        List<Long> allowedDeptIds = resolveVisibleDeptIds();
        if (selectedDeptId != null && !allowedDeptIds.contains(selectedDeptId)) {
            throw new ServiceException("无权查看该门店数据");
        }
        List<Long> visibleDeptIds = selectedDeptId == null
                ? allowedDeptIds
                : Collections.singletonList(selectedDeptId);
        if (redisCache.getCacheObject(LAST_REFRESH_AT_KEY) == null) {
            refreshStatsWithLock();
        }

        MemberDashboardDeptStats merged = new MemberDashboardDeptStats();
        int missing = 0;
        for (Long deptId : visibleDeptIds) {
            MemberDashboardDeptStats item = redisCache.getCacheObject(DEPT_KEY_PREFIX + deptId);
            if (item == null) {
                missing++;
                continue;
            }
            merged.add(item);
        }

        MemberDashboardLoginStats loginStats = new MemberDashboardLoginStats();
        loginStats.setTotalLoginCount(merged.getTotalLoginCount());
        loginStats.setYearLoginCount(merged.getYearLoginCount());
        loginStats.setMonthLoginCount(merged.getMonthLoginCount());
        loginStats.setYesterdayLoginCount(merged.getYesterdayLoginCount());

        MemberDashboardOverview overview = new MemberDashboardOverview();
        overview.setStats(merged);
        overview.setLoginStats(loginStats);
        overview.setVisibleDeptIds(visibleDeptIds);
        overview.setVisibleDeptCount(visibleDeptIds.size());
        overview.setMissingDeptCount(missing);
        overview.setRefreshedAt(redisCache.getCacheObject(LAST_REFRESH_AT_KEY));
        overview.setRefreshStatus(redisCache.getCacheObject(LAST_REFRESH_STATUS_KEY));
        overview.setScopeName(buildScopeName(visibleDeptIds));
        overview.setLoginStatsScopeNote("登录次数和会员码展示量均按会员所属门店统计，并受当前登录人的数据权限限制。");
        return overview;
    }

    @Override
    public List<SysDept> getVisibleDeptOptions() {
        List<Long> visibleDeptIds = resolveVisibleDeptIds();
        List<SysDept> result = new ArrayList<>();
        for (Long deptId : visibleDeptIds) {
            SysDept dept = deptMapper.selectDeptById(deptId);
            if (dept != null && "0".equals(dept.getStatus())) {
                result.add(dept);
            }
        }
        return result;
    }

    @Override
    public boolean refreshStatsWithLock() {
        String lockValue = UUID.randomUUID().toString();
        Boolean locked = redisCache.redisTemplate.opsForValue().setIfAbsent(LOCK_KEY, lockValue, 10, TimeUnit.MINUTES);
        if (!Boolean.TRUE.equals(locked)) {
            return false;
        }
        try {
            refreshStats();
            return true;
        } catch (Exception e) {
            redisCache.setCacheObject(LAST_REFRESH_STATUS_KEY, "FAILED: " + e.getMessage());
            throw e;
        } finally {
            releaseLock(lockValue);
        }
    }

    @Scheduled(cron = "0 */5 * * * ?")
    public void scheduledRefresh() {
        refreshStatsWithLock();
    }

    private void refreshStats() {
        List<MemberDashboardDeptStats> memberStats = dashboardMapper.selectDeptMemberStats();
        List<MemberDashboardDeptStats> codeStats = dashboardMapper.selectDeptMemberCodeStats();
        List<MemberDashboardDeptStats> loginStats = dashboardMapper.selectDeptLoginStats();
        List<Long> oldDeptIds = redisCache.getCacheObject(DEPT_IDS_KEY);
        if (oldDeptIds != null && !oldDeptIds.isEmpty()) {
            redisCache.deleteObject(oldDeptIds.stream().map(id -> DEPT_KEY_PREFIX + id).collect(Collectors.toList()));
        }

        Set<Long> deptIds = new HashSet<>();
        for (MemberDashboardDeptStats item : memberStats) {
            if (item.getDeptId() == null) {
                continue;
            }
            deptIds.add(item.getDeptId());
            redisCache.setCacheObject(DEPT_KEY_PREFIX + item.getDeptId(), item);
        }
        for (MemberDashboardDeptStats item : codeStats) {
            if (item.getDeptId() == null) {
                continue;
            }
            MemberDashboardDeptStats cached = redisCache.getCacheObject(DEPT_KEY_PREFIX + item.getDeptId());
            if (cached == null) {
                cached = new MemberDashboardDeptStats();
                cached.setDeptId(item.getDeptId());
                cached.setDeptName(item.getDeptName());
            }
            cached.setTotalMemberCodeShowCount(item.getTotalMemberCodeShowCount());
            cached.setYearMemberCodeShowCount(item.getYearMemberCodeShowCount());
            cached.setMonthMemberCodeShowCount(item.getMonthMemberCodeShowCount());
            cached.setYesterdayMemberCodeShowCount(item.getYesterdayMemberCodeShowCount());
            deptIds.add(item.getDeptId());
            redisCache.setCacheObject(DEPT_KEY_PREFIX + item.getDeptId(), cached);
        }
        for (MemberDashboardDeptStats item : loginStats) {
            if (item.getDeptId() == null) {
                continue;
            }
            MemberDashboardDeptStats cached = redisCache.getCacheObject(DEPT_KEY_PREFIX + item.getDeptId());
            if (cached == null) {
                cached = new MemberDashboardDeptStats();
                cached.setDeptId(item.getDeptId());
                cached.setDeptName(item.getDeptName());
            }
            cached.setTotalLoginCount(item.getTotalLoginCount());
            cached.setYearLoginCount(item.getYearLoginCount());
            cached.setMonthLoginCount(item.getMonthLoginCount());
            cached.setYesterdayLoginCount(item.getYesterdayLoginCount());
            deptIds.add(item.getDeptId());
            redisCache.setCacheObject(DEPT_KEY_PREFIX + item.getDeptId(), cached);
        }

        redisCache.setCacheObject(DEPT_IDS_KEY, new ArrayList<>(deptIds));
        redisCache.setCacheObject(LAST_REFRESH_AT_KEY, new Date());
        redisCache.setCacheObject(LAST_REFRESH_STATUS_KEY, "SUCCESS");
    }

    private List<Long> resolveVisibleDeptIds() {
        SysUser user = SecurityUtils.getLoginUser().getUser();
        if (user == null) {
            return Collections.emptyList();
        }
        if (user.isAdmin()) {
            return selectNormalDeptIds();
        }

        Set<Long> result = new LinkedHashSet<>();
        List<SysRole> roles = user.getRoles();
        if (roles != null) {
            for (SysRole role : roles) {
                if (role == null || !"0".equals(role.getStatus())) {
                    continue;
                }
                String scope = role.getDataScope();
                if (Constants.Dept.DATA_SCOPE_ALL.equals(scope)) {
                    return selectNormalDeptIds();
                }
                if (Constants.Dept.DATA_SCOPE_CUSTOM.equals(scope)) {
                    result.addAll(deptMapper.selectDeptListByRoleId(role.getRoleId(), role.isDeptCheckStrictly()));
                } else if (Constants.Dept.DATA_SCOPE_DEPT.equals(scope) || Constants.Dept.DATA_SCOPE_SELF.equals(scope)) {
                    addDept(result, user.getDeptId());
                } else if (Constants.Dept.DATA_SCOPE_DEPT_AND_CHILD.equals(scope)) {
                    addDeptAndChildren(result, user.getDeptId());
                }
            }
        }
        if (result.isEmpty()) {
            addDept(result, user.getDeptId());
        }
        return new ArrayList<>(result);
    }

    private List<Long> selectNormalDeptIds() {
        SysDept dept = new SysDept();
        dept.setStatus("0");
        return deptMapper.selectDeptList(dept).stream().map(SysDept::getDeptId).collect(Collectors.toList());
    }

    private void addDept(Set<Long> result, Long deptId) {
        if (deptId != null && deptId > 0) {
            result.add(deptId);
        }
    }

    private void addDeptAndChildren(Set<Long> result, Long deptId) {
        addDept(result, deptId);
        if (deptId == null) {
            return;
        }
        List<SysDept> children = deptMapper.selectChildrenDeptById(deptId);
        for (SysDept child : children) {
            if ("0".equals(child.getStatus())) {
                result.add(child.getDeptId());
            }
        }
    }

    private String buildScopeName(List<Long> visibleDeptIds) {
        if (visibleDeptIds == null || visibleDeptIds.isEmpty()) {
            return "无可见部门";
        }
        if (visibleDeptIds.size() == 1) {
            SysDept dept = deptMapper.selectDeptById(visibleDeptIds.get(0));
            return dept == null || StringUtils.isBlank(dept.getDeptName()) ? "当前部门" : dept.getDeptName();
        }
        return "可见部门 " + visibleDeptIds.size() + " 个";
    }

    private void releaseLock(String lockValue) {
        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        script.setResultType(Long.class);
        script.setScriptText("if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end");
        redisCache.redisTemplate.execute(script, Collections.singletonList(LOCK_KEY), lockValue);
    }
}
