package com.laysan.autojob.core.controller;

import com.alibaba.cola.dto.MultiResponse;
import com.alibaba.cola.dto.SingleResponse;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.laysan.autojob.core.service.UserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("user")
public class UserController extends BaseController {
    @Resource
    private UserService userService;

    @GetMapping("menu")
    public MultiResponse menu(HttpServletRequest request) {
        StringBuilder sb = new StringBuilder();
        sb.append("[")
                .append("{'name':'task','parentId':0,'id':1,'meta':{'icon':'dashboard','title':'我的任务','show':true},'component':'Workplace'}")
                .append(",{'name':'log','parentId':0,'id':2,'meta':{'icon':'dashboard','title':'任务日志','show':true},'component':'Workplace'}")
                .append("]")
        ;
        JSONArray menus = JSON.parseArray(sb.toString());
        return MultiResponse.of(menus);
    }

    @GetMapping("info")
    public SingleResponse info(HttpServletRequest request) {
        Long userId = getLoginUserId(request);
        return SingleResponse.of(userService.findById(userId));
    }
}
