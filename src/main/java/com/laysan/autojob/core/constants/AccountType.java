package com.laysan.autojob.core.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

@Getter
@AllArgsConstructor
public enum AccountType {
    /**
     * 时光相册
     */
    MODULE_EVERPHOTO("everphoto", "时光相册", false),
    MODULE_CLOUD189("cloud189", "天翼云盘", true),
    MODULE_WPS("wps", "WPS", true),
    MODULE_YUN139("yun139", "和彩云", false),
    MODULE_YOUDAO("youdao", "有道云", false);

    private String code;
    private String desc;
    private boolean cloudFunction;

    public static AccountType get(String code) {
        return Arrays.stream(AccountType.values()).filter(v -> v.code.equals(code)).findFirst().get();
    }
}
