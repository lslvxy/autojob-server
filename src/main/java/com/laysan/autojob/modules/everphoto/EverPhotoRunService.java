package com.laysan.autojob.modules.everphoto;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.laysan.autojob.core.constants.Constants;
import com.laysan.autojob.core.entity.Account;
import com.laysan.autojob.core.entity.EventLog;
import com.laysan.autojob.core.repository.EventLogRepository;
import com.laysan.autojob.core.service.AutoRun;
import com.laysan.autojob.core.service.AutoRunService;
import com.laysan.autojob.core.service.MessageService;
import com.laysan.autojob.core.utils.AESUtil;
import com.laysan.autojob.core.utils.LogUtils;
import io.vavr.control.Try;
import lombok.extern.slf4j.Slf4j;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;

@Service
@Slf4j
public class EverPhotoRunService implements AutoRun {
    @Autowired
    EventLogRepository eventLogRepository;
    @Autowired
    private MessageService messageService;

    static String url      = "https://api.everphoto.cn/users/self/checkin/v2";
    static String urllogin = "https://web.everphoto.cn/api/auth";

    @Override
    @PostConstruct
    public void registry() {
        AutoRunService.handlers.put(Constants.LOG_TYPE_EVERPHOTO, this);
    }

    @Override
    public void run(Account account) {
        EventLog eventLog = new EventLog();
        Try.of(() -> {
            if (Objects.isNull(account) || !Objects.equals(account.getType(), Constants.LOG_TYPE_EVERPHOTO)) {
                log.error("账户type不正确");
                throw new RuntimeException("账户type不正确");
            }
            String token = "";
            String password = account.getPassword();
            if (password.length() != 32) {
                String originalPassword = AESUtil.decrypt(password);
                password = DigestUtils.md5DigestAsHex(("tc.everphoto." + originalPassword).getBytes());
            }
            String login = login(account.getAccount(), password);
            final JSONObject loginResult = JSON.parseObject(login);
            if (Objects.isNull(loginResult)) {
                throw new RuntimeException("登录失败");
            }
            if (!loginResult.getInteger("code").equals(0) || !loginResult.containsKey("data")) {
                throw new RuntimeException("登录失败," + (loginResult.containsKey("message") ? loginResult.getString("message") : ""));
            }
            final JSONObject loginData = loginResult.getJSONObject("data");
            if (loginData.containsKey("token")) {
                token = loginData.getString("token");
                //            log.info("登录成功,token={}", token);
                LogUtils.info(log, Constants.LOG_MODULES_EVERPHOTO, Constants.LOG_OPERATE_LOGIN, "{}登录成功", account.getAccount());
            }
            if (StringUtils.isEmpty(token)) {
                throw new RuntimeException("获取token失败");
            }
            final JSONObject checkinResponse = JSON.parseObject(checkin(token));
            if (!checkinResponse.getInteger("code").equals(0) || !checkinResponse.containsKey("data")) {
                throw new RuntimeException("签到失败");
            }
            final JSONObject checkinData = checkinResponse.getJSONObject("data");
            Result result = checkinData.toJavaObject(Result.class);
            eventLog.setUserId(account.getUserId());
            List<String> detail = new LinkedList<>();
            detail.add("签到结果:" + (result.getCheckin_result().equals("true") ? "成功" : "失败(今日已签到)"));
            detail.add("累计签到:" + (result.getContinuity()) + "天");
            detail.add("总容量:" + (result.getTotal_reward() / 1024 / 1024) + "MB");
            detail.add("明日可得:" + (result.getTomorrow_reward() / 1024 / 1024) + "MB");
            String detail1 = detail.stream().collect(Collectors.joining("；"));
            eventLog.setDetail(detail1);
            eventLog.setType(Constants.LOG_TYPE_EVERPHOTO);
            eventLogRepository.save(eventLog);
            LogUtils.info(log, Constants.LOG_MODULES_EVERPHOTO, Constants.LOG_OPERATE_CHECKIN, "{},{}", account.getAccount(), detail1);

            messageService.sendMessage(account.getUserId(), "时光相册签到", detail1);
            return null;
        });
    }

    private String checkin(String token) throws IOException {
        OkHttpClient client = new OkHttpClient();
        RequestBody body = new FormBody.Builder().build();
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .header("User-Agent", "EverPhoto/2.7.0 (Android;2702;ONEPLUS A6000;28;oppo")
                .header("x-device-mac", "02:00:00:00:00:00")
                .header("application", "tc.everphoto")
                .header("x-locked", "1")
                .header("content-length", "0")
                .header("authorization", "Bearer " + token)
                .build();
        try (Response response = client.newCall(request).execute()) {
            return (response.body().string());
        }
    }

    private String login(String account, String password) throws IOException {
        //        String md5 = DigestUtils.md5DigestAsHex(base.getBytes());

        OkHttpClient client = new OkHttpClient();
        RequestBody body = new FormBody.Builder().add("mobile", account).add("password", password).build();
        Request request = new Request.Builder()
                .url(urllogin)
                .post(body)
                .header("User-Agent", "EverPhoto/2.7.0 (Android;2702;ONEPLUS A6000;28;oppo")
                .header("x-device-mac", "02:00:00:00:00:00")
                .header("application", "tc.everphoto")
                .header("authorization", "Bearer 94P6RfZFfqvVQ2hH4jULaYGI")
                .header("x-locked", "1")
                .header("content-length", "0")
                .header("accept-encoding", "gzip")
                .build();
        try (Response response = client.newCall(request).execute()) {
            return uncompress(response.body().bytes());
        }
    }

    public static String uncompress(byte[] str) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (GZIPInputStream gis = new GZIPInputStream(new ByteArrayInputStream(str))) {
            int b;
            while ((b = gis.read()) != -1) {
                baos.write((byte) b);
            }
        } catch (Exception e) {
            return new String(str);
        }
        return new String(baos.toByteArray(), StandardCharsets.UTF_8);
    }
}
