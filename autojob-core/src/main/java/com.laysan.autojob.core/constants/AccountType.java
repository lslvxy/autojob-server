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
    MODULE_EVERPHOTO("everphoto", "时光相册", "https://web.everphoto.cn/images/favicon.ico"),
    MODULE_CLOUD189("cloud189", "天翼云盘", "https://cloud.189.cn/logo.ico"),

    MODULE_PTT("ptt", "PTT", "https://www.pttime.org/pic/ptt-w.png");
    //MODULE_WPS("wps", "WPS", "https://cloud.189.cn/logo.ico"), MODULE_YUN139("yun139", "和彩云", "https://cloud.189.cn/logo.ico"),
    // MODULE_YOUDAO("youdao", "有道云", "https://cloud.189.cn/logo.ico");
;
    private String code;
    private String desc;
    private String icon;

    public static AccountType get(String code) {
        return Arrays.stream(AccountType.values()).filter(v -> v.code.equals(code)).findFirst().get();
    }
}


