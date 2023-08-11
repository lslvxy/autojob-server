package com.laysan.autojob.service;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSON;
import com.laysan.autojob.core.constants.AccountType;
import com.laysan.autojob.core.entity.Account;
import com.laysan.autojob.core.entity.TaskLog;
import com.laysan.autojob.core.helper.AutojobContext;
import com.laysan.autojob.core.repository.AccountRepository;
import com.laysan.autojob.core.service.TaskLogService;
import java.util.Date;
import javax.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class AutoRunService {
    @Resource
    TaskLogService    taskLogService;
    @Resource
    AccountRepository accountRepository;

    //@Async
    public void run(Account account, boolean forceRun) {
        log.info("run job {}", JSON.toJSONString(account));
        JobRuner jobRuner = JobRuner.HANDLERS.get(account.getType());
        if (ObjectUtil.isNull(jobRuner)) {
            log.info("handler is null,type={}", account.getType());
            taskLogService.saveErrorLog(account, "任务执行器配置错误");
            return;
        }
        Date now = new Date();

        AutojobContext context = new AutojobContext();
        context.setExecuteTime(now);
        context.setAccount(account);
        context.setAccountType(AccountType.get(account.getType()));
        try {
            if (!forceRun && account.getTodayExecuted() == 1) {
                context.setDetailMessage("今日已执行");
                context.setSucceed(true);
                return;
            }
            if (forceRun || account.getTodayExecuted() == -1) {
                jobRuner.run(context);
            }
        } catch (Exception e) {
            context.setSucceed(false);
            context.setDetailMessage(e.getMessage());
        } finally {
            account.setLastRunTime(now);
            accountRepository.save(account);
            log.info("context:{}", context);
            taskLogService.saveLog(buildTaskLog(context));
        }
    }

    private TaskLog buildTaskLog(AutojobContext context) {
        Account account = context.getAccount();
        TaskLog taskLog = new TaskLog();
        taskLog.setDetail(context.getDetailMessage());
        taskLog.setUserId(account.getUserId());
        taskLog.setAccountId(account.getId());
        taskLog.setAccount(account.getAccount());
        taskLog.setType(account.getType());
        taskLog.setSucceed(context.getSucceed() ? 1 : 0);
        return taskLog;
    }
}
