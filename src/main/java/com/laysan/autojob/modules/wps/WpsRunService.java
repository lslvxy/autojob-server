//package com.laysan.autojob.modules.wps;
//
//import com.laysan.autojob.core.constants.AccountType;
//import com.laysan.autojob.core.entity.Account;
//import com.laysan.autojob.core.entity.TaskLog;
//import com.laysan.autojob.core.repository.TaskLogRepository;
//import com.laysan.autojob.core.service.AutoRun;
//import com.laysan.autojob.core.service.MessageService;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//
//import javax.annotation.PostConstruct;
//
///**
// * @author lise
// * @version CloudAutoCheckInService.java, v 0.1 2020年11月27日 13:43 lise
// */
//@Service
//@Slf4j
//public class WpsRunService implements AutoRun {
//    private String phone = "abc";
//    @Autowired
//    private TaskLogRepository taskLogRepository;
//    @Autowired
//    private MessageService messageService;
//
//    @Override
//    @PostConstruct
//    public void registry() {
//        HANDLERS.put(AccountType.MODULE_WPS.getCode(), this);
//    }
//
//    @Override
//    public boolean run(Account account, boolean forceRun) {
//        TaskLog taskLog = new TaskLog();
//        taskLog.setUserId(account.getUserId());
//        taskLog.setType(AccountType.MODULE_WPS.getCode());
//        phone = account.getAccount();
//        String detail;
//        try {
////            FcUtils.createFunction(account);
////            ThreadUtil.sleep(500);
////            detail = FcUtils.invokeFunction(account);
////            log.info(detail);
////            ThreadUtil.sleep(200);
////            FcUtils.deleteFunction(account);
//        } catch (Exception e) {
//            detail = "签到失败";
//        }
//
////        eventLog.setDetail(detail);
//        taskLogRepository.save(taskLog);
////        LogUtils.info(log, AccountType.MODULE_WPS.getCode(), phone, detail);
////        messageService.sendMessage(account.getUserId(), "WPS签到", detail);
//        return true;
//
//    }
//
//}