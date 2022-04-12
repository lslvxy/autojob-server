package com.laysan.autojob.core.controller;

import com.laysan.autojob.core.entity.Account;
import com.laysan.autojob.core.entity.TaskLog;
import com.laysan.autojob.core.repository.TaskLogRepository;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("logs")
public class LogController {

    @Resource
    private TaskLogRepository taskLogRepository;

    @PostMapping("list")
    @ResponseBody
    public List getLogs(@RequestBody Account dto) {
        TaskLog el = new TaskLog();
        el.setUserId(dto.getUserId());
        el.setType(dto.getType());
        Example<TaskLog> ex = Example.of(el);
        PageRequest page = PageRequest.of(0, 20, Sort.by(Direction.DESC, "gmtCreate"));
        return taskLogRepository.findAll(ex, page).toList();
    }
}
