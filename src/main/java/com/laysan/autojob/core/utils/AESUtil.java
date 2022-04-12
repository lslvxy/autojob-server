
package com.laysan.autojob.core.utils;

import cn.hutool.core.codec.Base64;
import cn.hutool.crypto.SecureUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @author lise
 * @version AESUtil.java, v 0.1 2020年11月27日 17:41 lise
 */
@Component
public class AESUtil {
    @Value("${autojob.aes_key}")
    public String key;

    /**
     * AES
     *
     * @param message
     * @return
     */
    public String encrypt(String message) {
        byte[] keyByte = Base64.decode(this.key);
        return SecureUtil.aes(keyByte).encryptBase64(message);
    }

    /**
     * AES
     *
     * @return
     */
    public String decrypt(String encrypted) {
        byte[] keyByte = Base64.decode(this.key);
        byte[] decode = Base64.decode(encrypted);
        return SecureUtil.aes(keyByte).decryptStr(decode);
    }


}