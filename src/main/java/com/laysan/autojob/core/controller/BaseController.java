package com.laysan.autojob.core.controller;

import javax.servlet.http.HttpServletRequest;

public class BaseController {

    protected Long getLoginUserId(HttpServletRequest request) {
        return 1L;
    }

}
