package com.laysan.autojob;

import com.laysan.autojob.core.entity.Account;
import com.laysan.autojob.core.repository.AccountRepository;
import com.laysan.autojob.core.utils.JobUtils;
import com.laysan.autojob.quartz.QuartzJob;
import com.laysan.autojob.quartz.entity.QuartzBean;
import com.laysan.autojob.quartz.repository.QuartzBeanRepository;
import com.laysan.autojob.quartz.util.QuartzUtils;
import org.quartz.Scheduler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.util.List;
import java.util.Objects;

@SpringBootApplication
@EnableJpaAuditing
public class AutojobApplication implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(AutojobApplication.class, args);
    }

    @Autowired
    private Scheduler            scheduler;
    @Autowired
    private QuartzBeanRepository quartzBeanRepository;
    @Autowired
    private AccountRepository    accountRepository;

    @Override
    public void run(String... args) {
        List<Account> accountList = accountRepository.findAll();
        accountList.forEach(account -> {
            QuartzBean quartzBean = quartzBeanRepository.findByAccountId(account.getId());
            if (Objects.isNull(quartzBean)) {
                quartzBean = new QuartzBean();
            }
            quartzBean.setUserId(account.getUserId());
            quartzBean.setAccountId(account.getId());
            quartzBean.setType(account.getType());
            quartzBean.setJobClass(QuartzJob.class.getName());
            quartzBean.setJobName(JobUtils.buildJobName(account));
            quartzBean.setCronExpression(JobUtils.buildCron(account));
            quartzBeanRepository.save(quartzBean);
            try {
                QuartzUtils.createScheduleJob(scheduler, quartzBean);
            } catch (Exception e) {
                QuartzUtils.updateScheduleJob(scheduler, quartzBean);
            }
        });

    }
}
