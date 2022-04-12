package com.laysan.autojob.modules.everphoto;

import lombok.Data;


@Data
public class LoginResult {
    public String token;
    public UserProfile userProfile;
}
