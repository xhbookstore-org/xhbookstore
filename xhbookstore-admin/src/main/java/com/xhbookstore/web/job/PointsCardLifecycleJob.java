package com.xhbookstore.web.job;

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import com.xhbookstore.system.service.member.IPointsCardLifecycleService;

/** 每分钟处理退卡冻结积分冲销和到期积分解冻。 */
@Component
public class PointsCardLifecycleJob {
    private static final Logger log = LoggerFactory.getLogger(PointsCardLifecycleJob.class);
    @Autowired private IPointsCardLifecycleService lifecycleService;

    @Scheduled(cron = "0 * * * * ?")
    public void execute() {
        Map<String, Integer> result = lifecycleService.processPending(200);
        if (result.getOrDefault("scanned", 0) > 0) log.info("会员卡积分生命周期处理完成：{}", result);
    }
}
