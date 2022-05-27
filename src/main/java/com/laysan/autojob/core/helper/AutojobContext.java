package com.laysan.autojob.core.helper;

import cn.hutool.core.util.StrUtil;
import lombok.Data;
import okhttp3.OkHttpClient;

import java.util.Arrays;

@Data
public class AutojobContext {
    private String detailMessage;
    private String account;
    private OkHttpClient client;
    private Boolean checkInSuccess;
    private String decryptPassword;


    public void appendMessage(String message) {
        if (StrUtil.isBlank(detailMessage)) {
            detailMessage = message;
        }
        if (StrUtil.equals(detailMessage, message)) {
            return;
        }
        String[] split = detailMessage.split("#");
        if (!Arrays.asList(split).contains(message)) {
            detailMessage += "#" + message;
        }
    }

    public void replaceMessage(String oldMessage, String message) {
        detailMessage = StrUtil.replace(detailMessage, oldMessage, message);
    }

}
