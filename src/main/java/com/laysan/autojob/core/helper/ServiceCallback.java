package com.laysan.autojob.core.helper;

import okhttp3.OkHttpClient;

public interface ServiceCallback<T> {

    OkHttpClient initOkHttpClient();

    void prepare();

    void process();

}
