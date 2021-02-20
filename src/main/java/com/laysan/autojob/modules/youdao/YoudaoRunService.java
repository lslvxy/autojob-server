package com.laysan.autojob.modules.youdao;

import com.laysan.autojob.core.constants.Constants;
import com.laysan.autojob.core.entity.Account;
import com.laysan.autojob.core.repository.EventLogRepository;
import com.laysan.autojob.core.service.AutoRun;
import com.laysan.autojob.core.service.AutoRunService;
import com.laysan.autojob.core.service.MessageService;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.apache.commons.codec.digest.DigestUtils;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

//@Service
@Slf4j
public class YoudaoRunService implements AutoRun {
    //    @Autowired
//    EventLogRepository eventLogRepository;
//    @Autowired
//    private MessageService messageService;

    String login_url = "https://note.youdao.com/login/acc/urs/verify/check";
    String checkin_url = "http://note.youdao.com/yws/mapi/user";
    OkHttpClient client;

    @Override
    @PostConstruct
    public void registry() {
        AutoRunService.handlers.put(Constants.MODULE_YOUDAO, this);
    }

    @Override
    public void run(Account account) {
        try {
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
                    .add("username", "lslvxy@163.com")
                    .add("password", DigestUtils.md5Hex("SnowShmily1314"))
                    .add("app", "web")
                    .add("product", "YNOTE")
                    .add("tp", "urstoken")
                    .add("cf", "6")
                    .add("fr", "1")
                    .add("systemName", "")
                    .add("deviceType", "")
                    .add("vcode", "")
                    .add("ru", "https://note.youdao.com/signIn/loginCallback.html")
                    .add("er", "https://note.youdao.com/signIn/loginCallback.html")
                    .add("systemName", "mac")
                    .add("deviceType", "MacPC")
                    .add("timestamp", "1611466345699")

                    .build();
            Request request = new Request.Builder()
                    .url(login_url)
                    .post(body)
                    .build();

            client.newCall(request).execute();

            {
                RequestBody body2 = new FormBody.Builder().add("method", "checkin").build();

                Request checkinRequest = new Request.Builder().url(checkin_url).post(body2).build();

                Response response = client.newCall(checkinRequest).execute();
                System.out.println(response.code());
                String text = response.body().string();
                //{"total":4194304,"success":0,"time":1613800646734,"space":4194304}

                System.out.println(text);

            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        YoudaoRunService s = new YoudaoRunService();
        s.run(null);
    }
}
