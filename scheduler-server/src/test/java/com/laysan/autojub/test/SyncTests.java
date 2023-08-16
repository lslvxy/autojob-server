package com.laysan.autojub.test;

import com.laysan.autojob.AutoJobRunApplication;
import com.laysan.autojob.core.constants.AccountType;
import com.laysan.autojob.core.entity.Account;
import com.laysan.autojob.core.service.AccountService;
import com.laysan.autojob.core.service.MessageService;
import com.laysan.autojob.core.utils.AESUtil;
import com.laysan.autojob.modules.cloud189.Cloud189RunService;
import com.laysan.autojob.modules.everphoto.EverPhotoRunService;
import com.laysan.autojob.modules.ptt.PttRunService;
import com.laysan.autojob.service.AutoRunService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(classes = AutoJobRunApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
//@ActiveProfiles("mysql")
class SyncTests {

    @Autowired
    private AccountService      accountService;
    @Autowired
    private MessageService      messageService;
    @Autowired
    private AESUtil        aesUtil;
    @Autowired
    private AutoRunService autoRunService;

    @Test
    void comm() throws Exception {
        //messageService.sendMessage("111111", "xxx", "ooo");
        Account account = new Account();
        String encrypt = aesUtil.encrypt("1122");
        account.setAccount("22");
        account.setPassword(encrypt);
        account.setTime("00:00");
        account.setType(AccountType.MODULE_CLOUD189.getCode());
        account.setTodayExecuted(-1);
        account.setUserId(1L);
        account.setId(1L);
        //account = accountService.findById(1L);
        autoRunService.run(account, true);

    }

}
