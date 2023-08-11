package com.laysan.autojob.core.service;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.laysan.autojob.core.entity.Account;
import com.laysan.autojob.core.entity.TaskLog;
import com.laysan.autojob.core.repository.TaskLogRepository;
import java.util.Date;
import java.util.List;
import javax.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class TaskLogService {
    @Resource
    TaskLogRepository taskLogRepository;

    public void save(TaskLog taskLog) {
        taskLogRepository.save(taskLog);
    }

    public void saveLog(TaskLog taskLog) {
        taskLogRepository.save(taskLog);
    }

    public void saveSuccessLog(Account account, String message) {
        if (StrUtil.isBlank(message)) {
            message = "操作成功";
        }
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
        if (StrUtil.isBlank(message)) {
            message = "操作失败";
        }
        TaskLog taskLog = new TaskLog();
        taskLog.setDetail(message);
        taskLog.setUserId(account.getUserId());
        taskLog.setAccountId(account.getId());
        taskLog.setAccount(account.getAccount());
        taskLog.setType(account.getType());
        taskLog.setSucceed(0);
        taskLogRepository.save(taskLog);
    }

    public Page<TaskLog> findAll(Long accountId, PageRequest pageRequest) {
        TaskLog probe = new TaskLog();
        probe.setAccountId(accountId);
        Example<TaskLog> ex = Example.of(probe);
        Page<TaskLog> page = taskLogRepository.findAll(ex, pageRequest.withSort(Sort.by(Sort.Direction.DESC, "gmtCreate")));
        page.getContent().forEach(v -> {
            v.setDetail(v.getDetail().replaceAll("#", ","));
        });
        return page;
    }

    public List<TaskLog> finaAllToday(Long userId) {
        TaskLog probe = new TaskLog(userId);
        probe.setSucceed(1);
        probe.setExecutedDay(DateUtil.today());
        Example<TaskLog> ex = Example.of(probe);
        return taskLogRepository.findAll(ex);
    }

    public Boolean todayExecuted(Account account) {
        TaskLog probe = new TaskLog(account.getUserId());
        probe.setSucceed(1);
        probe.setType(account.getType());
        probe.setAccount(account.getAccount());
        probe.setExecutedDay(DateUtil.format(new Date(), DatePattern.NORM_DATE_PATTERN));
        Example<TaskLog> ex = Example.of(probe);
        return taskLogRepository.exists(ex);
    }

}
