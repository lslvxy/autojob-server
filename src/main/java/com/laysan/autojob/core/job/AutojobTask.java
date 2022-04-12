package com.laysan.autojob.core.job;

import cn.hutool.core.util.ObjectUtil;
import com.laysan.autojob.core.entity.Account;
import com.laysan.autojob.core.repository.TaskLogRepository;
import com.laysan.autojob.core.service.AccountService;
import com.laysan.autojob.core.service.AutoRunService;
import com.laysan.autojob.core.service.MessageService;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Slf4j
@Component
public class AutojobTask extends QuartzJobBean {
    @Resource
    AccountService accountService;
    @Resource
    TaskLogRepository taskLogRepository;
    @Resource
    AutoRunService autoRunService;
    @Resource
    MessageService messageService;

    @Override
    protected void executeInternal(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        JobDetail jobDetail = jobExecutionContext.getJobDetail();
        final String name = jobDetail.getKey().getName();
        JobDataMap jobDataMap = jobDetail.getJobDataMap();
        log.info("定时任务-执行Job:{}", name);
        Long accountId = Long.valueOf(String.valueOf(jobDataMap.get("accountId")));

        Account account = accountService.findById(accountId);
        if (ObjectUtil.isNull(account)) {
            log.info("account is null,accountId={}", accountId);
            return;
        }
        if (ObjectUtil.isNull(account.getType())) {
            log.info("account type is null,accountId={}", accountId);
            return;
        }
        autoRunService.run(account);
    }
}
