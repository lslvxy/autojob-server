package com.laysan.autojob.modules.everphoto;

import lombok.Data;

@Data
public class UserProfile {
    public Long id;
    public String name;
    public String countryCode;
    public String avatarFid;
    public String mobile;
    public String mobileLocal;
    public Long usage;
    public String secretDigitEnc;
    public String secretDigitEncV2;
    public String secretDigitSignature;
    public Boolean weixinAuth;
    public Boolean qqAuth;
    public Boolean gioneeAuth;
    public Integer estimatedMediaNum;
    public Integer membership;
    public Integer gender;
    public String estimateGender;
    public Integer vipLevel;
    public String vipDesc;
    public String createdAt;
    public Integer daysFromCreated;
    public Integer trashShowDays;
    public Integer maxFileSize;
    public Double clusterThreshold;
    public String memberAdUrl;

}
