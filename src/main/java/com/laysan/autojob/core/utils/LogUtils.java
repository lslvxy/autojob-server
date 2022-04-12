package com.laysan.autojob.core.utils;

import com.laysan.autojob.core.constants.AccountType;
import com.laysan.autojob.core.entity.Account;
import org.slf4j.Logger;

/**
 * @author lise
 * @version LogUtil.java, v 0.1 2020年11月30日 18:31 lise
 */

public class LogUtils {
    public static void debug(Logger log, AccountType accountType, String account, String format, Object... args) {
        log.debug(getFormat(format), accountType.getCode(), account, "", args);
    }

    public static void debug(Logger log, AccountType accountType, Account account, String format, Object... args) {
        log.debug(getFormat(format), accountType.getCode(), account.getId(), account.getAccount(), args);
    }

    public static void info(Logger log, AccountType accountType, String account, String format, Object... args) {
        log.info(getFormat(format), accountType.getCode(), account, "", args);
    }

    public static void info(Logger log, AccountType accountType, Account account, String format, Object... args) {
        log.info(getFormat(format), accountType.getCode(), account.getId(), account.getAccount(), args);
    }

    public static void error(Logger log, AccountType accountType, String account, String format, Object... args) {
        log.info(getFormat(format), accountType.getCode(), account, "", args);
    }

    public static void error(Logger log, AccountType accountType, Account account, String format, Object... args) {
        log.info(getFormat(format), accountType.getCode(), account.getId(), account.getAccount(), args);
    }

    public static void error(Logger log, String module, Account account, String format, Object... args) {
        log.info(getFormat(format), module, account.getId(), account.getAccount(), args);
    }

    public static void error(Logger log, String module, String account, String format, Object... args) {
        log.info(getFormat(format), module, account, "", args);
    }

    private static String getFormat(String format) {
        return "[{}]-[{}.{}]-" + format;
    }
}