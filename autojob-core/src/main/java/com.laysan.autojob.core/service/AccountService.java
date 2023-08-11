package com.laysan.autojob.core.service;

import cn.hutool.core.date.DateUtil;
import com.laysan.autojob.core.entity.Account;
import com.laysan.autojob.core.repository.AccountRepository;
import com.laysan.autojob.core.utils.AESUtil;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import javax.annotation.Resource;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
public class AccountService {
    @Resource
    AccountRepository accountRepository;
    @Resource
    UserService       userService;
    @Resource
    AESUtil           aesUtil;

    public Account findById(Long id) {
        return accountRepository.findById(id).orElse(null);
    }

    public List<Account> findAll() {
        return accountRepository.findAll();
    }

    public Page<Account> findAccountPage(Long userId, PageRequest pageRequest) {
        Example<Account> ex = Example.of(new Account(userId));
        Page<Account> all = accountRepository.findAll(ex, pageRequest.withSort(Sort.by(Sort.Direction.ASC, "type", "todayExecuted")));
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

    public Account createNewAccount(Account account) {
        account.setPassword(aesUtil.encrypt(account.getPassword()));
        account.setTodayExecuted(-1);
        return accountRepository.save(account);
    }

    public Account save(Account account) {
        Account save = accountRepository.save(account);
        userService.updateTotalAccountCount(account.getUserId());
        return save;
    }

    public void deleteById(Long id) {
        Optional<Account> account = accountRepository.findById(id);
        account.ifPresent(v -> {
            accountRepository.delete(v);
            userService.updateTotalAccountCount(v.getUserId());
        });
    }

    public List<Account> findToRun() {
        Date now = new Date();
        String endTime = DateUtil.format(now, "HH:mm");

        PageRequest pageable = PageRequest.of(0, 20);
        return accountRepository.findToRun(-1, endTime, pageable);
    }
}
