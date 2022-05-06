package com.laysan.autojob.modules.cloud189;

import lombok.Data;

/**
 * {"userSignId":null,"userId":11111,"signTime":"2022-05-05T02:29:23.679+00:00","netdiskBonus":29,"isSign":false}
 */
@Data
public class Cloud189CheckInResult extends Cloud189Result {
    private String userSignId;
    private String userId;
    private String signTime;
    private String netdiskBonus;
    private Boolean isSign;
}
