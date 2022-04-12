package com.laysan.autojob.core.service;

import com.laysan.autojob.core.entity.Account;
import com.laysan.autojob.core.entity.TaskLog;
import com.laysan.autojob.core.repository.TaskLogRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
@Slf4j
public class TaskLogService {
    @Resource
    TaskLogRepository taskLogRepository;

    public void saveSuccessLog(Account account, String message) {
        TaskLog taskLog = new TaskLog();
        taskLog.setDetail(message);
        taskLog.setUserId(account.getUserId());
        taskLog.setAccountId(account.getId());
        taskLog.setAccount(account.getAccount());
        taskLog.setType(account.getType());
        taskLog.setSucceed(Boolean.TRUE);
        taskLogRepository.save(taskLog);
    }

    public void saveErrorLog(Account account, String message) {
        TaskLog taskLog = new TaskLog();
        taskLog.setDetail(message);
        taskLog.setUserId(account.getUserId());
        taskLog.setAccountId(account.getId());
        taskLog.setAccount(account.getAccount());
        taskLog.setType(account.getType());
        taskLog.setSucceed(Boolean.FALSE);
        taskLogRepository.save(taskLog);
    }
}
