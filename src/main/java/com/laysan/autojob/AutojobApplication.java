package com.laysan.autojob;

import cn.hutool.core.codec.Base64;
import cn.hutool.crypto.SecureUtil;
import com.laysan.autojob.core.entity.Account;
import com.laysan.autojob.core.service.AccountService;
import com.laysan.autojob.core.utils.QuartzUtils;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Scheduler;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;

import javax.annotation.Resource;
import java.io.FileOutputStream;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.List;

@SpringBootApplication
@EnableJpaAuditing
@Slf4j
@EnableAsync
@EnableCaching
public class AutojobApplication implements CommandLineRunner {

    public static void main(String[] args) {
        if (args.length > 0 && args[0].equals("generateKey")) {
            try {
                KeyPair secretKey = SecureUtil.generateKeyPair("RSA");
                PublicKey aPublic = secretKey.getPublic();
                PrivateKey aPrivate = secretKey.getPrivate();
                byte[] aPublicEncoded = aPublic.getEncoded();
                String publicKeyStr = Base64.encodeStr(aPublicEncoded, false, false);
                try (FileOutputStream fos = new FileOutputStream("./publicKey.txt")) {
                    fos.write(publicKeyStr.getBytes());
                    fos.flush();
                }
                byte[] aPrivateEncoded = aPrivate.getEncoded();
                String privateKeyStr = Base64.encodeStr(aPrivateEncoded, false, false);
                try (FileOutputStream fos2 = new FileOutputStream("./privateKey.txt")) {
                    fos2.write(privateKeyStr.getBytes());
                    fos2.flush();
                }
            } catch (Exception ignored) {
            }
        } else {
            SpringApplication.run(AutojobApplication.class, args);
        }
    }

    @Resource
    private Scheduler scheduler;
    @Resource
    private AccountService accountService;

    @Override
    public void run(String... args) {
        List<Account> accountList = accountService.findAll();
        accountList.forEach(account -> {
            try {
                QuartzUtils.createScheduleJob(scheduler, account);
            } catch (Exception e) {
                QuartzUtils.updateScheduleJob(scheduler, account);
            }
        });

    }
}
