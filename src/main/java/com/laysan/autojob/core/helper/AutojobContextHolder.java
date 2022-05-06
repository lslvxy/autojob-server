package com.laysan.autojob.core.helper;

public final class AutojobContextHolder {

    private static final ThreadLocal<AutojobContext> AUTOJOB_CONTEXT_THREAD_LOCAL = new ThreadLocal<AutojobContext>();

    private AutojobContextHolder() {
    }

    public static AutojobContext get() {
        return AUTOJOB_CONTEXT_THREAD_LOCAL.get();
    }

    public static void init() {
        AUTOJOB_CONTEXT_THREAD_LOCAL.set(new AutojobContext());
    }

    public static void set(AutojobContext autojobContext) {
        AUTOJOB_CONTEXT_THREAD_LOCAL.set(autojobContext);
    }

    public static void clear() {
        AUTOJOB_CONTEXT_THREAD_LOCAL.set(null);
        AUTOJOB_CONTEXT_THREAD_LOCAL.remove();
    }
}
