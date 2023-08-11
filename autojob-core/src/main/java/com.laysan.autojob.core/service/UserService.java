package com.laysan.autojob.core.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import com.laysan.autojob.core.entity.Account;
import com.laysan.autojob.core.entity.User;
import com.laysan.autojob.core.repository.AccountRepository;
import com.laysan.autojob.core.repository.UserRepository;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    @Resource
    private UserRepository userRepository;
    @Resource
    private AccountRepository accountRepository;

    public User findById(Long id) {
        return userRepository.findById(id).orElse(null);
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public User findByOpenId(String openId) {
        return userRepository.findByOpenId(openId);
    }

    public User save(User user) {
        User userFromDb = userRepository.findByOpenId(user.getOpenId());
        if (ObjectUtil.isNotNull(userFromDb)) {
            BeanUtil.copyProperties(user, userFromDb, "id");
        } else {
            userFromDb = user;
        }
        user = userRepository.save(userFromDb);

        return user;
    }

    public void updateTodayRunCount(Long userId) {
        Optional<User> optional = userRepository.findById(userId);
        optional.ifPresent(user -> {
            user.setTodayRunCount(Math.addExact(user.getTodayRunCount(), 1));
            userRepository.save(user);
        });
    }

    public void updateTotalAccountCount(Long userId) {
        Optional<User> optional = userRepository.findById(userId);
        optional.ifPresent(user -> {
            List<Account> accountList = accountRepository.findByUserId(user.getId());
            user.setTotalAccountCount(accountList.size());
            userRepository.save(user);
        });
    }

}
