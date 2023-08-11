package com.laysan.autojob;

import cn.hutool.core.codec.Base64;
import cn.hutool.crypto.SecureUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import java.io.FileOutputStream;
import java.nio.charset.Charset;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.cookie.Cookie;
import org.apache.hc.client5.http.impl.cookie.BasicClientCookie;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableJpaAuditing
@Slf4j
@EnableAsync
@EnableCaching
@EnableScheduling
public class AutoJobRunApplication {

    public static void main(String[] args) {
        if (args.length > 0 && args[0].equals("generateKey")) {
            try {
                KeyPair secretKey = SecureUtil.generateKeyPair("RSA");
                PublicKey aPublic = secretKey.getPublic();
                PrivateKey aPrivate = secretKey.getPrivate();
                byte[] aPublicEncoded = aPublic.getEncoded();
                String publicKeyStr = Base64.encodeStr(aPublicEncoded, false, false);
                try (FileOutputStream fos = new FileOutputStream("./publicKey.txt")) {
                    fos.write(publicKeyStr.getBytes(Charset.defaultCharset()));
                    fos.flush();
                }
                byte[] aPrivateEncoded = aPrivate.getEncoded();
                String privateKeyStr = Base64.encodeStr(aPrivateEncoded, false, false);
                try (FileOutputStream fos2 = new FileOutputStream("./privateKey.txt")) {
                    fos2.write(privateKeyStr.getBytes(Charset.defaultCharset()));
                    fos2.flush();
                }
            } catch (Exception ignored) {
            }
        } else {
            SpringApplication.run(AutoJobRunApplication.class, args);
        }
    }

}
