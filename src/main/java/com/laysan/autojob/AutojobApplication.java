package com.laysan.autojob;

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

    @Override
    public void run(String... args) {

        List<QuartzBean> all = quartzBeanRepository.findAll();
        all.forEach(v -> {
            try {
                QuartzUtils.createScheduleJob(scheduler, v);
            } catch (Exception e) {
                QuartzUtils.updateScheduleJob(scheduler, v);
            }
            // QuartzUtils.runOnce(scheduler, v.getJobName());
        });
    }
}
