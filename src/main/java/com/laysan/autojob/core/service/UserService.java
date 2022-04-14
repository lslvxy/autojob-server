package com.laysan.autojob.core.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import com.laysan.autojob.core.entity.User;
import com.laysan.autojob.core.entity.UserOauth;
import com.laysan.autojob.core.repository.UserOauthRepository;
import com.laysan.autojob.core.repository.UserRepository;
import me.zhyd.oauth.model.AuthUser;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class UserService {
    @Resource
    private UserRepository userRepository;
    @Resource
    private UserOauthRepository userOauthRepository;

    public User findById(Long id) {
        return userRepository.findById(id).orElse(null);
    }

    public User findByAccessToken(String accessToken) {
        return userRepository.findByAccessToken(accessToken);
    }

    public User save(AuthUser authUser) {
        User user = User.fromAuthUser(authUser);
        User userFromDb = userRepository.findByUuidAndSource(user.getUuid(), user.getSource());
        if (ObjectUtil.isNotNull(userFromDb)) {
            BeanUtil.copyProperties(user, userFromDb, "id");
        } else {
            userFromDb = user;
        }
        user = userRepository.save(userFromDb);

        UserOauth userOauth = UserOauth.fromAuthUser(authUser);
        UserOauth userOauthFromDb = userOauthRepository.findByUserId(user.getId());

        if (ObjectUtil.isNotNull(userOauthFromDb)) {
            BeanUtil.copyProperties(userOauth, userOauthFromDb, "id");
        } else {
            userOauthFromDb = userOauth;
        }
        userOauthFromDb.setUserId(user.getId());
        userOauthRepository.save(userOauthFromDb);
        return user;
    }
}
