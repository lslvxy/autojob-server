package com.laysan.autojob.modules.everphoto;

import cn.hutool.core.util.ObjectUtil;
import lombok.Data;


@Data
public class CheckInResult {
    private boolean checkinResult;
    private Long continuity;
    private Long totalReward;
    private Long tomorrowReward;

    public String getTotalRewardStr() {
        if (ObjectUtil.isNotNull(totalReward)) {
            return totalReward / 1024 / 1024 + "MB";
        }
        return "0MB";
    }

    public String getTomorrowRewardStr() {
        if (ObjectUtil.isNotNull(tomorrowReward)) {
            return tomorrowReward / 1024 / 1024 + "MB";
        }
        return "0MB";
    }

    @Override
    public String toString() {
        return checkinResult ? "签到成功" : "已签到" +
                ", 连续签到" + continuity +
                "天, 累计获得" + getTotalRewardStr() +
                ", 明日可得" + getTomorrowRewardStr();
    }
}
