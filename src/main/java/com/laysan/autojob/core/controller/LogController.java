package com.laysan.autojob.core.controller;

import com.laysan.autojob.core.entity.Account;
import com.laysan.autojob.core.entity.EventLog;
import com.laysan.autojob.core.repository.EventLogRepository;
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
    private EventLogRepository eventLogRepository;

    @PostMapping("list")
    @ResponseBody
    public List getLogs(@RequestBody Account dto) {
        EventLog el = new EventLog();
        el.setUserId(dto.getUserId());
        el.setType(dto.getType());
        Example<EventLog> ex = Example.of(el);
        PageRequest page = PageRequest.of(0, 20, Sort.by(Direction.DESC, "gmtCreate"));
        return eventLogRepository.findAll(ex, page).toList();
    }
}
