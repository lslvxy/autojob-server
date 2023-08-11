package com.laysan.autojob.core.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

@Getter
@AllArgsConstructor
public enum AuthSourceEnums {
    GITHUB("github", "github"),
    WECHAT("wechat", "微信"),
    OSCHINA("oschina", "开源中国");

    private String code;
    private String desc;

    public static AccountType get(String code) {
        return Arrays.stream(AccountType.values()).filter(v -> v.getCode().equals(code)).findFirst().get();
    }
}
