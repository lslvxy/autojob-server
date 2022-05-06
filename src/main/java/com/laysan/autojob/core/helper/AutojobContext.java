package com.laysan.autojob.core.helper;

import cn.hutool.core.util.StrUtil;
import lombok.Data;
import okhttp3.OkHttpClient;

@Data
public class AutojobContext {
    private String detailMessage;
    private String account;
    private OkHttpClient client;
    private Boolean checkInSuccess;


    public void appendMessage(String message) {
        if (StrUtil.isBlank(detailMessage)) {
            detailMessage = message;
        }
        if (!StrUtil.contains(detailMessage, message)) {
            detailMessage += ", " + message;
        }
    }
}