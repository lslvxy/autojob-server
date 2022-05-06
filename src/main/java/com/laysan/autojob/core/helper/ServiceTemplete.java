package com.laysan.autojob.core.helper;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import com.alibaba.cola.exception.BizException;
import com.laysan.autojob.core.constants.AccountType;
import com.laysan.autojob.core.entity.Account;
import com.laysan.autojob.core.entity.TaskLog;
import com.laysan.autojob.core.utils.LogUtils;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;

import java.util.Date;

@Slf4j
public class ServiceTemplete {
    public static void execute(AccountType accountType, Account account, ServiceCallback callback) {
        TaskLog taskLog = new TaskLog();
        long start = System.currentTimeMillis();
        try {
            AutojobContextHolder.init();
            callback.checkTodayExecuted();
            OkHttpClient client = callback.initOkHttpClient();
            AutojobContextHolder.get().setClient(client);
            AutojobContextHolder.get().setAccount(account.getAccount());
            callback.prepare();
            callback.process();
            callback.updateAccount();
            taskLog.setSucceed(1);
        } catch (BizException e) {
            AutojobContextHolder.get().setDetailMessage(e.getMessage());
            LogUtils.error(log, accountType, account.getAccount(), e.getMessage());
            taskLog.setSucceed(0);
        } catch (Exception e) {
            AutojobContextHolder.get().setDetailMessage("定时任务执行失败");
            LogUtils.error(log, accountType, account.getAccount(), "定时任务执行失败");
            taskLog.setSucceed(0);
        } finally {
            long time = System.currentTimeMillis() - start;
            LogUtils.info(log, accountType, account.getAccount(), "定时任务执行完成.花费时间:{}s", time / 1000);
            taskLog.setUserId(account.getUserId());
            taskLog.setType(accountType.getCode());
            taskLog.setAccount(account.getAccount());
            taskLog.setAccountId(account.getId());
            taskLog.setTimeCosted(time / 1000);
            taskLog.setExecutedDay(DateUtil.format(new Date(), DatePattern.NORM_DATE_PATTERN));

            taskLog.setDetail(AutojobContextHolder.get().getDetailMessage());
            callback.saveTaskLog(taskLog);
            AutojobContextHolder.clear();
        }
    }
}
