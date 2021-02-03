
package com.laysan.autojob.core.utils;

import org.slf4j.Logger;

/**
 * @author lise
 * @version LogUtil.java, v 0.1 2020年11月30日 18:31 lise
 */
public class LogUtils {
    public static void info(Logger log, String module, String account, String detail) {
        log.info("[{}]-[{}]-{}", account, module, detail);
    }

}