package com.laysan.autojob.core.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import com.laysan.autojob.core.entity.User;
import com.laysan.autojob.core.repository.UserRepository;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class UserService {
    @Resource
    private UserRepository userRepository;

    public User findById(Long id) {
        return userRepository.findById(id).orElse(null);
    }

    public User findByAccessToken(String accessToken) {
        return userRepository.findByAccessToken(accessToken);
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
}
