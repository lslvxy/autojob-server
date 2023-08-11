package com.laysan.autojob.client.controller;

import cn.hutool.core.util.StrUtil;
import com.alibaba.cola.dto.MultiResponse;
import com.alibaba.cola.dto.Response;
import com.alibaba.cola.dto.SingleResponse;
import com.laysan.autojob.core.dto.TypeDTO;
import com.laysan.autojob.core.entity.Menu;
import com.laysan.autojob.core.entity.User;
import com.laysan.autojob.core.repository.MenuRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("user")
public class UserController extends BaseController {
    @Autowired
    private MenuRepository menuRepository;

    @GetMapping("menu")
    public MultiResponse<TypeDTO> menu(HttpServletRequest request) {
        List<Menu> values = menuRepository.findAll();
        List<TypeDTO> list = values.stream().map(at -> new TypeDTO(at.getType(), at.getName(), at.getIcon())).collect(
                Collectors.toList());
        return MultiResponse.of(list);
    }

    @GetMapping("me")
    public SingleResponse<User> info(HttpServletRequest request) {
        User userId = getLoginUser(request);
        return SingleResponse.of((userId));
    }

    @PostMapping("me")
    public Response save(@RequestBody User user, HttpServletRequest request) {
        User userDb = getLoginUser(request);
        if (StrUtil.isBlank(user.getSctKey())) {
            userDb.setSctKey(null);
        } else {
            userDb.setSctKey(user.getSctKey());
        }
        userService.save(userDb);
        return Response.buildSuccess();
    }
}
