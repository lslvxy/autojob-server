
package com.laysan.autojob.core.utils;

import org.apache.commons.codec.binary.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

/**
 * @author lise
 * @version AESUtil.java, v 0.1 2020年11月27日 17:41 lise
 */
public class AESUtil {
    public static String key = System.getenv("autojob_aes_key");

    /**
     * AES
     *
     * @param message
     * @return
     * @throws Exception
     */
    public static String encrypt(String message) throws Exception {
        SecretKeySpec skeySpec = new SecretKeySpec(key.getBytes(), "AES");

        // Instantiate the cipher
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5PADDING");
        cipher.init(Cipher.ENCRYPT_MODE, skeySpec);

        byte[] encrypted = cipher.doFinal(message.getBytes());
        return Base64.encodeBase64String(encrypted);
    }

    /**
     * AES
     *
     * @return
     * @throws Exception
     */
    public static String decrypt(String encrypted) throws Exception {
        SecretKeySpec skeySpec = new SecretKeySpec(key.getBytes(), "AES");

        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5PADDING");
        cipher.init(Cipher.DECRYPT_MODE, skeySpec);
        byte[] original = cipher.doFinal(Base64.decodeBase64(encrypted));
        return new String(original);
    }

}