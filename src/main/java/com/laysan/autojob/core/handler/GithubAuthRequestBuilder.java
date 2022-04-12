package com.laysan.autojob.core.handler;

import com.laysan.autojob.core.constants.AuthSourceEnums;
import com.laysan.autojob.core.entity.AuthKeyConfig;
import me.zhyd.oauth.config.AuthConfig;
import me.zhyd.oauth.request.AuthGithubRequest;
import me.zhyd.oauth.request.AuthRequest;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class GithubAuthRequestBuilder implements AuthRequestBuilder {
    @PostConstruct
    public void init() {
        AuthRequestBuilderRegister.register(AuthSourceEnums.GITHUB, this);
    }

    @Override
    public AuthRequest buildAuthRequest(AuthKeyConfig authKeyConfig) {
        return new AuthGithubRequest(AuthConfig.builder()
                .clientId(authKeyConfig.getClientId())
                .clientSecret(authKeyConfig.getClientSecret())
                .redirectUri(authKeyConfig.getRedirectUri())
                .build());
    }
}
