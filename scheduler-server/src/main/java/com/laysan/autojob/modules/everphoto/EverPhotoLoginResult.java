package com.laysan.autojob.modules.everphoto;

import lombok.Data;


@Data
public class EverPhotoLoginResult {
    public String token;
    public EverPhotoUserProfile userProfile;
}
