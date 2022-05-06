package com.laysan.autojob.core.service;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSON;
import com.laysan.autojob.core.entity.Account;
import com.laysan.autojob.core.repository.AccountRepository;
import io.vavr.control.Try;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;

@Service
@Slf4j
public class AutoRunService {
    @Resource
    TaskLogService taskLogService;
    @Resource
    AccountRepository accountRepository;

    @Async
    public void run(Account account, boolean forceRun) {
        log.info("run job {}", JSON.toJSONString(account));
        AutoRun autoRun = AutoRun.HANDLERS.get(account.getType());
        if (ObjectUtil.isNull(autoRun)) {
            log.info("handler is null,type={}", account.getType());
            taskLogService.saveErrorLog(account, "任务执行器配置错误");
            return;
        }
        Try.of(() -> autoRun.run(account, forceRun)).onSuccess(result -> {
            account.setLastRunTime(new Date());
            accountRepository.save(account);
        }).onFailure(e -> {
            log.error("run job error", e);
            account.setLastRunTime(new Date());
            accountRepository.save(account);
            taskLogService.saveErrorLog(account, e.getMessage());
        });
    }
}
