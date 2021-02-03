package com.laysan.autojob.modules.everphoto;

import lombok.Data;

@Data
public class Result {
    //{'timestamp': 1604250005, 'code': 0, 'data': {'checkin_result': True, 'continuity': 195, 'reward': 52428800, 'promtp': '', 'total_reward': 56257150976, 'tomorrow_reward': 52428800, 'cache_time': 0, 'checkin_push': 0}}
//
    private String checkin_result;
    private String continuity;
    private String reward;
    private String promtp;
    private Long total_reward;
    private Long tomorrow_reward;
    private String cache_time;
    private String checkin_push;

}
