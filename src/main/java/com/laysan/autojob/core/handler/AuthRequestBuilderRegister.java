package com.laysan.autojob.core.handler;

import com.laysan.autojob.core.constants.AuthSourceEnums;
import com.laysan.autojob.core.entity.AuthKeyConfig;
import me.zhyd.oauth.request.AuthRequest;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class AuthRequestBuilderRegister {
    public static final Map<String, AuthRequestBuilder> builderMap = new ConcurrentHashMap<>(16);

    public static void register(AuthSourceEnums authSourceEnums, AuthRequestBuilder builder) {
        builderMap.put(authSourceEnums.getCode(), builder);
    }

    public static AuthRequest buildAuthRequest(AuthKeyConfig authKeyConfig) {
        return builderMap.get(authKeyConfig.getSource()).buildAuthRequest(authKeyConfig);
    }
}
