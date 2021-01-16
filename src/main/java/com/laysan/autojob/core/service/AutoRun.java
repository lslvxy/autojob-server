package com.laysan.autojob.core.service;

import com.laysan.autojob.core.entity.Account;

public interface AutoRun {
    default void registry() {}

    void run(Account account);
}
