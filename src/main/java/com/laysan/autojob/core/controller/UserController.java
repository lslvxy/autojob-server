package com.laysan.autojob.core.controller;

import com.alibaba.cola.dto.MultiResponse;
import com.alibaba.cola.dto.SingleResponse;
import com.laysan.autojob.core.constants.AccountType;
import com.laysan.autojob.core.dto.TypeDTO;
import com.laysan.autojob.core.entity.User;
import com.laysan.autojob.core.service.UserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("user")
public class UserController extends BaseController {
    @Resource
    private UserService userService;

    @GetMapping("menu")
    public MultiResponse<TypeDTO> menu(HttpServletRequest request) {
        AccountType[] values = AccountType.values();
        List<TypeDTO> list = Arrays.stream(values).map(at -> new TypeDTO(at.getCode(), at.getDesc(), at.getIcon())).collect(Collectors.toList());
        return MultiResponse.of(list);
    }

    @GetMapping("info")
    public SingleResponse<User> info(HttpServletRequest request) {
        Long userId = getLoginUserId(request);
        return SingleResponse.of(userService.findById(userId));
    }

}
