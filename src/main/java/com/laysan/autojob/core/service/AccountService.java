package com.laysan.autojob.core.service;

import com.laysan.autojob.core.entity.Account;
import com.laysan.autojob.core.repository.AccountRepository;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class AccountService {
    @Resource
    AccountRepository accountRepository;

    public Account findById(Long id) {
        return accountRepository.findById(id).get();
    }
}
