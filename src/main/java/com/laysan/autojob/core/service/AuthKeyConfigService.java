package com.laysan.autojob.core.service;

import com.laysan.autojob.core.entity.AuthKeyConfig;
import com.laysan.autojob.core.repository.AuthKeyConfigRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class AuthKeyConfigService {
    @Resource
    private AuthKeyConfigRepository authConfigRepository;

    @Cacheable(cacheNames = "authKeyConfig", key = "#id", condition = "#id > 0")
    public AuthKeyConfig findBySource(String source) {
        return authConfigRepository.findBySource(source);
    }
}
