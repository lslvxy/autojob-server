package com.laysan.autojob.core.helper;

import cn.hutool.core.util.StrUtil;
import com.laysan.autojob.core.constants.AccountType;
import com.laysan.autojob.core.entity.Account;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import lombok.Data;
import lombok.ToString;
import okhttp3.OkHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;

@Data
@ToString(exclude = "decryptPassword")
public class AutojobContext {
    private String              detailMessage = "";
    private Account             account;
    private AccountType         accountType;
    private OkHttpClient        client;
    private CloseableHttpClient httpClient;
    private Boolean             succeed;
    private String              decryptPassword;
    private Date                executeTime;
    Map<String, String> extraMap = new HashMap<>();

    public void setExtraMap(Map<String, String> extraMap) {
        this.extraMap.putAll(extraMap);
    }

    public void appendMessage(String message) {
        if (StrUtil.isBlank(detailMessage)) {
            detailMessage = message;
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
