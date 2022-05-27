package com.laysan.autojob.config;

import com.laysan.autojob.core.job.CoreTask;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class QuartzConfiguration {
    // 使用jobDetail包装job
    @Bean
    public JobDetail coreJobDetail() {
        return JobBuilder.newJob(CoreTask.class).withIdentity("corkTask").storeDurably().build();
    }

    // 把jobDetail注册到Cron表达式的trigger上去
    @Bean
    public Trigger CronJobTrigger() {
//        每天凌晨0点执行一次：
        CronScheduleBuilder cronScheduleBuilder = CronScheduleBuilder.cronSchedule("0 0 0 * * ?");

        return TriggerBuilder.newTrigger()
                .forJob(coreJobDetail())
                .withIdentity("corkTaskTrigger")
                .withSchedule(cronScheduleBuilder)
                .build();
    }
}
