package com.laysan.autojob.client.controller;

import cn.hutool.core.util.NumberUtil;
import com.alibaba.cola.dto.Response;
import com.laysan.autojob.core.constants.EventTypeEnums;
import com.laysan.autojob.core.entity.Account;
import com.laysan.autojob.core.entity.Event;
import com.laysan.autojob.core.repository.EventRepository;
import com.laysan.autojob.core.service.AccountService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@RestController
@Slf4j
public class JobController extends BaseController {

    @Resource
    private AccountService  accountService;
    @Resource
    private EventRepository eventRepository;

    @PostMapping("/job/{id}")
    public Response run(@PathVariable("id") Long accountId, HttpServletRequest request) {
        Account account = accountService.findById(accountId);
        Long loginUserId = getLoginUserId(request);
        if (!NumberUtil.equals(account.getUserId(), loginUserId)) {
            Response.buildFailure("500", "您无权操作");
        }
        Event event = new Event();
        event.setUserId(loginUserId);
        event.setAccountId(account.getId());
        event.setEventType(EventTypeEnums.RUN_JOB.name());
        eventRepository.save(event);
        return Response.buildSuccess();
    }

}
