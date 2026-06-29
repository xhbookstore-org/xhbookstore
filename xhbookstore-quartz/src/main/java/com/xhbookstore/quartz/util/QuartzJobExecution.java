package com.xhbookstore.quartz.util;

import org.quartz.JobExecutionContext;
import com.xhbookstore.quartz.domain.SysJob;

/**
 * 定时任务处理（允许并发执行）
 * 
 * @author xhbookstore
 *
 */
public class QuartzJobExecution extends AbstractQuartzJob
{
    @Override
    protected void doExecute(JobExecutionContext context, SysJob sysJob) throws Exception
    {
        JobInvokeUtil.invokeMethod(sysJob);
    }
}
