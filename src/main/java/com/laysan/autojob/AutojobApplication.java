package com.laysan.autojob;

import com.laysan.autojob.core.entity.Account;
import com.laysan.autojob.core.service.AccountService;
import com.laysan.autojob.core.utils.QuartzUtils;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Scheduler;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;

import javax.annotation.Resource;
import java.util.List;

@SpringBootApplication
@EnableJpaAuditing
@Slf4j
@EnableAsync
@EnableCaching
public class AutojobApplication implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(AutojobApplication.class, args);
    }

    @Resource
    private Scheduler scheduler;
    @Resource
    private AccountService accountService;

    @Override
    public void run(String... args) {
        List<Account> accountList = accountService.findAll();
        accountList.forEach(account -> {
            try {
                QuartzUtils.createScheduleJob(scheduler, account);
            } catch (Exception e) {
                QuartzUtils.updateScheduleJob(scheduler, account);
            }
        });

    }
}
