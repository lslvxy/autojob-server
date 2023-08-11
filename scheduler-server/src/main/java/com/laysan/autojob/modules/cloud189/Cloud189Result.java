package com.laysan.autojob.modules.cloud189;

import cn.hutool.core.util.StrUtil;
import lombok.Data;

import java.util.Objects;

@Data
public class Cloud189Result {
    private String errorCode;
    private String errorMsg;

    public boolean timeout() {
        return Objects.equals(errorCode, "TimeOut");
    }

    public boolean userNotChance() {
        return Objects.equals(errorCode, "User_Not_Chance");
    }

    public boolean isError() {
        return StrUtil.isNotBlank(errorCode) && !userNotChance();
    }
}
