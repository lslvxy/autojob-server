package com.laysan.autojob.core.helper;

import com.laysan.autojob.core.entity.TaskLog;
import okhttp3.OkHttpClient;

public interface ServiceCallback<T> {
    void checkTodayExecuted();

    OkHttpClient initOkHttpClient();

    void prepare();

    void process();

    void saveTaskLog(TaskLog taskLog);

    void updateAccount();
}
