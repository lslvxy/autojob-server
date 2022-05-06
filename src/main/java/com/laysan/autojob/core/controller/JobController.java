package com.laysan.autojob.core.controller;

import cn.hutool.core.util.NumberUtil;
import com.laysan.autojob.core.entity.Account;
import com.laysan.autojob.core.service.AccountService;
import com.laysan.autojob.core.utils.QuartzUtils;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Scheduler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@RestController
@Slf4j
public class JobController extends BaseController {

    @Resource
    private AccountService accountService;
    @Resource
    private Scheduler scheduler;

    @PostMapping("/job/{id}")
    public String run(@PathVariable("id") Long accountId, HttpServletRequest request) {
        Account account = accountService.findById(accountId);
        Long loginUserId = getLoginUserId(request);
        if (!NumberUtil.equals(account.getUserId(), loginUserId)) {
            return "您无权操作";
        }
        QuartzUtils.runOnce(scheduler, account, true);
        return "Success";
    }


}
