package com.laysan.autojob.scheduler;

import cn.hutool.core.collection.CollUtil;
import com.laysan.autojob.core.entity.Account;
import com.laysan.autojob.core.entity.Event;
import com.laysan.autojob.core.entity.User;
import com.laysan.autojob.core.repository.EventRepository;
import com.laysan.autojob.core.service.AccountService;
import com.laysan.autojob.core.service.UserService;
import com.laysan.autojob.service.AutoRunService;
import java.util.List;
import javax.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class CoreTask {
    @Resource
    AccountService  accountService;
    @Resource
    UserService     userService;
    @Resource
    EventRepository eventRepository;
    @Resource
    AutoRunService  autoRunService;

    @Scheduled(cron = "0 0 0 * * ?")
    public void autoReset() {
        log.info("每日零点自动初始化状态");

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

    //每1分钟触发任务
    @Scheduled(cron = "0 * * * * ?")
    public void runJob() {
        try {

            List<Account> accountList = accountService.findToRun();
            log.info("执行任务，size={}", accountList.size());

            accountList.forEach(v -> autoRunService.run(v, false));
        } catch (Exception e) {
            log.error("EventTask 执行失败");
        }
    }

    //每1分钟触发任务
    @Scheduled(cron = "0/30 * * * * ?")
    public void manualJob() {
        try {

            List<Event> accountList = eventRepository.findAll();

            log.info("执行手动任务，size={}", accountList.size());

            accountList.forEach(v -> {
                Account account = accountService.findById(v.getAccountId());
                autoRunService.run(account, true);
                eventRepository.deleteById(v.getId());
            });
        } catch (Exception e) {
            log.error("EventTask 执行失败");
        }
    }
}
