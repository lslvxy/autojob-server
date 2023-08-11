package com.laysan.autojob.modules.everphoto;

import lombok.Data;

/**
 *
 */
@Data
public class EverPhotoHttpResult<T> {
    public Integer timestamp;
    public Integer code;
    public String message;
    public T data;

    public boolean isSuccess() {
        return code == EverPhotoConstant.SUCCESS_CODE;
    }
}
