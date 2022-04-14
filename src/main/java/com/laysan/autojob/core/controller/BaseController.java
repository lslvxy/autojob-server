package com.laysan.autojob.core.controller;

import org.springframework.data.domain.PageRequest;

import javax.servlet.http.HttpServletRequest;

public class BaseController {
    protected static final int PAGE_SIZE = 20;

    protected Long getLoginUserId(HttpServletRequest request) {
        return 1L;
    }

    protected PageRequest getPageRequest(int page, int pageSize) {
        return PageRequest.of(page - 1, pageSize);
    }

    protected PageRequest getPageRequest(int page) {
        return PageRequest.of(page - 1, PAGE_SIZE);
    }
}
