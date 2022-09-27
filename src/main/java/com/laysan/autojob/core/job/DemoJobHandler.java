package com.laysan.autojob.core.job;

import com.laysan.autojob.core.entity.User;
import com.laysan.autojob.core.service.UserService;
import com.xxl.job.core.handler.IJobHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DemoJobHandler extends IJobHandler {
    @Autowired
    UserService userService;

    @Override
    public void execute() throws Exception {
        User byId = userService.findById(1L);
        System.out.println(byId);
    }
}
