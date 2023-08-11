package com.laysan.autojob.core.helper;

import com.alibaba.cola.exception.BizException;
import com.laysan.autojob.core.entity.Account;
import com.laysan.autojob.core.utils.LogUtils;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;

@Slf4j
public class ServiceTemplate {
    public static void execute(AutojobContext context, ServiceCallback callback) {
        long start = System.currentTimeMillis();
        Account account = context.getAccount();
        try {
            String decryptPassword = callback.decryptPassword(account.getPassword());
            context.setDecryptPassword(decryptPassword);
            OkHttpClient client = callback.initOkHttpClient();
            context.setClient(client);
            context.setHttpClient(callback.initHttpClient());
            callback.doLogin();
            callback.doCheckIn();
            account.setTodayExecuted(1);
            context.setSucceed(true);
        } catch (BizException e) {
            context.appendMessage(e.getMessage());
            LogUtils.error(log, context.getAccountType(), account.getAccount(), e.getMessage());
            account.setTodayExecuted(0);
            account.setExtendInfo(null);
            context.setSucceed(false);
        } catch (Exception e) {
            context.appendMessage(e.getMessage());
            LogUtils.error(log, context.getAccountType(), account.getAccount(), "定时任务执行失败,{}", e);
            account.setTodayExecuted(0);
            account.setExtendInfo(null);
            context.setSucceed(false);
        } finally {
            long time = System.currentTimeMillis() - start;
            LogUtils.info(log, context.getAccountType(), account.getAccount(), "定时任务执行完成.花费时间:{}ms", time);
        }
    }
}
