package com.laysan.autojub.test;

import com.laysan.autojob.AutojobApplication;
import com.laysan.autojob.core.entity.Account;
import com.laysan.autojob.core.service.AccountService;
import com.laysan.autojob.modules.cloud189.Cloud189RunService;
import com.laysan.autojob.modules.everphoto.EverPhotoRunService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = AutojobApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SyncTests {

    @Autowired
    private EverPhotoRunService everPhotoRunService;
    @Autowired
    private Cloud189RunService cloud189RunService;
    @Autowired
    private AccountService accountService;

    @Test
    void comm() throws Exception {
        Account account = accountService.findById(1L);
        cloud189RunService.run(account);

    }

}
