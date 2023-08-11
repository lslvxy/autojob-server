package com.laysan.autojob.modules.cloud189;

import lombok.Data;

/**
 * {"data":{"accountType":"01","agreementCheck":"uncheck","appKey":"cloud","clientType":1,"defaultSaveName":"1",
 * "defaultSaveNameCheck":"uncheck","isOauth2":false,"loginSort":"Qr|Pw|Sms","mailSuffix":"@189.cn","pageKey":"normal",
 * "paramId":"34E6343122ACC83CD2F156C9B74F326392DCDFBEEF8023CCC1077B3B785CAB1E5A8A70C211EAD9BC","regReturnUrl":"https%253A%252F%252Fcloud
 * .189.cn%252Fweb","reqId":"493ccd6afded4c489adc3d4959ae19c6","returnUrl":"https://cloud.189.cn/api/portal/callbackUnify
 * .action?redirectURL=https%3A%2F%2Fcloud.189.cn%2Fweb%2Fredirect.html%3FreturnURL%3D%2Fmain.action","showFeedback":"true",
 * "showPwSaveName":"true","showQrSaveName":"true","showSmsSaveName":"true","sso":"yes"},"msg":"成功","result":"0"}
 */
@Data
public class AppConfigResponse {
    private Detail data;
    private String result;
    private String msg;

    @Data
    class Detail {
        private String accountType;
        private String returnUrl;
        private String paramId;

    }
}

