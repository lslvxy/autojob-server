package com.laysan.autojob.client.controller;

import com.alibaba.cola.dto.PageResponse;
import com.laysan.autojob.core.entity.TaskLog;
import com.laysan.autojob.core.service.TaskLogService;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@RestController
public class LogController extends BaseController {

    @Resource
    private TaskLogService taskLogService;

    @GetMapping("logs")
    @ResponseBody
    public PageResponse getLogs(int current, int pageSize, Long accountId, HttpServletRequest request) {
        Page<TaskLog> accountPage = taskLogService.findAll(accountId, getPageRequest(current, pageSize));
        return PageResponse.of(accountPage.getContent(), Math.toIntExact(accountPage.getTotalElements()), pageSize, current);
    }
}
