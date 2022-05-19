package com.laysan.autojob.core.service;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.laysan.autojob.core.entity.User;
import com.laysan.autojob.core.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.Objects;

/**
 * @author lise
 * @version MessageService.java, v 0.1 2020年11月26日 14:11 lise
 */

@Service
@Slf4j
public class MessageService {
    @Resource
    UserRepository userRepository;

    public void sendMessage(String openId, String title, String detail) {
        User user = userRepository.findByOpenId(openId);
        if (Objects.isNull(user) || StrUtil.isBlank(user.getMessageKey())) {
            return;
        }
        String messageType = user.getMessageType();
        if (StrUtil.isBlank(messageType)) {
            messageType = "sct";
        }
        switch (messageType) {
            case "pushdeer":
                sendPushdeer(user.getMessageKey(), title, detail);
                break;
            default:
                sendSct(user.getMessageKey(), title, detail);
                break;
        }
    }

    private void sendSct(String sendKey, String title, String detail) {
        OkHttpClient client = new OkHttpClient.Builder().build();
        RequestBody body = new FormBody.Builder().add("title", title).add("desp", detail).build();
        Request request = new Request.Builder().url("https://sctapi.ftqq.com/" + sendKey + ".send").post(body).build();
        try {
            Response execute = client.newCall(request).execute();
            String string = execute.body().string();
            log.info(string);

        } catch (IOException e) {
            log.error("发送消息失败");
        }
    }

    public static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    private void sendPushdeer(String pushkey, String text, String desp) {
        OkHttpClient client = new OkHttpClient.Builder().build();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("pushkey", pushkey);
        jsonObject.put("text", text);
        jsonObject.put("desp", desp);
        String json = jsonObject.toJSONString();
        RequestBody body = RequestBody.create(json, JSON);
        Request request = new Request.Builder().url("https://api2.pushdeer.com/message/push").post(body).build();
        try {
            Response execute = client.newCall(request).execute();
            String string = execute.body().string();
            log.info(string);
        } catch (IOException e) {
            log.error("发送消息失败");
        }

    }


}