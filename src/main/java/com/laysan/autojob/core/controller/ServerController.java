package com.laysan.autojob.core.controller;

import com.laysan.autojob.core.entity.Server;
import com.laysan.autojob.core.repository.ServerRepository;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Objects;

@RestController
@RequestMapping("server")
public class ServerController {

    @Resource
    private ServerRepository serverRepository;

    @PostMapping("create")
    @ResponseBody
    public String create(@RequestBody Server dto) {
        Server byUserId = serverRepository.findByUserId(dto.getUserId());
        if (!Objects.isNull(byUserId)) {
            byUserId.setSckey(dto.getSckey());
            serverRepository.save(byUserId);
        } else {
            serverRepository.save(dto);
        }
        return "Success";
    }

    @PostMapping("get")
    @ResponseBody
    public Server get(@RequestBody Server dto) {
        return serverRepository.findByUserId(dto.getUserId());
    }
}
