package com.laysan.autojob.core.job;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import com.laysan.autojob.core.entity.Account;
import com.laysan.autojob.core.entity.User;
import com.laysan.autojob.core.repository.TaskLogRepository;
import com.laysan.autojob.core.service.AccountService;
import com.laysan.autojob.core.service.AutoRunService;
import com.laysan.autojob.core.service.MessageService;
import com.laysan.autojob.core.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;

@Slf4j
@Component
public class CoreTask extends QuartzJobBean {
    @Resource
    AccountService accountService;
    @Resource
    UserService userService;

    @Override
    protected void executeInternal(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        log.info("定时任务-执行Job，每日自动初始化状态");

        List<Account> accountList = accountService.findAll();
        if (CollUtil.isNotEmpty(accountList)) {
            accountList.forEach(v -> {
                v.setTodayExecuted(-1);
                accountService.save(v);
            });
        }

        List<User> userList = userService.findAll();
        if (CollUtil.isNotEmpty(userList)) {
            userList.forEach(v -> {
                v.setTodayRunCount(0);
                userService.save(v);
            });
        }
    }
}
