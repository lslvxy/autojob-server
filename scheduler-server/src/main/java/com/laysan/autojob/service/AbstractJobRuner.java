package com.laysan.autojob.service;

import com.laysan.autojob.core.helper.AutojobContext;
import com.laysan.autojob.core.service.AccountService;
import com.laysan.autojob.core.service.MessageService;
import com.laysan.autojob.core.service.TaskLogService;
import com.laysan.autojob.core.service.UserService;
import com.laysan.autojob.core.utils.AESUtil;
import javax.annotation.Resource;
import org.springframework.stereotype.Service;

@Service
public abstract class AbstractJobRuner implements JobRuner {
    @Resource
    protected AESUtil        aesUtil;
    @Resource
    protected TaskLogService taskLogService;
    @Resource
    protected MessageService messageService;
    @Resource
    protected AccountService accountService;
    @Resource
    protected UserService    userService;

    @Override
    public void run(AutojobContext context) {
        doRun(context);
    }

    public abstract void doRun(AutojobContext context);
}
