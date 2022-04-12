package com.laysan.autojob.core.service;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSON;
import com.laysan.autojob.core.entity.Account;
import io.vavr.control.Try;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
@Slf4j
public class AutoRunService {
    @Resource
    TaskLogService taskLogService;

    @Async

    public void run(Account account) {
        log.info("run job {}", JSON.toJSONString(account));
        AutoRun autoRun = AutoRun.HANDLERS.get(account.getType());
        if (ObjectUtil.isNull(autoRun)) {
            log.info("handler is null,type={}", account.getType());
            taskLogService.saveErrorLog(account, "任务执行器配置错误");
            return;
        }
        Try.of(() -> autoRun.run(account)).onFailure(e -> {
            log.error("run job error", e);
            taskLogService.saveErrorLog(account, e.getMessage());
        });
    }
}
