package com.laysan.autojob.core.utils;

import cn.hutool.core.codec.Base64;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.asymmetric.KeyType;
import cn.hutool.crypto.asymmetric.RSA;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * @author lise
 * @version AESUtil.java, v 0.1 2020年11月27日 17:41 lise
 */
@Component
@Slf4j
public class AESUtil {
    @Value("${autojob.rsa.public_key}")
    private String publicKeyBase64 = "";
    @Value("${autojob.rsa.private_key}")
    private String privateKeyBase64 = "";
    private RSA rsa;

    @PostConstruct
    public void init() {
        byte[] publicKey = Base64.decode(publicKeyBase64);
        byte[] privateKey = Base64.decode(privateKeyBase64);
        rsa = SecureUtil.rsa(privateKey, publicKey);
    }

    /**
     * AES
     *
     * @param message
     * @return
     */
    public String encrypt(String message) {
        try {
            return rsa.encryptBase64(message, KeyType.PublicKey);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * AES
     *
     * @return
     */
    public String decrypt(String encrypted) {
        try {
            return rsa.decryptStr(encrypted, KeyType.PrivateKey);
        } catch (Exception e) {
            return null;
        }
    }
}