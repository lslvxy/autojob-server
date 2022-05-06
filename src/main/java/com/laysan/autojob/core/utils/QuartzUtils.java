package com.laysan.autojob.core.utils;


import com.laysan.autojob.core.entity.Account;
import lombok.extern.slf4j.Slf4j;
import org.quartz.CronScheduleBuilder;
import org.quartz.CronTrigger;
import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;
import org.quartz.impl.matchers.GroupMatcher;

import java.util.Objects;
import java.util.Set;

@Slf4j
public class QuartzUtils {

    /**
     * 创建定时任务 定时任务创建之后默认启动状态
     *
     * @param scheduler  调度器
     * @param quartzBean 定时任务信息类
     * @throws Exception
     */
    public static void createScheduleJob(Scheduler scheduler, Account account) throws Exception {
        //获取到定时任务的执行类  必须是类的绝对路径名称
        //定时任务类需要是job类的具体实现 QuartzJobBean是job的抽象类。
        Class<? extends Job> jobClass = (Class<? extends Job>) Class.forName(account.buildJobClass());
        // 构建定时任务信息
        JobDataMap s = new JobDataMap();
        s.put("accountId", account.getId());
        JobDetail jobDetail = JobBuilder.newJob(jobClass).withIdentity(account.buildJobName()).usingJobData(s).build();
        // 设置定时任务执行方式
        CronScheduleBuilder scheduleBuilder = CronScheduleBuilder.cronSchedule(account.buildCronExpression());
        // 构建触发器trigger
        CronTrigger trigger = TriggerBuilder.newTrigger().withIdentity(account.buildJobName()).withSchedule(scheduleBuilder).build();
        scheduler.scheduleJob(jobDetail, trigger);

    }

    /**
     * 根据任务名称暂停定时任务
     *
     * @param scheduler 调度器
     * @param jobName   定时任务名称
     * @throws SchedulerException
     */
    public static void pauseScheduleJob(Scheduler scheduler, String jobName) {
        JobKey jobKey = JobKey.jobKey(jobName);
        try {
            scheduler.pauseJob(jobKey);
        } catch (SchedulerException e) {
            System.out.println("暂停定时任务出错：" + e.getMessage());
        }
    }

    public static Set<JobKey> listAll(Scheduler scheduler) {
        try {
            return scheduler.getJobKeys(GroupMatcher.anyGroup());
        } catch (SchedulerException e) {
            System.out.println("查询定时任务出错：" + e.getMessage());
        }
        return null;
    }

    /**
     * 根据任务名称恢复定时任务
     *
     * @param scheduler 调度器
     * @param jobName   定时任务名称
     * @throws SchedulerException
     */
    public static void resumeScheduleJob(Scheduler scheduler, String jobName) {
        JobKey jobKey = JobKey.jobKey(jobName);
        try {
            scheduler.resumeJob(jobKey);
        } catch (SchedulerException e) {
            System.out.println("启动定时任务出错：" + e.getMessage());
        }
    }

    /**
     * 根据任务名称立即运行一次定时任务
     * 强制执行
     *
     * @param scheduler 调度器
     * @throws SchedulerException
     */
    public static void runOnce(Scheduler scheduler, Account account, boolean forceRun) {
        JobKey jobKey = account.buildJobKey();
        try {
            JobDataMap map = new JobDataMap();
            map.put("forceRun", forceRun);
            if (scheduler.checkExists(jobKey)) {
                scheduler.triggerJob(jobKey, map);
            } else {
                createScheduleJob(scheduler, account);
                scheduler.triggerJob(jobKey, map);
            }
        } catch (Exception e) {
            LogUtils.error(log, "Quartz", account, "Quartz runOnce error, {}", e.getMessage());
        }
    }

    /**
     * 根据任务名称立即运行一次定时任务
     *
     * @param scheduler 调度器
     * @throws SchedulerException
     */
    public static void runOnce(Scheduler scheduler, Account account) {
        runOnce(scheduler, account, false);
    }

    /**
     * 更新定时任务
     *
     * @param scheduler 调度器
     * @param account   定时任务信息类
     * @throws SchedulerException
     */
    public static void updateScheduleJob(Scheduler scheduler, Account account) {
        try {
            //获取到对应任务的触发器
            TriggerKey triggerKey = TriggerKey.triggerKey(account.buildJobName());
            //设置定时任务执行方式
            CronScheduleBuilder scheduleBuilder = CronScheduleBuilder.cronSchedule(account.buildCronExpression());
            //重新构建任务的触发器trigger
            CronTrigger trigger = (CronTrigger) scheduler.getTrigger(triggerKey);
            if (Objects.isNull(trigger)) {
                return;
            }
            trigger = trigger.getTriggerBuilder().withIdentity(triggerKey).withSchedule(scheduleBuilder).build();
            //重置对应的job
            scheduler.rescheduleJob(triggerKey, trigger);
        } catch (SchedulerException e) {
            System.out.println("更新定时任务出错：" + e.getMessage());
        }
    }

    /**
     * 根据定时任务名称从调度器当中删除定时任务
     *
     * @param scheduler 调度器
     * @param jobName   定时任务名称
     * @throws SchedulerException
     */
    public static void deleteScheduleJob(Scheduler scheduler, String jobName) {
        JobKey jobKey = JobKey.jobKey(jobName);
        try {
            scheduler.deleteJob(jobKey);
        } catch (SchedulerException e) {
            System.out.println("删除定时任务出错：" + e.getMessage());
        }
    }
}