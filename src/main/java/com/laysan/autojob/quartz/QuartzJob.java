package com.laysan.autojob.quartz;

import com.laysan.autojob.core.entity.Account;
import com.laysan.autojob.core.repository.EventLogRepository;
import com.laysan.autojob.core.service.AccountService;
import com.laysan.autojob.core.service.AutoRunService;
import com.laysan.autojob.core.service.MessageService;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class QuartzJob extends QuartzJobBean {
    @Autowired
    AccountService     accountService;
    @Autowired
    EventLogRepository eventLogRepository;
    @Autowired
    AutoRunService     autoRunService;
    @Autowired
    MessageService     messageService;

    @Override
    protected void executeInternal(JobExecutionContext jobExecutionContext) {
        JobDetail jobDetail = jobExecutionContext.getJobDetail();
        final String name = jobDetail.getKey().getName();
        JobDataMap jobDataMap = jobDetail.getJobDataMap();
        log.info("定时任务-执行Job:{}", name);
        Long accountId = Long.valueOf(String.valueOf(jobDataMap.get("accountId")));

        Account account = accountService.findById(accountId);
        autoRunService.run(account);
    }
}
