package com.laysan.autojob.core.handler;

import com.laysan.autojob.core.entity.AuthKeyConfig;
import me.zhyd.oauth.request.AuthRequest;

public interface AuthRequestBuilder {
    AuthRequest buildAuthRequest(AuthKeyConfig authKeyConfig);
}
