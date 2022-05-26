package com.laysan.autojob.modules.cloud189;

import cn.hutool.core.util.StrUtil;
import lombok.Data;

/**
 * {"prizeId":"SIGNIN_CLOUD_50M","prizeName":"天翼云盘50M空间","prizeGrade":1,"prizeType":4,"description":"1","useDate":"2022-05-05 10:29:24","userId":499126215,"isUsed":1,"activityId":"ACT_SIGNIN","prizeStatus":1,"showPriority":1}
 */
@Data
public class Cloud189LotteryResult extends Cloud189Result {
    private String prizeId;
    private String prizeName;
    private String description;
    private String useDate;

    public String getPrizeName() {
        return prizeName;
    }

    public void setPrizeName(String prizeName) {
        if (StrUtil.isNotBlank(prizeName)) {
            this.prizeName = prizeName.replace("天翼云盘", "").replace("空间", "");
        } else {
            this.prizeName = prizeName;
        }
    }
}
