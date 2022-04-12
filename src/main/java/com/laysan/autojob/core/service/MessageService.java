
package com.laysan.autojob.core.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.laysan.autojob.core.repository.ServerRepository;
import io.vavr.control.Try;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author lise
 * @version MessageService.java, v 0.1 2020年11月26日 14:11 lise
 */

@Service
@Slf4j
public class MessageService {
    private Token tokenStore;
    @Resource
    ServerRepository serverRepository;

    public String getToken() {
        if (!Objects.isNull(tokenStore)) {
            log.info("缓存的Token:{} ", tokenStore.toString());
            if (Objects.isNull(tokenStore.getErrcode()) && !Objects.isNull(tokenStore.getTimestamp())
                    && tokenStore.getTimestamp() - System.currentTimeMillis() > 10 * 60 * 1000) {
                return tokenStore.getAccess_token();
            }
        }
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
            log.info("token response:{}", responseText);
            ObjectMapper mapper = new ObjectMapper();
            Token token = mapper.readValue(responseText, Token.class);
            token.setTimestamp(System.currentTimeMillis() + token.expires_in * 1000);
            log.info("获取新的Token:{} ", token.toString());

            this.tokenStore = token;
            return token.getAccess_token();
        }).onFailure(Throwable::printStackTrace).andFinally(() -> response.get().close()).getOrElse("");
    }

    public void sendMessage(Long userId, String title, String detail) {
    }

//    public void sendMessage(String userId, String type, String detail) {
//        Message message = new Message();
//
//        //        message.setTemplate_id("j5OIz1iUpiBpx_80xtO0fmmc92gL0MFqU81GH2mTe_Y");
//        message.setTemplate_id("UYmCUg__IsjSMNPEhsHYx440P84NanoSS1fABW2WApw");
//
//        DataDetail da = new DataDetail();
//        da.setThing1(new ValueDetail(type));
//        //        da.setDate2(new ValueDetail(LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME)));
//        //        da.setDate2(new ValueDetail(new Date()));
//        da.setThing2(new ValueDetail(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))));
//        da.setThing3(new ValueDetail(detail.length() > 20 ? detail.substring(0, 20) : detail));
//        message.setData(da);
//
//        message.setTouser(userId);
//        String token = getToken();
//        LogUtils.info(log, "token", userId, token);
//        message.setAccess_token(token);
//        //String url="http://localhost:8080/logs/send";
//        String url = "https://api.weixin.qq.com/cgi-bin/message/subscribe/send?access_token=" + token;
//        //        String url = "https://api.weixin.qq.com/cgi-bin/message/custom/send?access_token=" + token;
//        OkHttpClient client = new OkHttpClient();
//       // RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), JSON.toJSONString(message));
//        Request request = new Request.Builder()
//                .url(url)
//              //  .post(body)
//                .build();
//        AtomicReference<Response> response = new AtomicReference<>();
//        Try.of(() -> {
//            response.set(client.newCall(request).execute());
//            log.info("发送服务消息:{},{}", userId, response.get().body().string());
//            return response;
//        }).andFinally(() -> response.get().close());
//
//        Server server = serverRepository.findByUserId(userId);
//        if (!Objects.isNull(server) && !Objects.isNull(server.getSckey())) {
//            RequestBody scbody = new FormBody.Builder()
//                    .add("text", type)
//                    .add("desp", detail).build();
//            Request sckeyRequest = new Request.Builder()
//                    .url("https://sc.ftqq.com/" + server.getSckey() + ".send")
//                    .post(scbody)
//                    .build();
//            Try.of(() -> {
//                Response execute = client.newCall(sckeyRequest).execute();
//                execute.close();
//                return null;
//            });
//        }
//
//    }

    @Getter
    @Setter
    @ToString
    static class Token {
        private String access_token;
        private Integer expires_in;
        private Integer errcode;
        private String errmsg;
        private Long timestamp;
    }
}