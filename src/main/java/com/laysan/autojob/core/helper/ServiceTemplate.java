package com.laysan.autojob.core.helper;

import cn.hutool.core.date.DateUtil;
import com.alibaba.cola.exception.BizException;
import com.laysan.autojob.core.constants.AccountType;
import com.laysan.autojob.core.entity.Account;
import com.laysan.autojob.core.entity.TaskLog;
import com.laysan.autojob.core.utils.LogUtils;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;

@Slf4j
public class ServiceTemplate {
    public static void execute(AccountType accountType, Account account, ServiceTemplateService serviceTemplateService, ServiceCallback callback) {
        TaskLog taskLog = new TaskLog();
        long start = System.currentTimeMillis();
        try {
            AutojobContextHolder.init();
            if (Boolean.TRUE.equals(account.getTodayExecuted())) {
                throw new BizException("todayExecuted");
            }
            String decryptPassword = serviceTemplateService.decryptPassword(account.getPassword());
            AutojobContextHolder.get().setDecryptPassword(decryptPassword);
            OkHttpClient client = callback.initOkHttpClient();
            AutojobContextHolder.get().setClient(client);
            AutojobContextHolder.get().setAccount(account.getAccount());
            callback.prepare();
            callback.process();
            account.setTodayExecuted(1);
            taskLog.setSucceed(1);
        } catch (BizException e) {
            AutojobContextHolder.get().setDetailMessage(e.getMessage());
            LogUtils.error(log, accountType, account.getAccount(), e.getMessage());
            account.setTodayExecuted(0);
            taskLog.setSucceed(0);
        } catch (Exception e) {
            AutojobContextHolder.get().setDetailMessage("定时任务执行失败");
            LogUtils.error(log, accountType, account.getAccount(), "定时任务执行失败");
            account.setTodayExecuted(0);
            taskLog.setSucceed(0);
        } finally {
            long time = System.currentTimeMillis() - start;
            LogUtils.info(log, accountType, account.getAccount(), "定时任务执行完成.花费时间:{}ms", time);
            taskLog.setUserId(account.getUserId());
            taskLog.setType(accountType.getCode());
            taskLog.setAccount(account.getAccount());
            taskLog.setAccountId(account.getId());
            taskLog.setTimeCosted(time);
            taskLog.setExecutedDay(DateUtil.today());
            taskLog.setDetail(AutojobContextHolder.get().getDetailMessage());
            serviceTemplateService.updateAccount(account);

            if (!taskLog.getDetail().equals("todayExecuted")) {
                serviceTemplateService.saveTaskLog(taskLog);
                serviceTemplateService.updateTodayRunCount(account.getUserId());
                serviceTemplateService.sendNotifyMsg(account.getUserId());
            }
            AutojobContextHolder.clear();
        }
    }
}
