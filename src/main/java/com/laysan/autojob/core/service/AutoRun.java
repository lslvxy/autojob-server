package com.laysan.autojob.core.service;

import com.laysan.autojob.core.entity.Account;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public interface AutoRun {
    Map<String, AutoRun> HANDLERS = new ConcurrentHashMap<>();

    default void registry() {
    }


    boolean run(Account account, boolean forceRun) throws Exception;
}
