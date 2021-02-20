package com.laysan.autojob;

import cn.hutool.core.thread.ThreadUtil;
import com.laysan.autojob.core.entity.Account;
import com.laysan.autojob.core.repository.AccountRepository;
import com.laysan.autojob.core.utils.JobUtils;
import com.laysan.autojob.quartz.QuartzJob;
import com.laysan.autojob.quartz.entity.QuartzBean;
import com.laysan.autojob.quartz.repository.QuartzBeanRepository;
import com.laysan.autojob.quartz.util.QuartzUtils;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Scheduler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.Async;

import java.util.List;
import java.util.Objects;

@SpringBootApplication
@EnableJpaAuditing
@Slf4j
@Async
public class AutojobApplication implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(AutojobApplication.class, args);
    }

    @Autowired
    private Scheduler scheduler;
    @Autowired
    private QuartzBeanRepository quartzBeanRepository;
    @Autowired
    private AccountRepository accountRepository;

    @Override
    public void run(String... args) {
        ThreadUtil.execute(() -> {


            try {
                List<Account> accountList = accountRepository.findAll();
                int size = accountList.size();
                for (int i = 0; i < size; i++) {
                    Account account = accountList.get(i);
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
//                        FcUtils.createFunction(account);

                        QuartzUtils.createScheduleJob(scheduler, quartzBean);
//                    QuartzUtils.runOnce(scheduler,quartzBean.getJobName());
                    } catch (Exception e) {
                        e.printStackTrace();
                        QuartzUtils.updateScheduleJob(scheduler, quartzBean);
                    }
                    log.info("{} in {}", i, size);
                }
            } catch (Exception e) {
            }
        });
    }
}
