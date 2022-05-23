package com.laysan.autojob.core.helper;

import com.laysan.autojob.core.entity.Account;
import com.laysan.autojob.core.entity.TaskLog;
import com.laysan.autojob.core.service.AccountService;
import com.laysan.autojob.core.service.MessageService;
import com.laysan.autojob.core.service.TaskLogService;
import com.laysan.autojob.core.service.UserService;
import com.laysan.autojob.core.utils.AESUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ServiceTemplateService {
    @Autowired
    private AESUtil aesUtil;
    @Autowired
    TaskLogService taskLogService;
    @Autowired
    private MessageService messageService;
    @Autowired
    private AccountService accountService;
    @Autowired
    private UserService userService;

    public void saveTaskLog(TaskLog taskLog) {
        taskLogService.save(taskLog);
    }

    public void updateAccount(Account account) {
        accountService.save(account);
    }

    public void updateTodayRunCount(Long userId) {
        userService.updateTodayRunCount(userId);
    }

    public void sendNotifyMsg(Long userId) {
        messageService.sendMessage(userId);
    }

    public String decryptPassword(String password) {
        return aesUtil.decrypt(password);
    }
}
