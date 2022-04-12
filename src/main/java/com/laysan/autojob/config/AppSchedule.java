package com.laysan.autojob.config;

import com.laysan.autojob.core.entity.Account;
import com.laysan.autojob.core.repository.AccountRepository;
import com.laysan.autojob.core.service.AccountService;
import com.laysan.autojob.core.service.AutoRunService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
@Slf4j
public class AppSchedule {
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    AccountService accountService;
    @Autowired
    AutoRunService autoRunService;

//    @Scheduled(cron = "0 0/1 * * * ? ")
    public void run() {
        String nowTime = LocalTime.now().format(formatter);
        log.info("run scheduled job at:" + nowTime);
        List<Account> accountList = accountRepository.findByTime(nowTime);
        if (accountList.isEmpty()) {
            log.info("no job to run at:" + nowTime);
            return;
        }
        accountList.forEach(v -> autoRunService.run(v));
    }
}
