package com.laysan.autojub.test;

import com.laysan.autojob.AutojobApplication;
import com.laysan.autojob.core.service.AccountService;
import com.laysan.autojob.core.service.MessageService;
import com.laysan.autojob.core.utils.AESUtil;
import com.laysan.autojob.modules.cloud189.Cloud189RunService;
import com.laysan.autojob.modules.everphoto.EverPhotoRunService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(classes = AutojobApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("local")
class SyncTests {

    @Autowired
    private EverPhotoRunService everPhotoRunService;
    @Autowired
    private Cloud189RunService cloud189RunService;
    @Autowired
    private AccountService accountService;
    @Autowired
    private MessageService messageService;
    @Autowired
    private AESUtil aesUtil;

    @Test
    void comm() throws Exception {
        //messageService.sendMessage("111111", "xxx", "ooo");
        System.out.println(aesUtil.decrypt("nOtu8RwWB8ECpBeWlCtT3geEBIfNkKtVeQoQxFVcOfj6ywM2vutgH8SZB431xapxipGv4g2ERuxSrUeLwmNvswH3mn9t/aARJPwPUrSLxQNeaUS0g1eAktnu351SjcRZspNc6jSF2GVMY1navDjmXyzOKm/rfDghbOLxL5/nN38="));
//        Account account = accountService.findById(1L);
//        cloud189RunService.run(account, false);

    }

}
