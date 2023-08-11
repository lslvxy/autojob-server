package com.laysan.autojob.core.helper;

import okhttp3.OkHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;

public interface ServiceCallback<T> {

    OkHttpClient initOkHttpClient();

    void doLogin();

    void doCheckIn();

    String decryptPassword(String password);

    CloseableHttpClient initHttpClient();
}
