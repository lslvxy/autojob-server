package com.laysan.autojob.modules.youdao;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.laysan.autojob.core.constants.AccountType;
import com.laysan.autojob.core.entity.Account;
import com.laysan.autojob.core.entity.EventLog;
import com.laysan.autojob.core.repository.EventLogRepository;
import com.laysan.autojob.core.service.AutoRun;
import com.laysan.autojob.core.service.AutoRunService;
import com.laysan.autojob.core.service.MessageService;
import com.laysan.autojob.core.utils.AESUtil;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class YoudaoRunService implements AutoRun {
    @Autowired
    EventLogRepository eventLogRepository;
    @Autowired
    private MessageService messageService;

    String login_url = "https://note.youdao.com/login/acc/urs/verify/check?app=web&product=YNOTE&tp=urstoken&cf=6&fr=1&systemName=&deviceType=&ru=https%3A%2F%2Fnote.youdao.com%2FsignIn%2F%2FloginCallback.html&er=https%3A%2F%2Fnote.youdao.com%2FsignIn%2F%2FloginCallback.html&vcode=&systemName=mac&deviceType=MacPC&timestamp=1611466345699";
    String checkin_url = "http://note.youdao.com/yws/mapi/user?method=checkin";
    OkHttpClient client;

    @Override
    @PostConstruct
    public void registry() {
        AutoRunService.handlers.put(AccountType.MODULE_YOUDAO.getCode(), this);
    }

    @Override
    public void run(Account account) {
        try {
            EventLog eventLog = new EventLog();

            List<Cookie> cookieStore = new ArrayList<>();
            client = new OkHttpClient.Builder().cookieJar(new CookieJar() {
                @Override
                public void saveFromResponse(HttpUrl httpUrl, List<Cookie> list) {
                    cookieStore.addAll(list);
                }

                @Override
                public List<Cookie> loadForRequest(HttpUrl httpUrl) {
                    return cookieStore;
                }
            }).build();
            RequestBody body = new FormBody.Builder()
                    .add("username", account.getAccount())
                    .add("password", DigestUtils.md5Hex(AESUtil.decrypt(account.getPassword())))
//                    .add("app", "web")
//                    .add("product", "YNOTE")
//                    .add("tp", "urstoken")
//                    .add("cf", "6")
//                    .add("fr", "1")
//                    .add("systemName", "")
//                    .add("deviceType", "")
//                    .add("vcode", "")
//                    .add("ru", "https://note.youdao.com/signIn/loginCallback.html")
//                    .add("er", "https://note.youdao.com/signIn/loginCallback.html")
//                    .add("systemName", "mac")
//                    .add("deviceType", "MacPC")
//                    .add("timestamp", "1611466345699")

                    .build();
            Request request = new Request.Builder()
                    .url(login_url)
                    .post(body)
                    .build();

            Response response1 = client.newCall(request).execute();
            String string = response1.body().string();


            RequestBody body2 = new FormBody.Builder().add("method", "checkin").build();

            Request checkinRequest = new Request.Builder().url(checkin_url).post(body2).build();

            Response response = client.newCall(checkinRequest).execute();
            int code = response.code();
            System.out.println(code);
            if (code != 200) {
                eventLog.setDetail("签到失败");
            }
            eventLog.setType(AccountType.MODULE_YOUDAO.getCode());
            eventLog.setUserId(account.getUserId());
            eventLog.setAccountId(account.getId());

            String text = response.body().string();
            //{"total":4194304,"success":0,"time":1613800646734,"space":4194304}
            JSONObject jsonObject = JSON.parseObject(text);
            System.out.println(text);
            String detail = "签到获得:" + (jsonObject.getLong("space") / 1048576) + "M,总计:" + (jsonObject.getLong("total") / 1048576) + "M";

            eventLog.setDetail(detail);
            eventLogRepository.save(eventLog);

            messageService.sendMessage(account.getUserId(), "有道云签到", detail);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        YoudaoRunService s = new YoudaoRunService();
        s.run(null);
    }
}
