package com.laysan.autojob.core.service;

import com.laysan.autojob.core.entity.Account;
import com.laysan.autojob.core.repository.AccountRepository;
import com.laysan.autojob.core.utils.AESUtil;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class AccountService {
    @Resource
    AccountRepository accountRepository;
    @Resource
    AESUtil aesUtil;

    public Account findById(Long id) {
        return accountRepository.findById(id).get();
    }

    public List<Account> findAll() {
        return accountRepository.findAll();
    }

    public Page<Account> findAccountPage(Long userId, PageRequest pageRequest) {
        Example<Account> ex = Example.of(new Account(userId));
        Page<Account> all = accountRepository.findAll(ex, pageRequest.withSort(Sort.by(Sort.Direction.DESC, "type")));
        all.getContent().forEach(account -> account.setPassword("******"));
        return all;
    }

    public long findAccountCountByType(Long userId, String type) {
        Example<Account> ex = Example.of(new Account(userId, type));
        return accountRepository.count(ex);
    }

    public boolean accountExistByType(Long userId, String account) {
        Account probe = new Account(userId);
        probe.setAccount(account);
        Example<Account> ex = Example.of(probe);
        return accountRepository.exists(ex);
    }

    public Account save(Account account) {
        account.setPassword(aesUtil.encrypt(account.getPassword()));
        return accountRepository.save(account);
    }

    public void deleteById(Long id) {
        accountRepository.deleteById(id);
    }
}
