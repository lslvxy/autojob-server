package com.laysan.autojob.client.controller;

import cn.hutool.core.util.StrUtil;
import com.laysan.autojob.core.entity.User;
import com.laysan.autojob.core.service.UserService;
import com.laysan.autojob.core.utils.AESUtil;
import java.util.Objects;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class BaseController {
    protected static final int         PAGE_SIZE = 20;
    @Resource
    protected              UserService userService;
    @Resource
    protected              AESUtil     aesUtil;

    protected Long getLoginUserId(HttpServletRequest request) {
        String openId = request.getHeader("token");
        if (StrUtil.isBlank(openId)) {
            throw new RuntimeException("用户未登录");
        }
        User user = userService.findByOpenId(openId);
        if (Objects.isNull(user)) {
            user = new User();
            user.setOpenId(openId);
            user.setTotalAccountCount(0);
            user.setTodayRunCount(0);
            user = userService.save(user);
        }
        return user.getId();
    }

    protected User getLoginUser(HttpServletRequest request) {
        String openId = request.getHeader("token");
        return userService.findByOpenId(openId);
    }

    protected String getOpenId(HttpServletRequest request) {
        return request.getHeader("token");
    }

    protected PageRequest getPageRequest(int page, int pageSize) {
        return PageRequest.of(page - 1, pageSize);
    }

    protected PageRequest getPageRequest(int page) {
        return PageRequest.of(page - 1, PAGE_SIZE);
    }
}
