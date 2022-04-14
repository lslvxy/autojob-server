package com.laysan.autojob.core.service;

import com.laysan.autojob.core.entity.Account;
import com.laysan.autojob.core.entity.TaskLog;
import com.laysan.autojob.core.repository.TaskLogRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
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
        taskLog.setSucceed(1);
        taskLogRepository.save(taskLog);
    }

    public void saveErrorLog(Account account, String message) {
        TaskLog taskLog = new TaskLog();
        taskLog.setDetail(message);
        taskLog.setUserId(account.getUserId());
        taskLog.setAccountId(account.getId());
        taskLog.setAccount(account.getAccount());
        taskLog.setType(account.getType());
        taskLog.setSucceed(0);
        taskLogRepository.save(taskLog);
    }

    public Page<TaskLog> findAll(Long userId, PageRequest pageRequest) {
        Example<TaskLog> ex = Example.of(new TaskLog(userId));
        return taskLogRepository.findAll(ex, pageRequest.withSort(Sort.by(Sort.Direction.DESC, "gmtCreate")));
    }
}
