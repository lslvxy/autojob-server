package com.laysan.autojob.core.utils;

import com.laysan.autojob.core.entity.Account;
import org.springframework.util.StringUtils;

import java.time.LocalTime;
import java.util.StringJoiner;

public class JobUtils {

    public static String buildJobName(Account account) {
        StringJoiner stringJoiner = new StringJoiner(".", "", "");
        stringJoiner.add("job").add(account.getType()).add(account.getAccount()).add(account.getId().toString());
        return stringJoiner.toString();
    }

    public static String buildCron(Account account) {
        String time = account.getTime();
        if (StringUtils.isEmpty(time)) {
            return "0 0 0 * * ?";
        }
        LocalTime parse = LocalTime.parse(time);
        StringJoiner stringJoiner = new StringJoiner(" ", "", "");
        stringJoiner.add("0").add(parse.getMinute() + "").add(parse.getHour() + "").add("*").add("*").add("?");
        return stringJoiner.toString();
    }

}
