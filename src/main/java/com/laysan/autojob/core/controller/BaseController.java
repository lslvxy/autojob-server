package com.laysan.autojob.core.controller;

import com.laysan.autojob.core.entity.User;
import com.laysan.autojob.core.service.UserService;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Objects;

@RestController
public class BaseController {
    protected static final int PAGE_SIZE = 20;
    @Resource
    private UserService userService;


    protected Long getLoginUserId(HttpServletRequest request) {
        String openId = request.getHeader("token");
        User user = userService.findByOpenId(openId);
        if (Objects.isNull(user)) {
            user = new User();
            user.setOpenId(openId);
            user = userService.save(user);
        }
        return user.getId();
    }

    protected PageRequest getPageRequest(int page, int pageSize) {
        return PageRequest.of(page - 1, pageSize);
    }

    protected PageRequest getPageRequest(int page) {
        return PageRequest.of(page - 1, PAGE_SIZE);
    }
}
