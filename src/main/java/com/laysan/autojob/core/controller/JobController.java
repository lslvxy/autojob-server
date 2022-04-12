package com.laysan.autojob.core.controller;

import cn.hutool.core.util.NumberUtil;
import com.laysan.autojob.core.entity.Account;
import com.laysan.autojob.core.service.AccountService;
import com.laysan.autojob.core.utils.JobUtils;
import com.laysan.autojob.core.utils.QuartzUtils;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Scheduler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("job")
@Slf4j
public class JobController extends BaseController {

    @Resource
    private AccountService accountService;
    @Resource
    private Scheduler scheduler;

    @PostMapping("/run")
    public String run(Long accountId, HttpServletRequest request) {
        Account account = accountService.findById(accountId);
        Long loginUserId = getLoginUserId(request);
        if (!NumberUtil.equals(accountId, loginUserId)) {
            return "您无权操作";
        }
        QuartzUtils.runOnce(scheduler, account);
        return "Success";
    }


}
