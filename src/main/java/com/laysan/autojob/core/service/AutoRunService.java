package com.laysan.autojob.core.service;

import com.laysan.autojob.core.entity.Account;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;

@Service
public class AutoRunService implements AutoRun {
    public static ConcurrentHashMap<String, AutoRun> handlers = new ConcurrentHashMap<>();


    @Override
    public void run(Account account) {
        handlers.get(account.getType()).run(account);
    }
}
