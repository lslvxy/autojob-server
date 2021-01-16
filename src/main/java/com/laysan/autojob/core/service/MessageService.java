
package com.laysan.autojob.core.service;

import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.laysan.autojob.core.service.dto.DataDetail;
import com.laysan.autojob.core.service.dto.Message;
import com.laysan.autojob.core.service.dto.ValueDetail;
import io.vavr.control.Try;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author lise
 * @version MessageService.java, v 0.1 2020年11月26日 14:11 lise
 */

@Service
@Slf4j
public class MessageService {
    public String getToken() {
        String secret = System.getenv("wx127525214d4abbe0_secret");

        String url = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=wx127525214d4abbe0&secret="
                + secret;
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .build();
        AtomicReference<Response> response = new AtomicReference<>();
        return Try.of(() -> {
            response.set(client.newCall(request).execute());
            String responseText = response.get().body().string();
            ObjectMapper mapper = new ObjectMapper();
            Map map = mapper.readValue(responseText, Map.class);
            String openid = map.get("access_token").toString();

            return openid;
        }).andFinally(() -> response.get().close()).getOrElse("");
    }

    public void sendMessage(String userId, String type, String detail) {
        Message message = new Message();

        //        message.setTemplate_id("j5OIz1iUpiBpx_80xtO0fmmc92gL0MFqU81GH2mTe_Y");
        message.setTemplate_id("UYmCUg__IsjSMNPEhsHYx440P84NanoSS1fABW2WApw");

        DataDetail da = new DataDetail();
        da.setThing1(new ValueDetail(type));
        //        da.setDate2(new ValueDetail(LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME)));
        //        da.setDate2(new ValueDetail(new Date()));
        da.setThing2(new ValueDetail(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))));
        da.setThing3(new ValueDetail(detail.length() > 20 ? detail.substring(0, 20) : detail));
        message.setData(da);

        message.setTouser(userId);
        String token = getToken();
        message.setAccess_token(token);
        //String url="http://localhost:8080/logs/send";
        String url = "https://api.weixin.qq.com/cgi-bin/message/subscribe/send?access_token=" + token;
        //        String url = "https://api.weixin.qq.com/cgi-bin/message/custom/send?access_token=" + token;
        OkHttpClient client = new OkHttpClient();
        RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), JSON.toJSONString(message));
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        AtomicReference<Response> response = new AtomicReference<>();
        Try.of(() -> {
            response.set(client.newCall(request).execute());
            log.info("发送服务消息:{}", response.get().body().string());
            return response;
        }).andFinally(() -> response.get().close());
    }
}