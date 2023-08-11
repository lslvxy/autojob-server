package com.laysan.autojob.service;

import com.laysan.autojob.core.helper.AutojobContext;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public interface JobRuner {
    Map<String, JobRuner> HANDLERS = new ConcurrentHashMap<>();

    void registry();

    void run(AutojobContext context);
}
